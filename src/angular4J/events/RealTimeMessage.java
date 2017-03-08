package angular4J.events;

import java.util.HashMap;
import java.util.Map;

/**
 * a RealTime message is a server to client message translated to an angularJS event. (only with
 * realTime context)
 */
public class RealTimeMessage {

   private final Map<String, Object> data = new HashMap<>();

   public RealTimeMessage set(String modelName, Object value) {
      data.put(modelName, value);

      return this;
   }

   public Map<String, Object> build() {
      return data;
   }

   @Override
   public String toString() {
      return data.toString();
   }
}
