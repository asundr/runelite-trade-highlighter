package org.asundr;

import net.runelite.api.ItemComposition;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.game.ItemManager;
import org.asundr.ui.SearchItemPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemIdUtils
{
    private static ArrayList<ItemIdNamePair> nonGeItems = new ArrayList<>();

    static final class ItemIdNamePair implements Comparable<ItemIdNamePair>
    {
        int id;
        String name;
        ItemIdNamePair(int id) { this.id = id;}
        ItemIdNamePair(int id, String name) { this.id = id; this.name = name; }
        @Override public int compareTo(ItemIdNamePair o) {
            return name.compareTo(o.name);
        }
        @Override public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (!(obj instanceof ItemIdNamePair))
            {
                return false;
            }
            ItemIdNamePair other = (ItemIdNamePair) obj;
            return other.id == this.id;
        }
        @Override public int hashCode() { return id; }
    }

    // https://oldschool.runescape.wiki/w/Grand_Exchange/Non-tradeable_items as of Feb 1 2026
    private static int[] unhandledItemIds = {
            ItemID.COINS, ItemID.PLATINUM, // Currency
            7954, 369, 357, 323, 20854, 20869, 343, 367, 3383, ItemID.BURNT_CAVE_EEL, 10140, 381, 375, 3148, 7948, 387, 399, ItemID.BURNT_ANGLERFISH, 11938, 393, // burnt seafood
            2311, 2329, 2305, 1903, 2144, 2146, 7222,9982, 3375, 6301, 9990, 2880, 7570, 2426, 2345, 7090, 7092,  7094, 2005, 2013, 6699, 2199, 2247, 5990, 2175, 6303,   // burnt other food
            598, 2532, 2534, 2536, 2538, 2540, 21328, 11217,  // fire arrows
            12728, 13254, 12738, 12732, 11881, 12734, 12736, 13250, 13252, 21698, 21704, 21707, 21701, 11887, 11879, 12730,  // item packs
            4049, 4053, 8936, 4045, 4047, 8938, 4043, 4051,  // Mini games
            10880, 10881, 10878, 10877, 10879, 10882,  // Satchels
            7622, 7624, 7626, 7630, 2391, 4313, 4209, 11173, 11174, 1586, 4490, 4492, 1577, 767, 968, 4462, 4002, 2964, 759, // Quest items
            11266, 600, 583, 27485, 4496, 1575, 22361, 5978, 4291, 4293, 1633, 4073, 25571, 6675, 409, 7934, 956, 27216, 11656, 7658, 5976, 22355, // miscellaneous
            3899, 550, 11171, 27494, 22358, 195, 4289, 4287, 1940, 2959, 2518, 3377, 13563, 733, 27488, 27491, 3224, 3209, 3805, 7738, 966, 1883 // misc continued
    };

    static void rebuildNonGeItemData()
    {
        final ItemManager itemManager = TradeHighlighterUtils.getItemManager();
        nonGeItems = new ArrayList<>();
        final ArrayList<Integer> configIds = Arrays.stream(TradeHighlighterUtils.getConfig().getNonGeIds().split(","))
                .map(String::trim).filter(TradeHighlighterUtils::isInteger).mapToInt(Integer::parseInt)
                .boxed().collect(Collectors.toCollection(ArrayList::new));
        TradeHighlighterUtils.getClientThread().invoke(()->{
            for (final int id : unhandledItemIds)
            {
                final ItemComposition comp = itemManager.getItemComposition(id);
                if (!comp.getName().equals("null"))
                {
                    nonGeItems.add(new ItemIdNamePair(id, comp.getMembersName()));
                }
            }
            for (final int id : configIds)
            {
                final ItemComposition comp = itemManager.getItemComposition(id);
                if (!comp.getName().equals("null"))
                {
                    nonGeItems.add(new ItemIdNamePair(id, comp.getMembersName()));
                }
            }
        });
    }

    public static ArrayList<SearchItemPanel> matchNonGeItems(String query)
    {
        query = query.toLowerCase();
        final ArrayList<SearchItemPanel> matches = new ArrayList<>();
        for (final ItemIdNamePair pair : nonGeItems)
        {
            if (pair.name.toLowerCase().contains(query))
            {
                matches.add(new SearchItemPanel(pair.id, pair.name));
            }
        }
        return matches;
    }
}
