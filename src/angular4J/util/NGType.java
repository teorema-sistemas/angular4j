package angular4J.util;

import com.google.common.reflect.TypeToken;

/**
 * Implements the class type that will be used to make the Cast in JSON defined in abstract class or
 * interface
 * <p>
 * 
 * Ex. ... NGType<?> ngEntity = new NGType<ImplementedBean>(){}; ...
 * 
 * @author Osni Marin
 **/
public class NGType<T>extends TypeToken<T> {}