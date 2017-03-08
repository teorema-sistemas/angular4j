package angular4J.js.cache;

import java.util.HashMap;
import java.util.Map;

import angular4J.util.ClosureCompiler;

/**
 * A cache for the static (non beans instances dependent) angular-beans.js code The content of this
 * cache can be loaded by an implementation of a {@link JsLoader}. <b>It's mandatory to first
 * load the content of this class before any bean generation. </b>
 *
 * @see angular4J.js.cache.JsLoader
 * @see angular4J.js.cache.JsCacheFactory
 * @see angular4J.js.cache.DefaultJsCacheLoader
 */
public class JsCache {

   private static JsCache instance;

   private final Map<Class, StringBuilder> cachedBean = new HashMap<>();
   private StringBuilder coreScript = new StringBuilder();
   private StringBuilder extentionsScript = new StringBuilder();

   private JsCache() {}

   private static final void createInstance() {
      instance = new JsCache();
   }

   public static final synchronized JsCache getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Map<Class, StringBuilder> getCachedBean() {
      return this.cachedBean;
   }

   public StringBuilder getCore() {
      return this.coreScript;
   }

   public void appendToCore(StringBuilder str) {
      this.coreScript.append(str);
   }

   public StringBuilder getExtensions() {
      return this.extentionsScript;
   }

   public void appendToExtensions(StringBuilder str) {
      this.extentionsScript.append(str);
   }

   public void Compress() {
      coreScript = new StringBuilder(new ClosureCompiler().getCompressedJavaScript(coreScript.toString()));
      extentionsScript = new StringBuilder(new ClosureCompiler().getCompressedJavaScript(extentionsScript.toString()));
   }
}