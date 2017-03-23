
package angular4J.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * enable CORS remote method calls (for cordova/ ionic / GitHub's Electron framework.. for example)
 * <strong>work only with HTTP methods call's annotated properties</strong>
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CORS {}
