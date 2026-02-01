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

        rebuildNonGeItemData();
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

    ////

    static final class ItemIdNamePair implements Comparable<ItemIdNamePair>
    {
        int id;
        String name;
        ItemIdNamePair(int id) { this.id = id;}
        ItemIdNamePair(int id, String name) { this.id = id; this.name = name; }
        @Override public int compareTo(ItemIdNamePair o) {
            return name.compareTo(o.name);
        }
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!(obj instanceof ItemIdNamePair))
            {
                return false;
            }
            ItemIdNamePair other = (ItemIdNamePair) obj;
            return other.id == this.id;
        }
        @Override public int hashCode() { return id; }
    }

    // https://oldschool.runescape.wiki/w/Grand_Exchange/Non-tradeable_items as of Feb 1 2026
    private static int[] unhandledItemIds = {
        ItemID.COINS, ItemID.PLATINUM, // Currency
        7954, 369, 357, 323, 20854, 20869, 343, 367, 3383, ItemID.BURNT_CAVE_EEL, 10140, 381, 375, 3148, 7948, 387, 399, ItemID.BURNT_ANGLERFISH, 11938, 393, // burnt seafood
        2311, 2329, 2305, 1903, 2144, 2146, 7222,9982, 3375, 6301, 9990, 2880, 7570, 2426, 2345, 7090, 7092,  7094, 2005, 2013, 6699, 2199, 2247, 5990, 2175, 6303,   // burnt other food
        598, 2532, 2534, 2536, 2538, 2540, 21328, 11217,  // fire arrows
        12728, 13254, 12738, 12732, 11881, 12734, 12736, 13250, 13252, 21698, 21704, 21707, 21701, 11887, 11879, 12730,  // item packs
        4049, 4053, 8936, 4045, 4047, 8938, 4043, 4051,  // Mini games
        10880, 10881, 10878, 10877, 10879, 10882,  // Satchels
        7622, 7624, 7626, 7630, 2391, 4313, 4209, 11173, 11174, 1586, 4490, 4492, 1577, 767, 968, 4462, 4002, 2964, 759, // Quest items
        11266, 600, 583, 27485, 4496, 1575, 22361, 5978, 4291, 4293, 1633, 4073, 25571, 6675, 409, 7934, 956, 27216, 11656, 7658, 5976, 22355, // miscellaneous
        3899, 550, 11171, 27494, 22358, 195, 4289, 4287, 1940, 2959, 2518, 3377, 13563, 733, 27488, 27491, 3224, 3209, 3805, 7738, 966, 1883 // misc continued
    };

    private static TreeSet<ItemIdNamePair> nonGeItems = new TreeSet<>();

    static void rebuildNonGeItemData()
    {
        nonGeItems = new TreeSet<>();
        final ArrayList<Integer> configIds = Arrays.stream(config.getNonGeIds().split(","))
                .map(String::trim).filter(TradeHighlighterUtils::isInteger).mapToInt(Integer::parseInt)
                .boxed().collect(Collectors.toCollection(ArrayList::new));
        clientThread.invoke(()->{
            for (final int id : unhandledItemIds)
            {
                final ItemComposition comp = itemManager.getItemComposition(id);
                if (!comp.getName().equals("null"))
                {
                    nonGeItems.add(new ItemIdNamePair(id, comp.getMembersName()));
                }
            }
            for (final int id : configIds)
            {
                final ItemComposition comp = itemManager.getItemComposition(id);
                if (!comp.getName().equals("null"))
                {
                    nonGeItems.add(new ItemIdNamePair(id, comp.getMembersName()));
                }
            }
        });
    }

    public static ArrayList<SearchItemPanel> matchNonGeItems(String query)
    {
        query = query.toLowerCase();
        final ArrayList<SearchItemPanel> matches = new ArrayList<>();
        for (final ItemIdNamePair pair : nonGeItems)
        {
            if (pair.name.toLowerCase().contains(query))
            {
                matches.add(new SearchItemPanel(pair.id, pair.name));
            }
        }
        return matches;
    }
}
