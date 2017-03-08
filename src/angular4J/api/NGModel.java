package angular4J.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declare an angular bean property mapped as model on the js proxy side this is a property based
 * annotation (on the getter)
 * 
 * this give the possibility to separate mapped properties from internal java side concern
 * properties (properties non annotated with @NGModel will not be availables on the angularJS
 * service proxy)
 **/

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NGModel {}
