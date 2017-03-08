package angular4J.log;

/**
 *         a class internally used by angular beans to send log messages to the front end (with
 *         NGLogger)
 */
public class LogMessage {

   private final String level;
   private final String message;

   public String getLevel() {
      return level;
   }

   public String getMessage() {
      return message;
   }

   public LogMessage(String level, String message) {
      this.level = level;
      this.message = message;
   }

   @Override
   public String toString() {
      return "LEVEL: " + getLevel() + " MESSAGE: " + getMessage();
   }
}
