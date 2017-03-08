package angular4J.boot;

import java.io.IOException;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import angular4J.context.NGSessionScopeContext;
import angular4J.realtime.GlobalConnectionHolder;

/**
 * Returns a generated script for resource "/ng4j.js". the script will be lazily generated based on
 * the registered beans in the {@link BeanRegistry} class.
 */

@ApplicationScoped
@WebServlet(loadOnStartup = 1, urlPatterns = "/ng4j.js")
public final class BootServlet extends HttpServlet {

   @Inject
   Logger log;

   @Override
   public void init() throws ServletException {
      ModuleGenerator.getInstance().generate();
   }

   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      String sessionID = request.getSession().getId();

      GlobalConnectionHolder.getInstance().removeConnection(sessionID);
      NGSessionScopeContext.getInstance().setCurrentContext(sessionID);

      response.setContentType("text/javascript");
      response.getWriter().write(String.format("var sessionId=\"%s\";", sessionID) + ModuleGenerator.getInstance().getValue().toString());
      response.getWriter().flush();
   }
}
