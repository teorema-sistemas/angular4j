package angular4J.realtime;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 
 *  A method annotated with @RealTime will be called over websocket (or one of the
 *         fallback protocol list if sockjs is used: ['websocket','xhr-polling','jsonp-polling',
 *         'xdr-polling', 'xdr-streaming', 'xhr-streaming', 'iframe-xhr-polling',
 *         'iframe-eventsource', 'iframe-htmlfile' ])
 *
 *         if the method has no return (void) then it will be a one way call, if not we will have a
 *         request-response realTime window.
 */

@Retention(RUNTIME)
@Target({TYPE, METHOD, FIELD, PARAMETER})
@Documented
public @interface RealTime {}
