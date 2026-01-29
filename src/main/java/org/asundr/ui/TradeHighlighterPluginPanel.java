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

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.http.api.item.ItemPrice;
import org.asundr.*;

import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Slf4j
public class TradeHighlighterPluginPanel extends PluginPanel
{
    enum PanelTab
    {
        DEFINITIONS,
        ADD_NEW
    }
    public static int PANEL_WIDTH;

    static ClientThread clientThread;

    private final TradeHighlightManager tradeHighlightManager;


    private PanelTab currentTab = PanelTab.DEFINITIONS;
    private final JPanel definitionsMainPanel = new JPanel();
    private final JPanel definitionListPanel = new JPanel();
    private final JPanel searchMainPanel = new JPanel();
    private final JPanel searchListPanel = new JPanel();
    private final IconTextField itemSearchBar = new IconTextField();


    public TradeHighlighterPluginPanel(TradeHighlightManager tradeHighlightManager)
    {
        super(false); // disables scrolling
        TradeHighlighterPluginPanel.PANEL_WIDTH = getPreferredSize().width;

        this.tradeHighlightManager = tradeHighlightManager;
        SearchItemPanel.mainPanel = this;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildDefinitionsPanel();
        buildSearchPanel();

        definitionsMainPanel.setVisible(true);
        searchMainPanel.setVisible(false);
    }

    public static void initialize(ClientThread clientThread)
    {
        TradeHighlighterPluginPanel.clientThread = clientThread;
    }

    private void buildDefinitionsPanel()
    {
        definitionsMainPanel.setLayout(new BorderLayout());

        final JPanel toolbar = buildDefinitionsToolbar();

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

        definitionsMainPanel.add(toolbar, BorderLayout.NORTH);
        definitionsMainPanel.add(definitionHistoryScroll, BorderLayout.CENTER);

        add(definitionsMainPanel);
    }

    private void buildSearchPanel()
    {
        searchMainPanel.setLayout(new BorderLayout());

        JPanel toolbar = buildSearchToolbar();

        searchListPanel.setLayout(new BoxLayout(searchListPanel, BoxLayout.Y_AXIS));
        JScrollPane searchListScroll = new JScrollPane(searchListPanel);
        searchListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchListScroll.setPreferredSize(new Dimension(PANEL_WIDTH, 2000));

        searchMainPanel.add(toolbar, BorderLayout.NORTH);
        searchMainPanel.add(searchListScroll, BorderLayout.CENTER);


        add(searchMainPanel);
    }


    @Subscribe private void onEventDefinitionAdded(EventDefinitionAdded evt) { addDefinitionPanel(evt.getDefinition()); }

    @Subscribe private void onEventDefinitionRemoved(EventDefinitionRemoved evt) { removeDefinitionPanel(evt.getDefinition()); }
    @Subscribe private void onEventDefinitionsRefreshed(EventDefinitionsRefreshed evt) { refreshDefinitionPanel(evt.getDefinitions()); }

    private JPanel buildDefinitionsToolbar()
    {
        JPanel toolbar = new JPanel();
        JButton addDefinitionButton = new JButton("+");
        addDefinitionButton.addActionListener(e -> setTab(PanelTab.ADD_NEW));
        toolbar.add(addDefinitionButton);
        return toolbar;
    }

    private JPanel buildSearchToolbar()
    {
        final JPanel toolbar = new JPanel();
        JButton backToDefinitionsButton = new JButton("◁");
        backToDefinitionsButton.addActionListener(e -> setTab(PanelTab.DEFINITIONS));

        itemSearchBar.setIcon(IconTextField.Icon.SEARCH);
//        searchBar.addActionListener(e -> executor.execute(() -> priceLookup(false)));
        itemSearchBar.addActionListener(e -> updateSearchList());

        toolbar.add(backToDefinitionsButton);
        toolbar.add(itemSearchBar);
        //toolbar.
        return toolbar;
    }

    private void updateSearchList()
    {
        searchListPanel.removeAll();
        final String searchQuery = itemSearchBar.getText();
        if (searchQuery.isBlank())
        {
            SwingUtilities.invokeLater(searchListPanel::updateUI);
            return;
        }
        final List<ItemPrice> result = TradeHighlighterUtils.getItemManager().search(searchQuery);
        if (result.isEmpty())
        {
            itemSearchBar.setIcon(IconTextField.Icon.ERROR);
            //errorPanel.setContent("No results found.", "No items were found with that name, please try again.");
            //cardLayout.show(centerPanel, ERROR_PANEL);
            itemSearchBar.setEditable(true);
            return;
        }
        //clientThread.invokeLater(() -> processResult(result, searchQuery, exactMatch));
        clientThread.invoke(() ->{
        for (final ItemPrice item : result)
        {
            int id = item.getId();
            final ItemComposition comp = TradeHighlighterUtils.getItemManager().getItemComposition(id);
            if (!comp.isTradeable())
            {
                continue;
            }
            if (comp.getNote() != -1)
            {
                id = comp.getLinkedNoteId();
            }
            if (tradeHighlightManager.hasDefinition(id))
            {
                continue;
            }
            final String name = comp.getMembersName();
            searchListPanel.add(new SearchItemPanel(id, name));
        }
        });
        SwingUtilities.invokeLater(searchListPanel::updateUI);
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

    private void refreshDefinitionPanel(final HashMap<Integer, HighlightDefinition> definitions)
    {
        SwingUtilities.invokeLater(()->{
            for (HighlightDefinition definition : definitions.values())
            {
                final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition, tradeHighlightManager);
                definitionListPanel.add(highlightDefinitionPanel);
            }
            SwingUtilities.invokeLater(definitionListPanel::updateUI);
        });
    }

    public void setTab(PanelTab tab)
    {
        if (tab == currentTab)
        {
            return;
        }
        definitionsMainPanel.setVisible(false);
        searchMainPanel.setVisible(false);
        switch (tab)
        {
            case DEFINITIONS:
                itemSearchBar.setText("");
                definitionsMainPanel.setVisible(true); break;
            case ADD_NEW:
                searchMainPanel.setVisible(true); break;
            default:
                log.warn("Unhandled tab in TradeHighlightPluginPanel::setTab()");
        }
        currentTab = tab;
        SwingUtilities.invokeLater(this::updateUI);
    }
}
