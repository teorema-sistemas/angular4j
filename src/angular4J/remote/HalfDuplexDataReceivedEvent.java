package angular4J.remote;

import java.io.Serializable;

import com.google.gson.JsonObject;

/**
 * a HalfDuplexDataReceivedEvent concern data reception with a standard HTTP protocol
 */

@SuppressWarnings("serial")
public class HalfDuplexDataReceivedEvent implements DataReceived, Serializable {

   private final JsonObject data;

   @Override
   public JsonObject getData() {
      return data;
   }

   public HalfDuplexDataReceivedEvent(JsonObject data) {
      this.data = data;
   }
}
