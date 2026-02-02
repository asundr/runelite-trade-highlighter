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

import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;

import java.awt.*;
import java.util.regex.Pattern;

public class HighlightConfirmOverlay extends Overlay
{
    private static final Pattern p = Pattern.compile("([^<]+)");

    final private TradeHighlightManager tradeHighlightManager;

    HighlightConfirmOverlay(TradeHighlightManager tradeHighlightManager)
    {
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.tradeHighlightManager = tradeHighlightManager;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        final Widget confirmWidgetOther = TradeHighlighterUtils.getWidget(InterfaceID.TRADECONFIRM, 29);
        if (confirmWidgetOther == null)
        {
            return  null;
        }
        for (int i = 0; i < 28; ++i)
        {
            final Widget child = confirmWidgetOther.getChild(i);
            if (child == null)
            {
                continue;
            }
            final java.util.regex.Matcher matcher = p.matcher(child.getText());
            if (matcher.find())
            {
                final String name = matcher.group(1).toLowerCase();
                final HighlightDefinition def = tradeHighlightManager.getOfferedNameMap().get(name);
                if (def != null)
                {
                    final Rectangle bounds = child.getBounds();
                    graphics.setColor(def.getColor());
                    graphics.drawRect(bounds.x - 4, bounds.y - bounds.height - 2, bounds.width - 2, bounds.height - 6);
                }
            }
        }
        return null;
    }
}
