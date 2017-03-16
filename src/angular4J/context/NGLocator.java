package angular4J.context;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.AnnotationLiteral;

/**
 * provide a lookup method to obtain an angular4J reference from an external context to the HTTP
 * Session context (useful with realTime methods calls)
 */
@SuppressWarnings("serial")
public class NGLocator implements Serializable {

   private BeanManager beanManager;

   private static NGLocator instance;

   private NGLocator() {
      this.beanManager = CDI.current().getBeanManager();
   }

   private static final void createInstance() {
      instance = new NGLocator();
   }

   public static final synchronized NGLocator getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Object lookup(String beanName, String sessionID) {
      NGSessionScopeContext.getInstance().setCurrentContext(sessionID);

      Class<?> beanClass = NGHolder.getInstance().getClass(beanName);
      Set<Bean<?>> beans = this.beanManager.getBeans(beanName);
      if (beans.isEmpty()) {
         beans = this.beanManager.getBeans(beanClass, new AnnotationLiteral<Any>(){});
      }

      Bean bean = this.beanManager.resolve(beans);
      Class<?> scopeAnnotationClass = bean.getScope();
      Context context;
      if (scopeAnnotationClass.equals(RequestScoped.class)) {
         context = this.beanManager.getContext((Class<? extends Annotation>) scopeAnnotationClass);
         if (context == null) return bean.create(this.beanManager.createCreationalContext(bean));

      } else {
         if (scopeAnnotationClass.equals(NGSessionScopeContext.class)) {
            context = NGSessionScopeContext.getInstance();
         } else {
            context = this.beanManager.getContext((Class<? extends Annotation>) scopeAnnotationClass);
         }
      }

      return context.get(bean, this.beanManager.createCreationalContext(bean));
   }
}
