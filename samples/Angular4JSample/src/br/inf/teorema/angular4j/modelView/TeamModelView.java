package br.inf.teorema.angular4j.modelView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import angular4J.api.Angular4J;
import angular4J.api.http.Get;
import angular4J.context.NGSessionScoped;
import br.inf.teorema.angular4j.model.Info;

@Angular4J
@NGSessionScoped
public class TeamModelView extends GenericModelView {

   class Person {

      public Person() {}

      public Person(String name, String position, String mail, String link) {
         this.name = name;
         this.position = position;
         this.mail = mail;
         this.link = link;
      }

      private String name;
      private String position;
      private String mail;
      private String link;

      public String getName() {
         return name;
      }

      public void setName(String name) {
         this.name = name;
      }

      public String getPosition() {
         return position;
      }

      public void setPosition(String position) {
         this.position = position;
      }

      public String getMail() {
         return mail;
      }

      public void setMail(String mail) {
         this.mail = mail;
      }

      public String getLink() {
         return link;
      }

      public void setLink(String link) {
         this.link = link;
      }
   }

   @Override
   public Info reload() {
      return null;
   }

   @Get
   public List<Person> getTeam() {
      List<Person> teams = new ArrayList<>();
      teams.add(new Person("Gustavo Ribas", "General Manager", "gustavo@teorema.inf.br", null));
      teams.add(new Person("Osni Marin", "Team Leader/System Architect", "osni@teorema.inf.br", "https://github.com/osnimarin"));
      teams.add(new Person("Felipe Weiber", "Developer", "felipe.weiber@teorema.inf.br", "https://github.com/fweiber"));
      teams.add(new Person("Julio Fabiane", "Developer", "julio@teorema.inf.br", "https://github.com/juliofabiane"));
      teams.add(new Person("Rafael Reynoud", "Developer", "rafael@teorema.inf.br", "https://github.com/rafareypy"));
      teams.add(new Person("Jonathan Delgado", "Designer", "marketing@teorema.inf.br", null));

      return teams;
   }

   @Get
   public Map<String, Person> getPeoples() {
      Map<String, Person> teams = new HashMap<>();
      teams.put("Person1", new Person("Gustavo Ribas", "General Manager", "gustavo@teorema.inf.br", null));
      teams.put("Person2", new Person("Osni Marin", "Team Leader/System Architect", "osni@teorema.inf.br", "https://github.com/osnimarin"));
      teams.put("Person3", new Person("Felipe Weiber", "Developer", "felipe.weiber@teorema.inf.br", "https://github.com/fweiber"));
      teams.put("Person4", new Person("Julio Fabiane", "Developer", "julio@teorema.inf.br", "https://github.com/juliofabiane"));
      teams.put("Person5", new Person("Rafael Reynoud", "Developer", "rafael@teorema.inf.br", "https://github.com/rafareypy"));
      teams.put("Person6", new Person("Jonathan Delgado", "Designer", "marketing@teorema.inf.br", null));

      return teams;
   }

   @Get
   public Map<String, List<Person>> getPeoples2() {
      Map<String, List<Person>> teams = new HashMap<>();
      teams.put("peoples", this.getTeam());
      return teams;
   }
}
