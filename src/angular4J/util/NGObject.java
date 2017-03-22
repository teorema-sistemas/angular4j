package angular4J.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import angular4J.context.NGHolder;

/**
 * a wrapper for an angular4J CDI view class to provide utility methods for reflection processing
 * issues
 */

@SuppressWarnings("serial")
public class NGObject implements Serializable {

   private Class<?> targetClass = null;
   private String name = null;
   private List<Method> methods;

   public NGObject(Class targetClass) {
      this.targetClass = targetClass;
      this.scan();
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public Class getTargetClass() {
      return this.targetClass;
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
      setName(NGHolder.getInstance().getName(targetClass));
      methods = this.getMethods(targetClass);
   }

   public List<Method> getMethods() {
      return methods;
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
      NGObject other = (NGObject) obj;
      if (name == null) {
         if (other.name != null) return false;
      } else if (!name.equals(other.name)) return false;
      if (targetClass == null) {
         if (other.targetClass != null) return false;
      } else if (!targetClass.equals(other.targetClass)) return false;
      return true;
   }
}
