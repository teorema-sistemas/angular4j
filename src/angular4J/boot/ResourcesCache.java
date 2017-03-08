package angular4J.boot;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import angular4J.util.NGParser;

/**
 * 
 * The ResourcesCache is a cache for already requested resources (any properties files converted to
 * JSON) to avoid redundant transformations. used by ResourceServlet
 */
public class ResourcesCache {

   private static ResourcesCache instance;

   private final Map<String, String> cache = new HashMap<>();

   private ResourcesCache() {}

   private static final void createInstance() {
      instance = new ResourcesCache();
   }

   public static final synchronized ResourcesCache getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public String get(String resourceName, ServletContext servletContext) {
      String json = null;

      if (!cache.containsKey(resourceName)) {
         InputStream is = servletContext.getResourceAsStream("/META-INF" + resourceName + ".properties");
         Properties properties = new Properties();
         try {
            properties.load(is);
            json = NGParser.getInstance().getJson(properties, null);
         }
         catch (IOException e) {
            e.printStackTrace();
         }
      }
      return json;
   }
}
