package angular4J.log;

import java.util.logging.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

/**
 * Produces a CDI injectable Logger class
 */
public class LoggerProducer {

   @Produces
   public Logger produce(InjectionPoint injectionPoint) {
      final Class<?> declaringClass = injectionPoint.getMember().getDeclaringClass();
      return Logger.getLogger(declaringClass.getName());
   }
}
