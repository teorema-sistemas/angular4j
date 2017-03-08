package angular4J.events;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import angular4J.context.SessionMapper;

/**
 * when a broadcast operation is triggered (ModelQuery broadcast or Event broadcast) the
 * RealTimeClient willUse the BroadcastManager to now witch sessions's will be involved in the data
 * broadcast
 */

public class BroadcastManager {

   private static BroadcastManager instance;
   
   private final Map<String, Set<String>> subscriptions = Collections.synchronizedMap(new HashMap<>());

   private BroadcastManager() {}

   private static final void createInstance() {
      instance = new BroadcastManager();
   }

   public static final synchronized BroadcastManager getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   /**
    * check if a specific sockJs session is subscribed or not to a specific channel
    * 
    * @param sockJSSessionID
    *           sockJs Session id
    * @param channel
    *           the channel
    * @return the session passed in sockJSSessionID parameter is subscribed (true) or not (false) to
    *         the specified channel
    */

   public boolean isSubscribed(String sockJSSessionID, String channel) {

      String httpSessionId = SessionMapper.getInstance().getHTTPSessionID(sockJSSessionID);
      if (this.subscriptions.get(channel) == null) {
         this.subscriptions.put(channel, new HashSet<String>());
      }
      return this.subscriptions.get(channel).contains(httpSessionId);
   }

   public void subscribe(String httpSessionID, String channel) {

      if (this.subscriptions.get(channel) == null) {
         this.subscriptions.put(channel, new HashSet<String>());
      }
      this.subscriptions.get(channel).add(httpSessionID);
   }

   public void unsubscribe(String httpSessionID, String channel) {
      if (this.subscriptions.get(channel) != null) this.subscriptions.get(channel).remove(httpSessionID);
   }

   public Map<String, Set<String>> getSubscriptions() {
      return this.subscriptions;
   }
}
