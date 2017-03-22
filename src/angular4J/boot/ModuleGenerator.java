package angular4J.boot;

import static angular4J.events.Callback.AFTER_SESSION_READY;
import static angular4J.events.Callback.BEFORE_SESSION_READY;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import angular4J.api.CORS;
import angular4J.api.Eval;
import angular4J.api.http.Delete;
import angular4J.api.http.Get;
import angular4J.api.http.Post;
import angular4J.api.http.Put;
import angular4J.api.http.RequestIgnore;
import angular4J.context.GlobalNGSessionContextsHolder;
import angular4J.context.NGLocator;
import angular4J.context.NGSessionScopeContext;
import angular4J.events.Callback;
import angular4J.js.cache.JsCache;
import angular4J.realtime.RealTime;
import angular4J.util.ClosureCompiler;
import angular4J.util.CommonUtils;
import angular4J.util.Constants;
import angular4J.util.NGObject;

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
   public void generate() {
      if (this.script == null) {

         NGSessionScopeContext.getInstance().setCurrentContext(Constants.GENERATE_SESSION_ID);

         this.script = new StringBuilder();
         this.script.append(JsCache.getInstance().getCore());
         StringBuilder beans = new StringBuilder();

         for (NGObject model: NGRegistry.getInstance().getNGModels()) {
            beans.append(this.generateModel(model));
         }

         this.script.append(this.compiler.getCompressedJavaScript(beans.toString()));
         this.script.append(JsCache.getInstance().getExtensions().toString());

         GlobalNGSessionContextsHolder.getInstance().destroySession(Constants.GENERATE_SESSION_ID);
      }
   }

   public StringBuilder getValue() {
      this.generate();

      return this.script;
   }

   /**
    * this method concern is the generation of the AngularJS service from the @Angular4J CDI model.
    * 
    * @param model
    *           the bean wrapper for an @Angular4J CDI model.
    * @return a StringBuilder containing the generated angular service code.
    */
   private StringBuilder generateModel(NGObject model) {
      Object reference = NGLocator.getInstance().lookup(model.getName(), Constants.GENERATE_SESSION_ID);

      StringBuilder builder = new StringBuilder();
      builder.append(";app.factory('").append(model.getName()).append("',function ").append(model.getName()).append("(");
      builder.append("$rootScope, $http, $location,logger,responseHandler,$q");
      builder.append(",realtimeManager){");
      builder.append("var ").append(model.getName()).append("={serviceID:'").append(model.getName()).append("'};");
      builder.append("var rpath=$rootScope.baseUrl+'http/invoke/service/';");

      for (Method m: model.getMethods()) {
         Annotation ann = CommonUtils.getAnnotation(m, Eval.class);
         if (ann != null) {

            Callback callback = ((Eval) ann).value();
            try {
               String execution = (String) m.invoke(reference);
               StringBuilder js = new StringBuilder();

               if (callback.equals(BEFORE_SESSION_READY)) {
                  js.append(execution);
               } else if (callback.equals(AFTER_SESSION_READY)) {
                  js.append("setTimeout(listen,500);function listen(){if(realtimeManager.ready){");
                  js.append(execution);
                  js.append("}else{setTimeout(listen,500);}}");
               }
               builder.append(js);
            }
            catch (ClassCastException e) {
               throw new RuntimeException("for bean name: " + model.getName() + " --> an @Eval bloc must return a String");
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
               e.printStackTrace();
            }

         }
      }

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
    *         java class of the model (to initialize the angularJs service we need to call getters on
    *         the CDI model instance and that is considered as the dynamic part of the angular4J
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

         if (m.isAnnotationPresent(Eval.class)) {
            continue;
         }

         String httpMethod = null;
         boolean corsPresent = false;
         boolean realTimePresent = false;
         if (m.isAnnotationPresent(CORS.class)) {
            corsPresent = true;
            httpMethod = "get";
         } else if (realTimePresent = CommonUtils.isAnnotationPresent(m, RealTime.class)) {
            realTimePresent = true;
            httpMethod = "none";
         } else {
            if (CommonUtils.isAnnotationPresent(m, Get.class)) {
               httpMethod = "get";
            } else if (CommonUtils.isAnnotationPresent(m, Post.class)) {
               httpMethod = "post";
            } else if (CommonUtils.isAnnotationPresent(m, Put.class)) {
               httpMethod = "put";
            } else if (CommonUtils.isAnnotationPresent(m, Delete.class)) {
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

            if (corsPresent) {
               cachedPart.append("/CORS");
            } else {
               cachedPart.append("/BASE64");
            }

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
}