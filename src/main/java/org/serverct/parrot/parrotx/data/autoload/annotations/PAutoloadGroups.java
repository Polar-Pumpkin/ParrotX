package org.serverct.parrot.parrotx.data.autoload.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PAutoloadGroups {
    PAutoloadGroup[] value();
}
