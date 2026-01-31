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
import org.asundr.TradeHighlightManager;
import org.asundr.TradeHighlighterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SearchItemPanel extends JButton
{
    private final static Border BORDER_EMPTY = BorderFactory.createEmptyBorder(0, 0, 6, 0);
    final static int SIZE_VERTICAL = 42;
    final static int SIZE_HORIZONTAL = TradeHighlighterPluginPanel.SCROLL_PANEL_WIDTH;


    public static TradeHighlightManager tradeHighlightManager;
    static TradeHighlighterPluginPanel mainPanel;

    private final int itemId;
    private final String itemName;
    public SearchItemPanel(int itemId, String itemName)
    {
        this.itemId = itemId;
        this.itemName = itemName;
        buildPanel();
        addActionListener(this::onButtonPressed);
        setBorder(BORDER_EMPTY);
        setBackground(ColorScheme.DARK_GRAY_COLOR);
    }

    private void buildPanel()
    {
        setLayout(new BorderLayout());
        final Dimension PREFERRED_SIZE = new Dimension(SIZE_HORIZONTAL, SIZE_VERTICAL);
        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
        setMaximumSize(PREFERRED_SIZE);
//        setBackground(ColorScheme.DARKER_GRAY_COLOR);
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));

        final JPanel itemWrapper = new JPanel();
        final JLabel itemLabel = new JLabel();
        final AsyncBufferedImage img = TradeHighlighterUtils.getItemManager().getImage(itemId, 1000000, false);
        img.addTo(itemLabel);
        //itemLabel.setToolTipText(itemName);
        itemWrapper.setPreferredSize(new Dimension(SIZE_VERTICAL + 6, SIZE_VERTICAL));
        itemWrapper.setMaximumSize(new Dimension(SIZE_VERTICAL + 6, SIZE_VERTICAL));
        itemWrapper.setMinimumSize(new Dimension(SIZE_VERTICAL + 6, SIZE_VERTICAL));
        itemWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        itemWrapper.add(itemLabel);

        final JPanel nameWrapper = new JPanel();
        nameWrapper.setLayout(new BoxLayout(nameWrapper, BoxLayout.X_AXIS));
        final JLabel nameLabel = new JLabel(itemName);
        nameLabel.setPreferredSize(new Dimension(160, SIZE_VERTICAL));
        nameLabel.setMaximumSize(new Dimension(160, SIZE_VERTICAL));
        nameLabel.setMinimumSize(new Dimension(160, SIZE_VERTICAL));

        nameWrapper.add(nameLabel);
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        nameWrapper.setPreferredSize(new Dimension(160, SIZE_VERTICAL));
        nameWrapper.setMaximumSize(new Dimension(160, SIZE_VERTICAL));
        nameWrapper.setMinimumSize(new Dimension(160, SIZE_VERTICAL));

        setToolTipText("Add definition for " + itemName);

        final JPanel plusWrapper = new JPanel();
        //plusWrapper.setLayout(new BoxLayout(plusWrapper, BoxLayout.X_AXIS));
        final JLabel plusLabel = new JLabel("<html><b>+</b></html>");
        plusLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        plusWrapper.add(plusLabel);
        plusWrapper.setBackground(new Color(0, 90, 0));
        plusWrapper.setPreferredSize(new Dimension(20, SIZE_VERTICAL));

        add(itemWrapper, BorderLayout.WEST);
        add(nameWrapper, BorderLayout.CENTER);
        add(plusWrapper, BorderLayout.EAST);
    }

    protected void onButtonPressed(ActionEvent e)
    {
        HighlightDefinition definition = new HighlightDefinition(itemId, TradeHighlighterUtils.getConfig().defaultColor(), TradeHighlighterUtils.getConfig().defaultNotify());
        definition.setName(itemName);
        tradeHighlightManager.addDefinition(definition);
        mainPanel.setTab(TradeHighlighterPluginPanel.PanelTab.DEFINITIONS);
        setVisible(false);
    }

    public String getItemName() { return itemName; }
    public int getItemId() { return itemId; }

}
