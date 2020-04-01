package org.serverct.parrot.parrotx.data.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.LOCAL_VARIABLE)
@Retention(RetentionPolicy.CLASS)
public @interface PConfigDouble {
    String path();

    double defaultValue();
}
