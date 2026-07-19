package com.pixulse.infx.mixin;

import com.pixulse.infx.world.R196VillageProgression;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Structure generation has no public event carrying both world progress and selected structure. */
@Mixin(ChunkGenerator.class)
abstract class ChunkGeneratorMixin {
    @Inject(method = "tryGenerateStructure", at = @At("HEAD"), cancellable = true)
    private void infx$gateVillages(
            StructureSet.StructureSelectionEntry selected,
            StructureManager structureManager,
            RegistryAccess registryAccess,
            RandomState randomState,
            StructureTemplateManager templates,
            long seed,
            ChunkAccess chunk,
            ChunkPos chunkPos,
            SectionPos sectionPos,
            ResourceKey<Level> level,
            CallbackInfoReturnable<Boolean> callback) {
        if (!level.equals(Level.OVERWORLD) || R196VillageProgression.generationUnlocked()) return;
        boolean village = selected.structure().unwrapKey()
                .map(key -> key.identifier().getPath().startsWith("village_"))
                .orElse(false);
        if (village) callback.setReturnValue(false);
    }
}
