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

import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.AsyncBufferedImage;
import org.asundr.HighlightDefinition;
import org.asundr.TradeHighlighterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SearchItemPanel extends JButton
{
    private static final int SIZE_HORIZONTAL = TradeHighlighterPluginPanel.SIZE_SCROLL_PANEL_WIDTH;
    private static final int SIZE_SPACING = 6;
    private static final int SIZE_VERTICAL = 36;
    private final static int SIZE_VERTICAL_FULL = SIZE_VERTICAL + SIZE_SPACING;
    private final static int SIZE_IMAGE_X = SIZE_VERTICAL_FULL + 6;
    private final static int SIZE_PLUS_X = 20;
    private final static int SIZE_NAME_X = SIZE_HORIZONTAL - SIZE_IMAGE_X - SIZE_PLUS_X;
    private final Dimension DIMENSION_PANEL = new Dimension(SIZE_HORIZONTAL, SIZE_VERTICAL_FULL);
    private final Dimension DIMENSION_IMAGE_ICON = new Dimension(SIZE_IMAGE_X, SIZE_VERTICAL_FULL);
    private final Dimension DIMENSION_PLUS = new Dimension(SIZE_PLUS_X, SIZE_VERTICAL_FULL);
    private final Dimension DIMENSION_ITEM_NAME = new Dimension(SIZE_NAME_X - 15, SIZE_VERTICAL_FULL);
    private final Color COLOR_PLUS_BACKGROUND = new Color(0, 90, 0);
    private final static Border BORDER_PANEL_EMPTY = BorderFactory.createEmptyBorder(0, 0, SIZE_SPACING, 0);
    private final static Border BORDER_PLUS_EMPTY = BorderFactory.createEmptyBorder(5, 0, 5, 0);
    private final static String TEXT_PLUS = "<html><b>+</b></html>";
    private final static String TEMPLATE_PANEL_TOOLTIP = "Add definition for %s";


    private final int itemId;
    private final String itemName;

    public SearchItemPanel(int itemId, String itemName)
    {
        this.itemId = itemId;
        this.itemName = itemName;
        buildPanel();
        addActionListener(this::onButtonPressed);
    }

    private void buildPanel()
    {
        // Root panel setup
        setLayout(new BorderLayout());
        TradeHighlighterUtils.setFixedSize(this, DIMENSION_PANEL);
        setBorder(BORDER_PANEL_EMPTY);
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setToolTipText(String.format(TEMPLATE_PANEL_TOOLTIP, itemName));

        // item image
        final JPanel itemWrapper = new JPanel();
        final JLabel itemLabel = new JLabel();
        final AsyncBufferedImage img = TradeHighlighterUtils.getItemManager().getImage(itemId, Integer.MAX_VALUE, false);
        img.addTo(itemLabel);
        TradeHighlighterUtils.setFixedSize(itemWrapper, DIMENSION_IMAGE_ICON);
        itemWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        itemWrapper.add(itemLabel);

        // item name
        final JPanel nameWrapper = new JPanel();
        final JLabel nameLabel = new JLabel(itemName);
        nameWrapper.setLayout(new BoxLayout(nameWrapper, BoxLayout.X_AXIS));
        nameWrapper.add(nameLabel);
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        TradeHighlighterUtils.setFixedSize(nameWrapper, DIMENSION_ITEM_NAME);
        TradeHighlighterUtils.setFixedSize(nameLabel, DIMENSION_ITEM_NAME);

        // plus panel
        final JPanel plusWrapper = new JPanel();
        final JLabel plusLabel = new JLabel(TEXT_PLUS);
        plusLabel.setBorder(BORDER_PLUS_EMPTY);
        plusWrapper.add(plusLabel);
        plusWrapper.setBackground(COLOR_PLUS_BACKGROUND);
        plusWrapper.setPreferredSize(DIMENSION_PLUS);

        add(itemWrapper, BorderLayout.WEST);
        add(nameWrapper, BorderLayout.CENTER);
        add(plusWrapper, BorderLayout.EAST);
    }

    protected void onButtonPressed(ActionEvent e)
    {
        final HighlightDefinition definition = new HighlightDefinition(itemId, TradeHighlighterUtils.getConfig().defaultColor(), TradeHighlighterUtils.getConfig().defaultNotify());
        definition.setName(itemName);
        TradeHighlighterUtils.getTradeHighlightManager().addDefinition(definition);
        TradeHighlighterPluginPanel.setTab(TradeHighlighterPluginPanel.PanelTab.DEFINITIONS);
        setVisible(false);
    }

    public String getItemName() { return itemName; }
    public int getItemId() { return itemId; }
    public static int getFullHeight() { return SIZE_VERTICAL_FULL; }

}
