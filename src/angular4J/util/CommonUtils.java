package angular4J.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public abstract class CommonUtils {

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