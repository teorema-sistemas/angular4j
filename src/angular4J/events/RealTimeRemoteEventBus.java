package angular4J.events;

import static angular4J.events.Callback.BEFORE_SESSION_READY;

import java.util.Map;

import javax.inject.Inject;

import angular4J.api.Angular4J;
import angular4J.api.Eval;
import angular4J.context.NGSessionScoped;
import angular4J.realtime.RealTime;

/**
 * the RealTimeRemoteEventBus is a service called by ng4J throw an angularJS service extend
 * angularJS event firing to the CDI container (server) side with RealTime protocoles (exp:
 * WebSockets)
 */
@Angular4J
@NGSessionScoped
public class RealTimeRemoteEventBus {

   @Inject
   @Angular4J
   RemoteEventBus remoteEventBus;

   @Eval(BEFORE_SESSION_READY)
   public String addOnReadyCallback() {
      return "realTimeRemoteEventBus.onReadyState=function(fn){realtimeManager.onReadyState(fn);};";
   }

   @RealTime
   public void subscribe(String channel) {
      this.remoteEventBus.subscribe(channel);
   }

   @RealTime
   public void unsubscribe(String channel) {
      this.remoteEventBus.unsubscribe(channel);
   }

   @RealTime
   public void fire(NGEvent event) throws ClassNotFoundException {
      this.remoteEventBus.fire(event);
   }

   @RealTime
   public void broadcast(String channel, Map<String, Object> data, boolean withoutMe) {
      this.remoteEventBus.broadcast(channel, data, withoutMe);
   }
}