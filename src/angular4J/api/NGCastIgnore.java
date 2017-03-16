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
 * ...Ex:
 * 
 * @NGParamCast(@NGParamCast(@NGParam(id = "ngEntity", index = 0, type = NGParamType.METHOD,
 *                                       required = true))) public <V extends Entity> List<V>
 *                                       getEntities(V entity) {...}
 **/

@Inherited
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NGCastIgnore {}
