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

import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;
import org.asundr.HighlightDefinition;
import org.asundr.TradeHighlightManager;
import org.asundr.TradeHighlighterUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HighlightDefinitionPanel extends JPanel
{

    private static final int SIZE_ITEM_ICON = 36;
    private static final Dimension PREFERRED_SIZE = new Dimension(SIZE_ITEM_ICON, SIZE_ITEM_ICON);

    private static final int SIZE_COLOR_SELECTOR = SIZE_ITEM_ICON - 12;
    private static final Dimension COLOR_SELECTOR_SIZE = new Dimension(SIZE_COLOR_SELECTOR, SIZE_COLOR_SELECTOR);
    private static final Color COLOR_BASE = new Color(30,30,30);
    private static final float BACKGROUND_COLOR_ALPHA = 0.02f;

    private static Client client;
    private static ItemManager itemManager;
    private static ColorPickerManager colorPickerManager;

    private final HighlightDefinition definition;

    public HighlightDefinitionPanel(HighlightDefinition definition, TradeHighlightManager tradeHighlightManager)
    {
        this.definition = definition;
        buildPanel(tradeHighlightManager);
    }

    public static void initialize(Client client, ItemManager itemManager, ColorPickerManager colorPickerManager)
    {
        HighlightDefinitionPanel.client = client;
        HighlightDefinitionPanel.itemManager = itemManager;
        HighlightDefinitionPanel.colorPickerManager = colorPickerManager;
    }

    public HighlightDefinition getDefinition() { return definition; }

    private void buildPanel(TradeHighlightManager tradeHighlightManager)
    {
        final Dimension PREFERRED_SIZE = new Dimension(TradeHighlighterPluginPanel.PANEL_WIDTH - 8, 48);
        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
        setMaximumSize(PREFERRED_SIZE);
//        setBackground(Color.DARK_GRAY);
        Color lerped = TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA);
        setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA));
        setBorder(BorderFactory.createLineBorder(Color.BLACK));


        // add item label, item name tooltip
        buildItemLabel();
        buildColorSelector();
        buildNotifyCheckbox();
        buildDeleteButton(tradeHighlightManager);
        // add color selector
        // add notification checkbox
        // X icon to delete

        setToolTipText(definition.getName());
    }

    private void buildItemLabel()
    {
        JLabel itemLabel = new JLabel();
        final AsyncBufferedImage img = getItemImage(definition.getId());
        img.addTo(itemLabel);
        itemLabel.setToolTipText(definition.getName());
        itemLabel.setPreferredSize(PREFERRED_SIZE);
        itemLabel.setMinimumSize(PREFERRED_SIZE);
        add(itemLabel);
    }

    private void buildColorSelector()
    {
        JButton colorButton = new JButton();
        colorButton.setBackground(definition.getColor());
        colorButton.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(() ->
                {
                    RuneliteColorPicker colorPicker = colorPickerManager.create(client, definition.getColor(), "Trade Highlight", true);
                    colorPicker.setOnClose(c ->
                    {
                        definition.setColor(c);
                        colorButton.setBackground(c);
                        setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, c, BACKGROUND_COLOR_ALPHA));
                        TradeHighlighterUtils.saveDefinitions();
                    });
                    colorPicker.setLocationRelativeTo(colorButton);
                    colorPicker.setVisible(true);
                });

            }
        });
        colorButton.setToolTipText("Select the highlight color");
        colorButton.setPreferredSize(COLOR_SELECTOR_SIZE);
        colorButton.setMinimumSize(COLOR_SELECTOR_SIZE);
        colorButton.setMaximumSize(COLOR_SELECTOR_SIZE);
        add(colorButton);
    }

    private void buildNotifyCheckbox()
    {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected(definition.getNotify());
        checkbox.addActionListener(e -> {
            definition.setNotify(checkbox.isSelected());
            TradeHighlighterUtils.saveDefinitions();
        });
        checkbox.setToolTipText("Enable to send notification when this item is offered by the other player");
        add(checkbox);
    }

    private void buildDeleteButton(TradeHighlightManager tradeHighlightManager)
    {
        JButton deleteButton = new JButton("X");
        deleteButton.addActionListener(e -> tradeHighlightManager.removeDefinition(definition.getId()));
        deleteButton.setToolTipText("Delete rule for " + definition.getName());
        add(deleteButton);
    }

    private static AsyncBufferedImage getItemImage(int id)
    {
        return itemManager.getImage(id, 1000000, false);
    }


}
