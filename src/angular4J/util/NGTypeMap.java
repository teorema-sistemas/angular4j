package angular4J.util;

import java.util.HashMap;
import java.util.Map;

public class NGTypeMap {

   private Map<String, NGType<?>> casts =  new HashMap<>();

   public void add(String id, NGType<?> ngType) {
      if (CommonUtils.isStrValid(id) && ngType != null) {
         this.casts.put(id, ngType);
      }
   }

   public void add(String id, String signature) {
      if (CommonUtils.isStrValid(id) && CommonUtils.isStrValid(signature)) {
         this.casts.put(id, (NGType<?>) ReflectionUtils.getParameterizedType(signature));
      }
   }

   public void remove(String id) {
      this.casts.remove(id);
   }

   public NGType<?> getNGType(String id) {
      return this.casts.get(id);
   }
}