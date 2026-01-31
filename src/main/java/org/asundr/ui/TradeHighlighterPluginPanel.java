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
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;
import net.runelite.http.api.item.ItemPrice;
import org.asundr.*;

import java.util.*;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class TradeHighlighterPluginPanel extends PluginPanel
{
    enum PanelTab
    {
        DEFINITIONS,
        ADD_NEW
    }
    public static final int MAX_SEARCH_COUNT = 50;
    static final int PANEL_PADDING_X = 10;
    static final int SCROLL_PANEL_WIDTH =  PANEL_WIDTH - PANEL_PADDING_X;

    private final static Border BORDER_EMPTY = BorderFactory.createEmptyBorder(0, 0, 0, 0);


    static ClientThread clientThread;

    private final TradeHighlightManager tradeHighlightManager;

    private static final ExecutorService searchExecutor = Executors.newFixedThreadPool(1);

    private final JPanel display = new JPanel();
    private PanelTab currentTab = PanelTab.DEFINITIONS;
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
    private MaterialTab definitionsTab;
    private MaterialTab searchTab;
    private final JLabel searchFooterMessage = new JLabel();
    private final JPanel searchFooterPanel = new JPanel();

    private final JPanel definitionsMainPanel = new JPanel();
    private final JPanel definitionListPanel = new JPanel();
    private final JPanel searchMainPanel = new JPanel();
    private final JPanel searchListPanel = new JPanel();
    private final IconTextField itemSearchBar = new IconTextField();


    public TradeHighlighterPluginPanel(TradeHighlightManager tradeHighlightManager)
    {
        super(false); // disables scrolling
        //TradeHighlighterPluginPanel.PANEL_WIDTH = getPreferredSize().width;

        this.tradeHighlightManager = tradeHighlightManager;
        SearchItemPanel.mainPanel = this;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        buildHeader();

        display.setPreferredSize(new Dimension(PANEL_WIDTH, 2000));
        add(display);

        buildDefinitionsPanel();
        buildSearchPanel();

        tabGroup.select(definitionsTab);
        revalidate();
    }

    public static void initialize(ClientThread clientThread)
    {
        TradeHighlighterPluginPanel.clientThread = clientThread;
    }

    @Subscribe private void onEventDefinitionAdded(EventDefinitionAdded evt) { addDefinitionPanel(evt.getDefinition()); }
    @Subscribe private void onEventDefinitionRemoved(EventDefinitionRemoved evt) { removeDefinitionPanel(evt.getDefinition()); }
    @Subscribe private void onEventDefinitionsRefreshed(EventDefinitionsRefreshed evt) { refreshDefinitionPanel(evt.getDefinitions()); }

    private void buildHeader()
    {
//        final JPanel titleWrapper = new JPanel();
//        final JLabel titleLabel = new JLabel("<html><span style='font-size:16;color:yellow'><b><nobr>Trade Highlighter</nobr></b></span><html><br>");
//        titleLabel.setBorder(BORDER_EMPTY);
//        titleWrapper.add(titleLabel);
//        titleWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
//        titleLabel.setToolTipText("Created by asundr");
//        //titleWrapper.setBorder(BORDER_EMPTY);
//        titleWrapper.setPreferredSize(new Dimension(PANEL_WIDTH, 20));
//        //headerPanel.add(titleWrapper, CENTER_ALIGNMENT);

        final JPanel tabWrapper = new JPanel();
        definitionsTab = new MaterialTab("Definitions", tabGroup, definitionsMainPanel);
        searchTab = new MaterialTab("Add", tabGroup, searchMainPanel);
        searchTab.setOnSelectEvent(() -> {
            SwingUtilities.invokeLater(itemSearchBar::requestFocusInWindow);
            return true;
        });
        tabGroup.addTab(definitionsTab);
        tabGroup.addTab(searchTab);
        tabWrapper.add(tabGroup);

//        add(titleWrapper);
        add(tabWrapper);
    }

    private void buildDefinitionsPanel()
    {
        definitionsMainPanel.setLayout(new BorderLayout());

        definitionListPanel.setLayout(new BoxLayout(definitionListPanel, BoxLayout.Y_AXIS));
        JScrollPane definitionHistoryScroll = new JScrollPane(definitionListPanel);
        definitionHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        definitionHistoryScroll.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, 2000));
        definitionListPanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, 1));
        definitionListPanel.setBorder(BorderFactory.createEmptyBorder(0, PANEL_PADDING_X, 0, 0));
