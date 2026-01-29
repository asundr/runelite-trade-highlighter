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
import net.runelite.client.config.ConfigManager;
import net.runelite.client.game.ItemManager;

import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;

public class TradeHighlighterUtils
{
    @Getter private static ItemManager itemManager;
    @Getter private static TradeHighligherConfig config;
    private static ConfigManager configManager;

    private static Gson gson;

    public static void initialize(TradeHighligherConfig config, ConfigManager configManager, ItemManager itemManager, Gson gson)
    {
        TradeHighlighterUtils.config = config;
        TradeHighlighterUtils.configManager = configManager;
        TradeHighlighterUtils.itemManager = itemManager;
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
        final Collection<HighlightDefinition> definitions = TradeHighlighterPlugin.tradeHighlightManager.definitions.values();
        Gson builder = TradeHighlighterUtils.getGsonBuilder();
        String json = builder.toJson(definitions);
        configManager.setConfiguration(config.CONFIG_GROUP, "definitions", json);
    }

    public static void loadDefinitions()
    {
        Gson builder = TradeHighlighterUtils.getGsonBuilder();
        String json = configManager.getConfiguration(config.CONFIG_GROUP, "definitions");
        if (json == null)
        {
            return;
        }
        Type type = new TypeToken<Collection<HighlightDefinition>>(){}.getType();
        Collection<HighlightDefinition> definitionValues = builder.fromJson(json, type);
        HashMap<Integer, HighlightDefinition> definitions = new HashMap<>();
        for (HighlightDefinition definition : definitionValues)
        {
            definitions.put(definition.getId(), definition);
        }
        TradeHighlighterPlugin.tradeHighlightManager.refreshDefinition(definitions);
    }
}
