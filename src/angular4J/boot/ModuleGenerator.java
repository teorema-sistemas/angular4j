package angular4J.boot;

import static angular4J.events.Callback.AFTER_SESSION_READY;
import static angular4J.events.Callback.BEFORE_SESSION_READY;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import angular4J.api.CORS;
import angular4J.api.Eval;
import angular4J.api.NGPostConstruct;
import angular4J.api.http.Delete;
import angular4J.api.http.Get;
import angular4J.api.http.Post;
import angular4J.api.http.Put;
import angular4J.api.http.RequestIgnore;
import angular4J.context.BeanLocator;
import angular4J.context.GlobalNGSessionContextsHolder;
import angular4J.events.Callback;
import angular4J.js.cache.JsCache;
import angular4J.realtime.RealTime;
import angular4J.util.ClosureCompiler;
import angular4J.util.CommonUtils;
import angular4J.util.NGBean;
import angular4J.util.NGParser;

/**
 * <p>
 * ModuleGenerator is the main component for javascript generation. This class uses the registered
 * beans in BeanRegistry during application deployment to generate a minified script.
 * </p>
 *
 * @see BeanRegistry
 */
public class ModuleGenerator implements Serializable {

   private static final String SESSION_ID = "_SESSION_GENERATOR_NG4J";

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

         GlobalNGSessionContextsHolder.getInstance().getSession(SESSION_ID);

         this.script = new StringBuilder();
         this.script.append(JsCache.getInstance().getCore());
         StringBuilder beans = new StringBuilder();

         for (NGBean mb: BeanRegistry.getInstance().getNGBeans()) {
            beans.append(generateBean(mb));
         }

         this.script.append(this.compiler.getCompressedJavaScript(beans.toString()));
         this.script.append(JsCache.getInstance().getExtensions().toString());

