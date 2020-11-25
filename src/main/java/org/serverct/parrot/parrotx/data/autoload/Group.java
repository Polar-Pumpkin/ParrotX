package org.serverct.parrot.parrotx.data.autoload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Group {
    String name();

    String path();
}
