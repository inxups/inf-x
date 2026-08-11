package com.pixulse.infx.item;

/** Selects which player actions receive an equipment type's reach bonus. */
public enum ReachMode {
    INTERACTION(true, false),
    MELEE(false, true),
    BOTH(true, true),
    NONE(false, false);

    private final boolean extendsInteraction;
    private final boolean extendsMelee;

    ReachMode(boolean extendsInteraction, boolean extendsMelee) {
        this.extendsInteraction = extendsInteraction;
        this.extendsMelee = extendsMelee;
    }

    public boolean extendsInteraction() {
        return extendsInteraction;
    }

    public boolean extendsMelee() {
        return extendsMelee;
    }
}
