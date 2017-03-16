package angular4J.remote;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import angular4J.api.NGCastIgnore;
import angular4J.api.NGClassCast;
import angular4J.api.NGParam;
import angular4J.api.NGParamCast;
import angular4J.context.NGSessionScopeContext;
import angular4J.log.NGLogger;
import angular4J.log.NGLogger.Level;
import angular4J.util.CommonUtils;
import angular4J.util.ModelQueryFactory;
import angular4J.util.ModelQueryImpl;
import angular4J.util.NGParamType;
import angular4J.util.NGParser;

/**
 * Angular4J RPC main handler.
 */

@SuppressWarnings("serial")
@ApplicationScoped
public class InvocationHandler implements Serializable {

   @Inject
   NGLogger logger;

   @Inject
   ModelQueryFactory modelQueryFactory;

   public void realTimeInvoke(Object ServiceToInvoque, String methodName, JsonObject params, RealTimeDataReceivedEvent event, long reqID, String UID) {

      NGSessionScopeContext.getInstance().setCurrentContext(UID);
      Map<String, Object> returns = new HashMap<>();
      returns.put("isRT", true);
      try {
         genericInvoke(ServiceToInvoque, methodName, params, returns, reqID, UID, null);

         if (returns.get("mainReturn") != null) {
            event.getConnection().write(NGParser.getInstance().getJson(returns, null), false);
         }
      }
      catch (SecurityException | ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
         e.printStackTrace();
      }
   }

   public Map<String, Object> invoke(Object o, String method, JsonObject params, String UID, HttpServletRequest request) {

      NGSessionScopeContext.getInstance().setCurrentContext(UID);
      Map<String, Object> returns = new HashMap<>();
      try {
         returns.put("isRT", false);
         genericInvoke(o, method, params, returns, 0, UID, request);
      }
      catch (Exception e) {
         e.printStackTrace();
      }
      return returns;
   }

   private Class<?> getPrimitiveClass(JsonElement element) {
      if (element.getAsJsonPrimitive().isBoolean()) {
         return Boolean.class;
      }

      if (element.getAsJsonPrimitive().isNumber()) {
         if (element.getAsString().contains(".")) {
            return Double.class;
         }
         return Long.class;
      }

      return String.class;
   }

   private Method getMethod(Class<?> clazz, String methodName, int paramSize) {
      for (Method m: clazz.getMethods()) {
         if (m.getName().equals(methodName) && m.getGenericParameterTypes().length == paramSize && !Modifier.isVolatile(m.getModifiers())) {
            return m;
         }
      }
      return null;
   }

   private boolean isPrimitiveParseRequired(Class<?> klass) {
      if (klass.isAssignableFrom(java.util.Date.class)) {
         return true;
      }

      if (klass.isAssignableFrom(java.sql.Date.class)) {
         return true;
      }

      if (klass.isAssignableFrom(java.sql.Time.class)) {
         return true;
      }

      if (klass.isAssignableFrom(java.sql.Timestamp.class)) {
         return true;
      }

      if (klass.isAssignableFrom(byte[].class)) {
         return true;
      }

      return false;
   }

   private Object getPrimitiveType(Type param, JsonElement element) {
      Class<?> klass = null;
      String typeString = ((param).toString());

      if (typeString.startsWith("class")) {
         try {
            klass = Class.forName(typeString.substring(6));
            if (klass.equals(Object.class)) {
               klass = this.getPrimitiveClass(element);
            }
         }
         catch (Exception e) {}
      } else {
         switch (typeString) {
            case "int": {
               klass = Integer.TYPE;
            }
               break;
            case "long": {
               klass = Long.TYPE;
            }
               break;
            case "double": {
               klass = Double.TYPE;
            }
               break;
            case "float": {
               klass = Float.TYPE;
            }
               break;
            case "boolean": {
               klass = Boolean.TYPE;
            }
               break;
            case "char": {
               klass = Character.TYPE;
            }
               break;
            case "byte": {
               klass = Byte.TYPE;
            }
               break;
            case "short": {
               klass = Short.TYPE;
            }
               break;
         }
      }

      if (this.isPrimitiveParseRequired(klass)) {
         return NGParser.getInstance().deserialise(klass, element);
      } else {
         return NGParser.getInstance().deserialiseFromString(element.getAsString(), klass);
      }
   }

   private Map<Integer, Object[]> getParamCastMap(Method m) {
      if (CommonUtils.isAnnotationPresent(m, NGParamCast.class)) {
         NGParamCast ngCast = (NGParamCast) CommonUtils.getAnnotation(m, NGParamCast.class);
         if (ngCast != null) {
            NGParam[] params = ngCast.value();
            if (params != null && params.length > 0) {

               Map<Integer, Object[]> result = new HashMap<>();

               for (NGParam param: params) {
                  Object[] aParams = new Object[3];
                  aParams[0] = param.id();
                  aParams[1] = param.type();
                  aParams[2] = param.required();

                  for (int i: param.index()) {
                     result.put(i, aParams);
                  }
               }

               return result;
            }
         }
      }
      return null;
   }

