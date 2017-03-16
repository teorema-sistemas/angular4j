package angular4J.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.spi.Context;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;

import angular4J.api.Angular4J;
import angular4J.api.NGApp;
import angular4J.boot.NGRegistry;
import angular4J.ngservices.NGExtension;
import angular4J.ngservices.NGService;

/**
 * <p>
 * Scans and registers all components annotated with @Angluar4J, @NGExtension and @NGApp during
 * application deployment.
 * </p>
 * 
 * @see javax.enterprise.inject.spi.Extension
 * @see <a href=
 *      "https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html">https://docs.jboss.org/weld/reference/latest/en-US/html/extend.html</a>
 */
public class Angular4JCDIExtension implements Extension {

   /**
    * Observes the ProcessAnnotatedType event and register scanned ng4J specific CDI models to the
    * NGRegistry.
    * 
    * @see NGRegistry
    * @param processAnnotatedType
    */
   public <T> void processAnnotatedType(@Observes @WithAnnotations(value = {Angular4J.class, NGExtension.class, NGApp.class}) ProcessAnnotatedType<T> processAnnotatedType) {

      AnnotatedType<T> annotatedType = processAnnotatedType.getAnnotatedType();
      Class<T> typeClass = annotatedType.getJavaClass();

      Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Registering " + annotatedType.getJavaClass().getName());

      // Handle @Angular4J annotated components
      if (annotatedType.isAnnotationPresent(Angular4J.class)) {
         NGRegistry.getInstance().registerNGModel(typeClass);
         return;
      }

      // Handle @NGExtension annotated components
      if (annotatedType.isAnnotationPresent(NGExtension.class)) {
         try {
            NGRegistry.getInstance().registerNGService((NGService) annotatedType.getJavaClass().newInstance());
            return;
         }
         catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
         }
      }

      // Handle @NGApp annotated components
      if (annotatedType.isAnnotationPresent(NGApp.class)) {
         NGRegistry.getInstance().registerApp(typeClass);
      }
   }

   /**
    * <p>
    * Invoked by the container once all the annotated types has model discovered, then registers the
    * NGSessionScopeContext (and the NGSessionScoped custom CDI scope)
    * </p>
    * 
    * @see javax.enterprise.inject.spi.AfterBeanDiscovery
    * @see javax.enterprise.inject.spi.BeanManager
    * @see angular4J.context.NGSessionScoped
    * @see angular4J.context.NGSessionScopeContext
    */
   public void registerContext(@Observes final AfterBeanDiscovery event, BeanManager manager) {
      Context context = NGSessionScopeContext.getInstance();
      event.addContext(context);
   }
}
