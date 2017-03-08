
package angular4J.log;

import java.io.Serializable;
import java.util.LinkedList;

import angular4J.context.NGSessionScoped;

/**
 * when injected into an @Angular4J, NGLogger will log messages directly on the browser console.
 * 
 **/

@SuppressWarnings("serial")
@NGSessionScoped
public class NGLogger implements Serializable {

   public enum Level {
      LOG("log"),
      INFO("info"),
      WARN("warn"),
      ERROR("error"),
      DEBUG("debug");

      private final String level;

      private Level(String level) {
         this.level = level;
      }

      public String getLevel() {
         return level;
      }
   }

   private final LinkedList<LogMessage> logPool = new LinkedList<>();

   /**
    * main method of the NGLogger
    * 
    * @param level
    *           : ng4J.log.NGLogger.Level : LOG("log"), INFO("info"), WARN("warn"),
    *           ERROR("error"), DEBUG("debug")
    * 
    * 
    * @param message
    *           :String message to log
    * @param args
    *           : define values with String template
    */

   public void log(Level level, String message, Object... args) {
      logPool.addLast(new LogMessage(level.getLevel(), String.format(message, args)));

   }

   public LogMessage poll(String UID) {
      return logPool.pollFirst();
   }

   public LinkedList<LogMessage> getLogPool() {
      return logPool;
   }
}
