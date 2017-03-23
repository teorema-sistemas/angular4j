package angular4J.events;

import java.util.Map;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import angular4J.api.Angular4J;
import angular4J.api.CORS;
import angular4J.context.NGSessionScoped;
import angular4J.realtime.RealTimeClient;
import angular4J.util.NGParser;

/**
 * the RemoteEventBus is a service called by ng4J throw an angularJS service extend angularJS event
 * firing to the CDI container (server) side
 */

@Angular4J
@NGSessionScoped
public class RemoteEventBus {

   @Inject
   @AngularEvent
   Event<Object> ngEventBus;

   @Inject
   HttpSession session;

   @Inject
   RealTimeClient client;

   @CORS
   public void subscribe(String channel) {
      BroadcastManager.getInstance().subscribe(this.session.getId(), channel);
   }

   @CORS
   public void unsubscribe(String channel) {
      BroadcastManager.getInstance().unsubscribe(this.session.getId(), channel);
   }

   @CORS
   public void fire(NGEvent event) throws ClassNotFoundException {
      Object eventObject = NGParser.getInstance().convertEvent(event);
      this.ngEventBus.fire(eventObject);
   }

   @CORS
   public void broadcast(String channel, Map<String, Object> data, boolean withoutMe) {
      RealTimeMessage realTimeMessage = new RealTimeMessage();

      for (Map.Entry<String, Object> entry: data.entrySet()) {
         realTimeMessage.set(entry.getKey(), entry.getValue());
      }

      this.client.broadcast(channel, realTimeMessage, withoutMe);
   }
}
