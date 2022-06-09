package com.npcnames;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@PluginDescriptor(
        name = "meepspeak",
        description = "Changes NPC/item/widget names to meepspeak",
        tags = {"meep"},
        enabledByDefault = true,
        hidden = false
)
public class NpcNamesPlugin extends Plugin {

    private static final Set<MenuAction> NPC_MENU_ACTIONS = ImmutableSet.of(
            MenuAction.NPC_FIRST_OPTION, MenuAction.NPC_SECOND_OPTION,
            MenuAction.NPC_THIRD_OPTION, MenuAction.NPC_FOURTH_OPTION,
            MenuAction.NPC_FIFTH_OPTION, MenuAction.WIDGET_TARGET_ON_NPC,
            MenuAction.ITEM_USE_ON_NPC, MenuAction.EXAMINE_NPC,
            MenuAction.EXAMINE_OBJECT);

    private static final Set<MenuAction> ITEM_MENU_ACTIONS = ImmutableSet.of(
            MenuAction.ITEM_FIRST_OPTION, MenuAction.ITEM_SECOND_OPTION,
            MenuAction.ITEM_THIRD_OPTION, MenuAction.ITEM_FOURTH_OPTION,
            MenuAction.ITEM_FIFTH_OPTION,
            MenuAction.GROUND_ITEM_FIRST_OPTION, MenuAction.GROUND_ITEM_SECOND_OPTION,
            MenuAction.GROUND_ITEM_THIRD_OPTION, MenuAction.GROUND_ITEM_FOURTH_OPTION,
            MenuAction.GROUND_ITEM_FIFTH_OPTION,
            MenuAction.ITEM_USE, MenuAction.ITEM_USE_ON_PLAYER,
            MenuAction.ITEM_USE_ON_ITEM, MenuAction.ITEM_USE_ON_GROUND_ITEM,
            MenuAction.ITEM_USE_ON_NPC, MenuAction.ITEM_USE_ON_GAME_OBJECT,
            MenuAction.EXAMINE_ITEM, MenuAction.EXAMINE_ITEM_GROUND,
            // Inventory + Using Item on Players/NPCs/Objects
            MenuAction.CC_OP, MenuAction.CC_OP_LOW_PRIORITY, MenuAction.WIDGET_TARGET,
            MenuAction.WIDGET_TARGET_ON_PLAYER, MenuAction.WIDGET_TARGET_ON_NPC,
            MenuAction.WIDGET_TARGET_ON_GAME_OBJECT, MenuAction.WIDGET_TARGET_ON_GROUND_ITEM,
            MenuAction.WIDGET_TARGET_ON_WIDGET);

    private static final ImmutableMap<String, String> ItemNameRemap = ImmutableMap.<String, String>builder()
            .put("Dragon warhammer", "bonky hammer")
            .put("Dinh's bulwark", "stompy eye ball shield")
            .put("Zaryte crossbow", "kitty ear crossbow")
            .build();

    private static final ImmutableMap<String, String> NPCNameRemap = ImmutableMap.<String, String>builder()
            .put("Tekton", "stompy rock man")
            .put("Vasa Nistirio", "mean stompy skele stone spider")
            .put("Vespula", "ouchies sting bug")
            .put("Abyssal Portal", "ouchies sting bug portal")
            .put("Muttadile", "puppy dile")
            .put("Baby Muttadile", "puppy dile")
            .put("Big Muttadile", "puppy dile")
            .put("Vanguard", "crawlie ouchie trio")
            .put("Guardian", "rock throwing ouchie stone man")
            .put("TzKal-Zuk", "poops ouch balls")
            .put("Tzkal-Zuk", "poops ouch balls")
            .put("TzTok-Jad", "stompy fire spipey man")
            .put("The Maiden of Sugadinti", "creepy ouchie blood lady")
            .put("Pestilent Bloat", "stinky stompy fat man")
            .put("The Nylocas", "confusing switchy ouchie spooders")
            .put("Nylocas Vasilias", "confusing switchy ouchie spooders")
            .put("Xarpus", "flappy poison throw up bat")
            .put("Sotetseg", "stompy rhino puppy wiff a death rune")
            .put("Verzik Vitur", "yuck youchie fat spider lady")
            .put("Verzik Vitur's Vault", "with a purple, imagine? po chew")
            .put("The Final Challenge", "yuck youchie fat spider lady")
            .put("Ice demon", "snowball throwing ouchie ice man")
            .put("Corrupted Hunllef", "baby stompy moose puppy")
            .put("Crystalline Hunllef", "baby stompy moose puppy")
            .put("Great Olm", "lizard goat")
            .put("Great Olm - Head", "lizard goat - Head")
            .put("Great Olm - Left Hand", "lizard goat - Left Hand")
            .put("Great Olm - Right Hand", "lizard goat - Right Hand")
            .put("Great Olm (Right claw)", "lizard goat (Right claw)")
            .put("Great Olm (Left claw)", "lizard goat (Left claw)")
            .put("Corrupted scavenger", "sleepy alien")
            .put("Zulrah", "punchy fire snake")
            .put("Vorkath", "blue dragon puppy wiff wiffle horns")
            .put("Kree'arra", "flappy ouchi birb man")
            .put("K'ril Tsutsaroth", "stompy spipey red demon wiff lil goat feet")
            .put("Commander Zilyana", "uglee godsword lady wiff wings")
            .put("General Graardor", "ouchi unicorn ogre man")
            .put("Nex", "yuck speedy winged demon lady thing wiff goat feet")
            .put("Black dragon", "down ear dragon puppy wiff spipey tail")
            .put("Cerberus", "three headed awoo lava puppy")
            .put("Corporeal Beast", "giant mummy puppy")
            .put("The Nightmare", "yucky crouchie nasty sleep lady")
            .put("Chaos Elemental", "cotton candy monster")
            .put("Giant Mole", "diggy scratchy feet baby wiff whiskers")
            .put("Kalphite Queen", "spipey crawly flying bug wiff pinchers")
            .put("Penance Queen", "one eyed creepy alien wiff a tummy bump")
            .build();

