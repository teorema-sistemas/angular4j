package angular4J.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * a global holder for all bean stores (on Holder by sessionID)
 */
public class GlobalNGSessionContextsHolder {

   private static GlobalNGSessionContextsHolder instance;

   private final Map<String, NGSessionContextHolder> sessions = Collections.synchronizedMap(new HashMap<>());

   private GlobalNGSessionContextsHolder() {}

   private static final void createInstance() {
      instance = new GlobalNGSessionContextsHolder();
   }

   public static final synchronized GlobalNGSessionContextsHolder getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public void destroySession(String holderId) {
      this.sessions.remove(holderId);
   }

   public NGSessionContextHolder getSession(String holderId) {
      if (!this.sessions.containsKey(holderId)) {
         this.sessions.put(holderId, new NGSessionContextHolder());
      }
      return this.sessions.get(holderId);
   }
}
