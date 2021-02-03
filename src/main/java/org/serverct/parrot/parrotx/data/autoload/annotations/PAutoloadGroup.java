package org.serverct.parrot.parrotx.data.autoload.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Repeatable(PAutoloadGroups.class)
public @interface PAutoloadGroup {
    String name() default "default";

    String value() default "{GROUP}";

    boolean ignoreDefaultPath() default false;
}
