package com.npcnames;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(NpcNamesConfig.GROUP)
public interface NpcNamesConfig extends Config
{
	String GROUP = "NpcNamesConfig";

	@ConfigSection(
			name = "NPCs",
			description = "NPC Section",
			position = 0
	)
	String npcs = "NPCs";

	@ConfigItem(
			position = 0,
			keyName = "npcNameToggle",
			name = "Enable Default NPC Renames",
			description = "Toggle to turn all default npc names on/off",
			section = npcs
	)
	default boolean npcNameToggle()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "customNPCList",
			name = "Custom NPC Name List",
			description = "Custom NPC Name List (CSV Format)",
			section = npcs
	)
	default String customNPCList()
	{
		return "Rat,Invent Tagger";
	}

	@ConfigSection(
			name = "Items",
			description = "Item Section",
			position = 1
	)
	String items = "Items";

	@ConfigItem(
			position = 0,
			keyName = "itemNameToggle",
			name = "Enable Default Item Renames",
			description = "Toggle to turn all default item names on/off",
			section = items
	)
	default boolean itemNameToggle()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "customItemList",
			name = "Custom Item List",
			description = "Custom Item List (CSV Format)",
			section = items
	)
	default String customItemList()
	{
		return "";
	}
}