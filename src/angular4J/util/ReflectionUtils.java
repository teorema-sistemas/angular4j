package angular4J.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ClassUtils;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

public class ReflectionUtils {

   private ReflectionUtils() {}

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

   public static boolean isAnnotationPresent(Method m, Class<? extends Annotation> annClass) {
      return (getAnnotation(m, annClass) != null);
   }

   public static Type getParameterizedType(String signature) {

      class InnerClass {
         
         public int lastLowerIndexOf(String value, String subString, int fromIndex) {
            if (value == null || value.length() == 0 || subString == null || subString.length() == 0) {
               return -1;
            }

            if (fromIndex > value.length()) {
               return -1;
            }

            int len = subString.length();
            if (len > fromIndex) {
               return -1;
            }

            for (int i = (fromIndex - len); i >= 0; i--) {
               if (subString.equals(value.substring(i, i + len))) {
                  return i;
               }
            }

            return -1;
         }


         private final Class<?> makeClass(String className) {
            try {
               return Class.forName(className.trim());
            }
            catch (Exception e) {}

            return null;
         }

         private final List<String> splitSignature(String signature) {
            if (signature == null) {
               return null;
            }

            List<String> signatures = new ArrayList<>();
            while (true) {
               int id1 = signature.indexOf(',');
               if (id1 == -1) {
                  id1 = signature.length();
               }

               int id2 = lastLowerIndexOf(signature, "<", id1);
               if (id2 > 0) {
                  id1 = signature.indexOf('>') + 1;
               }

               signatures.add(signature.substring(0, id1).trim());
               signature = signature.substring(id1).trim();

               if (signature.startsWith(",")) {
                  signature = signature.substring(1);
               }

               if (!CommonUtils.isStrValid(signature)) {
                  break;
               }
            }
            return signatures;
         }

         private Type toType(Pair<Class<?>, Type[]> params) {
            if (params.getValue() == null) {
               return ParameterizedTypeImpl.make(params.getKey(), new Type[]{}, null);
            } else {
               return ParameterizedTypeImpl.make(params.getKey(), params.getValue(), null);
            }
         }

         private Pair<Class<?>, Type[]> getParams(String signature) {
            int iIni = signature.indexOf('<');
            if (iIni > -1) {
               Type[] types = null;
               List<String> subParams = this.splitSignature(signature.substring(iIni + 1, signature.lastIndexOf('>')));
               types = new Type[subParams.size()];
               for (int i = 0; i < types.length; i++) {
                  String subParam = subParams.get(i);
                  if (subParam.contains("<")) {
                     types[i] = this.toType(this.getParams(subParam));
                  } else {
                     types[i] = this.makeClass(subParam);
                  }
               }
               return new Pair(makeClass(signature.substring(0, iIni)), types);
            }
            return new Pair(makeClass(signature), null);
         }
      }

      if (CommonUtils.isStrValid(signature)) {
         InnerClass inner = new InnerClass();

         return inner.toType(inner.getParams(signature));
      }

      return null;
   }
}
