package angular4J.remote;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * a qualifier for the DataReceivedEvent CDI event bus
 */

@Target({TYPE, METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
@Qualifier
public @interface DataReceivedEvent {}
