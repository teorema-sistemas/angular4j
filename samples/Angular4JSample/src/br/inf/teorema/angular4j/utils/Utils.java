package br.inf.teorema.angular4j.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {

   private Utils() {}

   public static byte[] extractBytes(InputStream inputStream) throws IOException {
      if (inputStream != null) {
         ByteArrayOutputStream buffer = new ByteArrayOutputStream();
         int nRead;
         byte[] data = new byte[16384];

         while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
         }

         buffer.flush();

         return buffer.toByteArray();
      }
      return null;
   }
}
