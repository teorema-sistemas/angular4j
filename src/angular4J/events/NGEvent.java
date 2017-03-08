package angular4J.events;

/**
 * an NGEvent is a utility class used internally by ng4J to convert an angularJS event to a
 * CDI event.
 */
public class NGEvent {

   private String dataClass;
   private String data;

   public String getDataClass() {
      return dataClass;
   }

   public void setDataClass(String dataClass) {
      this.dataClass = dataClass;
   }

   public String getData() {
      return data;
   }

   public void setData(String data) {
      this.data = data;
   }
}
