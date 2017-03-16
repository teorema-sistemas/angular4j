package br.inf.teorema.angular4j.modelView;

import java.io.Serializable;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import angular4J.api.NGClassCast;
import angular4J.api.NGParam;
import angular4J.api.NGParamCast;
import angular4J.api.http.Get;
import angular4J.api.http.Put;
import br.inf.teorema.angular4j.model.GenericBean;
import br.inf.teorema.angular4j.model.Info;
import br.inf.teorema.angular4j.singleton.SystemControl;

public abstract class GenericModelView<T extends GenericBean> implements Serializable {

   @Inject
   private HttpSession session;

   protected HttpSession getSession() {
      return this.session;
   }

   @NGClassCast("ngClassCast")
   protected abstract Class<?> getNgClassCast();

   @Get
   public Info getInfo() {
      return SystemControl.getInstance().getInfo(this.getSession().getId());
   }

   @Get
   public abstract Info reload();

   @Put
   @NGParamCast(@NGParam(id = "ngClassCast"))
   public void submit(T model) {
      SystemControl.getInstance().setInfo(this.getSession().getId(), (Info) model);
   }
}