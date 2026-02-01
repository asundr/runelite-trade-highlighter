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


    // https://oldschool.runescape.wiki/w/Grand_Exchange/Non-tradeable_items as of Feb 1 2026
    private final static int[] unhandledItemIds =
    {
        // Currency
        ItemID.COINS,
        ItemID.PLATINUM,

        // burnt seafood
        ItemID.BURNT_SHRIMP,
        ItemID.BURNTFISH5,
        ItemID.BURNTFISH3,
        ItemID.BURNTFISH1,
        ItemID.RAIDS_FISH_BURNT,
        ItemID.RAIDS_BAT_BURNT,
        ItemID.BURNTFISH2,
        ItemID.BURNTFISH4,
        ItemID.BURNT_EEL,
        ItemID.BURNT_CAVE_EEL,
        ItemID.HUNTING_RAW_FISH_SPECIAL_BURNT,
        ItemID.BURNT_LOBSTER,
        ItemID.BURNT_SWORDFISH,
        ItemID.TBWT_BURNT_KARAMBWAN,
        ItemID.BURNT_MONKFISH,
        ItemID.BURNT_SHARK,
        ItemID.BURNT_SEATURTLE,
        ItemID.BURNT_ANGLERFISH,
        ItemID.BURNT_DARK_CRAB,
        ItemID.BURNT_MANTARAY,

        // burnt other food
        ItemID.BURNT_BREAD,
        ItemID.BURNT_PIE,
        ItemID.BURNT_PIZZA,
        ItemID.BURNT_CAKE,
        ItemID.BURNT_CHICKEN,
        ItemID.BURNT_MEAT,
        ItemID.SPIT_BURNED_RABBIT_MEAT,
        ItemID.SPIT_BURNED_BIRD_MEAT,
        ItemID.BURNT_SNAIL,
        ItemID.TBW_SPIDER_ON_STICK_BURNT,
        ItemID.SPIT_BURNED_BEAST_MEAT,
        ItemID.RUINED_CHOMPY,
        ItemID._100_JUBBLY_MEAT_BURNED,
        ItemID.BURNT_UW_OOMLIE,
        ItemID.BURNT_OOMLIE,
        ItemID.BOWL_EGG_BURNT,
        ItemID.BOWL_ONION_BURNT,
        ItemID.BOWL_MUSHROOM_BURNT,
        ItemID.BURNT_STEW,
        ItemID.BURNT_CURRY,
        ItemID.POTATO_BURNT,
        ItemID.BURNT_CRUNCHIES,
        ItemID.BURNT_BATTA,
        ItemID.SWEETCORN_BURNT,
        ItemID.BURNT_GNOMEBOWL,
        ItemID.TBW_SPIDER_ON_SHAFT_BURNT,

        // fire arrows
        ItemID.UNLITARROW,
        ItemID.IRON_UNLITARROW,
        ItemID.STEEL_UNLITARROW,
        ItemID.MITHRIL_UNLITARROW,
        ItemID.ADAMANT_UNLITARROW,
        ItemID.RUNE_UNLITARROW,
        ItemID.AMETHYST_UNLITARROW,
        ItemID.DRAGON_UNLITARROW,

        // item packs
        ItemID.PACK_AIRRUNE,
        ItemID.PACK_BASKET,
        ItemID.PACK_CHAOSRUNE,
        ItemID.PACK_EARTHRUNE,
        ItemID.PACK_FEATHER,
        ItemID.PACK_FIRERUNE,
        ItemID.PACK_MINDRUNE,
        ItemID.PACK_PLANT_POT_COMPOST,
        ItemID.PACK_SACK,
        ItemID.PACK_AIRRUNE_TZHAAR,
        ItemID.PACK_EARTHRUNE_TZHAAR,
        ItemID.PACK_FIRERUNE_TZHAAR,
        ItemID.PACK_WATERRUNE_TZHAAR,
        ItemID.PACK_SLAYER_BROAD_BOLT_UNFINISHED,
        ItemID.PACK_VIAL_WATER,
        ItemID.PACK_WATERRUNE,

        // Mini games
        ItemID.CASTLEWARS_BANDAGES,
        ItemID.CASTLEWARS_BARRICADE,
        ItemID.BREW_BLUE_FLOWER,
        ItemID.CASTLEWARS_EXPLOSIVES_POTION,
        ItemID.CASTLEWARS_CLIMBING_ROPE,
        ItemID.BREW_RED_FLOWER,
        ItemID.CASTLEWARS_CATAPULT_ROCK,
        ItemID.CASTLEWARS_TOOLKIT,

        // Satchels
        ItemID.TOL_BLACK_SACK,
        ItemID.TOL_GOLD_SACK,
        ItemID.TOL_GREEN_SACK,
        ItemID.TOL_PLAIN_SACK,
        ItemID.TOL_RED_SACK,
        ItemID.TOL_RUNE_SACK,

        // Quest items
        ItemID.BURGH_RUBBLE_BUCKET_1,
        ItemID.BURGH_RUBBLE_BUCKET_2,
        ItemID.BURGH_RUBBLE_BUCKET_3,
        ItemID.BURGH_GENERALSTORE_CRATE,
        ItemID.GROUND_BAT_BONES,
        ItemID.ROVING_CRYSTAL_BOOK,
        ItemID.ROVING_CADARN_BOOK,
        ItemID.ARRAVCERTIFICATE_LFT,
        ItemID.ARRAVCERTIFICATE_RHT,
        ItemID.MISC_KEY,
        ItemID.MDAUGHTER_MUD,
        ItemID.MDAUGHTER_ROCK,
        ItemID.PETECANDLESTICK,
        ItemID.PHOENIX_CROSSBOW,
        ItemID.ROCK,
        ItemID.FAVOUR_HERBTEA_RUINED,
        ItemID.MM_REINITIALISATION_HINT,
        ItemID.BOWL_EMPTY_FILLIMAN,
        ItemID.PHOENIXKEY2,

        // miscellaneous
        ItemID.II_ANCHOVY_PASTE,
        ItemID.BOOK_OF_ASTROLOGY,
        ItemID.BUCKET_BAILING,
        ItemID.HW22_TRICK_BANANA,
        ItemID.MDAUGHTER_BROKEN_STICK,
        ItemID.FELINEMEDAL,
        ItemID.EASTER18_HANDEGG_CHAOS,
        ItemID.COCONUT_SHELL,
        ItemID.COOKED_CHICKEN_UNDEAD,
        ItemID.COOKED_MEAT_UNDEAD,
        ItemID.CRUSHED_GEMSTONE,
        ItemID.DAMP_TINDERBOX,
        ItemID.TEMPOROSS_DAMP_EGG,
        ItemID.EMPTY_FISHFOOD_BOX,
        ItemID.OYSTEREMPTY,
        ItemID.PEST_FIELD_RATION,
        ItemID.FLIER,
        ItemID.TOA_LOOT_POO,
        ItemID.OBSERVATORY_GLASSBLOWING_BOOK,
        ItemID.BURGH_UNFINISHED_GUTHIX_BALANCE_1,
        ItemID.COCONUT_HALF,
        ItemID.EASTER18_HANDEGG_LIGHT,
        ItemID.IRON_SICKLE,
        ItemID.NEWCOMER_MAP,
        ItemID.QIP_SOA_NEWSPAPER2,
        ItemID.HW22_TRICK_WOOL,
        ItemID.EASTER18_HANDEGG_BALANCE,
        ItemID.ACNE_POTION,
        ItemID.RAW_CHICKEN_UNDEAD,
        ItemID.RAW_BEEF_UNDEAD,
        ItemID.RAWSWAMPPASTE,
        ItemID.ROTTEN_FOOD,
        ItemID.ROTTEN_TOMATO,
        ItemID.EMPTY_DYE_BOTTLE,
        ItemID.LOVAKENGJ_EQUIPMENT_CRATE,
        ItemID.SMASHED_GLASS,
        ItemID.HW22_TRICK_SOCK,
        ItemID.HW22_TRICK_EGG,
        ItemID.REGICIDE_CLOTH,
        ItemID.REGICIDE_SULPHAR,
        ItemID.VIKING_TANKARD_EMPTY,
        ItemID.POH_TEA_LEAVES,
        ItemID.ROOFTILE,
        ItemID.UGTHANKI_KEBAB_BAD
    };
}
