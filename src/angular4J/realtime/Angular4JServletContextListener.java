package angular4J.realtime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import javax.websocket.DeploymentException;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import angular4J.js.cache.DefaultJsCacheLoader;
import angular4J.js.cache.JsCacheFactory;
import angular4J.sockjs.SockJsServer;
import angular4J.sockjs.servlet.RawWebsocketEndpoint;
import angular4J.sockjs.servlet.SockJsEndpoint;
import angular4J.util.ClosureCompiler;
import angular4J.util.Constants;

/**
 * this listener:
 * initialize the sockJs server end point
 */

@WebListener
public class Angular4JServletContextListener implements ServletContextListener {

   public static SockJsServer sockJsServer;
   private static final Pattern SESSION_PATTERN = Pattern.compile(".*/.+/(.+)/websocket$");

   ClosureCompiler compiler = new ClosureCompiler();
   ServletContext context;

   @Override
   public void contextInitialized(ServletContextEvent servletContextEvent) {
      context = servletContextEvent.getServletContext();

      try {
         if (sockJsServer == null) {
            initJSR356();
         }
      }
      catch (ServletException e) {
         e.printStackTrace();
      }

      JsCacheFactory jsCacheFactory = new JsCacheFactory(DefaultJsCacheLoader.class);
      jsCacheFactory.BuildJsCache();
   }

   @Override
   public void contextDestroyed(ServletContextEvent servletContextEvent) {}

   private String extractPrefixFromMapping(String mapping) {
      if (mapping.endsWith("*")) {
         mapping = mapping.substring(0, mapping.length() - 1);
      }
      if (mapping.endsWith("/")) {
         mapping = mapping.substring(0, mapping.length() - 1);
      }
      return mapping;
   }

   private ServerEndpointConfig.Configurator configuratorFor(final String prefix, final boolean isRaw) {
      return new ServerEndpointConfig.Configurator(){

         @Override
         public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
            try {
               return endpointClass.getConstructor(SockJsServer.class, String.class, String.class).newInstance(sockJsServer, context.getContextPath(), prefix);
            }
            catch (Exception e) {
               throw new RuntimeException(e);
            }
         }

         @Override
         public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
            if (isRaw) {
               return;
            }
            String path = request.getRequestURI().getPath();
            Matcher matcher = SESSION_PATTERN.matcher(path);
            if (matcher.matches()) {
               String sessionId = matcher.group(1);
               saveHeaders(sessionId, request.getHeaders());
            }
         }
      };
   }

   private static final int MAX_INFLIGHT_HEADERS = 100;
   private static final Map<String, Map<String, List<String>>> savedHeaders = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, List<String>>>(){

      @Override
      protected boolean removeEldestEntry(Map.Entry eldest) {
         return size() > MAX_INFLIGHT_HEADERS;
      }
   });

   static void saveHeaders(String sessionId, Map<String, List<String>> headers) {
      savedHeaders.put(sessionId, headers);
   }

   public static Map<String, List<String>> retrieveHeaders(String sessionId) {
      return savedHeaders.remove(sessionId);
   }

   public void initJSR356() throws ServletException {
      sockJsServer = new SockJsServer();
      sockJsServer.init();

      if (sockJsServer.options.websocket) {

         final String commonPrefix = extractPrefixFromMapping(Constants.URL_PATTERNS_SERVICE);

         String websocketPath = commonPrefix + "/{server}/{session}/websocket";
         ServerEndpointConfig sockJsConfig = ServerEndpointConfig.Builder.create(SockJsEndpoint.class, websocketPath).configurator(configuratorFor(commonPrefix, false)).build();

         String rawWebsocketPath = commonPrefix + "/websocket";

         ServerEndpointConfig rawWsConfig = ServerEndpointConfig.Builder.create(RawWebsocketEndpoint.class, rawWebsocketPath).configurator(configuratorFor(commonPrefix, true)).build();

         ServerContainer serverContainer = (ServerContainer) context.getAttribute("javax.websocket.server.ServerContainer");
         try {
            serverContainer.addEndpoint(sockJsConfig);
            serverContainer.addEndpoint(rawWsConfig);

            Logger.getLogger(this.getClass().getSimpleName()).info("deployement of programmatic Web socket EndPoint :" + rawWebsocketPath);
         }
         catch (DeploymentException ex) {
            throw new ServletException("Error deploying websocket endpoint:", ex);
         }
      }
   }
}
