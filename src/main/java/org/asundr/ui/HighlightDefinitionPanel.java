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
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;
import net.runelite.client.util.AsyncBufferedImage;
import org.asundr.HighlightDefinition;
import org.asundr.TradeHighlighterUtils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HighlightDefinitionPanel extends JPanel
{
    private static final int SIZE_VERTICAL = 40;
    private static final int SPACING_VERTICAL = 6;
    private static final int SIZE_DELETE_X = 20;
    private static final int SIZE_VERTICAL_FULL = SIZE_VERTICAL + SPACING_VERTICAL;
    private static final int SIZE_HORIZONTAL = TradeHighlighterPluginPanel.SIZE_SCROLL_PANEL_WIDTH;
    private static final int SIZE_ITEM_ICON_X = SIZE_VERTICAL + 2;
    private final static int SIZE_IMAGE_PADDING_LEFT = 4;
    private final static int SIZE_IMAGE_PADDING_BOTTOM = 3;
    private static final int SIZE_COLOR_SELECTOR = SIZE_VERTICAL - 16;
    private static final int SIZE_OPTION_X = 25;
    private static final int SIZE_CONTENTS_X = SIZE_HORIZONTAL - SIZE_ITEM_ICON_X - SIZE_DELETE_X;
    private static final int SIZE_NAME_X = SIZE_CONTENTS_X - 2 * SIZE_OPTION_X - 20; // was 85
    private static final int SIZE_NAME_PADDING_BOTTOM = SIZE_VERTICAL / 2 - 2;
    private static final Dimension DIMENSION_COLOR_BUTTON = new Dimension(SIZE_COLOR_SELECTOR, SIZE_COLOR_SELECTOR);
    private static final Dimension DIMENSION_COLOR_BUTTON_WRAPPER = new Dimension(SIZE_OPTION_X, SIZE_VERTICAL);
    private static final Dimension DIMENSION_ITEM_ICON = new Dimension(SIZE_ITEM_ICON_X, SIZE_VERTICAL);
    private static final Dimension DIMENSION_CONTENTS = new Dimension(SIZE_CONTENTS_X, SIZE_VERTICAL/2);
    private static final Dimension DIMENSION_CHECKBOX_WRAPPER = new Dimension(SIZE_OPTION_X, SIZE_VERTICAL);
    private static final Dimension DIMENSION_DELETE_BUTTON = new Dimension(SIZE_DELETE_X, SIZE_VERTICAL_FULL);
    private static final Dimension DIMENSION_PANEL = new Dimension(SIZE_HORIZONTAL, SIZE_VERTICAL_FULL);
    private static final Dimension PREFERRED_DIMENSION_TEXT = new Dimension(SIZE_NAME_X, SIZE_VERTICAL);
    private final static Border BORDER_EMPTY = BorderFactory.createEmptyBorder(SPACING_VERTICAL/2, 0, SPACING_VERTICAL/2, 0);
    private final static Border BORDER_ITEM_PADDING = BorderFactory.createEmptyBorder(0, SIZE_IMAGE_PADDING_LEFT, SIZE_IMAGE_PADDING_BOTTOM, 0);
    private final static Border BORDER_NAME_PADDING = BorderFactory.createEmptyBorder(0, 0, SIZE_NAME_PADDING_BOTTOM, 0);
    private final static Border BORDER_NOTIFY = BorderFactory.createLineBorder(ColorScheme.MEDIUM_GRAY_COLOR);
    private static final Color COLOR_BASE = new Color(30,30,30);
    private static final Color COLOR_DELETE_BUTTON = new Color(70, 0 ,0);
    private static final String TEXT_DELETE_SYMBOL = "×";
    private static final String TEXT_COLOR_PICKER_TITLE = "Trade Highlight";
    private static final String TEMPLATE_TOOLTIP_DELETE = "Delete definition for %s";
    private static final String TOOLTIP_COLOR_BUTTON = "Select the highlight color";
    private static final String TOOLTIP_NOTIFY_CHECKBOX = "Enable to send notification when this item is offered by the other player";
    private static final float BACKGROUND_COLOR_ALPHA = 0.02f;

    //private static final ImageIcon ICON_NOTIFY = TradeHighlighterUtils.getIconFromName("notify.png", 20, 20, Image.SCALE_SMOOTH);

    private static HighlightDefinitionPanel activePanel = null;
    private static JPopupMenu popupMenu = null;
    private final HighlightDefinition definition;
    private final JPanel itemWrapper = new JPanel();
    private final JButton colorButton = new JButton();

    public HighlightDefinitionPanel(HighlightDefinition definition)
    {
        this.definition = definition;
        buildPanel();
    }

    private void buildPanel()
    {
        setLayout(new BorderLayout());
        TradeHighlighterUtils.setFixedSize(this, DIMENSION_PANEL);
        setBorder(BORDER_EMPTY);

        // add item label, item name tooltip
        buildItemLabel();

        // Add item name, color selector, and notify checkbox
        final JPanel contents = new JPanel();
        TradeHighlighterUtils.setFixedSize(contents, DIMENSION_CONTENTS);
        contents.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        buildItemName(contents);
        buildColorSelector(contents);
        buildNotifyCheckbox(contents);
        add(contents, BorderLayout.CENTER);
        contents.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3)
                {
                    activePanel = HighlightDefinitionPanel.this;
                    if (popupMenu == null)
                    {
                        buildPopup();
                    }
                    popupMenu.show(contents, e.getX(), e.getY());
                }
            }
        });

        // Add delete button
        buildDeleteButton();
        setToolTipText(definition.getName());
    }

    private void buildItemLabel()
    {
        final JLabel itemLabel = new JLabel();
        final AsyncBufferedImage img = TradeHighlighterUtils.getItemImage(definition.getId());
        img.addTo(itemLabel);
        itemWrapper.add(itemLabel);
        itemWrapper.setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, definition.getColor(), BACKGROUND_COLOR_ALPHA));
        itemWrapper.setBorder(BORDER_ITEM_PADDING);
        TradeHighlighterUtils.setFixedSize(itemWrapper, DIMENSION_ITEM_ICON);
        add(itemWrapper, BorderLayout.WEST);
    }

    private void buildItemName(JPanel parent)
    {
        final JPanel nameWrapper = new JPanel();
        final JLabel nameLabel = new JLabel(definition.getName());
        nameWrapper.add(nameLabel);
        nameWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        TradeHighlighterUtils.setFixedSize(nameLabel, PREFERRED_DIMENSION_TEXT);
        TradeHighlighterUtils.setFixedSize(nameWrapper, PREFERRED_DIMENSION_TEXT);
        nameLabel.setBorder(BORDER_NAME_PADDING);
        parent.add(nameWrapper);
    }

    private void buildColorSelector(JPanel parent)
    {
        final JPanel colorButtonWrapper = new JPanel();
        colorButtonWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        TradeHighlighterUtils.setFixedSize(colorButtonWrapper, DIMENSION_COLOR_BUTTON_WRAPPER);


        TradeHighlighterUtils.setFixedSize(colorButton, DIMENSION_COLOR_BUTTON);
        colorButton.setBackground(definition.getColor());
        colorButton.setToolTipText(TOOLTIP_COLOR_BUTTON);
        colorButton.addActionListener(event -> SwingUtilities.invokeLater(() ->
        {
            final RuneliteColorPicker colorPicker = TradeHighlighterUtils.createColorPicker(definition.getColor(), TEXT_COLOR_PICKER_TITLE, true);
            colorPicker.setOnClose(c ->
            {
                setColor(c);
                TradeHighlighterUtils.saveDefinitions();
            });
            colorPicker.setLocationRelativeTo(colorButton);
            colorPicker.setVisible(true);
        }));

        colorButtonWrapper.add(colorButton);
        parent.add(colorButtonWrapper);
    }

    private void buildNotifyCheckbox(JPanel parent)
    {
        final JPanel checkboxWrapper = new JPanel();
        checkboxWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        TradeHighlighterUtils.setFixedSize(checkboxWrapper, DIMENSION_CHECKBOX_WRAPPER);

        JCheckBox checkbox = new JCheckBox();
        checkbox.setSelected(definition.getNotify());
        checkbox.addActionListener(e -> {
            definition.setNotify(checkbox.isSelected());
            TradeHighlighterUtils.saveDefinitions();
        });
        checkbox.setBorder(BORDER_NOTIFY);
        checkbox.setBackground(Color.black);
        checkbox.setOpaque(true);
        checkbox.setToolTipText(TOOLTIP_NOTIFY_CHECKBOX);

        checkboxWrapper.add(checkbox);
        parent.add(checkboxWrapper);
    }

    private void buildDeleteButton()
    {
        JButton deleteButton = new JButton(TEXT_DELETE_SYMBOL);
        deleteButton.setPreferredSize(DIMENSION_DELETE_BUTTON);
        deleteButton.addActionListener(e -> TradeHighlighterUtils.getTradeHighlightManager().removeDefinition(definition.getId()));
        deleteButton.setToolTipText(String.format(TEMPLATE_TOOLTIP_DELETE, definition.getName()));
        deleteButton.setBackground(COLOR_DELETE_BUTTON);
        deleteButton.setBorderPainted(false);
        add(deleteButton, BorderLayout.EAST);
    }

    private void buildPopup()
    {
        popupMenu = new JPopupMenu();

        final JMenu copySubmenu = new JMenu("Copy");
        JMenuItem copyColor = new JMenuItem("Color");
        copyColor.addActionListener(e -> TradeHighlighterUtils.copyToClipboard(TradeHighlighterUtils.colorToHexString(activePanel.getDefinition().getColor())));
        copySubmenu.add(copyColor);


        final JMenu pasteSubmenu = new JMenu("Paste");
        JMenuItem pasteColor = new JMenuItem("Color");
        pasteColor.addActionListener(e ->
        {
            final String hexString = TradeHighlighterUtils.getFromClipboard();
            if (hexString != null && hexString.matches("^#[\\da-fA-F]{1,8}$"))
            {
                activePanel.setColor(Color.decode(hexString));
                TradeHighlighterUtils.saveDefinitions();
            }
        });
        pasteSubmenu.add(pasteColor);

        popupMenu.add(copySubmenu);
        popupMenu.add(pasteSubmenu);
    }

    private void setColor(final Color color)
    {
        definition.setColor(color);
        colorButton.setBackground(color);
        itemWrapper.setBackground(TradeHighlighterUtils.lerp(COLOR_BASE, color, BACKGROUND_COLOR_ALPHA));
    }

    public HighlightDefinition getDefinition() { return definition; }
    public static int getFullHeight() { return SIZE_VERTICAL_FULL; }
}
