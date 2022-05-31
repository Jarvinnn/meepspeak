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
			position = 0,
			closedByDefault = true
	)
	String npc = "NPCs";
	@ConfigItem(
			position = 0,
			keyName = "npcNameToggle",
			name = "Enable default NPC renames",
			description = "Toggle to turn all default npc  names on/off",
			section = npc
	)
	default boolean npcNameToggle()
	{
		return true;
	}

	@ConfigItem(
			position = 1,
			keyName = "customNPCList",
			name = "Custom NPC name list",
			description = "Change NPC names using csv",
			section = npc
	)
	default String customNPCList()
	{
		return "Rat,Invent Tagger\n";
	}

	@ConfigSection(
			name = "Items",
			description = "Item Section",
			position = 1,
			closedByDefault = true
	)
	String items = "Items";

	@ConfigItem(
			position = 0,
			keyName = "itemNameToggle",
			name = "Enable default item renames",
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
			name = "Custom item name list",
			description = "Change item names using csv",
			section = items
	)
	default String customItemList()
	{
		return "";
	}

}
