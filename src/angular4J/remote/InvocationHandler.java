package angular4J.remote;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import angular4J.api.NGClassCast;
import angular4J.api.NGIgnoreCast;
import angular4J.api.NGParam;
import angular4J.api.NGParamCast;
import angular4J.api.NGPostConstruct;
import angular4J.context.NGSessionScopeContext;
import angular4J.log.NGLogger;
import angular4J.log.NGLogger.Level;
import angular4J.util.CommonUtils;
import angular4J.util.Constants;
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

   public Object invoke(Object o, String method, JsonObject params, String UID, HttpServletRequest request) {

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

   private Map<Integer, Object[]> getParamCastMap(Method m) {
      if (!m.isAnnotationPresent(NGIgnoreCast.class)) {
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

   private Class getPrimitiveClass(JsonElement element) {
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

         Map<Integer, Object[]> ngCast = this.getParamCastMap(m);

         Type[] parameters = m.getGenericParameterTypes();
         if (parameters.length == args.size()) {
            List<Object> argsValues = new ArrayList<>();
            for (int i = 0; i < parameters.length; i++) {
               JsonElement element = args.get(i);
               Type type = null;

               if (ngCast != null) {
                  Object[] ngCastParam = ngCast.get(i);
                  if (ngCastParam != null) {
                     type = this.getParamClass(service, (String) ngCastParam[0], (NGParamType) ngCastParam[1], (boolean) ngCastParam[2]);
                  }
               }

               if (element.isJsonPrimitive()) {
                  Class<?> clazz = null;
                  String typeString = ((parameters[i]).toString());
                  if (typeString.startsWith("class")) {
                     clazz = Class.forName(typeString.substring(6));
                     if (clazz.equals(Object.class)) {
                        clazz = this.getPrimitiveClass(element);
                     }
                  } else {
                     clazz = CommonUtils.getWrapperClass(typeString);
                  }

                  if (clazz.equals(byte[].class) && CommonUtils.getBytesArrayBind().equals(Constants.BASE64_BIND)) {
                     type = clazz;
                  }

                  String val = element.getAsString();
                  if (type == null) {
                     argsValues.add(CommonUtils.convertFromString(val, clazz));
                  } else {
                     argsValues.add(NGParser.getInstance().deserialise(type, element));
                  }
               } else if (element.isJsonArray()) {

                  JsonArray arr = element.getAsJsonArray();

                  if (type == null) {
                     argsValues.add(NGParser.getInstance().deserialise(parameters[i], arr));
                  } else {
                     argsValues.add(NGParser.getInstance().deserialise(type, arr));
                  }

               } else {
                  if (type == null) {
                     argsValues.add(NGParser.getInstance().deserialise(parameters[i], element));
                  } else {
                     argsValues.add(NGParser.getInstance().deserialise(type, element));
                  }
               }
            }

            if (!CommonUtils.isGetter(m)) {
               update(service, params);
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
         // handling methods that took HttpServletRequest as parameter
         if (!CommonUtils.isGetter(m)) {
            update(service, params);
         }

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

      String[] updates = null;

      if (m != null) {
         Annotation ann = CommonUtils.getAnnotation(m, NGPostConstruct.class);
         if (ann != null) {
            updates = ((NGPostConstruct) ann).updates();
         }
      }

      if (updates != null) {
         if ((updates.length == 1) && (updates[0].equals("*"))) {

            List<String> upd = new ArrayList<>();
            for (Method met: service.getClass().getDeclaredMethods()) {

               if (CommonUtils.isGetter(met)) {

                  String fieldName = (met.getName()).substring(3);
                  String firstCar = fieldName.substring(0, 1);
                  upd.add((firstCar.toLowerCase() + fieldName.substring(1)));
               }
            }

            updates = new String[upd.size()];
            for (int i = 0; i < upd.size(); i++) {
               updates[i] = upd.get(i);
            }
         }
      }

      if (updates != null) {
         for (String up: updates) {
            String getterName = CommonUtils.obtainGetter(up, false);

            Method getter;
            try {
               getter = service.getClass().getMethod(getterName);
            }
            catch (NoSuchMethodException e) {
               getter = service.getClass().getMethod(CommonUtils.obtainGetter(up, true));
            }

            Object result = getter.invoke(service);

            returns.put(up, result);
         }
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

   private void update(Object o, JsonObject params) {
      if (params != null) {
         for (Map.Entry<String, JsonElement> entry: params.entrySet()) {
            JsonElement value = entry.getValue();
            String name = entry.getKey();

            if ((name.equals("sessionUID")) || (name.equals("args"))) {
               continue;
            }

            if ((value.isJsonObject()) && (!value.isJsonNull())) {

               String getName;
               try {
                  getName = CommonUtils.obtainGetter(o.getClass().getDeclaredField(name));
                  Method getter = o.getClass().getMethod(getName);
                  Object subObj = getter.invoke(o);
                  update(subObj, value.getAsJsonObject());
               }
               catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException e) {
                  e.printStackTrace();
               }
            }
            if (value.isJsonArray()) {
               try {
                  String getter = CommonUtils.obtainGetter(o.getClass().getDeclaredField(name));
                  Method get = o.getClass().getDeclaredMethod(getter);
                  Type type = get.getGenericReturnType();
                  ParameterizedType pt = (ParameterizedType) type;
                  Type actType = pt.getActualTypeArguments()[0];
                  String className = actType.toString();

                  className = className.substring(className.indexOf("class") + 6);
                  Class clazz = Class.forName(className);

                  JsonArray array = value.getAsJsonArray();

                  Collection collection = (Collection) get.invoke(o);
                  Object elem;
                  for (JsonElement element: array) {
                     if (element.isJsonPrimitive()) {
                        JsonPrimitive primitive = element.getAsJsonPrimitive();

                        elem = element;
                        if (primitive.isBoolean()) elem = primitive.getAsBoolean();
                        if (primitive.isString()) {
                           elem = primitive.getAsString();
                        }

                        if (primitive.isNumber()) {
                           elem = primitive.isNumber();
                        }
                     } else {
                        elem = NGParser.getInstance().deserialise(clazz, element);
                     }

                     try {
                        if (collection instanceof List) {
                           if (collection.contains(elem)) {
                              collection.remove(elem);
                           }
                        }
                        collection.add(elem);
                     }
                     catch (UnsupportedOperationException e) {
                        Logger.getLogger("Angular4J").log(java.util.logging.Level.WARNING, "trying to modify an immutable collection : " + name);
                     }
                  }
               }
               catch (Exception e) {
                  e.printStackTrace();
               }
            }

            if (value.isJsonPrimitive() && (!name.equals("setSessionUID"))) {
               try {
                  if (!CommonUtils.hasSetter(o.getClass(), name)) {
                     continue;
                  }
                  name = CommonUtils.obtainSetter(name);

                  Class type = null;
                  for (Method set: o.getClass().getDeclaredMethods()) {
                     if (CommonUtils.isSetter(set)) {
                        if (set.getName().equals(name)) {
                           Class<?>[] pType = set.getParameterTypes();

                           type = pType[0];
                           break;
                        }
                     }
                  }

                  Object param = null;
                  if ((params.entrySet().size() >= 1) && (type != null)) {
                     param = CommonUtils.convertFromString(value.getAsString(), type);
                  }
                  o.getClass().getMethod(name, type).invoke(o, param);
               }
               catch (Exception e) {
                  e.printStackTrace();
               }
            }
         }
      }
   }
}
