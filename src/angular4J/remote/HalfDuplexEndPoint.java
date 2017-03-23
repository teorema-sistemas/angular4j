package angular4J.remote;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;

import com.google.gson.JsonObject;

import angular4J.context.NGLocator;
import angular4J.context.NGSessionScopeContext;
import angular4J.util.Constants;
import angular4J.util.NGParser;

/**
 * The HalfDuplexEndPoint servlet is a standard HTTP protocol endpoint
 */
@SuppressWarnings("serial")
@WebServlet(asyncSupported = true, urlPatterns = Constants.URL_PATTERNS_INVOKE)
public class HalfDuplexEndPoint extends HttpServlet implements Serializable {

   @Inject
   InvocationHandler remoteInvoker;

   @Inject
   HttpSession session;

   @Inject
   @DataReceivedEvent
   private Event<DataReceived> receiveEvents;

   @Override
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      process(req, resp);
   }

   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      process(req, resp);
   }

   @Override
   protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      process(req, resp);
   }

   @Override
   protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      process(req, resp);
   }

   @Override
   protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      process(request, response);
   }

   private String base64Compress(String string) {
      if (string != null) {
         try {
            byte[] data = string.getBytes("ISO-8859-1");

            Deflater deflater = new Deflater();
            deflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            deflater.finish();
            byte[] buffer = new byte[1024];
            while (!deflater.finished()) {
               int count = deflater.deflate(buffer);
               outputStream.write(buffer, 0, count);
            }
            outputStream.close();

            return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
         }
         catch (Exception e) {}
      }
      return null;
   }

   private String base64Decompress(String base64) {
      if (base64 != null) {
         try {
            byte[] bytes = DatatypeConverter.parseBase64Binary(base64);

            Inflater inflater = new Inflater();
            int numberOfBytesToDecompress = bytes.length;
            inflater.setInput(bytes, 0, numberOfBytesToDecompress);
            int bufferSizeInBytes = numberOfBytesToDecompress;
            List<Byte> bytesDecompressedSoFar = new ArrayList<Byte>();
            while (inflater.needsInput() == false) {
               byte[] bytesDecompressedBuffer = new byte[bufferSizeInBytes];
               int numberOfBytesDecompressedThisTime = inflater.inflate(bytesDecompressedBuffer);
               for (int b = 0; b < numberOfBytesDecompressedThisTime; b++) {
                  bytesDecompressedSoFar.add(bytesDecompressedBuffer[b]);
               }
            }

            bytes = new byte[bytesDecompressedSoFar.size()];
            for (int b = 0; b < bytes.length; b++) {
               bytes[b] = (byte) (bytesDecompressedSoFar.get(b));
            }

            inflater.end();

            return new String(bytes, 0, bytes.length, "ISO-8859-1");
         }
         catch (Exception e) {}
      }
      return null;
   }

   private void process(HttpServletRequest request, HttpServletResponse resp) {
      AsyncContext asyncContext = request.startAsync();

      resp.setCharacterEncoding("UTF-8");

      if (request.getRequestURL().toString().endsWith("/CORS")) {
         resp.addHeader("Access-Control-Allow-Origin", "*");
         resp.addHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE");
         resp.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
      }

      if (request.getMethod().equals("OPTIONS")) {
         resp.setStatus(HttpServletResponse.SC_OK);
      }

      resp.setContentType("application/text");

      String fullPath = request.getRequestURI();
      fullPath = fullPath.substring(fullPath.indexOf("/service/") + 9);

      String parts[] = fullPath.split("/");

      String params = request.getParameter("params");

      if (request.getMethod().equals("PUT") || request.getMethod().equals("POST")) {
         try {
            StringBuilder builder = new StringBuilder();
            BufferedReader reader;

            reader = request.getReader();

            String line;

            while ((line = reader.readLine()) != null) {
               builder.append(line);
            }

            params = builder.toString();

         }
         catch (Exception e) {}
      }

      JsonObject paramsObj = NGParser.getInstance().deserialize(this.base64Decompress(params)).getAsJsonObject();

      String UID = this.session.getId();

      NGSessionScopeContext.getInstance().setCurrentContext(UID);

      receiveEvents.fire(new HalfDuplexDataReceivedEvent(paramsObj));

      Map<String, Object> result = remoteInvoker.invoke(NGLocator.getInstance().lookup(parts[0], UID), parts[1], paramsObj, UID, request);
      try {
         PrintWriter writer = asyncContext.getResponse().getWriter();
         writer.write(this.base64Compress(NGParser.getInstance().serialize(result, request)));
         writer.flush();
         asyncContext.complete();
      }
      catch (IOException e) {
         e.printStackTrace();
      }
   }
}
