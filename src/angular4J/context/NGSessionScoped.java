package angular4J.context;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.enterprise.context.NormalScope;

/**
 * NGSessionScoped is a custom CDI scope an NGSessionScoped Bean is a HTTP session scoped Bean but
 * shared between HTTP session and webSocket session <strong>can be an @Angular4J or just a CDI
 * NGSessionScoped component to use (without generating the angularJS service)</strong>
 **/

@NormalScope(passivating = false)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})

@Documented
public @interface NGSessionScoped {}