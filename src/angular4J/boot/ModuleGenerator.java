package angular4J.boot;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import angular4J.api.http.Delete;
import angular4J.api.http.Get;
import angular4J.api.http.Post;
import angular4J.api.http.Put;
import angular4J.api.http.RequestIgnore;
import angular4J.js.cache.JsCache;
import angular4J.realtime.RealTime;
import angular4J.util.ClosureCompiler;
import angular4J.util.NGObject;
import angular4J.util.ReflectionUtils;

/**
 * <p>
 * ModuleGenerator is the main component for javascript generation. This class uses the registered
 * beans in BeanRegistry during application deployment to generate a minified script.
 * </p>
 *
 * @see BeanRegistry
 */
@SuppressWarnings("serial")
public class ModuleGenerator implements Serializable {

   private static ModuleGenerator instance;

   private StringBuilder script = null;

   private ModuleGenerator() {}

   private static final void createInstance() {
      instance = new ModuleGenerator();
   }

   public static final synchronized ModuleGenerator getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   ClosureCompiler compiler = new ClosureCompiler();

   /**
    * this method generate the ng4j.js content and write it to the <br>
    * jsBuffer used by BootServlet
    * 
    * @param jsBuffer
    */
   public synchronized void generate() {
      if (this.script == null) {
         this.script = new StringBuilder();
         this.script.append(JsCache.getInstance().getCore());
         StringBuilder beans = new StringBuilder();

         for (NGObject model: NGRegistry.getInstance().getNGModels()) {
            beans.append(this.generateModel(model));
         }

         this.script.append(this.compiler.getCompressedJavaScript(beans.toString()));
         this.script.append(JsCache.getInstance().getExtensions().toString());
      }
   }

   /**
    * this method concern is the generation of the AngularJS service from the @Angular4J CDI model.
    * 
    * @param model
    *           the bean wrapper for an @Angular4J CDI model.
    * @return a StringBuilder containing the generated angular service code.
    */
   private StringBuilder generateModel(NGObject model) {
      StringBuilder builder = new StringBuilder();
      builder.append(";app.factory('").append(model.getName()).append("',function ").append(model.getName()).append("(");
      builder.append("$rootScope, $http, $location,logger,responseHandler,$q");
      builder.append(",realtimeManager){");
      builder.append("var ").append(model.getName()).append("={serviceID:'").append(model.getName()).append("'};");
      builder.append("var rpath=$rootScope.baseUrl+'http/invoke/service/';");

      builder.append(generateStaticPart(model).toString());

      builder.append(");");

      return builder;
   }

   /**
    * 
    * @param model
    *           the CDI bean wrapper
    * @return StringBuilder containing the javaScript code of the static (non properties values
    *         dependent) code. by static parts we mean the JS code that can be generated from the
    *         java class of the model (to initialize the angularJs service we need to call getters
    *         on the CDI model instance and that is considered as the dynamic part of the angular4J
    *         javascript generation)
    */
   private StringBuilder generateStaticPart(NGObject model) {
      StringBuilder cachedPart = new StringBuilder();
      if (JsCache.getInstance().getCachedModel().containsKey(model.getTargetClass())) {
         return JsCache.getInstance().getCachedModel().get(model.getTargetClass());
      }

      for (Method m: model.getMethods()) {
         if (m.isAnnotationPresent(RequestIgnore.class)) {
            continue;
         }

         String httpMethod = null;
         boolean realTimePresent = false;
         if (realTimePresent = ReflectionUtils.isAnnotationPresent(m, RealTime.class)) {
            realTimePresent = true;
            httpMethod = "none";
         } else {
            if (ReflectionUtils.isAnnotationPresent(m, Get.class)) {
               httpMethod = "get";
            } else if (ReflectionUtils.isAnnotationPresent(m, Post.class)) {
               httpMethod = "post";
            } else if (ReflectionUtils.isAnnotationPresent(m, Put.class)) {
               httpMethod = "put";
            } else if (ReflectionUtils.isAnnotationPresent(m, Delete.class)) {
               httpMethod = "_delete";
            }
         }

         if (httpMethod == null) {
            continue;
         }

         cachedPart.append("ng4J.addMethod(").append(model.getName()).append(",'").append(m.getName()).append("',function(");

         Type[] parameters = m.getParameterTypes();

         if (parameters.length > 0) {
            StringBuilder argsString = new StringBuilder();
            for (int i = 0; i < parameters.length; i++) {
               argsString.append("arg").append(i).append(",");
            }
            cachedPart.append(argsString.substring(0, argsString.length() - 1));
         }

         cachedPart.append("){").append("var mainReturn={data:{}};").append("var params={};");
         cachedPart.append(addParams(m, parameters));
         cachedPart.append("var request = angular.copy(params);");
         cachedPart.append("revertObjects(request);");
         if (realTimePresent) {
            cachedPart.append("return realtimeManager.call(").append(model.getName()).append(",'").append(model.getName()).append(".").append(m.getName()).append("',request");
            cachedPart.append(").then(function(response){");
            cachedPart.append("var msg=(response);");
            cachedPart.append("mainReturn.data= responseHandler.handleResponse(msg,").append(model.getName()).append(",true);");
            cachedPart.append("return mainReturn.data;");
            cachedPart.append("} ,function(response){return $q.reject(response.data);});");
         } else {
            cachedPart.append("return $http.").append(httpMethod).append("(rpath+'").append(model.getName()).append("/").append(m.getName());

            cachedPart.append("/BASE64");

            if (httpMethod.equals("put") || httpMethod.equals("post")) {
               cachedPart.append("',base64Compress(request)");
            } else {
               String paramsQuery = ("?params='+encodeURIComponent(base64Compress(angular.toJson(request)))");
               cachedPart.append(paramsQuery);
            }

            cachedPart.append(").then(function(response) {");
            cachedPart.append("response.data = generateJson(response.data);");
            cachedPart.append("var msg=response.data;");
            cachedPart.append("mainReturn.data= responseHandler.handleResponse(msg,").append(model.getName()).append(",true);");
            cachedPart.append("return mainReturn.data;");
            cachedPart.append("} ,function(response){return $q.reject(response.data);});");
         }

         cachedPart.append("});");
      }

      cachedPart.append("return ").append(model.getName()).append(";}");
      JsCache.getInstance().getCachedModel().put(model.getClass(), cachedPart);

      return cachedPart;
   }

   private StringBuilder addParams(Method m, Type[] args) {
      StringBuilder sb = new StringBuilder();

      if (args.length > 0) {
         StringBuilder argsString = new StringBuilder();
         for (int i = 0; i < args.length; i++) {
            argsString.append("arg").append(i).append(",");
         }
         argsString = new StringBuilder(argsString.substring(0, argsString.length() - 1));
         sb.append("params['args']=[").append(argsString).append("];");
      }

      return sb;
   }

   public StringBuilder getValue() {
      this.generate();

      return this.script;
   }
}