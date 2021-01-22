package org.serverct.parrot.parrotx.data.autoload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(PAutoloadGroups.class)
public @interface PAutoloadGroup {
    String name() default "default";

    String value() default "";
}
