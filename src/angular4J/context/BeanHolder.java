package angular4J.context;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

/**
 * used to obtain a bean java class from a bean name.
 */

public class BeanHolder {

   private static BeanHolder instance;

   private final Map<String, Class> beans = new HashMap<>();

   private BeanHolder() {}

   private static final void createInstance() {
      instance = new BeanHolder();
   }

   public static final synchronized BeanHolder getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Class getClass(String beanName) {
      return this.beans.get(beanName);
   }

   public String getName(Class targetClass) {
      if (targetClass.isAnnotationPresent(Named.class)) {
         Named named = (Named) targetClass.getAnnotation(Named.class);
         if (!named.value().isEmpty()) {
            return named.value();
         }
      }

      String name = Introspector.decapitalize(targetClass.getSimpleName());
      this.beans.put(name, targetClass);
      return name;
   }
}
