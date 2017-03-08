package angular4J.util;

import angular4J.util.NGBytesBase;

/**
 * Implements the type of class that will be used to maintain compatibility with LOB format
 * implemented by Angular4J
 * <p>
 **/
@SuppressWarnings("serial")
public class NGLob extends NGBytesBase {

   public NGLob(byte[] bytes) {
      super(bytes);
   }
}