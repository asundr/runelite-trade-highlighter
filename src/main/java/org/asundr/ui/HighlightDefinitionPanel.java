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
    private static final int SIZE_VERTICAL = 40;
    private static final int SPACING_VERTICAL = 6;
    private static final int SIZE_VERTICAL_FULL = SIZE_VERTICAL + SPACING_VERTICAL;
    private static final int SIZE_HORIZONTAL = TradeHighlighterPluginPanel.SCROLL_PANEL_WIDTH;

    private static final int SIZE_ITEM_ICON = SIZE_VERTICAL;
    private static final Dimension DIMENSION_ITEM_ICON = new Dimension(SIZE_ITEM_ICON - 2, SIZE_VERTICAL);

    private final static Border BORDER_EMPTY = BorderFactory.createEmptyBorder(0, 0, SPACING_VERTICAL, 0);
    private final static Border BORDER_NOTIFY = BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR);


    private static final int SIZE_COLOR_SELECTOR = SIZE_ITEM_ICON - 16;
    private static final Dimension COLOR_SELECTOR_SIZE = new Dimension(SIZE_COLOR_SELECTOR, SIZE_COLOR_SELECTOR);
    private static final Color COLOR_BASE = new Color(30,30,30);
    private static final float BACKGROUND_COLOR_ALPHA = 0.02f;

    //private static final ImageIcon ICON_NOTIFY = TradeHighlighterUtils.getIconFromName("notify.png", 20, 20, Image.SCALE_SMOOTH);

    private static Client client;
    private static ItemManager itemManager;
    private static ColorPickerManager colorPickerManager;

    private final HighlightDefinition definition;

    private final JPanel itemWrapper = new JPanel();

    public static int getSizeVertical() { return SIZE_VERTICAL_FULL; }

    public HighlightDefinitionPanel(HighlightDefinition definition, TradeHighlightManager tradeHighlightManager)
    {
        this.definition = definition;
        buildPanel(tradeHighlightManager);
        setBorder(BORDER_EMPTY);
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
        final Dimension PREFERRED_SIZE = new Dimension(SIZE_HORIZONTAL, SIZE_VERTICAL_FULL);
        setPreferredSize(PREFERRED_SIZE);
        setMinimumSize(PREFERRED_SIZE);
        setMaximumSize(PREFERRED_SIZE);

        // add item label, item name tooltip
        buildItemLabel();

        final JPanel contents = new JPanel();
        contents.setPreferredSize(new Dimension(SIZE_HORIZONTAL, SIZE_ITEM_ICON/2));
        contents.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        add(contents, BorderLayout.CENTER);
        buildItemName(contents);
        buildColorSelector(contents);
        buildNotifyCheckbox(contents);

        buildDeleteButton(tradeHighlightManager);
        setToolTipText(definition.getName());
    }

    private void buildItemLabel()
    {
        final JLabel itemLabel = new JLabel();
        final AsyncBufferedImage img = getItemImage(definition.getId());
        img.addTo(itemLabel);
        itemWrapper.add(itemLabel);
        itemWrapper.setPreferredSize(DIMENSION_ITEM_ICON);
        itemWrapper.setMaximumSize(DIMENSION_ITEM_ICON);
        itemWrapper.setMinimumSize(DIMENSION_ITEM_ICON);
        itemWrapper.setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA));
        add(itemWrapper, BorderLayout.WEST);
    }

    private void buildItemName(JPanel parent)
    {
        final JPanel nameWrapper = new JPanel();
        final JLabel nameLabel = new JLabel(definition.getName());
        nameWrapper.add(nameLabel);
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        final Dimension PREFERRED_DIMENSION_TEXT = new Dimension(85, SIZE_VERTICAL);
        nameLabel.setPreferredSize(PREFERRED_DIMENSION_TEXT);
        nameLabel.setMaximumSize(PREFERRED_DIMENSION_TEXT);
        nameLabel.setMinimumSize(PREFERRED_DIMENSION_TEXT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        nameWrapper.setPreferredSize(PREFERRED_DIMENSION_TEXT);
        nameWrapper.setMaximumSize(PREFERRED_DIMENSION_TEXT);
        nameWrapper.setMinimumSize(PREFERRED_DIMENSION_TEXT);
        parent.add(nameWrapper);
    }

    private void buildColorSelector(JPanel parent)
    {
        final JPanel colorButtonWrapper = new JPanel();
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
        final Dimension wrapperDimension = new Dimension(25, SIZE_VERTICAL);
        colorButtonWrapper.setPreferredSize(wrapperDimension);
        colorButtonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        colorButton.setToolTipText("Select the highlight color");
        colorButton.setPreferredSize(COLOR_SELECTOR_SIZE);
        colorButton.setMinimumSize(COLOR_SELECTOR_SIZE);
        colorButton.setMaximumSize(COLOR_SELECTOR_SIZE);

        colorButtonWrapper.add(colorButton);
        parent.add(colorButtonWrapper);
    }

    private void buildNotifyCheckbox(JPanel parent)
    {
        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected(definition.getNotify());
        checkbox.addActionListener(e -> {
            definition.setNotify(checkbox.isSelected());
            TradeHighlighterUtils.saveDefinitions();
        });

        final JPanel checkboxWrapper = new JPanel();
        final Dimension wrapperDimension = new Dimension(25, SIZE_VERTICAL);
        checkboxWrapper.setPreferredSize(wrapperDimension);
        checkboxWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        checkbox.setToolTipText("Enable to send notification when this item is offered by the other player");
        checkbox.setBorder(BORDER_NOTIFY);

        checkbox.setBackground(Color.black);
        checkbox.setOpaque(true);

        checkboxWrapper.add(checkbox);
        parent.add(checkboxWrapper);
    }

    private void buildDeleteButton(TradeHighlightManager tradeHighlightManager)
    {
        JButton deleteButton = new JButton("×");
        deleteButton.setPreferredSize(new Dimension(20, SIZE_VERTICAL_FULL));
        deleteButton.addActionListener(e -> tradeHighlightManager.removeDefinition(definition.getId()));
        deleteButton.setToolTipText("Delete definition for " + definition.getName());
        deleteButton.setBackground(new Color(70, 0 ,0));
        deleteButton.setBorderPainted(false);
        add(deleteButton, BorderLayout.EAST);
    }

    private static AsyncBufferedImage getItemImage(int id)
    {
        return itemManager.getImage(id, 1000000, false);
    }


}
