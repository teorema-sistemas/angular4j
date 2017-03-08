package angular4J.context;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * map a group of sockJS sessions to the current HTTP session.
 */
public class SessionMapper {

   private static SessionMapper instance;

   private final Map<String, Set<String>> sessions = Collections.synchronizedMap(new HashMap<>());

   private SessionMapper() {}

   private static final void createInstance() {
      instance = new SessionMapper();
   }

   public static final synchronized SessionMapper getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Map<String, Set<String>> getSessions() {
      return this.sessions;
   }

   public String getHTTPSessionID(String sockJSSessionID) {
      for (String httpSession: this.sessions.keySet()) {
         if (this.sessions.get(httpSession).contains(sockJSSessionID)) {
            return httpSession;
         }
      }
      return null;
   }
}
