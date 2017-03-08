
package angular4J.remote;

import java.io.Serializable;

import com.google.gson.JsonObject;

import angular4J.realtime.RealTimeClient;
import angular4J.sockjs.SockJsConnection;

/**
 * a RealTimeDataReceivedEvent concern data reception with the realtime sockjs protocol
 */

@SuppressWarnings("serial")
public class RealTimeDataReceivedEvent implements DataReceived, Serializable {

   private SockJsConnection connection;
   private JsonObject data;
   private RealTimeClient client;
   private String sessionId;

   public RealTimeDataReceivedEvent(SockJsConnection connection, JsonObject data) {
      this.connection = connection;
      this.data = data;

   }

   public void setConnection(SockJsConnection connection) {
      this.connection = connection;
   }

   public SockJsConnection getConnection() {
      return connection;
   }

   @Override
   public JsonObject getData() {
      return data;
   }

   public void setClient(RealTimeClient wSocketClient) {
      this.client = wSocketClient;

   }

   public RealTimeClient getClient() {
      return client;
   }

   public String getSessionId() {
      return sessionId;
   }

   public void setSessionId(String sessionId) {
      this.sessionId = sessionId;
   }
}
