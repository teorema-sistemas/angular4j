package angular4J.js.cache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import javax.inject.Named;

import angular4J.boot.BeanRegistry;
import angular4J.context.BeanHolder;
import angular4J.ngservices.NGService;

/**
 * This is a default implementation for {@link JsCacheLoader} for static Javascript loading into
 * memory.
 */
public class DefaultJsCacheLoader extends JsLoader {

   private StringBuilder readFile(String fileName) throws IOException {
      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

      StringBuilder content = new StringBuilder();
      InputStream fileStream = classLoader.getResourceAsStream(fileName);
      InputStreamReader fileReader = new InputStreamReader(fileStream);
      BufferedReader in = new BufferedReader(fileReader);

      String line;
      while ((line = in.readLine()) != null) {
         content.append(line);
      }

      return content;
   }

   @Override
   public void LoadCoreScript() {
      try {
         JsCache.getInstance().appendToCore(this.readFile("/js/script-detection.js"));
         JsCache.getInstance().appendToCore(this.readFile("/js/pako.min.js"));
         JsCache.getInstance().appendToCore(this.readFile("/js/angular4j-utils.js"));
         JsCache.getInstance().appendToCore(this.readFile("/js/angular4j-main-object.js"));
      }
      catch (IOException e) {
         e.printStackTrace();
      }

      Class<? extends Object> appClass = BeanRegistry.getInstance().getAppClass();
      String appName = getAppName(appClass);
      JsCache.getInstance().appendToCore(new StringBuilder(String.format("var app=angular.module('%s', [", appName)));
      JsCache.getInstance().appendToCore(new StringBuilder("]).run(function($rootScope) {$rootScope.sessionUID = sessionId;"));
      JsCache.getInstance().appendToCore(new StringBuilder("$rootScope.baseUrl=sript_origin;});"));
   }

   @Override
   public void LoadExtensions() {
      StringBuilder builder = new StringBuilder();
      for (NGService extention: BeanRegistry.getInstance().getExtentions()) {
         Method m;
         try {
            m = extention.getClass().getMethod("render");
            builder.append(m.invoke(extention)).append(";");
         }
         catch (Exception e) {
            e.printStackTrace();
         }
      }
      JsCache.getInstance().appendToExtensions(builder);
   }

   private String getAppName(Class<? extends Object> appClass) {
      String appName = null;
      if (appClass.isAnnotationPresent(Named.class)) {
         appName = appClass.getAnnotation(Named.class).value();
      }

      if ((appName == null) || (appName.length() < 1)) {
         appName = BeanHolder.getInstance().getName(appClass);
      }
      return appName;
   }
}