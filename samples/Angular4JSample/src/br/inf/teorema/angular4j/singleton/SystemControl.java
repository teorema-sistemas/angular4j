package br.inf.teorema.angular4j.singleton;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import br.inf.teorema.angular4j.model.Info;
import br.inf.teorema.angular4j.model.User;
import br.inf.teorema.angular4j.utils.Utils;

public class SystemControl {

   private static SystemControl instance;

   private Map<String, Info> infos = new HashMap<>();
   private Map<String, User> users = new HashMap<>();

   private SystemControl() {}

   private static final void createInstance() {
      instance = new SystemControl();
   }

   public static final synchronized SystemControl getInstance() {
      if (instance == null) {
         createInstance();
      }
      return instance;
   }

   public Info getInfo(String sessionID) {
      if (!this.infos.containsKey(sessionID)) {
         this.restore(sessionID);
      }
      return this.infos.get(sessionID);
   }

   public User getUser(String sessionID) {
      return this.users.get(sessionID);
   }

   public void setInfo(String sessionID, Info info) {
      this.infos.put(sessionID, info);
   }

   public void setUser(String sessionID, User user) {
      this.users.put(sessionID, user);
   }

   public void restore(String sessionID) {
      Info info = new Info();
      info.setCreated("Teorema Sistemas");

      try {
         String date = "01/01/2017 12:55:10";
         SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
         info.setDate(dateFormat.parse(date));
      }
      catch (ParseException e1) {}

      info.setImageName("image.png");

      InputStream inputStream = this.getClass().getResourceAsStream("/image.png");
      try {
         info.setImage(Utils.extractBytes(inputStream));
      }
      catch (IOException e) {
         e.printStackTrace();
      }
      this.infos.put(sessionID, info);
   }

   public void removeSession(String sessionID) {
      this.infos.remove(sessionID);
   }

   public void createSession(String sessionID) {
      this.restore(sessionID);
   }
}
