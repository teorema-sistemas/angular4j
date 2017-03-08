package angular4J.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Alternative;

/**
 * An object representing the $rootScope when updated this give a server to client way update only
 */

@SuppressWarnings("serial")
@Alternative
public class RootScopeImpl implements RootScope, Serializable {

   private final Map<String, Object> rootScopeMap = Collections.synchronizedMap(new HashMap<String, Object>());

   /**
    * change the value of the model of the $rootScope
    */
   @Override
   public void setProperty(String model, Object value) {
      rootScopeMap.put(model, value);
   }

   public synchronized Object getProperty(String model) {
      Object value = rootScopeMap.get(model);
      rootScopeMap.remove(value);
      return value;
   }

   public Set<String> getProperties() {
      return rootScopeMap.keySet();
   }

   public synchronized Map<String, Object> getRootScopeMap() {
      return rootScopeMap;
   }
}