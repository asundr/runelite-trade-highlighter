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
import com.google.gson.reflect.TypeToken;
import lombok.Getter;
import lombok.Setter;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import org.asundr.ui.SearchItemPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class TradeHighlighterUtils
{
    @Getter private static ClientThread clientThread;
    @Getter private static TradeHighligherConfig config;
    @Getter private static ItemManager itemManager;
    @Getter @Setter private static TradeHighlightManager tradeHighlightManager;
    private static Client client;
    private static ColorPickerManager colorPickerManager;
    private static ConfigManager configManager;
    private static Gson gson;

    public static void initialize(TradeHighligherConfig config, ConfigManager configManager, Client client, ClientThread clientThread, ItemManager itemManager, ColorPickerManager colorPickerManager, Gson gson)
    {
        TradeHighlighterUtils.client = client;
        TradeHighlighterUtils.config = config;
        TradeHighlighterUtils.configManager = configManager;
        TradeHighlighterUtils.clientThread = clientThread;
        TradeHighlighterUtils.itemManager = itemManager;
        TradeHighlighterUtils.colorPickerManager = colorPickerManager;
        TradeHighlighterUtils.gson = gson;
    }

    public static Color lerp(final Color a, final Color b, final float alpha)
    {
        return new Color(
            lerp(a.getRed(),    b.getRed(),     alpha),
            lerp(a.getGreen(),  b.getGreen(),   alpha),
            lerp(a.getBlue(),   b.getBlue(),    alpha)
        );
    }

    public static int lerp(int a, int b, float alpha)
    {
        return (int)((1f - alpha)*(float)a + alpha*(float)b);
    }

    public static Gson getGsonBuilder()
    {
        return gson.newBuilder().create();
    }

    public static void saveDefinitions()
    {
        final Collection<HighlightDefinition> definitions = TradeHighlighterPlugin.tradeHighlightManager.getDefinitions();
        final Gson builder = TradeHighlighterUtils.getGsonBuilder();
        final String json = builder.toJson(definitions);
        configManager.setConfiguration(config.CONFIG_GROUP, config.KEY_DEFINITIONS, json);
    }

    public static void loadDefinitions()
    {
        final Gson builder = TradeHighlighterUtils.getGsonBuilder();
        final String json = configManager.getConfiguration(config.CONFIG_GROUP, config.KEY_DEFINITIONS);
        if (json == null)
        {
            return;
        }
        final Type type = new TypeToken<Collection<HighlightDefinition>>(){}.getType();
        final Collection<HighlightDefinition> definitionValues = builder.fromJson(json, type);
        final HashMap<Integer, HighlightDefinition> definitions = new HashMap<>();
        for (HighlightDefinition definition : definitionValues)
        {
            definitions.put(definition.getId(), definition);
        }
        TradeHighlighterPlugin.tradeHighlightManager.refreshDefinition(definitions);
    }

    public static ImageIcon getIconFromName(final String filename, int width, int height, final int hints)
    {
        final BufferedImage iconImg = getImageFromName(filename);
        if (iconImg == null)
        {
            return null;
        }
        if (width == -1 && height == -1)
        {
            return new ImageIcon(iconImg);
        }
        if (width == -1)
        {
            width = height;
        }
        if (height == -1)
        {
            height = width;
        }
        return new ImageIcon(iconImg.getScaledInstance(width, height, hints));
    }

    public static BufferedImage getImageFromName(final String filename)
    {
        return ImageUtil.loadImageResource(TradeHighlighterPlugin.class, "/" + filename);
    }

    public static AsyncBufferedImage getItemImage(int id)
    {
        return itemManager.getImage(id, Integer.MAX_VALUE, false);
    }

    public static boolean isInteger(String s)
    {
        try
        {
            Integer.parseInt(s);
        }
        catch (Exception e)
        {
            return false;
        }
        return true;
    }

    public static void setFixedSize(final Component component, final Dimension dimension)
    {
        component.setPreferredSize(dimension);
        component.setMinimumSize(dimension);
        component.setMaximumSize(dimension);
    }

    public static RuneliteColorPicker createColorPicker(Color previousColor, String title, boolean alphaHidden)
    {
        return colorPickerManager.create(client, previousColor, title, alphaHidden);
    }

    public static Widget getWidget(int group, int child)
    {
        return client.getWidget(group, child);
    }

    public static int getUnnotedId(int id)
    {
        final ItemComposition itemComp = TradeHighlighterUtils.getItemManager().getItemComposition(id);
        return itemComp.getNote() == -1 ? id : itemComp.getLinkedNoteId();
    }

}
