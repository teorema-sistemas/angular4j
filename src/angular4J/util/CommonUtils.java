package angular4J.util;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.ClassUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import angular4J.api.CORS;
import angular4J.api.http.Delete;
import angular4J.api.http.Get;
import angular4J.api.http.Post;
import angular4J.api.http.Put;
import angular4J.realtime.RealTime;

public abstract class CommonUtils {

   public static String obtainGetter(Field field) {
      return obtainGetter(field.getName(), field.getType().equals(boolean.class));
   }

   public static String obtainGetter(String fieldName, boolean isNative) {
      String name = capitalize(fieldName);
      if (isNative) {
         return Constants.BOOLEAN_GETTER_PREFIX + name;
      } else {
         return Constants.GETTER_PREFIX + name;
      }
   }

   public static String obtainSetter(Field field) {
      return obtainSetter(field.getName());
   }

   public static String obtainSetter(String fieldName) {
      return Constants.SETTER_PREFIX + capitalize(fieldName);
   }

   /**
    * Create a wrapper object for one of the primitive Java types from a string. Basically it calls
    * {@code x.parseX(value)} after checking for {@code null} and empty argument.
    * <p>
    * For a {@code null} or empty value, {@code null} is returned.
    * 
    * @param value
    *           String to convert
    * @param type
    *           Type to convert to.
    * @return Instance of the corresponding wrapper class or {@code null}
    * @throws ArithmeticException
    *            if {@code value} cannot be parsed
    * @throws IllegalArgumentException
    *            if {@code value} is not one of: primitive type, wrapper type, String, collection
    */
   public static Object convertFromString(String value, Class type) {

      if (isNullOrEmpty(value) || type.equals(byte[].class) || type.equals(Byte[].class)) {
         return null;
      }

      if (String.class.equals(type)) {
         return value;
      }

      if (type.equals(int.class) || type.equals(Integer.class)) {
         return Integer.parseInt(value);
      }
      if (type.equals(float.class) || type.equals(Float.class)) {
         return Float.parseFloat(value);
      }
      if (type.equals(boolean.class) || type.equals(Boolean.class)) {
         return Boolean.parseBoolean(value);
      }
      if (type.equals(double.class) || type.equals(Double.class)) {
         return Double.parseDouble(value);
      }
      if (type.equals(byte.class) || type.equals(Byte.class)) {
         return Byte.parseByte(value);
      }
      if (type.equals(long.class) || type.equals(Long.class)) {
         return Long.parseLong(value);
      }
      if (type.equals(short.class) || type.equals(Short.class)) {
         return Short.parseShort(value);
      }

      throw new IllegalArgumentException("unknown primitive type :" + type.getCanonicalName());
   }

   public static Class getWrapperClass(String nativeName) {

      switch (nativeName) {
         case "int":
            return Integer.TYPE;
         case "long":
            return Long.TYPE;
         case "double":
            return Double.TYPE;
         case "float":
            return Float.TYPE;
         case "boolean":
            return Boolean.TYPE;
         case "char":
            return Character.TYPE;
         case "byte":
            return Byte.TYPE;
         case "short":
            return Short.TYPE;
      }
      return null;
   }

   public static boolean isSetter(Method m) {
      if (m == null) {
         return false;
      }

      return m.getName().startsWith(Constants.SETTER_PREFIX) && returnsVoid(m) && hasOneParameter(m);
   }

   public static boolean isGetter(Method m) {
      if (m == null) {
         return false;
      }
      if (returnsVoid(m)) {
         return false;
      }
      if (isHttpAnnotated(m) || isAnnotationPresent(m, RealTime.class) || isAnnotationPresent(m, CORS.class)) {
         return false;
      }
      return hasNoParameters(m) && m.getName().startsWith(Constants.GETTER_PREFIX) || returnsBoolean(m) && m.getName().startsWith(Constants.BOOLEAN_GETTER_PREFIX);
   }

   public static boolean hasSetter(Class clazz, String fieldName) {

      String setterName = Constants.SETTER_PREFIX + capitalize(fieldName);

      setterName = setterName.trim();
      for (Method m: clazz.getDeclaredMethods()) {

         if (m.getName().equals(setterName) && isSetter(m)) {
            return true;
         }
      }
      return false;
   }