    private final HashMap<String, String> CustomNPCRemap = new HashMap<>();
    private final HashMap<String, String> CustomItemRemap = new HashMap<>();

    @Inject
    private Client client;

    @Inject
    private NpcNamesConfig npcNamesConfig;

    @Inject
    private ConfigManager configManager;

    @Provides
    NpcNamesConfig getConfig(ConfigManager configManager) {
        return configManager.getConfig(NpcNamesConfig.class);
    }

    @Override
    protected void startUp() {
        parseConfig();
    }

    @Override
    protected void shutDown() {

    }

    @Subscribe
    protected void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(npcNamesConfig.GROUP))
            return;

        parseConfig();
    }

    private void parseConfig() {
        CustomItemRemap.clear();
        CustomNPCRemap.clear();

        try {
            String customNPCs = npcNamesConfig.customNPCList();
            if (customNPCs.isEmpty())
                return;

            String[] pairs = customNPCs.split("\n");
            for (String pair : pairs) {
                String[] kv = pair.split(",");
                if (kv.length != 2)
                    continue;
                CustomNPCRemap.put(kv[0], kv[1]);
            }
        } catch (Exception ignored) {
        }

        try {
            String customItems = npcNamesConfig.customItemList();
            if (customItems.isEmpty())
                return;

            String[] pairs = customItems.split("\n");
            for (String pair : pairs) {
                String[] kv = pair.split(",");
                if (kv.length != 2)
                    continue;
                CustomItemRemap.put(kv[0], kv[1]);
            }
        } catch (Exception ignored) {
        }
    }


    @Subscribe
    private void onBeforeRender(BeforeRender event) {
        if (client.getGameState() != GameState.LOGGED_IN)
            return;

        for (Widget widgetRoot : client.getWidgetRoots()) {
            remapWidget(widgetRoot);
        }
    }

    private void remapWidget(Widget widget) {
        final int groupId = WidgetInfo.TO_GROUP(widget.getId());
        final int CHAT_MESSAGE = 162, PRIVATE_MESSAGE = 163, FRIENDS_LIST = 429;

        if (groupId == CHAT_MESSAGE || groupId == PRIVATE_MESSAGE || groupId == FRIENDS_LIST)
            return;

        Widget[] children = widget.getDynamicChildren();
        if (children == null)
            return;

        Widget[] childComponents = widget.getDynamicChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);

        childComponents = widget.getStaticChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);

        childComponents = widget.getNestedChildren();
        if (childComponents != null)
            mapWidgetText(childComponents);
    }


    private void mapWidgetText(Widget[] childComponents) {
        for (Widget component : childComponents) {
            remapWidget(component);

            String text = component.getText();
            if (text.isEmpty())
                continue;

            if (npcNamesConfig.npcNameToggle()) {
                for (Map.Entry<String, String> entry : NPCNameRemap.entrySet()) {
                    if (text.contains(entry.getKey())) {
                        component.setText(text.replace(entry.getKey(), entry.getValue()));
                    }
                }
            }

            for (Map.Entry<String, String> entry : CustomNPCRemap.entrySet()) {
                if (text.contains(entry.getKey())) {
                    component.setText(text.replace(entry.getKey(), entry.getValue()));
                }
            }
        }
    }

    private void RemapWidgetText(Widget component, String text, HashMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (text.contains(entry.getKey())) {
                component.setText(text.replace(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void RemapWidgetText(Widget component, String text, ImmutableMap<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (text.contains(entry.getKey())) {
                component.setText(text.replace(entry.getKey(), entry.getValue()));
            }
        }
    }

    @Subscribe
    protected void onMenuEntryAdded(MenuEntryAdded event) {
        MenuEntry[] menuEntries = client.getMenuEntries();

        for (MenuEntry menuEntry : menuEntries) {
            if (NPC_MENU_ACTIONS.contains(menuEntry.getType())) {
                if (npcNamesConfig.npcNameToggle())
                    RemapMenuEntryText(menuEntry, NPCNameRemap);
                RemapMenuEntryText(menuEntry, CustomNPCRemap);
            } else if (ITEM_MENU_ACTIONS.contains(menuEntry.getType())) {
                if (npcNamesConfig.itemNameToggle())
                    RemapMenuEntryText(menuEntry, ItemNameRemap);
                RemapMenuEntryText(menuEntry, CustomItemRemap);
            }
        }

        client.setMenuEntries(menuEntries);
    }

    private void RemapMenuEntryText(MenuEntry menuEntry, HashMap<String, String> map) {
        String target = menuEntry.getTarget();
        String cleanTarget = Text.removeTags(target);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (cleanTarget.contains(entry.getKey())) {
                menuEntry.setTarget(target.replace(entry.getKey(), entry.getValue()));
            }
        }
    }

    private void RemapMenuEntryText(MenuEntry menuEntry, ImmutableMap<String, String> map) {
        String target = menuEntry.getTarget();
        String cleanTarget = Text.removeTags(target);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (cleanTarget.contains(entry.getKey())) {
                menuEntry.setTarget(target.replace(entry.getKey(), entry.getValue()));
            }
        }
    }
}


