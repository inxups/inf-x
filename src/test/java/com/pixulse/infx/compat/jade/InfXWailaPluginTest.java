package com.pixulse.infx.compat.jade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.animal.Animal;
import org.junit.jupiter.api.Test;
import snownee.jade.api.IWailaClientRegistration;

class InfXWailaPluginTest {
    @Test
    void registersSickStatusForEveryAnimalSubtype() {
        List<Object[]> entityRegistrations = new ArrayList<>();
        IWailaClientRegistration registration = (IWailaClientRegistration) Proxy.newProxyInstance(
                IWailaClientRegistration.class.getClassLoader(),
                new Class<?>[] {IWailaClientRegistration.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("registerEntityComponent")) {
                        entityRegistrations.add(args);
                    }
                    return null;
                });

        new InfXWailaPlugin().registerClient(registration);

        assertEquals(1, entityRegistrations.size());
        assertSame(InfXSickStatusProvider.INSTANCE, entityRegistrations.getFirst()[0]);
        assertSame(Animal.class, entityRegistrations.getFirst()[1]);
    }
}
