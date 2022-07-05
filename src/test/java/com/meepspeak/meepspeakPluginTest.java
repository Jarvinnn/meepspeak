package com.meepspeak;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class meepspeakPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(meepspeakPlugin.class);
		RuneLite.main(args);
	}
}