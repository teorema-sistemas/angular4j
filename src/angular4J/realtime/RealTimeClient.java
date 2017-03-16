
package angular4J.realtime;

import static angular4J.sockjs.ReadyState.OPEN;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.inject.Inject;

import angular4J.boot.NGRegistry;
import angular4J.context.NGSessionScoped;
import angular4J.context.SessionMapper;
import angular4J.context.NGHolder;
import angular4J.events.BroadcastManager;
import angular4J.events.RealTimeMessage;
import angular4J.events.RealTimeSessionCloseEvent;
import angular4J.events.RealTimeSessionReadyEvent;
import angular4J.events.ServerEvent;
import angular4J.log.NGLogger;
import angular4J.remote.DataReceivedEvent;
import angular4J.remote.RealTimeDataReceivedEvent;
import angular4J.sockjs.SockJsConnection;
import angular4J.util.ModelQuery;
import angular4J.util.ModelQueryImpl;
import angular4J.util.NGParser;
import angular4J.util.NGObject;

/**
 * when injected, a realTime client represent the current real time session (websocket or fallback
 * protocol)
 **/

@NGSessionScoped
public class RealTimeClient implements Serializable {

   private final Set<SockJsConnection> sessions = new HashSet<>();

   @Inject
   NGLogger logger;

   private boolean async = false;

   /**
    * the Realtime client will use will use AsyncRemote of the websocket api usage:
    * realTimeClient.async().*any-method()*.
    */

   public RealTimeClient async() {

      async = true;
      return this;
   }

   public void onSessionReady(@Observes @RealTimeSessionReadyEvent RealTimeDataReceivedEvent event) {

      GlobalConnectionHolder.getInstance().getConnections().add(event.getConnection());
      sessions.add(event.getConnection());

      List<NGObject> nGModels = NGRegistry.getInstance().getNGModels();
      for (NGObject model: nGModels) {

         String httpSessionId = SessionMapper.getInstance().getHTTPSessionID(event.getConnection().id);

         BroadcastManager.getInstance().subscribe(httpSessionId, model.getTargetClass().getSimpleName());
      }

      event.setClient(this);

   }

   public void onClose(@Observes @RealTimeSessionCloseEvent RealTimeDataReceivedEvent event) {
      GlobalConnectionHolder.getInstance().getConnections().remove(event.getConnection());
   }

   public void onData(@Observes @DataReceivedEvent RealTimeDataReceivedEvent event) {

      event.setClient(this);

   }

   /**
    * will close all current realTime sessions bound to the current HTTP session
    */
   public void invalidateSession() {
      for (SockJsConnection connection: sessions) {
         connection.close(javax.websocket.CloseReason.CloseCodes.CANNOT_ACCEPT.getCode(), "CLOSED BY BACKEND");
      }
   }

   /**
    * send a message to the current session front end
    * 
    * @param channel
    *           : can be
    * 
    *           - The Angular4J class name OR A custom channel
    * 
    * @param message
    *           : the RealTimeMessage to send
    */
   public void publish(String channel, RealTimeMessage message) {

      Map<String, Object> paramsToSend = prepareData(channel, message);

      publish(paramsToSend);

   }

   /**
    * send a ModelQuery to the current session front end Angular4J proxy to update his models
    * 
    * @param query
    *           : the ModelQuery to send
    */

   public void publish(ModelQuery query) {
      Map<String, Object> paramsToSend = prepareData(query);
      publish(paramsToSend);

   }

   /**
    * send a message to all front end open sessions
    * 
    * @param channel
    *           : can be
    * 
    *           - The Angular4J class name - A custom channel
    * 
    * @param message
    *           : the RealTimeMessage to send
    * @param withoutMe
    *           : possible values:
    *           <p>
    *           true: the current session client will not receive the message.
    *           <p>
    *           false: the current session client will also receive the message.
    */

   public void broadcast(String channel, RealTimeMessage message, boolean withoutMe) {

      Map<String, Object> paramsToSend = prepareData(channel, message);

      broadcast(channel, withoutMe, paramsToSend);

   }

   /**
    * send a ModelQuery to all front end open sessions
    * 
    * @param query
    *           : the ModelQuery to send
    * 
    * @param withoutMe
    *           : possible values:
    *           <p>
    *           true: the current session client will not receive the query.
    *           <p>
    *           false: the current session client will also receive the query.
    */

   public void broadcast(ModelQuery query, boolean withoutMe) {

      Map<String, Object> paramsToSend = prepareData(query);

      broadcast(query.getTargetServiceClass(), withoutMe, paramsToSend);

   }

   private Map<String, Object> prepareData(ModelQuery query) {
      Map<String, Object> paramsToSend = new HashMap<String, Object>();

      ModelQueryImpl modelQuery = (ModelQueryImpl) query;

      ServerEvent ngEvent = new ServerEvent();
      ngEvent.setName("modelQuery");
      ngEvent.setData(NGHolder.getInstance().getName(modelQuery.getOwner()));

      paramsToSend.putAll(modelQuery.getData());
      paramsToSend.put("ngEvent", ngEvent);
      paramsToSend.put("log", logger.getLogPool());
      paramsToSend.put("isRT", true);

      return paramsToSend;
   }

   private Map<String, Object> prepareData(String eventName, RealTimeMessage message) {
      Map<String, Object> paramsToSend = new HashMap<String, Object>(message.build());

      ServerEvent ngEvent = new ServerEvent();
      ngEvent.setName(eventName);
      ngEvent.setData(message.build());

      paramsToSend.put("ngEvent", ngEvent);
      paramsToSend.put("log", logger.getLogPool());
      paramsToSend.put("isRT", true);

      return paramsToSend;
   }

   private void broadcast(String channel, boolean withoutMe, Map<String, Object> paramsToSend) {

      for (SockJsConnection connection: GlobalConnectionHolder.getInstance().getConnections()) {

         if (!BroadcastManager.getInstance().isSubscribed(connection.id, channel)) {
            continue;
         }
         if (withoutMe) {
            if (sessions.contains(connection)) {
               continue;
            }
         }
         if (connection.getReadyState().equals(OPEN)) {

            String objectMessage = NGParser.getInstance().getJson(paramsToSend, null);
            connection.write(objectMessage, async);
            async = false;
         }
      }
   }

   private void publish(Map<String, Object> paramsToSend) {
      for (SockJsConnection session: new HashSet<SockJsConnection>(sessions)) {

         if (!session.getReadyState().equals(OPEN)) {
            sessions.remove(session);
         } else {

            session.write(NGParser.getInstance().getJson(paramsToSend, null), async);
            async = false;
         }
      }
   }
}
