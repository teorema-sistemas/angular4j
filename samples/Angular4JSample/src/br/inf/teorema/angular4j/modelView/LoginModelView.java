package br.inf.teorema.angular4j.modelView;

import angular4J.api.Angular4J;
import angular4J.api.NGCastIgnore;
import angular4J.api.http.Put;
import angular4J.api.http.RequestIgnore;
import angular4J.context.NGSessionScopeContext;
import angular4J.context.NGSessionScoped;
import br.inf.teorema.angular4j.model.Info;
import br.inf.teorema.angular4j.model.User;
import br.inf.teorema.angular4j.singleton.SystemControl;

@Angular4J
@NGSessionScoped
public class LoginModelView extends GenericModelView<User> {

   @Put
   public User authenticate(String email, String password) {
      User user = null;
      if (email.equalsIgnoreCase("admin@admin.com") && password.equalsIgnoreCase("admin")) {
         user = new User();
         user.setEmail(email);
         user.setName("Administrador");
         user.setId(1l);
         user.setPassword("");
      }

      return user;
   }

   @Override
   @RequestIgnore
   public Info reload() {
      return null;
   }

   @Override
   @Put
   @NGCastIgnore
   public void submit(User user) {
      SystemControl.getInstance().setUser(NGSessionScopeContext.getInstance().getCurrentSessionID(), user);
   }
}
