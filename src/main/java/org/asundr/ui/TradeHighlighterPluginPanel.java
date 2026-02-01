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
    private static final int SIZE_SCROLLBAR_X = 6;
    private static final int SIZE_PANEL_PADDING_X = 10;
    static final int SIZE_SCROLL_PANEL_WIDTH =  PANEL_WIDTH - SIZE_PANEL_PADDING_X;
    private static final int SIZE_TITLE_Y = 30;
    private static final int SIZE_SEARCH_FOOTER = 35;
    private static final Dimension DIMENSION_SCROLL_BAR = new Dimension(SIZE_SCROLLBAR_X, Integer.MAX_VALUE);
    private static final Dimension DIMENSION_SCROLL_PANEL = new Dimension(SIZE_SCROLL_PANEL_WIDTH, Integer.MAX_VALUE);
    private static final Dimension DIMENSION_SCROLL_PANEL_EMPTY = new Dimension(SIZE_SCROLL_PANEL_WIDTH, 1);
    private static final Color COLOR_TITLE_TEXT = Color.decode("#cccc06");
    private static final Color COLOR_TITLE_BORDER = Color.decode("#808000");
    private static final Color COLOR_TITLE_BACKGROUND = new Color(34, 34,34);
    private static final Border BORDER_TITLE_TEXT_PADDING = BorderFactory.createEmptyBorder(3, 0, 0, 0);
    private static final Border BORDER_TITLE_WRAPPER = BorderFactory.createMatteBorder(0, 0, 1, 0, COLOR_TITLE_BORDER);
    private static final Border BORDER_MAIN_PANEL = BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR);
    private static final Border BORDER_MAIN_LIST_PADDING = BorderFactory.createEmptyBorder(0, SIZE_PANEL_PADDING_X, 0, 0);
    private static final Border BORDER_SEARCH_FOOTER = BorderFactory.createMatteBorder(1, 0, 0, 0, ColorScheme.DARKER_GRAY_COLOR);
    private static final Border BORDER_SEARCH_FOOTER_PADDING = BorderFactory.createEmptyBorder(8, 0, 8, 0);
    private static final String TEXT_TITLE = "<html><span style='font-size:16'><b><nobr>Trade Highlighter</nobr></b></span><html><br>";
    private static final String TOOLTIP_TITLE = "Created by asundr";
    private static final String TEMPLATE_SEARCH_OMITTED = "<html>Omitting <span style=\"color:#8080FF\">%s</span> additional results</html>";
    private static final int MAX_SEARCH_COUNT = 50;
    private static final Comparator<HighlightDefinition> COMPARATOR_HIGHLIGHT_DEFINITION = Comparator.comparing(HighlightDefinition::getName);

    private static TradeHighlighterPluginPanel instance;
    private static TradeHighlightManager tradeHighlightManager;

    private static final ExecutorService searchExecutor = Executors.newFixedThreadPool(1);

    private final JPanel definitionsMainPanel = new JPanel();
    private final JPanel searchMainPanel = new JPanel();
    private final JPanel tabContentsDisplay = new JPanel();
    private final MaterialTabGroup tabGroup = new MaterialTabGroup(tabContentsDisplay);
    private final MaterialTab definitionsTab= new MaterialTab("Definitions", tabGroup, definitionsMainPanel);;
    private final MaterialTab searchTab = new MaterialTab("Add", tabGroup, searchMainPanel);;
    private final JLabel searchFooterMessage = new JLabel();
    private final JPanel searchFooterPanel = new JPanel();
    private final JPanel definitionListPanel = new JPanel();
    private final JPanel searchListPanel = new JPanel();
    private final IconTextField itemSearchBar = new IconTextField();


    public TradeHighlighterPluginPanel(TradeHighlightManager tradeHighlightManager)
    {
        super(false); // disables scrolling
        TradeHighlighterPluginPanel.instance = this;
        TradeHighlighterPluginPanel.tradeHighlightManager = tradeHighlightManager;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        buildHeader();
        tabContentsDisplay.setPreferredSize(new Dimension(PANEL_WIDTH, 1));
        add(tabContentsDisplay);
        buildDefinitionsPanel();
        buildSearchPanel();
        tabGroup.select(definitionsTab);
        revalidate();
    }

    @Subscribe private void onEventDefinitionAdded(EventDefinitionAdded evt) { addDefinitionPanel(evt.getDefinition()); }
    @Subscribe private void onEventDefinitionRemoved(EventDefinitionRemoved evt) { removeDefinitionPanel(evt.getDefinition()); }
    @Subscribe private void onEventDefinitionsRefreshed(EventDefinitionsRefreshed evt) { refreshDefinitionPanel(evt.getDefinitions()); }

    private void buildHeader()
    {
        final JPanel titleWrapper = new JPanel();
        final JLabel titleLabel = new JLabel(TEXT_TITLE);
        titleLabel.setForeground(COLOR_TITLE_TEXT);
        titleLabel.setBorder(BORDER_TITLE_TEXT_PADDING);
        titleWrapper.add(titleLabel);
        titleWrapper.setBackground(COLOR_TITLE_BACKGROUND);
        titleLabel.setToolTipText(TOOLTIP_TITLE);
        titleWrapper.setPreferredSize(new Dimension(PANEL_WIDTH, SIZE_TITLE_Y));
        titleWrapper.setBorder(BORDER_TITLE_WRAPPER);
        add(titleWrapper, CENTER_ALIGNMENT);

        final JPanel tabWrapper = new JPanel();
        searchTab.setOnSelectEvent(() -> {
            SwingUtilities.invokeLater(itemSearchBar::requestFocusInWindow);
            return true;
        });
        tabGroup.addTab(definitionsTab);
        tabGroup.addTab(searchTab);
        tabWrapper.add(tabGroup);
        add(tabWrapper);
    }

    private void buildDefinitionsPanel()
    {
        definitionsMainPanel.setLayout(new BorderLayout());
        definitionsMainPanel.setBorder(BORDER_MAIN_PANEL);

        // main panel
        definitionListPanel.setLayout(new BoxLayout(definitionListPanel, BoxLayout.Y_AXIS));
        final JScrollPane definitionHistoryScroll = new JScrollPane(definitionListPanel);
        definitionHistoryScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        definitionHistoryScroll.setPreferredSize(DIMENSION_SCROLL_PANEL);
        definitionListPanel.setPreferredSize(DIMENSION_SCROLL_PANEL_EMPTY);
        definitionListPanel.setBorder(BORDER_MAIN_LIST_PADDING);

        // Custom scrollbar
//        JScrollBar customScrollBar = new JScrollBar(JScrollBar.VERTICAL);
//        customScrollBar.setPreferredSize(DIMENSION_SCROLL_BAR);
//        definitionHistoryScroll.setVerticalScrollBar(customScrollBar);

        updateDefinitionPanelSize();
        definitionsMainPanel.add(definitionHistoryScroll, BorderLayout.CENTER);
    }

    private void buildSearchPanel()
    {
        searchMainPanel.setLayout(new BorderLayout());

        final JPanel toolbar = new JPanel();
        itemSearchBar.setIcon(IconTextField.Icon.SEARCH);
        itemSearchBar.addActionListener(e -> searchExecutor.execute(this::updateSearchList));
        itemSearchBar.setPreferredSize(new Dimension(PANEL_WIDTH - 16, 32));
        itemSearchBar.addClearListener(this::updateSearchList);
        itemSearchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        toolbar.add(itemSearchBar);

        final JScrollPane searchListScroll = new JScrollPane(searchListPanel);
        searchListScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        searchListScroll.setPreferredSize(DIMENSION_SCROLL_PANEL);
        searchListScroll.setBorder(BORDER_MAIN_PANEL);
        searchListPanel.setLayout(new BoxLayout(searchListPanel, BoxLayout.Y_AXIS));
        searchListPanel.setPreferredSize(DIMENSION_SCROLL_PANEL_EMPTY);
        searchListPanel.setBorder(BORDER_MAIN_LIST_PADDING);

        final Dimension footerDimension = new Dimension(PANEL_WIDTH, SIZE_SEARCH_FOOTER);
        searchFooterPanel.setPreferredSize(footerDimension);
        searchFooterPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        searchFooterPanel.setBorder(BORDER_SEARCH_FOOTER);
        searchFooterPanel.setVisible(false);
        searchFooterMessage.setBorder(BORDER_SEARCH_FOOTER_PADDING);
        searchFooterPanel.add(searchFooterMessage);

        searchMainPanel.add(toolbar, BorderLayout.NORTH);
        searchMainPanel.add(searchListScroll, BorderLayout.CENTER);
        searchMainPanel.add(searchFooterPanel, BorderLayout.SOUTH);
    }

    private void updateDefinitionPanelSize()
    {
        definitionListPanel.setPreferredSize(new Dimension(SIZE_SCROLL_PANEL_WIDTH, definitionListPanel.getComponentCount() * HighlightDefinitionPanel.getFullHeight()));
    }

    private void updateSearchPanelFooter(int omittedCount)
    {
        if (omittedCount > 0)
        {
            searchFooterMessage.setText(String.format(TEMPLATE_SEARCH_OMITTED, omittedCount));
            searchFooterPanel.setVisible(true);
        }
        else
        {
            searchFooterPanel.setVisible(false);
        }
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
        TradeHighlighterUtils.getClientThread().invoke(() ->{
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
            searchListPanel.setPreferredSize(new Dimension(SIZE_SCROLL_PANEL_WIDTH, addedCount * SearchItemPanel.getFullHeight()));
            updateSearchPanelFooter((result.size() + nonGeItems.size()) - (addedCount + alreadyDefinedCount));
            SwingUtilities.invokeLater(searchListPanel::updateUI);
        });
    }

    private void addDefinitionPanel(HighlightDefinition definition)
    {
        final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition);
        final int index = getInsertIndex(definition.getName());
        SwingUtilities.invokeLater(()->{
            definitionListPanel.add(highlightDefinitionPanel, index);
            updateDefinitionPanelSize();
            definitionListPanel.updateUI();
        });
    }

    private void removeDefinitionPanel(final HighlightDefinition definition)
    {
        for (Component component : definitionListPanel.getComponents())
        {
            if (component instanceof HighlightDefinitionPanel)
            {
                final HighlightDefinitionPanel panel = (HighlightDefinitionPanel) component;
                if (panel.getDefinition() == definition)
                {
                    Arrays.stream(searchListPanel.getComponents()).filter(e -> ((SearchItemPanel)e).getItemId() == definition.getId()).findFirst().ifPresent(c -> c.setVisible(true));
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
        final List<HighlightDefinition> sortedDefinitions = definitions.values().stream().sorted(COMPARATOR_HIGHLIGHT_DEFINITION).collect(Collectors.toList());
        SwingUtilities.invokeLater(()-> {
            for (HighlightDefinition definition : sortedDefinitions)
            {
                final HighlightDefinitionPanel highlightDefinitionPanel = new HighlightDefinitionPanel(definition);
                definitionListPanel.add(highlightDefinitionPanel);
            }
            updateDefinitionPanelSize();
            SwingUtilities.invokeLater(definitionListPanel::updateUI);
        });
    }

    private void setTabInternal(PanelTab tab)
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

    public static void setTab(PanelTab tab)
    {
        instance.setTabInternal(tab);
    }

    private int getInsertIndex(String name)
    {
        name = name.toLowerCase();
        int index = 0;
        for (Component component : definitionListPanel.getComponents())
        {
            if (component instanceof HighlightDefinitionPanel)
            {
                final HighlightDefinitionPanel panel = (HighlightDefinitionPanel) component;
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
