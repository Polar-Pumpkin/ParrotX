package org.serverct.parrot.parrotx.data.autoload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface Load {
    String group() default "default";

    String path();
}
