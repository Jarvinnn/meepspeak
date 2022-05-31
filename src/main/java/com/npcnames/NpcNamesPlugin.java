package com.npcnames;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;

import javax.inject.Inject;

import net.runelite.api.events.BeforeRender;
import net.runelite.api.widgets.Widget;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.util.HashMap;
import java.util.Map;

import net.runelite.api.Client;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
        name = "meepspeak",
        description = "Changes NPC/item/widget names to meepspeak",
        tags = {"meep"},
        enabledByDefault = true,
        hidden = false
)
public class NpcNamesPlugin extends Plugin {

    private static final ImmutableMap<String, String> ItemNameRemap = ImmutableMap.<String, String>builder()
            .put("Dragon warhammer", "bonky hammer")
            .put("Dinh's bulwark", "stompy eye ball shield")
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
            .put("TzTok-Jad", "stompy fire spipey man")
            .put("The Maiden of Sugadinti", "creepy ouchie blood lady")
            .put("Pestilent Bloat", "stinky stompy fat man")
            .put("The Nylocas", "confusing switchy ouchie spooders")
            .put("Sotetseg", "stompy rhino puppy wiff a death rune")
            .put("Verzik Vitur", "yuck youchie fat spider lady")
            .put("Verzik Vitur's Vault", "with a purple imagine? po chew")
            .put("The Final Challenge", "yuck youchie fat spider lady")
            .put("Xarpus", "flappy poison throw up bat")
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
            .put("Kree'arra", "flappy ouchi birb man")
            .put("K'ril Tsutsaroth", "stompy spipey red demon wiff lil goat feet")
            .put("Commander Zilyana", "uglee godsword lady wiff wings")
            .put("General Graardor", "ouchi unicorn ogre man")
            .put("Nex", "yuck speedy winged demon lady thing wiff goat feet")
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
        } catch (Exception e) {
            throw new RuntimeException(e);
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

            String mapped = NPCNameRemap.get(text);
            if (mapped == null)
                continue;

            component.setText(mapped);
        }
    }

    @Subscribe
    protected void onMenuEntryAdded(MenuEntryAdded event)
    {
        MenuEntry[] menuEntries = client.getMenuEntries();
        for (MenuEntry menuEntry : menuEntries) {
            String target = menuEntry.getTarget();
            String cleanTarget = Text.removeTags(target);

            if (ItemNameRemap.containsKey(cleanTarget)) {
                String remapped = ItemNameRemap.get(cleanTarget);
                if (remapped == null)
                    continue;
                menuEntry.setTarget("<col=ff9040>" + remapped + "</col>");
            } else {
                for (Map.Entry<String, String> entry : NPCNameRemap.entrySet()) {
                    if (cleanTarget.contains(entry.getKey())) {
                        menuEntry.setTarget(target.replace(entry.getKey(), entry.getValue()));
                    }
                }
                for (Map.Entry<String, String> entry : CustomNPCRemap.entrySet()) {
                    if (cleanTarget.contains(entry.getKey())) {
                        menuEntry.setTarget(target.replace(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
        client.setMenuEntries(menuEntries);
    }
}

