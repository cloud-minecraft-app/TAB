package me.neznamy.tab.shared.features.layout;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.TAB;
import me.neznamy.tab.shared.TabConstants;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import me.neznamy.tab.shared.features.types.Refreshable;
import me.neznamy.tab.shared.features.types.TabFeature;
import me.neznamy.tab.shared.chat.IChatBaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@RequiredArgsConstructor
public class FixedSlot extends TabFeature implements Refreshable {

    @Getter private final String featureName = "Layout";
    @Getter private final String refreshDisplayName = "Updating fixed slots";

    private final LayoutManager manager;
    @Getter private final int slot;
    private final LayoutPattern pattern;
    private final UUID id;
    private final String text;
    private final String propertyName;
    private final TabList.Skin skin;
    private final int ping;

    @Override
    public void refresh(@NotNull TabPlayer p, boolean force) {
        if (!manager.getViews().containsKey(p) || manager.getViews().get(p).getPattern() != pattern || p.getVersion().getMinorVersion() < 8 || p.isBedrockPlayer()) return;
        p.getTabList().updateDisplayName(id, IChatBaseComponent.optimizedComponent(p.getProperty(propertyName).updateAndGet()));
    }

    public @NotNull TabList.Entry createEntry(@NotNull TabPlayer viewer) {
        viewer.setProperty(this, propertyName, text);
        return new TabList.Entry(
                id,
                manager.getDirection().getEntryName(viewer, slot),
                skin,
                ping,
                0,
                IChatBaseComponent.optimizedComponent(viewer.getProperty(propertyName).updateAndGet()) // maybe just get is fine?
        );
    }

    public static @Nullable FixedSlot fromLine(@NotNull String line, @NotNull LayoutPattern pattern, @NotNull LayoutManager manager) {
        String[] array = line.split("\\|");
        if (array.length < 2) {
            TAB.getInstance().getMisconfigurationHelper().invalidFixedSlotDefinition(pattern.getName(), line);
            return null;
        }
        int slot;
        try {
            slot = Integer.parseInt(array[0]);
        } catch (NumberFormatException e) {
            TAB.getInstance().getMisconfigurationHelper().invalidFixedSlotDefinition(pattern.getName(), line);
            return null;
        }
        String text = array[1];
        String skin = array.length > 2 ? array[2] : "";
        int ping = array.length > 3 ? TAB.getInstance().getErrorManager().parseInteger(array[3], manager.getEmptySlotPing()) : manager.getEmptySlotPing();
        FixedSlot f = new FixedSlot(
                manager,
                slot,
                pattern,
                manager.getUUID(slot),
                text,
                "Layout-" + line + "-SLOT-" + slot,
                manager.getSkinManager().getSkin(skin.length() == 0 ? manager.getDefaultSkin() : skin),
                ping
        );
        if (text.length() > 0) TAB.getInstance().getFeatureManager().registerFeature(TabConstants.Feature.layoutSlot(line, slot), f);
        return f;
    }
}