   private Class<?> getParamClass(Object service, String id, NGParamType type, boolean required) {
      if (id != null && id.length() > 0) {
         try {
            if (type.equals(NGParamType.FIELD)) {
               Field[] fields = service.getClass().getDeclaredFields();
               for (Field field: fields) {
                  NGClassCast ann = field.getAnnotation(NGClassCast.class);
                  if (ann != null && ArrayUtils.contains(ann.value(), id)) {
                     field.setAccessible(true);

                     return (Class<?>) field.get(service);
                  }
               }
            } else {
               Method[] ms = service.getClass().getDeclaredMethods();
               if (ms != null && ms.length > 0) {
                  for (Method m: ms) {
                     NGClassCast ann = (NGClassCast) CommonUtils.getAnnotation(m, NGClassCast.class);
                     if (ann != null && ArrayUtils.contains(ann.value(), id)) {
                        m.setAccessible(true);
                        return (Class<?>) m.invoke(service);
                     }
                  }
               }
            }
         }
         catch (Exception e) {
            if (required) {
               e.printStackTrace();
            }
         }
      }
      return null;
   }

   private Class<?> getParamCastType(Object service, Method m, int paramNumber) {
      if (!m.isAnnotationPresent(NGCastIgnore.class)) {
         Map<Integer, Object[]> ngCast = this.getParamCastMap(m);
         if (ngCast != null) {
            Object[] ngCastParam = ngCast.get(paramNumber);
            if (ngCastParam != null) {
               return this.getParamClass(service, (String) ngCastParam[0], (NGParamType) ngCastParam[1], (boolean) ngCastParam[2]);
            }
         }
      }
      return null;
   }

   private Object castNGParam(Object service, Class<?> nGParam, JsonElement element) {
      if (element.isJsonArray()) {
         return NGParser.getInstance().deserialise(nGParam, element.getAsJsonArray());
      } else {
         return NGParser.getInstance().deserialise(nGParam, element);
      }
   }

   private Object castParam(Object service, Type param, JsonElement element) {
      if (element.isJsonPrimitive()) {
         return this.getPrimitiveType(param, element);
      } else if (element.isJsonArray()) {
         return NGParser.getInstance().deserialise(param, element.getAsJsonArray());
      } else {
         return NGParser.getInstance().deserialise(param, element);
      }
   }

   private void genericInvoke(Object service, String methodName, JsonObject params, Map<String, Object> returns, long reqID, String UID, HttpServletRequest request) throws SecurityException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

      Object mainReturn = null;
      Method m = null;
      JsonElement argsElem = params.get("args");

      if (reqID > 0) {
         returns.put("reqId", reqID);
      }
      if (argsElem != null) {

         JsonArray args = params.get("args").getAsJsonArray();
         m = this.getMethod(service.getClass(), methodName, args.size());

         Type[] parameters = m.getGenericParameterTypes();
         if (parameters.length == args.size()) {

            List<Object> argsValues = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
               Class<?> ngCastType = this.getParamCastType(service, m, i);
               if (ngCastType != null) {
                  argsValues.add(this.castNGParam(service, ngCastType, args.get(i)));
               } else {
                  argsValues.add(this.castParam(service, parameters[i], args.get(i)));
               }
            }

            try {
               mainReturn = m.invoke(service, argsValues.toArray());
            }
            catch (Exception e) {
               handleException(m, e);
               e.printStackTrace();
            }
         }
      } else {
         m = this.getMethod(service.getClass(), methodName, 0);
         try {
            mainReturn = m.invoke(service);
         }
         catch (Exception e) {}
      }

      ModelQueryImpl qImpl = (ModelQueryImpl) modelQueryFactory.get(service.getClass());
      Map<String, Object> scMap = new HashMap<>(qImpl.getData());
      returns.putAll(scMap);
      qImpl.getData().clear();
      if (!modelQueryFactory.getRootScope().getRootScopeMap().isEmpty()) {
         returns.put("rootScope", new HashMap<>(modelQueryFactory.getRootScope().getRootScopeMap()));
         modelQueryFactory.getRootScope().getRootScopeMap().clear();
      }

      returns.put("mainReturn", mainReturn);

      if (!logger.getLogPool().isEmpty()) {
         returns.put("log", logger.getLogPool().toArray());
         logger.getLogPool().clear();
      }
   }

   private void handleException(Method m, Exception e) {
      Throwable cause = e.getCause();
      StringBuilder exceptionString = new StringBuilder(m.getName());
      exceptionString.append(" -->");
      exceptionString.append(cause.getClass().getName());

      if (cause.getMessage() != null) {
         exceptionString.append(" ").append(cause.getMessage());
      }

      logger.log(Level.ERROR, exceptionString.toString());
   }
}
