package com.pixulse.infx.world;

/** Internal bridge for the mutable command permission bit removed from the 26.1.2 public API. */
public interface AllowCommandsAccess {
    void infx$setAllowCommands(boolean allowCommands);
}
