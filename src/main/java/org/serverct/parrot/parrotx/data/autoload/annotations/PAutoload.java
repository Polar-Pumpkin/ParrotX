package org.serverct.parrot.parrotx.data.autoload.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
@Documented
public @interface PAutoload {
    String group() default "default";

    String value() default "{FIELD}";
}
