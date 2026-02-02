package org.asundr;

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.ui.overlay.WidgetItemOverlay;

import java.awt.*;

public class HighlightOfferOverlay extends WidgetItemOverlay
{
    private static TradeHighlightManager tradeManager;

    HighlightOfferOverlay(TradeHighlightManager tradeManager)
    {
        showOnInterfaces(InterfaceID.TRADEMAIN);
        HighlightOfferOverlay.tradeManager = tradeManager;
    }

    @Override
    public void renderItemOverlay(Graphics2D graphics, int itemId, WidgetItem widgetItem)
    {
        if (tradeManager == null)
        {
            return;
        }
        if (widgetItem.getWidget().getParentId() != InterfaceID.Trademain.OTHER_OFFER)
        {
            return;
        }
        itemId = TradeHighlighterUtils.getUnnotedId(itemId);
        if (tradeManager.hasDefinition(itemId))
        {
            final HighlightDefinition definition = tradeManager.getDefinition(itemId);
            final Rectangle bounds = widgetItem.getCanvasBounds();
            bounds.y += -2;
            bounds.height += 2;
            bounds.x += -1;
            bounds.width += 1;
            graphics.setColor(definition.getColor());
            graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}
