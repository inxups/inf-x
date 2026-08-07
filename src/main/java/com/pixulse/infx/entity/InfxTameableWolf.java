package com.pixulse.infx.entity;

/** InfX wolves enforce a 5-second cooldown between failed taming attempts. */
public interface InfxTameableWolf {
    int tamingCooldown();

    void setTamingCooldown(int ticks);
}
