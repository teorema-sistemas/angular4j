package br.inf.teorema.angular4j.realtime;

import javax.servlet.annotation.WebServlet;

import com.google.gson.JsonObject;

import angular4J.remote.RealTimeEndPoint;
import angular4J.util.Constants;
import br.inf.teorema.angular4j.singleton.SystemControl;

@SuppressWarnings("serial")
@WebServlet(loadOnStartup = 2, asyncSupported = true, urlPatterns = Constants.URL_PATTERNS_SERVICE)
public class RealTimeEndPointExt extends RealTimeEndPoint {

   @Override
   public void afterConnect(String connectionId, String UID, JsonObject jObj) {
      SystemControl.getInstance().createSession(UID);
   }

   @Override
   public void onClose(String connectionId, String UID) {
      SystemControl.getInstance().removeSession(UID);
   }
}
