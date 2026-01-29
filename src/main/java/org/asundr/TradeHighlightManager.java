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

import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetClosed;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.Notifier;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.OverlayManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class TradeHighlightManager
{
    public static final int RECEIVED_CONTAINER = InventoryID.TRADEOFFER | 0x8000;
    public static final int TRADE_MENU = 335;
    public static final int TRADE_OTHER_CHILD_ID = 28;
    public static final int TRADE_MINE_CHILD_ID = 25;

    private final Client client;
    private final ClientThread clientThread;
    private final ItemManager itemManager;
    private final Notifier notifier;
    private final EventBus eventBus;
    private final OverlayManager overlayManager;

    final TradeHighlightOverlay tradeHighlightOverlay;
    final HashMap<Integer, HighlightDefinition> definitions = new HashMap<>();
    final ArrayList<Widget> highlighted = new ArrayList<>();
    HashSet<Integer> previousIds = new HashSet<>();

    static TradeHighligherConfig config;

    TradeHighlightManager(Client client, ClientThread clientThread, OverlayManager overlayManager, ItemManager itemManager, EventBus eventBus, TradeHighligherConfig config, Notifier notifier)
    {

        TradeHighlightManager.config = config;

        this.client = client;
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.notifier = notifier;
        this.eventBus = eventBus;
        this.overlayManager = overlayManager;
        tradeHighlightOverlay = new TradeHighlightOverlay(this, itemManager);
        overlayManager.add(tradeHighlightOverlay);
        eventBus.register(this);
    }

    public void shutdown()
    {
        eventBus.unregister(this);
        overlayManager.remove(tradeHighlightOverlay);
    }

    @Subscribe
    private void onItemContainerChanged(ItemContainerChanged container)
    {
        if (container.getContainerId() != RECEIVED_CONTAINER)
        {
            return;
        }
        highlighted.clear();
        clientThread.invokeLater(() ->
        {
            HashSet<Integer> currIds = new HashSet<>();
            final Widget widget = client.getWidget(TRADE_MENU, TRADE_OTHER_CHILD_ID);
            if (widget != null)
            {
                for (Widget child : Objects.requireNonNull(widget.getChildren()))
                {
                    int id = child.getItemId();
                    final ItemComposition comp = itemManager.getItemComposition(id);
                    if (comp.getNote() != -1)
                    {
                        id = comp.getLinkedNoteId();
                    }
                    if (definitions.containsKey(id))
                    {
                        highlighted.add(child);
                        final HighlightDefinition definition = definitions.get(id);
                        if (definition.getNotify() && !previousIds.contains(id))
                        {
                            notifier.notify(String.format("WARNING: Other player offered %s!", definition.getName()), TrayIcon.MessageType.WARNING);
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
        clientThread.invoke(() -> {
            definition.setName(itemManager.getItemComposition(definition.getId()).getMembersName());
            definitions.put(definition.getId(), definition);
            eventBus.post(new EventDefinitionAdded(definition));
        });
    }

    public void removeDefinition(final int itemId)
    {
        if (!definitions.containsKey(itemId))
        {
            return;
        }
        eventBus.post(new EventDefinitionRemoved(definitions.remove(itemId)));
    }



}
