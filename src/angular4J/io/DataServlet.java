package angular4J.io;

import java.io.OutputStream;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is the Angular4J Binary end point
 * 
 * that let the usage of a byte[] resource as an angular js model. (used in
 * ng-src="{{myBinaryModel}}" for example)
 */

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/lob/*")
public class DataServlet extends HttpServlet {

   protected void doGet(HttpServletRequest req, HttpServletResponse response) {

      String requestURI = req.getRequestURI();

      int index = (requestURI.indexOf("/lob")) + 5;
      String resourceId = requestURI.substring(index);

      response.setHeader("Access-Control-Allow-Origin", "*");

      byte[] data = null;

      try (OutputStream o = response.getOutputStream();) {
         if (ByteArrayCache.getInstance().getCache().containsKey(resourceId)) {
            data = ByteArrayCache.getInstance().getCache().get(resourceId);
            ByteArrayCache.getInstance().getCache().remove(resourceId);
         }

         if (data == null) {
            data = "File not found.".getBytes();
         }
         o.write(data);
         o.flush();
         o.close();

      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
}
