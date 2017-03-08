package angular4J.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.gson.ExclusionStrategy;

/***
 * Angular4J Configuration Util
 * 
 * <b>Properties:</b> ng.properties
 */
public class NGConfig {

   private static String ANGULAR_BEANS_PROPERTIES = "ng.properties";
   private static String GSON_EXCLUSION_STRATEGY = "gson.exclusionStrategy";

   public static String getProperty(String property) {
      try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ANGULAR_BEANS_PROPERTIES)) {
         if (is != null) {
            Properties props = new Properties();
            props.load(is);
            return props.getProperty(property);
         }
      }
      catch (IOException e) {
         Logger.getLogger(NGConfig.class.getName()).severe(e.toString());
      }

      return null;
   }

   public static ExclusionStrategy[] getGsonExclusionStrategy() {
      try {
         String exclusionStrategyName = NGConfig.getProperty(GSON_EXCLUSION_STRATEGY);

         if (exclusionStrategyName != null && exclusionStrategyName.length() > 0) {
            return new ExclusionStrategy[]{(ExclusionStrategy) Class.forName(exclusionStrategyName).newInstance()};
         }
      }
      catch (Exception e) {
         Logger.getLogger(NGConfig.class.getName()).severe(e.toString());
      }

      return new ExclusionStrategy[]{};
   }
}
