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

import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.util.*;

public class TradeHighlightManager
{
    private static final int RECEIVED_CONTAINER = InventoryID.TRADEOFFER | 0x8000;
    private static final int TRADE_MENU = 335;
    private static final int TRADE_OTHER_CHILD_ID = 28;
    private static final int TRADE_MINE_CHILD_ID = 25;
    private static final String TEMPLATE_NOTIFY_WARNING = "WARNING: Other player offered %s!";

    private static Notifier notifier;
    private static EventBus eventBus;
    private static OverlayManager overlayManager;

    private final TradeHighlightOverlay tradeHighlightOverlay;
    private HashMap<Integer, HighlightDefinition> definitions = new HashMap<>();
    private final ArrayList<Widget> highlighted = new ArrayList<>();
    private HashSet<Integer> previousIds = new HashSet<>();

    TradeHighlightManager(OverlayManager overlayManager, EventBus eventBus, Notifier notifier)
    {
        TradeHighlightManager.notifier = notifier;
        TradeHighlightManager.eventBus = eventBus;
        TradeHighlightManager.overlayManager = overlayManager;
        tradeHighlightOverlay = new TradeHighlightOverlay(this);
        overlayManager.add(tradeHighlightOverlay);
        eventBus.register(this);
    }

    public void shutdown()
    {
        eventBus.unregister(this);
        overlayManager.remove(tradeHighlightOverlay);
    }

    @Subscribe
    private void onConfigChanged(ConfigChanged evt)
    {
        if (evt.getGroup().equals(TradeHighligherConfig.CONFIG_GROUP))
        {
            if (evt.getKey().equalsIgnoreCase(TradeHighligherConfig.KEY_NON_GE_IDS))
            {
                ItemIdUtils.rebuildNonGeItemData();
            }
        }
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged container)
    {
        if (container.getContainerId() != RECEIVED_CONTAINER)
        {
            return;
        }
        highlighted.clear();
        TradeHighlighterUtils.getClientThread().invokeLater(() ->
        {
            final HashSet<Integer> currIds = new HashSet<>();
            final Widget widget = TradeHighlighterUtils.getWidget(TRADE_MENU, TRADE_OTHER_CHILD_ID);
            if (widget != null)
            {
                for (Widget child : Objects.requireNonNull(widget.getChildren()))
                {
                    final int id = TradeHighlighterUtils.getUnnotedId(child.getItemId());
                    if (definitions.containsKey(id))
                    {
                        highlighted.add(child);
                        final HighlightDefinition definition = definitions.get(id);
                        if (definition.getNotify() && !previousIds.contains(id))
                        {
                            notifier.notify(String.format(TEMPLATE_NOTIFY_WARNING, definition.getName()), TrayIcon.MessageType.WARNING);
                        }
                        currIds.add(id);
                    }
                }
                previousIds = currIds;
            }
        });
    }


    @Subscribe
    private void onWidgetClosed(WidgetClosed evt)
    {
        if (evt.getGroupId() == TRADE_MENU)
        {
            highlighted.clear();
            previousIds.clear();
        }
    }

    public void addDefinition(HighlightDefinition definition)
    {
        if (definitions.containsKey(definition.getId()))
        {
            return;
        }
        TradeHighlighterUtils.getClientThread().invoke(() -> {
            definition.setName(TradeHighlighterUtils.getItemManager().getItemComposition(definition.getId()).getMembersName());
            definitions.put(definition.getId(), definition);
            eventBus.post(new EventDefinitionAdded(definition));
            TradeHighlighterUtils.saveDefinitions();
        });
    }

    public void removeDefinition(final int itemId)
    {
        if (!definitions.containsKey(itemId))
        {
            return;
        }
        eventBus.post(new EventDefinitionRemoved(definitions.remove(itemId)));
        TradeHighlighterUtils.saveDefinitions();
    }

    public void refreshDefinition(final HashMap<Integer, HighlightDefinition> definitions)
    {
        TradeHighlighterUtils.getClientThread().invoke(() ->
        {
            for (HighlightDefinition definition : definitions.values())
            {
                definition.setName(TradeHighlighterUtils.getItemManager().getItemComposition(definition.getId()).getMembersName());
            }
            this.definitions = definitions;
            eventBus.post(new EventDefinitionsRefreshed(definitions));
        });
    }

    public ArrayList<Widget> getHighlighted() { return highlighted; }
    public boolean hasDefinition(int itemId)
    {
        return definitions.containsKey(itemId);
    }
    public HighlightDefinition getDefinition(int itemID) { return definitions.get(itemID); }
    public Collection<HighlightDefinition> getDefinitions() { return definitions.values(); }
}
