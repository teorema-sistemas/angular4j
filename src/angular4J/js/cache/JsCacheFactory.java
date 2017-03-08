package angular4J.js.cache;

public class JsCacheFactory {

   private Class<? extends JsLoader> jsLoader;

   public JsCacheFactory(Class<? extends JsLoader> staticJsLoader) {
      this.jsLoader = staticJsLoader;
   }

   public void BuildJsCache() {
      JsLoader loader;
      try {
         loader = jsLoader.newInstance();
         loader.LoadCoreScript();
         loader.LoadExtensions();
         JsCache.getInstance().Compress();
      }
      catch (InstantiationException e) {
         e.printStackTrace();
      }
      catch (IllegalAccessException e) {
         e.printStackTrace();
      }
   }
}
