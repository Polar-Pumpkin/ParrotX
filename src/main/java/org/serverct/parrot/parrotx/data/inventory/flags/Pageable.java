package org.serverct.parrot.parrotx.data.inventory.flags;

import java.util.Map;

public interface Pageable {

    Map<String, Integer> getPageMap();

    default int getPage(final String element) {
        return getPageMap().getOrDefault(element, 0);
    }

    int getMaxPage(String element);

    default void setPage(final String element, final int page) {
        getPageMap().put(element, Math.min(getMaxPage(element), Math.max(page, 1)));
    }

    default int nextPage(final String element, final boolean cycle) {
        final int page = getPage(element) + 1;
        if (page > getMaxPage(element)) {
            return cycle ? 1 : -1;
        }
        return page;
    }

    default int previousPage(final String element, final boolean cycle) {
        final int page = getPage(element) - 1;
        if (page == 0) {
            return cycle ? getMaxPage(element) : -1;
        }
        return page;
    }

}