   public static String obtainFieldNameFromAccessor(String methodName) {
      String fieldName;
      if (methodName.startsWith(Constants.GETTER_PREFIX) || methodName.startsWith(Constants.SETTER_PREFIX)) {
         fieldName = methodName.substring(3);
      } else if (methodName.startsWith(Constants.BOOLEAN_GETTER_PREFIX)) {
         fieldName = methodName.substring(2);
      } else {
         throw new IllegalArgumentException("Unable to obtain field name from method '" + methodName + "'.");
      }

      return Introspector.decapitalize(fieldName);
   }

   private static boolean isHttpAnnotated(Method m) {
      if (m == null) {
         return false;
      }

      return isAnnotationPresent(m, Get.class) || isAnnotationPresent(m, Post.class) || isAnnotationPresent(m, Put.class) || isAnnotationPresent(m, Delete.class);
   }

   private static boolean returnsBoolean(Method m) {
      return m.getReturnType().equals(boolean.class) || m.getReturnType().equals(Boolean.class);
   }

   private static boolean returnsVoid(Method m) {
      return m.getReturnType().equals(Void.class) || (m.getReturnType().equals(void.class));
   }

   private static boolean hasNoParameters(Method m) {
      return m.getParameterTypes().length == 0;
   }

   private static boolean hasOneParameter(Method m) {
      return m.getParameterTypes().length == 1;
   }

   private static String capitalize(String name) {
      return name.substring(0, 1).toUpperCase() + name.substring(1);
   }

   /**
    * Check if the parameter is null or empty. If {@code o} is neither a String, an array or a
    * Collection it is regarded as non-empty.
    *
    * @param <T>
    *           class type of tested object.
    * @param o
    *           parameter to check.
    * @return true if the parameter is {@code null} or empty, false otherwise.
    */
   private static <T> Boolean isNullOrEmpty(final T o) {
      if (o == null) {
         return true;
      }
      if (o instanceof String) {
         return "".equals(((String) o).trim());
      }
      if (o.getClass().isArray()) {
         return Array.getLength(o) == 0;
      }
      if (o instanceof Collection<?>) {
         return ((Collection<?>) o).isEmpty();
      }
      return false;
   }

   /**
    * Returns the full path of the server context
    * 
    * @param request
    *           Parameter HttpServletRequest
    * @return full path of the server context
    */
  
   private static Annotation getAnnotationValue(List<Class<?>> klasses, Method m, Class<? extends Annotation> annClass) {
      if (klasses != null && klasses.size() > 0) {
         for (Class<?> klass: klasses) {
            for (Method method: klass.getDeclaredMethods()) {
               if (method.getName().equals(m.getName()) && (method.getGenericParameterTypes().length == m.getGenericParameterTypes().length)) {
                  if (method.isAnnotationPresent(annClass)) {
                     return method.getAnnotation(annClass);
                  }
               }
            }
         }
      }
      return null;
   }

   /**
    * Returns the annotation for the specified type is present in the class or inheritance
    * 
    * @param m
    *           Method name
    * 
    * @param annClass
    *           annotation class type
    * 
    * @return true/false
    **/
   public static Annotation getAnnotation(Method m, Class<? extends Annotation> annClass) {
      if (m.isAnnotationPresent(annClass)) {
         return m.getAnnotation(annClass);
      }

      Class<?> methodKlass = m.getDeclaringClass();
      Annotation ann = null;

      ann = getAnnotationValue(ClassUtils.getAllSuperclasses(methodKlass), m, annClass);
      if (ann != null) {
         return ann;
      }

      ann = getAnnotationValue(ClassUtils.getAllInterfaces(methodKlass), m, annClass);
      if (ann != null) {
         return ann;
      }

      return null;
   }

   /**
    * Returns true if an annotation for the specified type is present in the class or inheritance
    * 
    * @param m
    *           Method name
    * 
    * @param annClass
    *           annotation class type
    * 
    * @return true/false
    **/
   public static boolean isAnnotationPresent(Method m, Class<? extends Annotation> annClass) {
      return (getAnnotation(m, annClass) != null);
   }

   public static JsonElement parseMessage(String message) {
      if (message == null) {
         return null;
      }
      if (!message.startsWith("{")) {
         return new JsonPrimitive(message);
      }
      JsonParser parser = new JsonParser();

      return parser.parse(message);
   }
}