//        definitionListPanel.setBackground(Color.BLUE);

        // Custom scrollbar
//        JScrollBar customScrollBar = new JScrollBar(JScrollBar.VERTICAL);
//        Dimension preferredSize = new Dimension(6, Integer.MAX_VALUE);
//        customScrollBar.setPreferredSize(preferredSize);
//        definitionHistoryScroll.setVerticalScrollBar(customScrollBar);

        updateDefinitionPanelSize();

        definitionsMainPanel.add(definitionHistoryScroll, BorderLayout.CENTER);
    }

    private void updateDefinitionPanelSize()
    {
        definitionListPanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, definitionListPanel.getComponentCount() * HighlightDefinitionPanel.getSizeVertical()));
    }

    private void buildSearchPanel()
    {
        searchMainPanel.setLayout(new BorderLayout());

        JPanel toolbar = buildSearchToolbar();

        searchListPanel.setLayout(new BoxLayout(searchListPanel, BoxLayout.Y_AXIS));
        JScrollPane searchListScroll = new JScrollPane(searchListPanel);
        searchListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchListScroll.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, 2000));
        searchListPanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, 1));
        searchListPanel.setBorder(BorderFactory.createEmptyBorder(0, PANEL_PADDING_X, 0, 0));

        final Dimension footerDimension = new Dimension(PANEL_WIDTH, 35);
        searchFooterPanel.setPreferredSize(footerDimension);
        searchFooterPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        searchFooterMessage.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        searchFooterPanel.add(searchFooterMessage);
        searchFooterPanel.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));
        searchFooterPanel.setVisible(false);


        searchMainPanel.add(toolbar, BorderLayout.NORTH);
        searchMainPanel.add(searchListScroll, BorderLayout.CENTER);
        searchMainPanel.add(searchFooterPanel, BorderLayout.SOUTH);


        //add(searchMainPanel);
    }

    private void updateSearchPanelFooter(int omittedCount)
    {
        if (omittedCount > 0)
        {
            searchFooterMessage.setText("<html>Omitting <span style=\"color:#8080FF\">" + omittedCount + "</span> additional results</html>");
            searchFooterPanel.setVisible(true);
        }
        else
        {
            searchFooterPanel.setVisible(false);
        }
    }

    private JPanel buildDefinitionsToolbar()
    {
        JPanel toolbar = new JPanel();
        JButton addDefinitionButton = new JButton("+");
        addDefinitionButton.addActionListener(e -> setTab(PanelTab.ADD_NEW));
        toolbar.add(addDefinitionButton);
        //toolbar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        return toolbar;
    }

    private JPanel buildSearchToolbar()
    {
        final JPanel toolbar = new JPanel();
        itemSearchBar.setIcon(IconTextField.Icon.SEARCH);
        itemSearchBar.addActionListener(e -> searchExecutor.execute(this::updateSearchList));
        itemSearchBar.setPreferredSize(new Dimension(PANEL_WIDTH - 16, 32));
        itemSearchBar.addClearListener(this::updateSearchList);
        itemSearchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        toolbar.add(itemSearchBar);
        toolbar.setBorder(BorderFactory.createLineBorder(ColorScheme.DARKER_GRAY_COLOR));
        return toolbar;
    }

    private void updateSearchList()
    {
        searchListPanel.removeAll();
        final String searchQuery = itemSearchBar.getText();
        if (searchQuery.isBlank())
        {
            updateSearchPanelFooter(-1);
            SwingUtilities.invokeLater(searchListPanel::updateUI);
            return;
        }
        final List<ItemPrice> result = TradeHighlighterUtils.getItemManager().search(searchQuery);
//        if (result.isEmpty())
//        {
//            itemSearchBar.setIcon(IconTextField.Icon.ERROR);
//            //errorPanel.setContent("No results found.", "No items were found with that name, please try again.");
//            //cardLayout.show(centerPanel, ERROR_PANEL);
//            itemSearchBar.setEditable(true);
//            return;
//        }
        //clientThread.invokeLater(() -> processResult(result, searchQuery, exactMatch));
        clientThread.invoke(() ->{
            int addedCount = 0, alreadyDefinedCount = 0;
            for (final ItemPrice item : result)
            {
                int id = item.getId();
                final ItemComposition comp = TradeHighlighterUtils.getItemManager().getItemComposition(id);
                if (comp.getNote() != -1)
                {
                    id = comp.getLinkedNoteId();
                }
                if (tradeHighlightManager.hasDefinition(id))
                {
                    ++alreadyDefinedCount;
                    continue;
                }
                final String name = comp.getMembersName();
                searchListPanel.add(new SearchItemPanel(id, name));
                if (++addedCount >= MAX_SEARCH_COUNT)
                {
                    break;
                };
            }
            final ArrayList<SearchItemPanel> nonGeItems = TradeHighlighterUtils.matchNonGeItems(searchQuery);
            for (final SearchItemPanel itemPanel : nonGeItems)
            {
                if (addedCount >= MAX_SEARCH_COUNT)
                {
                    break;
                }
                if (tradeHighlightManager.hasDefinition(itemPanel.getItemId()))
                {
                    ++alreadyDefinedCount;
                    continue;
                }
                searchListPanel.add(itemPanel);
                ++addedCount;
            }
            searchListPanel.setPreferredSize(new Dimension(SCROLL_PANEL_WIDTH, addedCount * SearchItemPanel.SIZE_VERTICAL));
            updateSearchPanelFooter((result.size() + nonGeItems.size()) - (addedCount + alreadyDefinedCount));
            SwingUtilities.invokeLater(searchListPanel::updateUI);
        });
    }

    private void addDefinitionPanel(HighlightDefinition definition)
    {
        final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition, tradeHighlightManager);
        final int index = getInsertIndex(definition.getName());
        // toggle visible if filter active

        SwingUtilities.invokeLater(()->{
            definitionListPanel.add(highlightDefinitionPanel, index);
            updateDefinitionPanelSize();
            definitionListPanel.updateUI();
        });
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
                    Arrays.stream(searchListPanel.getComponents()).filter(e -> ((SearchItemPanel)e).getItemName().equalsIgnoreCase(definition.getName())).findFirst().ifPresent(c -> c.setVisible(true));
                    SwingUtilities.invokeLater(() -> {
                        definitionListPanel.remove(component);
                        updateDefinitionPanelSize();
                        definitionListPanel.updateUI();
                        searchListPanel.updateUI();
                    });
                    return;
                }
            }
        }
    }

    private void refreshDefinitionPanel(final HashMap<Integer, HighlightDefinition> definitions)
    {
        final Comparator<HighlightDefinition> comparator = (o1, o2) -> o1.getName().compareTo(o2.getName());
        final List<HighlightDefinition> sortedDefinitions = definitions.values().stream().sorted(comparator).collect(Collectors.toList());
        SwingUtilities.invokeLater(()->{
            for (HighlightDefinition definition : sortedDefinitions)
            {
                final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition, tradeHighlightManager);
                definitionListPanel.add(highlightDefinitionPanel);
            }
            updateDefinitionPanelSize();
            SwingUtilities.invokeLater(definitionListPanel::updateUI);
        });
    }

    // TODO update with tab api
    public void setTab(PanelTab tab)
    {
        MaterialTab tabWidget;
        switch (tab)
        {
            case DEFINITIONS:
                tabWidget = definitionsTab; break;
            case ADD_NEW:
                tabWidget = searchTab;
                break;
            default:
                return;
        }
        if (tabWidget.isSelected())
        {
            return;
        }
        tabGroup.select(tabWidget);
    }

    private int getInsertIndex(String name)
    {
        name = name.toLowerCase();
        int index = 0;
        for (Component component : definitionListPanel.getComponents())
        {
            if (component instanceof HighlightDefinitionPanel)
            {
                HighlightDefinitionPanel panel = (HighlightDefinitionPanel) component;
                if (name.compareTo(panel.getDefinition().getName().toLowerCase()) < 0)
                {
                    return index;
                }
            }
            ++index;
        }
        return index;
    }
}
