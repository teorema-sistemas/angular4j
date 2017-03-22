
package angular4J.boot;

import java.util.ArrayList;
import java.util.List;

import angular4J.ngservices.NGService;
import angular4J.util.NGObject;

/**
 * used by:
 * <p>
 * -Angular4JServletContextListenerAnnotated
 * <p>
 * -ModuleGenerator
 * <p>
 * -Angular4JCDIExtention The NGRegistry is used to store CDI models info detected at deployment
 * time to boost javascript generation performances later on the ModuleGenerator (pre generated and
 * compressed js)
 * <p>
 * it will store specific CDI models definitions: '@Angular4J' (as wrapped NGModel) , ng4J built-in
 * angularJs services (NGService) , the '@NGApp' definition
 * <p>
 * combined with specific models dependent javascript part's (related to RPC methods call) will
 * produce the final "ng4j.js" script.
 */
public class NGRegistry {

   private static NGRegistry instance;

   private List<NGObject> nGModels;
   private List<NGService> ngServices;
   private Class<? extends Object> appClass;

   private NGRegistry() {
      this.nGModels = new ArrayList<>();
      this.ngServices = new ArrayList<>();
   }

   private static final void createInstance() {
      instance = new NGRegistry();
   }

   public static final synchronized NGRegistry getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public void registerApp(Class<? extends Object> appClass) {
      this.appClass = appClass;
   }

   /**
    * Registers the given @Angular4J for script generation.
    * 
    * @param targetClass
    */
   public void registerNGModel(Class targetClass) {
      nGModels.add(new NGObject(targetClass));
   }

   public void registerNGService(NGService nGService) {
      ngServices.add(nGService);
   }

   public List<NGObject> getNGModels() {
      return nGModels;
   }

   public List<NGService> getNGServices() {
      return ngServices;
   }

   public Class<? extends Object> getAppClass() {
      return appClass;
   }
}
