package angular4J.realtime;

import java.util.HashSet;
import java.util.Set;

import angular4J.context.SessionMapper;
import angular4J.sockjs.SockJsConnection;

/**
 * this is a holder for all sockJs opened sessions
 */
public class GlobalConnectionHolder {

   private static GlobalConnectionHolder instance;
   private final Set<SockJsConnection> connections;

   private GlobalConnectionHolder() {
      connections = new HashSet<>();
   }

   private static final void createInstance() {
      instance = new GlobalConnectionHolder();
   }

   public static final synchronized GlobalConnectionHolder getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Set<SockJsConnection> getConnections() {
      return connections;
   }

   public synchronized void removeConnection(String id) {
      try {
         for (SockJsConnection connection: connections) {
            String httpSessionId = SessionMapper.getInstance().getHTTPSessionID(connection.id);
            if (httpSessionId != null && httpSessionId.equals(id)) {
               SessionMapper.getInstance().getSessions().remove(id);
               try {
                  connection.destroy();
               }
               catch (Exception ex) {}

               connections.remove(connection);
            }
         }
      }
      catch (Exception e) {}
   }
}
