
package angular4J.remote;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;

import angular4J.context.NGSessionScopeContext;
import angular4J.context.SessionMapper;
import angular4J.events.RealTimeErrorEvent;
import angular4J.events.RealTimeSessionCloseEvent;
import angular4J.events.RealTimeSessionReadyEvent;
import angular4J.realtime.Angular4JServletContextListener;
import angular4J.sockjs.SockJsConnection;
import angular4J.sockjs.SockJsServer;
import angular4J.sockjs.servlet.SockJsServlet;
import angular4J.util.Constants;
import angular4J.util.NGParser;

/**
 * The RealTimeEndPoint servlet is the realtime sockjs protocol endpoint
 */

@SuppressWarnings("serial")
@WebServlet(loadOnStartup = 1, asyncSupported = true, urlPatterns = Constants.URL_PATTERNS_SERVICE)
public class RealTimeEndPoint extends SockJsServlet {

   @Inject
   @DataReceivedEvent
   private Event<RealTimeDataReceivedEvent> receiveEvents;

   @Inject
   @RealTimeSessionReadyEvent
   private Event<RealTimeDataReceivedEvent> sessionOpenEvent;

   @Inject
   @RealTimeSessionCloseEvent
   private Event<RealTimeDataReceivedEvent> sessionCloseEvent;

   @Inject
   @RealTimeErrorEvent
   private Event<RealTimeDataReceivedEvent> errorEvent;

   @Inject
   Logger logger;

   private Map<String, String> sessionsConn = new HashMap<>();

   @Override
   public void init() throws ServletException {
      SockJsServer server = Angular4JServletContextListener.sockJsServer;

      server.onConnection(new SockJsServer.OnConnectionHandler(){

         @Override
         public void handle(final SockJsConnection connection) {

            connection.onData(new SockJsConnection.OnDataHandler(){

               @Override
               public void handle(String message) {

                  if (message != null) {
                     JsonObject jObj = NGParser.getInstance().deserialize(message).getAsJsonObject();

                     String UID = null;

                     if (jObj.get("session") == null) {
                        UID = SessionMapper.getInstance().getHTTPSessionID(connection.id);
                     } else {
                        UID = jObj.get("session").getAsString();
                        SessionMapper.getInstance().getSessions().put(UID, new HashSet<String>());
                     }
                     SessionMapper.getInstance().getSessions().get(UID).add(connection.id);

                     RealTimeDataReceivedEvent ev = new RealTimeDataReceivedEvent(connection, jObj);

                     ev.setConnection(connection);
                     ev.setSessionId(UID);
                     NGSessionScopeContext.getInstance().setCurrentContext(UID);

                     String service = "";
                     if (jObj.has("service")) {
                        service = jObj.get("service").getAsString();
                     }

                     if (service.equals("ping") || service.equals("push")) {
                        sessionOpenEvent.fire(ev);

                        if (service.equals("ping")) {
                           sessionsConn.put(connection.id, UID);

                           logger.info("Angular4J-client " + UID + " connected.");
                        }
                     } else {
                        receiveEvents.fire(ev);
                     }

                     afterConnect(connection.id, UID, jObj);
                  }

               }
            });

            connection.onClose(new SockJsConnection.OnCloseHandler(){

               @Override
               public void handle() {
                  String UID = sessionsConn.get(connection.id);
                  sessionsConn.remove(connection.id);

                  onClose(connection.id, UID);

                  getServletContext().log("Realtime client " + UID + " disconnected.");
               }
            });
         }
      });

      setServer(server);

      super.init();
   }

   public void afterConnect(String connectionId, String UID, JsonObject jObj) {}

   public void onClose(String connectionId, String UID) {}
}
