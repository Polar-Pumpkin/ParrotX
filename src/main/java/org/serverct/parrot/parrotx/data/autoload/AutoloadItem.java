package org.serverct.parrot.parrotx.data.autoload;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AutoloadItem {

    private final String group;
    private final String path;
    private final String field;
    private final Class<?> type;
    private final List<Class<?>> paramTypes;

}
