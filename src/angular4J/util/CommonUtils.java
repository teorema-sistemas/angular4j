package angular4J.util;

import java.util.Iterator;
import java.util.List;

public abstract class CommonUtils {

   private CommonUtils() {}

   public static final boolean isStrValid(String string) {
      if (string != null && string.trim().length() > 0 && !string.equalsIgnoreCase("null")) {
         return true;
      }
      return false;
   }

   public static String parseStrArray(List<String> strings, String separator) {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> i = strings.iterator(); i.hasNext();) {
         sb.append(i.next()).append(i.hasNext() ? separator : "");
      }
      return sb.toString();
   }
}