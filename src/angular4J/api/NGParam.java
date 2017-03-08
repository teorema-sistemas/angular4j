package angular4J.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import angular4J.util.NGParamType;

/**
 * specify each parameter that will be used to make the Cast in JSON for case of Generic Class Implements
 * 
 * id
 *    Parameter id referenced in NGClassCast annotation
 * index
 *    Parameter index(es) referenced in method declaration
 * type
 *    Identifies the type of element that will be used to set the Class to cast (NGParamType.FIELD / NGParamType.METHOD is default)
 * required
 *    Parameter required or not (true is default)
 * 
 * ...Ex.:
 * 
 @NGParamCast(@NGParamCast(@NGParam(id = "ngEntity", index = 0, type = NGParamType.METHOD, required = true))) 
 * public <V extends Entity> List<V> getEntities(V entity) {...}
 **/

@Inherited
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NGParam {

   String id() default "";

   int[] index() default 0;

   NGParamType type() default NGParamType.METHOD;

   boolean required() default true;
}
