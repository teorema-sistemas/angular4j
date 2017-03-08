package angular4J.context;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * bean store for the NGSessionScopeContext
 */
@SuppressWarnings("serial")
public class NGSessionContextHolder implements Serializable {

   private final Map<Class, NGSessionScope> beans;

   public NGSessionContextHolder() {
      this.beans = Collections.synchronizedMap(new HashMap<Class, NGSessionScope>());
   }

   public Map<Class, NGSessionScope> getBeans() {
      return this.beans;
   }

   public NGSessionScope getBean(Class type) {
      return this.getBeans().get(type);
   }

   public void putBean(NGSessionScope customInstance) {
      this.getBeans().put(customInstance.getBean().getBeanClass(), customInstance);
   }

   void destroyBean(NGSessionScope customScope) {
      this.getBeans().remove(customScope.getBean().getBeanClass());

      customScope.getBean().destroy(customScope.getInstance(), customScope.getCtx());
   }
}
