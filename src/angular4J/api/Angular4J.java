package angular4J.api;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Declare a class as an Angular4J that will be proxied via an auto generated AngularJS service.
 * <p>
 * this stereotype define by default a RequestScoped CDI Bean
 * <p>
 * compatibles scopes : <br>
 * javax.enterprise.context.RequestScoped <br>
 * javax.enterprise.context.ApplicationScoped <br>
 * javax.enterprise.context.NGSessionScoped
 */
@Retention(RUNTIME)
@Target({METHOD, FIELD, PARAMETER, TYPE})
@Qualifier
@Documented
public @interface Angular4J {}
