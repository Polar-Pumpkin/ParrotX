package org.serverct.parrot.parrotx.data.inventory.element;

import org.jetbrains.annotations.NotNull;

public interface InventoryElement {
    @NotNull BaseElement getBase();

    boolean isClickable();

    default int getPriority() {
        return getBase().getPriority();
    }
}
