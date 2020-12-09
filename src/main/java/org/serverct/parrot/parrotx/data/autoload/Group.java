package org.serverct.parrot.parrotx.data.autoload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(Groups.class)
public @interface Group {
    String name() default "default";

    String path() default "";
}
