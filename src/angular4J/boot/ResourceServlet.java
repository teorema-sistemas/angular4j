
package angular4J.boot;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * The ResourceServlet is the resources end point by resources we mean properties files that will be
 * served as JSON data (translation files for example)<br>
 * consumed by "bundleService.loadBundle(bundle_prefix,aleas)".
 */

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/resources/*")
public class ResourceServlet extends HttpServlet {

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      int index = (req.getRequestURI().indexOf("/resources/")) + 10;

      resp.setHeader("Access-Control-Allow-Origin", "*");
      resp.getWriter().write(ResourcesCache.getInstance().get((req.getRequestURI().substring(index)), getServletConfig().getServletContext()));
   }
}
