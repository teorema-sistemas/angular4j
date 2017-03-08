package angular4J.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * specify the class type that will be used to make the Cast in JSON for case of Generic Class
 * Implements
 * 
 * value
 *    Parameter(s) id referenced in NGCParam annotation
 *
 * ...Ex.:
 * 
 * @NGClassCast("ngEntity")
 **/

@Inherited
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NGClassCast {

   String[] value();
}
