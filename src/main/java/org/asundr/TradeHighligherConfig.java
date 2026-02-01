/*
 Copyright (c) 2026, Arun <https://www.github.com/asundr/runelite-trade-highlighter/issues>
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.asundr;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

import java.awt.*;

@ConfigGroup(TradeHighligherConfig.CONFIG_GROUP)
public interface TradeHighligherConfig extends Config
{
	String CONFIG_GROUP = "trade-highlighter";
	String KEY_DEFINITIONS = "definitions";
	String KEY_NON_GE_IDS = "nonGeIds";

	@ConfigSection(
			name = "General",
			description = "General settings",
			position = -10
	)
	String SECTION_GENERAL = "general";

	@ConfigSection(
			name = "Defaults",
			description = "Default values for newly added definitions",
			position = -9
	)
	String SECTION_DEFAULTS = "defaults";

	@ConfigSection(
			name = "Advanced",
			description = "Options for advanced users",
			position = 100
	)
	String SECTION_ADVANCED = "advanced";

	/////

	@ConfigItem(
		keyName = "enableNotifications",
		name = "Enable Notifications",
		description = "If true, items that have been set to notify will do so once offered by the the other player",
		section = SECTION_GENERAL
	)
	default boolean enableNotifications() { return true; }

	@ConfigItem(
			keyName = "defaultColor",
			name = "Default color",
			description = "The default color set when adding a new highlight definition",
			section = SECTION_DEFAULTS
	)
	default Color defaultColor() { return Color.white; }

	@ConfigItem(
			keyName = "defaultNotify",
			name = "Default notify",
			description = "If enabled, any new definition with be set to notify by default",
			section = SECTION_DEFAULTS
	)
	default boolean defaultNotify() { return false; }

	@ConfigItem(
			keyName = KEY_NON_GE_IDS,
			name = "Custom item IDs",
			description = "Comma-separated list of item IDs that will allow those items to be added for highlighting." +
					"<br>Useful for new tradeable items that cannot be put in the GE that are not yet handled by the plugin." +
					"<br>Make an issue on the plugin's github page if this item should be handled by default.",
			section = SECTION_ADVANCED
	)
	default String getNonGeIds() { return ""; }
}
