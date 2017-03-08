package angular4J.io;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * this is a cache to store java methods calls that return the binary content from Angular4J
 */

public class ByteArrayCache implements Serializable {

   private static ByteArrayCache instance;
   
   private final Map<String, byte[]> cache = Collections.synchronizedMap(new HashMap<>());

   private ByteArrayCache() {}

   private static final void createInstance() {
      instance = new ByteArrayCache();
   }

   public static final synchronized ByteArrayCache getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Map<String, byte[]> getCache() {
      return cache;
   }
}
