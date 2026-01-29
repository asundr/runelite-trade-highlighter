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

package org.asundr.ui;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.PluginPanel;
import org.asundr.EventDefinitionAdded;
import org.asundr.EventDefinitionRemoved;
import org.asundr.HighlightDefinition;
import org.asundr.TradeHighlightManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TradeHighlighterPluginPanel extends PluginPanel
{
    public static int PANEL_WIDTH;

    static ClientThread clientThread;

    private final TradeHighlightManager tradeHighlightManager;

    private final JPanel definitionListPanel = new JPanel();


    public TradeHighlighterPluginPanel(TradeHighlightManager tradeHighlightManager)
    {
        super(false); // disables scrolling
        TradeHighlighterPluginPanel.PANEL_WIDTH = getPreferredSize().width;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildToolbar();

        this.tradeHighlightManager = tradeHighlightManager;

        definitionListPanel.setLayout(new BoxLayout(definitionListPanel, BoxLayout.Y_AXIS));
        //set border?
        JScrollPane definitionHistoryScroll = new JScrollPane(definitionListPanel);
        definitionHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        definitionHistoryScroll.setPreferredSize(new Dimension(PANEL_WIDTH, 2000));
        // Custom scrollbar
        JScrollBar customScrollBar = new JScrollBar(JScrollBar.VERTICAL);
        Dimension preferredSize = new Dimension(6, Integer.MAX_VALUE);
        customScrollBar.setPreferredSize(preferredSize);
        definitionHistoryScroll.setVerticalScrollBar(customScrollBar);

        add(definitionHistoryScroll);
    }

    public static void initialize(ClientThread clientThread)
    {
        TradeHighlighterPluginPanel.clientThread = clientThread;
    }

    @Subscribe private void onEventDefinitionAdded(EventDefinitionAdded evt) { addDefinitionPanel(evt.getDefinition()); }

    @Subscribe private void onEventDefinitionRemoved(EventDefinitionRemoved evt) { removeDefinitionPanel(evt.getDefinition()); }

    private void buildToolbar()
    {
        JPanel toolbar = new JPanel();

        JButton addDefinitionButton = new JButton("+");
        addDefinitionButton.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                tradeHighlightManager.addDefinition(new HighlightDefinition(995, Color.pink, true));
            }
        });
        toolbar.add(addDefinitionButton);

        add(toolbar);
    }

    private void addDefinitionPanel(HighlightDefinition definition)
    {
        //client thread? invoked later?
        final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition, tradeHighlightManager);
        // Create padding?
        // toggle visible if filter active

        SwingUtilities.invokeLater(()->{
                definitionListPanel.add(highlightDefinitionPanel);
                definitionListPanel.updateUI();
        });

        //definitionListPanel.add(highlightDefinitionPanel.paddingStruct);
        // update empty definitions message
    }

    private void removeDefinitionPanel(final HighlightDefinition definition)
    {
        for (Component component : definitionListPanel.getComponents())
        {
            if (component instanceof HighlightDefinitionPanel)
            {
                HighlightDefinitionPanel panel = (HighlightDefinitionPanel) component;
                if (panel.getDefinition() == definition)
                {
                    SwingUtilities.invokeLater(() -> {
                        definitionListPanel.remove(component);
                        definitionListPanel.updateUI();
                    });
                    return;
                }
            }
        }
    }
}
