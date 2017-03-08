
package angular4J.boot;

import java.util.ArrayList;
import java.util.List;

import angular4J.ngservices.NGService;
import angular4J.util.NGBean;

/**
 * used by:
 * <p>
 * -Angular4JServletContextListenerAnnotated
 * <p>
 * -ModuleGenerator
 * <p>
 * -Angular4JCDIExtention The BeanRegistry is used to store CDI beans info detected at deployment
 * time to boost javascript generation performances later on the ModuleGenerator (pre generated and
 * compressed js)
 * <p>
 * it will store specific CDI beans definitions: '@Angular4J' (as wrapped NGBean) , ng4J built-in
 * angularJs services (NGService) , the '@NGApp' definition
 * <p>
 * combined with specific beans dependent javascript part's (related to RPC methods call) will
 * produce the final "ng4j.js" script.
 */
public class BeanRegistry {

   private static BeanRegistry instance;

   private List<NGBean> nGBeans;
   private List<NGService> extentions;
   private Class<? extends Object> appClass;

   private BeanRegistry() {
      this.nGBeans = new ArrayList<>();
      this.extentions = new ArrayList<>();
   }

   private static final void createInstance() {
      instance = new BeanRegistry();
   }

   public static final synchronized BeanRegistry getInstance() {
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
   public void registerBean(Class targetClass) {
      nGBeans.add(new NGBean(targetClass));
   }

   public void registerExtention(NGService extention) {
      extentions.add(extention);
   }

   public List<NGBean> getNGBeans() {
      return nGBeans;
   }

   public List<NGService> getExtentions() {
      return extentions;
   }

   public Class<? extends Object> getAppClass() {
      return appClass;
   }
}
