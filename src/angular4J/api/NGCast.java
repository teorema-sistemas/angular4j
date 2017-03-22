package angular4J.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * specify each parameter that will be used to make the Cast in JSON for case of Generic Class
 * Implements
 * 
 * ...Ex.:
 * 
 * On generic method:
 * 
 * public void submit(@NGCast("ngTypeCast") T model)...
 * 
 * Solve cast:
 * 
 * ## NGType signature @NGCast("ngTypeCast") protected NGType<?> getNgTypeCast() { return new
 * NGType<Info>(){}; }
 * 
 * or
 * 
 * ## String signature @NGCast("ngTypeCast") protected String getNgTypeCast() { return
 * br.inf.teorema.angular4j.model.Info; }
 * 
 **/

@Inherited
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NGCast {

   String value();
}
