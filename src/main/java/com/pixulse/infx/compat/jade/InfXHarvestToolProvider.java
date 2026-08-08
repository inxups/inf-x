package com.pixulse.infx.compat.jade;

import com.pixulse.infx.InfiniteX;
import com.pixulse.infx.event.HarvestEvents;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.JadeIds;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.theme.IThemeHelper;
import snownee.jade.api.ui.Element;
import snownee.jade.api.ui.JadeUI;

/**
 * Client-side replacement for Jade's vanilla harvest-tool line.
 *
 * <p>Runs after Jade's {@code HarvestToolProvider} (priority 8000) and removes its line, then
 * renders the same line from InfX harvest data instead: tool icons resolved by
 * {@link InfxHarvestToolDisplay} (InfX mining families and harvest tiers) and a check mark
 * derived from {@link HarvestEvents#hasDestroyProgress}, the same gate the server applies to
 * mining starts. All of Jade's own harvest-tool config toggles keep working because they are
 * read from their original {@code jade:harvest_tool.*} keys.
 */
public final class InfXHarvestToolProvider implements IBlockComponentProvider {
    public static final InfXHarvestToolProvider INSTANCE = new InfXHarvestToolProvider();

    private static final Component CHECK = Component.literal("✔");
    private static final Component X = Component.literal("✕");

    private InfXHarvestToolProvider() {}

    @Override
    public Identifier getUid() {
        return InfiniteX.id("harvest_tool");
    }

    @Override
    public boolean isRequired() {
        // This replacement deliberately shares Jade's vanilla harvest-tool toggles in appendTooltip.
        // Do not register a second plugin config entry: Jade audits every such entry on the title screen.
        return true;
    }

    @Override
    public int getDefaultPriority() {
        // Must run after Jade's vanilla HarvestToolProvider (8000) so the vanilla line can be removed.
        return 8001;
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!config.get(JadeIds.MC_HARVEST_TOOL)) {
            // The user disabled the harvest-tool line entirely; nothing to show or remove.
            return;
        }
        // Replace Jade's vanilla harvest-tool line with the InfX one.
        tooltip.remove(JadeIds.MC_HARVEST_TOOL);

        Player player = accessor.getPlayer();
        if (!config.get(JadeIds.MC_HARVEST_TOOL_CREATIVE) && (player.isCreative() || player.isSpectator())) {
            return;
        }
        GameType gameType = gameType();
        if (gameType == GameType.ADVENTURE
                && player.blockActionRestricted(accessor.getLevel(), accessor.getPosition(), gameType)) {
            return;
        }
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();
        BlockState state = accessor.getBlockState();
        if (state.getDestroyProgress(player, level, pos) <= 0.0F) {
            if (!accessor.isServersideContent() && config.get(JadeIds.MC_SHOW_UNBREAKABLE)) {
                tooltip.add(JadeUI.text(
                                IThemeHelper.get().failure(Component.translatable("jade.harvest_tool.unbreakable")))
                        .narration(""));
            }
            return;
        }
        boolean newLine = config.get(JadeIds.MC_HARVEST_TOOL_NEW_LINE);
        List<Element> elements = getText(accessor, config);
        if (elements.isEmpty()) {
            return;
        }
        elements.forEach(element -> element.narration(""));
        if (newLine) {
            tooltip.add(elements);
        } else {
            tooltip.append(0, elements);
        }
    }

    private List<Element> getText(BlockAccessor accessor, IPluginConfig config) {
        BlockState state = accessor.getBlockState();
        if (!state.requiresCorrectToolForDrops() && !config.get(JadeIds.MC_EFFECTIVE_TOOL)) {
            return List.of();
        }
        List<ItemStack> tools = InfxHarvestToolDisplay.toolsFor(state);
        if (tools.isEmpty()) {
            return List.of();
        }
        List<Element> elements = new ArrayList<>(tools.size() + 2);
        for (ItemStack tool : tools) {
            elements.add(JadeUI.item(tool, .75F).offset(-1, -3).size(10, 0).narration(""));
        }
        boolean newLine = config.get(JadeIds.MC_HARVEST_TOOL_NEW_LINE);
        elements.addFirst(
                JadeUI.spacer(newLine ? -2 : 5, newLine ? 10 : 0).flexGrow(1000));
        // Same rule as Jade's vanilla provider: mark the line only when a correct tool matters.
        boolean correct = HarvestEvents.hasDestroyProgress(
                accessor.getPlayer(), state, accessor.getPosition());
        if (state.requiresCorrectToolForDrops() || !correct) {
            elements.add(JadeUI.text(correct ? IThemeHelper.get().success(CHECK) : IThemeHelper.get().danger(X))
                    .scale(.75F)
                    .size(0, 0)
                    .offset(-3, 3));
        }
        return elements;
    }

    private static GameType gameType() {
        var gameMode = Minecraft.getInstance().gameMode;
        return gameMode == null ? GameType.DEFAULT_MODE : gameMode.getPlayerMode();
    }
}
