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

import com.google.gson.Gson;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.overlay.OverlayManager;
import org.asundr.ui.TradeHighlighterPluginPanel;

import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Trade Highlighter",
	description = "Highlights configurable items in the player trade menu. Useful for scam prevention.",
	tags = {"trade" , "highlight", "scam", "prevention"}
)
public class TradeHighlighterPlugin extends Plugin
{
	@Inject private Client client;
	@Inject private ClientThread clientThread;
	@Inject private TradeHighligherConfig config;
	@Inject private OverlayManager overlayManager;
	@Inject private ItemManager itemManager;
	@Inject private EventBus eventBus;
	@Inject private Notifier notifier;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ColorPickerManager colorPickerManager;
	@Inject private Gson gson;
	@Inject private ConfigManager configManager;

	static TradeHighlightManager tradeHighlightManager;

	private NavigationButton navigationButton;
	private TradeHighlighterPluginPanel mainPanel;


	@Override
	protected void startUp() throws Exception
	{
		TradeHighlighterUtils.initialize(config, configManager, client, clientThread, itemManager, colorPickerManager, gson);
		TradeHighlighterPluginPanel.initialize();
		ItemIdUtils.rebuildNonGeItemData();
		tradeHighlightManager = new TradeHighlightManager(overlayManager, eventBus, notifier);
		TradeHighlighterUtils.setTradeHighlightManager(tradeHighlightManager);
		mainPanel = new TradeHighlighterPluginPanel(tradeHighlightManager);
		eventBus.register(mainPanel);
		addNavigationButton(mainPanel);
		TradeHighlighterUtils.loadDefinitions();
	}

	@Override
	protected void shutDown() throws Exception
	{
		eventBus.unregister(mainPanel);
		tradeHighlightManager.shutdown();
		clientToolbar.removeNavigation(navigationButton);
		TradeHighlighterPluginPanel.shutdown();
	}

	private void addNavigationButton(final TradeHighlighterPluginPanel mainPanel)
	{
		navigationButton = NavigationButton.builder()
				.tooltip("Trade Highlighter")
				.icon(makeIcon())
				.priority(5)
				.panel(mainPanel)
				.build();
		clientToolbar.addNavigation(navigationButton);
	}

	private static BufferedImage makeIcon()
	{
		BufferedImage icon = TradeHighlighterUtils.getImageFromName("tradehighlighter_nav_icon.png");
		if (icon != null)
		{
			return icon;
		}
		BufferedImage img = new BufferedImage(48,48, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		g.setColor(Color.blue);
		g.fillRect(0, 0, 48, 48);
		g.setColor(Color.white);
		g.setFont(new Font(Font.MONOSPACED, 0, 72));
		g.drawString("T", 6, 46);
		return img;
	}

	@Provides TradeHighligherConfig provideConfig(ConfigManager configManager) { return configManager.getConfig(TradeHighligherConfig.class); }
}
