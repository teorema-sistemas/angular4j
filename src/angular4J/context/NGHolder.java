package angular4J.context;

import java.beans.Introspector;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

/**
 * used to obtain a view java class from a view name.
 */

public class NGHolder {

   private static NGHolder instance;

   private final Map<String, Class> models = new HashMap<>();

   private NGHolder() {}

   private static final void createInstance() {
      instance = new NGHolder();
   }

   public static final synchronized NGHolder getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Class getClass(String viewName) {
      return this.models.get(viewName);
   }

   public String getName(Class targetClass) {
      if (targetClass.isAnnotationPresent(Named.class)) {
         Named named = (Named) targetClass.getAnnotation(Named.class);
         if (!named.value().isEmpty()) {
            return named.value();
         }
      }

      String name = Introspector.decapitalize(targetClass.getSimpleName());
      this.models.put(name, targetClass);
      return name;
   }
}
