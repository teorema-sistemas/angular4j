package angular4J.context;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;

public class NGSessionScope<T> {

   private Bean<T> bean;
   private CreationalContext<T> ctx;
   private T instance;

   public Bean<T> getBean() {
      return bean;
   }

   public void setBean(Bean<T> bean) {
      this.bean = bean;
   }

   public CreationalContext<T> getCtx() {
      return ctx;
   }

   public void setCtx(CreationalContext<T> ctx) {
      this.ctx = ctx;
   }

   public T getInstance() {
      return instance;
   }

   public void setInstance(T instance) {
      this.instance = instance;
   }
}
