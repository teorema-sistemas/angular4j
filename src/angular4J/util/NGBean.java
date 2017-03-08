package angular4J.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import angular4J.api.NGModel;
import angular4J.context.BeanHolder;

/**
 * a wrapper for an angularBean CDI bean class to provide utility methods for reflection processing
 * issues
 */

@SuppressWarnings("serial")
public class NGBean implements Serializable {

   private Class<?> targetClass = null;
   private String name = null;
   private final Set<Method> getters = new HashSet<>();
   private List<Method> methods;

   public NGBean(Class beanclass) {
      targetClass = beanclass;
      scan();
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;

   }

   public Class getTargetClass() {
      return targetClass;
   }

   private List<Method> getMethods(Class<?> clazz) {
      List<Method> ms = new ArrayList<>();
      for (Method m: clazz.getMethods()) {
         if (!Modifier.isVolatile(m.getModifiers())) {
            ms.add(m);
         }
      }
      return (ms.size() > 0 ? ms : null);
   }

   public void scan() {
      setName(BeanHolder.getInstance().getName(targetClass));
      methods = this.getMethods(targetClass);

      for (Method m: methods) {
         if (CommonUtils.isGetter(m) && !Modifier.isVolatile(m.getModifiers())) {
            if (CommonUtils.isAnnotationPresent(m, NGModel.class)) {
               getters.add(m);
            }
         }
      }
   }

   public List<Method> getMethods() {
      return methods;
   }

   public Set<Method> getters() {
      return getters;
   }

   @Override
   public int hashCode() {
      return name.hashCode();
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      NGBean other = (NGBean) obj;
      if (name == null) {
         if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      if (targetClass == null) {
         if (other.targetClass != null) return false;
      } else if (!targetClass.equals(other.targetClass)) return false;
      return true;
   }
}
