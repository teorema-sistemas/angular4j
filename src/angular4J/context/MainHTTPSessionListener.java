package angular4J.context;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import angular4J.util.Constants;

/**
 * MainHTTPSessionListener.
 */
@WebListener
public class MainHTTPSessionListener implements HttpSessionListener {

   @Override
   public void sessionCreated(HttpSessionEvent se) {
      se.getSession().setAttribute(Constants.NG4J_SESSION_ID, se.getSession().getId());
   }

   /**
    * if the HTTP Session is destroyed, the NGSession will be destroyed too
    */
   @Override
   public void sessionDestroyed(HttpSessionEvent se) {
      GlobalNGSessionContextsHolder.getInstance().destroySession(String.valueOf(se.getSession().getAttribute(Constants.NG4J_SESSION_ID)));
      SessionMapper.getInstance().getSessions().remove(se.getSession().getId());
   }
}
