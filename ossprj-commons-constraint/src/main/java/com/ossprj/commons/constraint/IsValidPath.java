package com.ossprj.commons.constraint;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = IsValidPathValidator.class)
public @interface IsValidPath {

    String message() default "must be a valid Path";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    boolean isDirectory() default false;
    boolean isFile() default false;

    boolean isReadable() default false;
    boolean isWriteable() default false;
}
