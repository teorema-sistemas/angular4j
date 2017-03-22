/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import angular4J.util.CommonUtils;

public class Dispatcher {

   private static final Logger log = Logger.getLogger(Dispatcher.class.getName());

   private DispatchFunction handle404 = new DispatchFunction(){

      @Override
      public Object handle(SockJsRequest req, SockJsResponse res, Object x) throws SockJsException {
         if (res.finished()) {
            return x;
         }
         res.setHeader("content-type", "text/plain; charset=UTF-8");
         res.writeHead(404);
         res.end("404 Error: Page not found\n");
         return true;
      }
   };

   private DispatchFunction handle405 = new DispatchFunction(){

      @Override
      @SuppressWarnings("unchecked")
      public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
         List<String> methods = (List<String>) data;
         res.setHeader("Allow", CommonUtils.parseStrArray(methods, ", "));
         res.writeHead(405);
         res.end();
         return true;
      }
   };

   private DispatchFunction handleError = new DispatchFunction(){

      @Override
      public Object handle(SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
         Exception x = (Exception) data;
         if (res.finished()) {
            return x;
         }
         log.log(Level.FINER, "handleError", x);
         if (x instanceof DispatchException) {
            DispatchException dx = (DispatchException) x;
            log.log(Level.FINE, "DispatchException message: {0}", dx.message);
            res.writeHead(dx.status);
            String message = dx.message;
            if (message == null) {
               message = "";
            }
            res.end(message);
         } else {
            try {
               res.writeHead(500);
               res.end("500 - Internal Server Error");
            }
            catch (Exception e) {}
         }
         return true;
      }
   };

   private List<DispatchEntry> rows = new ArrayList<>();

   public void push(String method, String pattern, DispatchFunction... functions) {
      rows.add(new DispatchEntry(method, pattern, functions));
   }

   public void push(String method, String[] patterns, DispatchFunction... functions) {
      DispatchEntry entry = new DispatchEntry(method, patterns[0], functions);
      entry.groups = Arrays.copyOfRange(patterns, 1, patterns.length);
      rows.add(entry);
   }

   private void executeRequest(List<DispatchFunction> functions, SockJsRequest req, SockJsResponse res, Object data) throws SockJsException {
      try {
         while (functions.size() > 0) {
            DispatchFunction function = functions.remove(0);
            req.lastFunction = function;
            data = function.handle(req, res, data);
         }
      }
      catch (DispatchException x) {
         if (x.status == 0) {
            return;
         } else if (x.status == 404) {
            handle404.handle(req, res, x);
         } else {
            handleError.handle(req, res, x);
         }
      }
      catch (Exception x) {
         handleError.handle(req, res, x);
      }
   }

   // Logic from sockjs-node's webjs.coffee exports.generateHandler
   public void dispatch(final SockJsRequest req, final SockJsResponse res) throws SockJsException {
      String path = req.getPath();
      if (path == null) {
         path = "/";
      }
      boolean found = false;
      List<String> allowedMethods = new ArrayList<String>();
      for (final DispatchEntry dispatchEntry: rows) {
         Pattern pattern = dispatchEntry.pattern;
         Matcher matcher = pattern.matcher(path);
         if (!matcher.matches()) {
            continue;
         }
         if (!req.getMethod().matches(dispatchEntry.method)) {
            allowedMethods.add(dispatchEntry.method);
            continue;
         }
         String[] groups = dispatchEntry.groups;
         for (int i = 0; i < groups.length; i++) {
            req.addMatch(groups[i], matcher.group(i + 1));
         }
         final List<DispatchFunction> functionList = new ArrayList<>(Arrays.asList(dispatchEntry.functions));
         req.nextFilter = new NextFilter(){

            @Override
            public void handle(Object data) throws SockJsException {
               executeRequest(functionList, req, res, data);
            }
         };
         req.nextFilter.handle("");
         found = true;
         break;
      }

      if (!found) {
         if (allowedMethods.size() > 0) {
            handle405.handle(req, res, allowedMethods);
         } else {
            handle404.handle(req, res, null);
         }
      }
   }

   public static class DispatchEntry {

      public DispatchEntry(String method, String pattern, DispatchFunction... functions) {
         this.method = method;
         this.pattern = Pattern.compile(pattern);
         this.functions = functions;
      }
      public String method;
      public Pattern pattern;
      public String[] groups = new String[]{};
      public DispatchFunction[] functions;
   }
}
