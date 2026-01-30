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
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;
import org.asundr.HighlightDefinition;
import org.asundr.TradeHighlightManager;
import org.asundr.TradeHighlighterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HighlightDefinitionPanel extends JPanel
{

    private static final int SIZE_ITEM_ICON = 48;
    private static final Dimension PREFERRED_SIZE = new Dimension(SIZE_ITEM_ICON, SIZE_ITEM_ICON);

    private final static Border BORDER_EMPTY = BorderFactory.createEmptyBorder(0, 5, 6, 5);
    private final static Border BORDER_NOTIFY = BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR);


    private static final int SIZE_COLOR_SELECTOR = SIZE_ITEM_ICON - 24;
    private static final Dimension COLOR_SELECTOR_SIZE = new Dimension(SIZE_COLOR_SELECTOR, SIZE_COLOR_SELECTOR);
    private static final Color COLOR_BASE = new Color(30,30,30);
    private static final float BACKGROUND_COLOR_ALPHA = 0.02f;

    private static Client client;
    private static ItemManager itemManager;
    private static ColorPickerManager colorPickerManager;

    private final HighlightDefinition definition;

    private final JPanel itemWrapper = new JPanel();


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
        setLayout(new BorderLayout());
        final Dimension PREFERRED_SIZE = new Dimension(TradeHighlighterPluginPanel.PANEL_WIDTH - 8, SIZE_ITEM_ICON);
        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
        setMaximumSize(PREFERRED_SIZE);
//        setBackground(Color.DARK_GRAY);
        Color lerped = TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA);
//        setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA));
        //setBorder(BorderFactory.createLineBorder(Color.BLACK));
        setBorder(BORDER_EMPTY);

        // add item label, item name tooltip
        buildItemLabel();

        JPanel contents = new JPanel();
        //contents.setLayout(new GridLayout());
        contents.setPreferredSize(new Dimension(TradeHighlighterPluginPanel.PANEL_WIDTH, SIZE_ITEM_ICON/2));
        contents.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        add(contents, BorderLayout.CENTER);
        buildItemName(contents);
        buildColorSelector(contents);
        buildNotifyCheckbox(contents);

        buildDeleteButton(tradeHighlightManager);
        // add color select or
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
//        itemLabel.setPreferredSize(PREFERRED_SIZE);
//        itemLabel.setMinimumSize(PREFERRED_SIZE);
        itemLabel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        itemWrapper.add(itemLabel);
        itemWrapper.setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA));
        itemWrapper.setPreferredSize(PREFERRED_SIZE);
        itemWrapper.setMinimumSize(PREFERRED_SIZE);
        add(itemWrapper, BorderLayout.WEST);
    }

    private void buildItemName(JPanel parent)
    {
        final JPanel nameWrapper = new JPanel();
        final JLabel nameLabel = new JLabel(definition.getName());
        nameWrapper.add(nameLabel);
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        Dimension PREFERRED_DIMENSION_TEXT = new Dimension(80, SIZE_ITEM_ICON/2);
        nameLabel.setPreferredSize(PREFERRED_DIMENSION_TEXT);
        nameLabel.setMaximumSize(PREFERRED_DIMENSION_TEXT);
        nameWrapper.setPreferredSize(PREFERRED_DIMENSION_TEXT);
        nameWrapper.setMaximumSize(PREFERRED_DIMENSION_TEXT);
        parent.add(nameWrapper);
    }

    private void buildColorSelector(JPanel parent)
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
                        itemWrapper.setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, c, BACKGROUND_COLOR_ALPHA));
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
        parent.add(colorButton);
    }

    private void buildNotifyCheckbox(JPanel parent)
    {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected(definition.getNotify());
        checkbox.addActionListener(e -> {
            definition.setNotify(checkbox.isSelected());
            TradeHighlighterUtils.saveDefinitions();
        });
        checkbox.setToolTipText("Enable to send notification when this item is offered by the other player");
        checkbox.setBorder(BORDER_NOTIFY);
        checkbox.setBorderPainted(true);
        parent.add(checkbox);
    }

    private void buildDeleteButton(TradeHighlightManager tradeHighlightManager)
    {
        JButton deleteButton = new JButton("X");
        deleteButton.addActionListener(e -> tradeHighlightManager.removeDefinition(definition.getId()));
        deleteButton.setToolTipText("Delete rule for " + definition.getName());
        deleteButton.setBackground(new Color(70, 0 ,0));
        add(deleteButton, BorderLayout.EAST);
    }

    private static AsyncBufferedImage getItemImage(int id)
    {
        return itemManager.getImage(id, 1000000, false);
    }


}
