package angular4J.context;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * MainHTTPSessionListener.
 */
@WebListener
public class MainHTTPSessionListener implements HttpSessionListener {

   /**
    * The NG_SESSION identifier.
    */
   public static final String NG_SESSION_ATTRIBUTE_NAME = "NG_SESSION_ID";

   @Override
   public void sessionCreated(HttpSessionEvent se) {
      se.getSession().setAttribute(NG_SESSION_ATTRIBUTE_NAME, se.getSession().getId());
   }

   /**
    * if the HTTP Session is destroyed, the NGSession will be destroyed too
    */
   @Override
   public void sessionDestroyed(HttpSessionEvent se) {
      GlobalNGSessionContextsHolder.getInstance().destroySession(String.valueOf(se.getSession().getAttribute(NG_SESSION_ATTRIBUTE_NAME)));
      SessionMapper.getInstance().getSessions().remove(se.getSession().getId());
   }
}
