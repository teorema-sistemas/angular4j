package angular4J.util;

/**
 *  An interface representing the $rootScope when updated this give a server to client
 *         way update only
 */

public interface RootScope {

   /**
    * change the value of the model of the $rootScope
    */
   void setProperty(String model, Object value);
}
