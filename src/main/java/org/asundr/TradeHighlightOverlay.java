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

package org.asundr;

import net.runelite.api.ItemComposition;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;

import java.awt.*;

public class TradeHighlightOverlay extends Overlay
{
    private static final int BOUNDS_OFFSET_X = -8;
    private static final int BOUNDS_OFFSET_Y = -7;
    private static final int BOUNDS_OFFSET_WIDTH = 0;
    private static final int BOUNDS_OFFSET_HEIGHT = 2;

    final private TradeHighlightManager tradeHighlightManager;

    TradeHighlightOverlay(TradeHighlightManager tradeHighlightManager)
    {
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.tradeHighlightManager = tradeHighlightManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        for (final Widget widget : tradeHighlightManager.getHighlighted())
        {
            if (widget == null || widget.getItemId() == -1)
            {
                continue;
            }
            int id = widget.getItemId();
            final ItemComposition comp = TradeHighlighterUtils.getItemManager().getItemComposition(id);
            if (comp.getNote() != -1)
            {
                id = comp.getLinkedNoteId();
            }
            if (tradeHighlightManager.hasDefinition(id))
            {
                final Rectangle bounds = widget.getBounds();
                bounds.x += BOUNDS_OFFSET_X;
                bounds.y +=  BOUNDS_OFFSET_Y;
                bounds.width +=  BOUNDS_OFFSET_WIDTH;
                bounds.height +=  BOUNDS_OFFSET_HEIGHT;
                graphics.setColor(tradeHighlightManager.getDefinition(id).getColor());
                graphics.drawRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        }
        return null;
    }
}