         GlobalNGSessionContextsHolder.getInstance().destroySession(SESSION_ID);
      }
   }

   public StringBuilder getValue() {
      this.generate();

      return this.script;
   }

   /**
    * this method concern is the generation of the AngularJS service from the @Angular4J CDI bean.
    * 
    * @param bean
    *           the bean wrapper for an @Angular4J CDI bean.
    * @return a StringBuilder containing the generated angular service code.
    */
   private StringBuilder generateBean(NGBean bean) {
      Object reference = BeanLocator.getInstance().lookup(bean.getName(), SESSION_ID);

      StringBuilder builder = new StringBuilder();
      builder.append(";app.factory('").append(bean.getName()).append("',function ").append(bean.getName()).append("(");
      builder.append("$rootScope, $http, $location,logger,responseHandler,$q");
      builder.append(",realtimeManager){");
      builder.append("var ").append(bean.getName()).append("={serviceID:'").append(bean.getName()).append("'};");
      builder.append("var rpath=$rootScope.baseUrl+'http/invoke/service/';");

      for (Method m: bean.getMethods()) {
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
               throw new RuntimeException("for bean name: " + bean.getName() + " --> an @Eval bloc must return a String");
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
               e.printStackTrace();
            }

         }
      }

      for (Method get: bean.getters()) {
         Object result = null;
         String getter = get.getName();
         String modelName = CommonUtils.obtainFieldNameFromAccessor(getter);

         Method m;
         try {
            m = bean.getTargetClass().getMethod(getter);

            result = m.invoke(reference);
            if ((result == null && (m.getReturnType().equals(String.class)))) {
               result = "";
            }

            if (result == null) {
               continue;
            }

            Class<? extends Object> resultClazz = result.getClass();
            if (!resultClazz.isPrimitive()) {
               result = NGParser.getInstance().getJson(result, null);
            }

         }
         catch (Exception e) {}

         builder.append(bean.getName()).append(".").append(modelName).append("=").append(result).append(";");
      }

      builder.append(generateStaticPart(bean).toString());
      builder.append(");");

      return builder;
   }

   /**
    * 
    * @param bean
    *           the CDI bean wrapper
    * @return StringBuilder containing the javaScript code of the static (non properties values
    *         dependent) code. by static parts we mean the JS code that can be generated from the
    *         java class of the bean (to initialize the angularJs service we need to call getters on
    *         the CDI bean instance and that is considered as the dynamic part of the angularBean
    *         javascript generation)
    */
   private StringBuilder generateStaticPart(NGBean bean) {
      StringBuilder cachedPart = new StringBuilder();
      if (JsCache.getInstance().getCachedBean().containsKey(bean.getTargetClass())) {
         return JsCache.getInstance().getCachedBean().get(bean.getTargetClass());
      }

      Method[] nativesMethods = Object.class.getMethods();

      boolean corsEnabled = false;
      for (Method m: bean.getMethods()) {

         if (m.isAnnotationPresent(Eval.class)) {
            continue;
         }

         if (m.isAnnotationPresent(RequestIgnore.class)) {
            continue;
         }

         for (Method nativeMethod: nativesMethods) {
            if (nativeMethod.equals(m) && !Modifier.isVolatile(m.getModifiers())) {
               continue;
            }
         }

         if ((!CommonUtils.isSetter(m)) && (!CommonUtils.isGetter(m))) {
            Set<Method> setters = new HashSet<>();

            String httpMethod = null;
            if (corsEnabled = (m.isAnnotationPresent(CORS.class))) {
               httpMethod = "get";
            }

            if (CommonUtils.isAnnotationPresent(m, Get.class)) {
               httpMethod = "get";
            } else if (CommonUtils.isAnnotationPresent(m, Post.class)) {
               httpMethod = "post";
            } else if (CommonUtils.isAnnotationPresent(m, Put.class)) {
               httpMethod = "put";
            } else if (CommonUtils.isAnnotationPresent(m, Delete.class)) {
               httpMethod = "_delete";
            } else if (httpMethod == null) {
               continue;
            }

            cachedPart.append("ng4J.addMethod(").append(bean.getName()).append(",'").append(m.getName()).append("',function(");

            Type[] parameters = m.getParameterTypes();

            if (parameters.length > 0) {
               StringBuilder argsString = new StringBuilder();
               for (int i = 0; i < parameters.length; i++) {
                  argsString.append("arg").append(i).append(",");
               }
               cachedPart.append(argsString.substring(0, argsString.length() - 1));
            }

            cachedPart.append("){").append("var mainReturn={data:{}};").append("var params={};");
            cachedPart.append(addParams(bean, setters, m, parameters));

            if (CommonUtils.isAnnotationPresent(m, RealTime.class)) {
               cachedPart.append("return realtimeManager.call(").append(bean.getName()).append(",'").append(bean.getName()).append(".").append(m.getName()).append("',params");
               cachedPart.append(").then(function(response){");
               cachedPart.append("var msg=(response);");
               cachedPart.append("mainReturn.data= responseHandler.handleResponse(msg,").append(bean.getName()).append(",true);");
               cachedPart.append("return mainReturn.data;");
               cachedPart.append("} ,function(response){return $q.reject(response.data);});");
            } else {
               cachedPart.append("return $http.").append(httpMethod).append("(rpath+'").append(bean.getName()).append("/").append(m.getName());

               if (corsEnabled) {
                  cachedPart.append("/CORS");
               } else {
                  cachedPart.append("/JSON");
               }

               if (httpMethod.equals("put") || httpMethod.equals("post")) {
                  cachedPart.append("',base64Compress(params)");
               } else {
                  String paramsQuery = ("?params='+encodeURIComponent(base64Compress(angular.toJson(params)))");
                  cachedPart.append(paramsQuery);
               }

               cachedPart.append(").then(function(response) {");
               cachedPart.append("response.data = generateJson(response.data);");
               cachedPart.append("var msg=response.data;");
               cachedPart.append("mainReturn.data= responseHandler.handleResponse(msg,").append(bean.getName()).append(",true);");
               cachedPart.append("return mainReturn.data;");
               cachedPart.append("} ,function(response){return $q.reject(response.data);});");
            }

            cachedPart.append("});");

            if ((!CommonUtils.isSetter(m)) && (!CommonUtils.isGetter(m))) {
               if (CommonUtils.isAnnotationPresent(m, NGPostConstruct.class)) {
                  cachedPart.append("realtimeManager.onReadyState(function(){");
                  cachedPart.append(bean.getName()).append(".").append(m.getName()).append("();");
                  cachedPart.append("});");
               }
            }
         }
      }

      cachedPart.append("return ").append(bean.getName()).append(";}");
      JsCache.getInstance().getCachedBean().put(bean.getClass(), cachedPart);

      return cachedPart;
   }

   private StringBuilder addParams(NGBean bean, Set<Method> setters, Method m, Type[] args) {
      StringBuilder sb = new StringBuilder();

      for (Method setter: setters) {
         String name = CommonUtils.obtainFieldNameFromAccessor(setter.getName());
         sb.append("params['").append(name).append("']=").append(bean.getName()).append(".").append(name).append(";");
      }

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