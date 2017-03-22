/**
 * Copyright (C) 2014 Red Hat, Inc, and individual contributors.
 * Copyright (C) 2011-2012 VMware, Inc.
 */

package angular4J.sockjs;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * The main entry point for handling SockJS connections.
 *
 * Typically a SockJsServer is created, its {@link #options} set, an {@link #onConnection} handler
 * added, and the server is then passed to a SockJsServlet to handle the routing of requests.
 */
public class SockJsServer {
   
   private Dispatcher dispatcher;
   private WebsocketHandler websocketHandler;
   private ScheduledExecutorService scheduledExecutor;
   private OnConnectionHandler onConnectionHandler;

   public void init() {
      websocketHandler = new WebsocketHandler();

      dispatcher = new Dispatcher();
      dispatcher.push("GET", p("/websocket"), websocketHandler.rawWebsocket);
      dispatcher.push("GET", t("/websocket"), websocketHandler.sockjsWebsocket);

      scheduledExecutor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory(){

         @Override
         public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
         }
      });
   }

   public void destroy() {
      scheduledExecutor.shutdownNow();
   }

   public void dispatch(SockJsRequest req, SockJsResponse res) throws SockJsException {
      dispatcher.dispatch(req, res);
   }

   protected String p(String match) {
      return "^" + match + "[/]?$";
   }

   protected String[] t(String match) {
      String pattern = p("/([^/.]+)/([^/.]+)" + match);
      return new String[]{pattern, "server", "session"};
   }

   /**
    * Handle incoming connections - a SockJsServer isn't very useful unless you set an
    * OnConnectionHandler here.
    * 
    * @param handler
    *           The handler to call when a new connection is established
    */
   public void onConnection(OnConnectionHandler handler) {
      onConnectionHandler = handler;
   }

   public void emitConnection(SockJsConnection connection) {
      if (onConnectionHandler != null) {
         onConnectionHandler.handle(connection);
      }
   }

   public ScheduledFuture setTimeout(Runnable callback, long delay) {
      return scheduledExecutor.schedule(callback, delay, TimeUnit.MILLISECONDS);
   }

   public void clearTimeout(ScheduledFuture future) {
      future.cancel(false);
   }

   

   public static interface OnConnectionHandler {

      public void handle(SockJsConnection connection);
   }
}
