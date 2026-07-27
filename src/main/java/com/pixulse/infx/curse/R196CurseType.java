package com.pixulse.infx.curse;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.Random;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/** The sixteen equally weighted witch curses defined by MITE R196. */
public enum R196CurseType implements StringRepresentable {
    EQUIPMENT_DECAYS_FASTER(1, "equipment_decay"),
    CANNOT_HOLD_BREATH(2, "cannot_hold_breath"),
    CANNOT_RUN(3, "cannot_run"),
    CANNOT_EAT_ANIMALS(4, "cannot_eat_animals"),
    CANNOT_EAT_PLANTS(5, "cannot_eat_plants"),
    CANNOT_DRINK(6, "cannot_drink"),
    ENDERMEN_AGGRO(7, "endermen_aggro"),
    CLUMSINESS(8, "clumsiness"),
    ENTANGLEMENT(9, "entanglement"),
    CANNOT_WEAR_ARMOR(10, "cannot_wear_armor"),
    CANNOT_OPEN_CHESTS(11, "cannot_open_chests"),
    CANNOT_SLEEP(12, "cannot_sleep"),
    FEAR_OF_SPIDERS(13, "fear_of_spiders"),
    FEAR_OF_WOLVES(14, "fear_of_wolves"),
    FEAR_OF_CREEPERS(15, "fear_of_creepers"),
    FEAR_OF_UNDEAD(16, "fear_of_undead");

    public static final Codec<R196CurseType> CODEC = StringRepresentable.fromEnum(R196CurseType::values);
    private static final R196CurseType[] BY_ID = new R196CurseType[17];

    static {
        Arrays.stream(values()).forEach(type -> BY_ID[type.id] = type);
    }

    private final int id;
    private final String path;

    R196CurseType(int id, String path) {
        this.id = id;
        this.path = path;
    }

    public int id() {
        return id;
    }

    public Component title() {
        return Component.translatable("curse.infx." + path + ".name");
    }

    public Component description() {
        return Component.translatable("curse.infx." + path + ".desc");
    }

    @Override
    public String getSerializedName() {
        return path;
    }

    public static @Nullable R196CurseType byId(int id) {
        return id > 0 && id < BY_ID.length ? BY_ID[id] : null;
    }

    /** Rejection sampling over the original 64-entry table preserves R196's exact selection order. */
    public static R196CurseType random(Random random) {
        R196CurseType selected;
        do {
            selected = byId(random.nextInt(64));
        } while (selected == null);
        return selected;
    }

    /** Reproduces MITE's username hash instead of Java's {@link String#hashCode()}. */
    public static int originalUsernameHash(String username) {
        int hash = 0;
        for (int index = 0; index < username.length(); index++) {
            hash += username.charAt(index) * index;
        }
        return hash;
    }

    /** A witch always assigns the same curse type to the same original username. */
    public static R196CurseType forWitch(int witchSeed, String username) {
        int combinedSeed = witchSeed + originalUsernameHash(username);
        return random(new Random((long) combinedSeed));
    }
}
