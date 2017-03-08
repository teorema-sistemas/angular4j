
package angular4J.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * first method to be called when initializing the js proxy
 * when the angular service is created it will send a HTTP GET request and Angular4J will call
 * the annotated method.
 * 
 * <strong>method annotated with @NGPostConstruct must be a public void and with no args</strong>
 * 
 * Attention! when used with RequestScoped Bean : this will trigger the creation of a new contextual
 * instance of the bean (the @NGPostConstruct will produce an extra HTTP request)
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface NGPostConstruct {

   String[] updates() default {};
}
