package forge.quest;

import com.google.common.base.Function;
import forge.GuiBase;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.item.*;
import forge.itemmanager.IItemManager;
import forge.itemmanager.SItemManagerUtil;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.quest.data.QuestPreferences.QPref;
import forge.quest.io.ReadPriceList;
import forge.util.ItemPool;
import forge.util.gui.SOptionPane;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class QuestSpellShop {
    private static Map<String, Integer> mapPrices;
    private static double multiplier;
    private static ItemPool<InventoryItem> decksUsingMyCards;

    public static Integer getCardValue(final InventoryItem card) {
        String ns;
        int value = 1337; // previously this was the returned default
        boolean foil = false;
        int foilMultiplier;

        if (card instanceof PaperCard) {
            ns = card.getName() + "|" + ((PaperCard) card).getEdition();
            foil = ((PaperCard) card).isFoil();
        }
        else {
            ns = card.getName();
        }

        if (mapPrices == null) { //initialize price map if not already done
            mapPrices = new ReadPriceList().getPriceList();
        }
        if (mapPrices.containsKey(ns)) {
            value = mapPrices.get(ns);
        }
        else if (card instanceof PaperCard) {
            switch (((IPaperCard) card).getRarity()) {
                case BasicLand:
                    value = 4;
                    break;
                case Common:
                    value = 6;
                    break;
                case Uncommon:
                    value = 40;
                    break;
                case Rare:
                    value = 120;
                    break;
                case MythicRare:
                    value = 600;
                    break;
                default:
                    value = 15;
                    break;
            }
        } else if (card instanceof BoosterPack) {
            value = 395;
        } else if (card instanceof TournamentPack) {
            value = 995;
        } else if (card instanceof FatPack) {
            value = 2365;
        } else if (card instanceof BoosterBox) {
            value = 8750;
        } else if (card instanceof PreconDeck) {
            value = QuestController.getPreconDeals((PreconDeck) card).getCost();
        }

        // TODO: make this changeable via a user-definable property?
        if (foil) {
            switch (((IPaperCard) card).getRarity()) {
                case BasicLand:
                    foilMultiplier = 2;
                    break;
                case Common:
                    foilMultiplier = 2;
                    break;
                case Uncommon:
                    foilMultiplier = 2;
                    break;
                case Rare:
                    foilMultiplier = 3;
                    break;
                case MythicRare:
                    foilMultiplier = 3;
                    break;
                default:
                    foilMultiplier = 2;
                    break;
            }
            value *= foilMultiplier;
        }

        return value;
    }

    public static final Function<Entry<InventoryItem, Integer>, Comparable<?>> fnPriceCompare = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return getCardValue(from.getKey());
        }
    };
    public static final Function<Entry<? extends InventoryItem, Integer>, Object> fnPriceGet = new Function<Entry<? extends InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<? extends InventoryItem, Integer> from) {
            return getCardValue(from.getKey());
        }
    };
    public static final Function<Entry<? extends InventoryItem, Integer>, Object> fnPriceSellGet = new Function<Entry<? extends InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<? extends InventoryItem, Integer> from) {
            return (int) (multiplier * getCardValue(from.getKey()));
        }
    };
    public static final Function<Entry<InventoryItem, Integer>, Comparable<?>> fnDeckCompare = new Function<Entry<InventoryItem, Integer>, Comparable<?>>() {
        @Override
        public Comparable<?> apply(final Entry<InventoryItem, Integer> from) {
            return decksUsingMyCards.count(from.getKey());
        }
    };
    public static final Function<Entry<? extends InventoryItem, Integer>, Object> fnDeckGet = new Function<Entry<? extends InventoryItem, Integer>, Object>() {
        @Override
        public Object apply(final Entry<? extends InventoryItem, Integer> from) {
            final Integer iValue = decksUsingMyCards.count(from.getKey());
            return iValue.toString();
        }
    };

    public static void buy(Iterable<Entry<InventoryItem, Integer>> items, IItemManager<InventoryItem> shopManager, IItemManager<InventoryItem> inventoryManager, boolean confirmPurchase) {
        long totalCost = 0;
        ItemPool<InventoryItem> itemsToBuy = new ItemPool<>(InventoryItem.class);
        for (Entry<InventoryItem, Integer> itemEntry : items) {
            final InventoryItem item = itemEntry.getKey();
            if (item instanceof PaperCard || item instanceof SealedProduct || item instanceof PreconDeck) {
                final int qty = itemEntry.getValue();
                itemsToBuy.add(item, qty);
                totalCost += qty * QuestSpellShop.getCardValue(item);
            }
        }
        if (itemsToBuy.isEmpty()) { return; }

        List<InventoryItem> itemFlatList = itemsToBuy.toFlatList();
        String suffix = SItemManagerUtil.getItemDisplayString(itemFlatList, 1, true);
        String displayList = SItemManagerUtil.buildDisplayList(itemsToBuy);
        String title = "Buy " + suffix;

        long creditsShort = totalCost - FModel.getQuest().getAssets().getCredits();
        if (creditsShort > 0) {
            SOptionPane.showMessageDialog("You need " + creditsShort + " more credits to purchase the following " + suffix.toLowerCase() + ".\n" + displayList, title);
            return;
        }

        if (confirmPurchase && !SOptionPane.showConfirmDialog("Pay " + totalCost + " credits to purchase the following " +
                suffix.toLowerCase() + "?\n" + displayList, title, "Buy", "Cancel")) {
            return;
        }

        ItemPool<InventoryItem> itemsToAdd = new ItemPool<>(InventoryItem.class);

        for (Entry<InventoryItem, Integer> itemEntry : itemsToBuy) {
            final InventoryItem item = itemEntry.getKey();

            final int qty = itemEntry.getValue();
            final int value = QuestSpellShop.getCardValue(item);

            if (item instanceof PaperCard) {
                FModel.getQuest().getCards().buyCard((PaperCard) item, qty, value);
                itemsToAdd.add(item, qty);
            }
            else if (item instanceof SealedProduct) {
                for (int i = 0; i < qty; i++) {
                    SealedProduct booster = null;
                    if (item instanceof BoosterPack) {
                        booster = (BoosterPack) ((BoosterPack) item).clone();
                        //Replace the booster here if it's a special colored booster
                        //This is to ensure that the correct sets are included in the booster
                        //When loading a quest save, the set information is not available to the booster loader
                        if (SealedProduct.specialSets.contains(booster.getEdition()) || booster.getEdition().equals("?")) {
                            String color = booster.getName().substring(0, booster.getName().indexOf(booster.getItemType()) - 1);
                            booster = new BoosterPack(color, QuestUtilCards.getColoredBoosterTemplate(color));
                        }
                    }
                    else if (item instanceof TournamentPack) {
                        booster = (TournamentPack) ((TournamentPack) item).clone();
                    }
                    else if (item instanceof FatPack) {
                        booster = (FatPack) ((FatPack) item).clone();
                    }
                    else if (item instanceof BoosterBox) {
                        booster = (BoosterBox) ((BoosterBox) item).clone();
                    }
                    FModel.getQuest().getCards().buyPack(booster, value);
                    assert booster != null;
                    final List<PaperCard> newCards = booster.getCards();

                    itemsToAdd.addAllFlat(newCards);

                    if (booster instanceof BoxedProduct && FModel.getPreferences().getPrefBoolean(FPref.UI_OPEN_PACKS_INDIV)) {

                        int totalPacks = ((BoxedProduct) booster).boosterPacksRemaining();
                        boolean skipTheRest = false;
                        final List<PaperCard> remainingCards = new ArrayList<>();

                        while (((BoxedProduct) booster).boosterPacksRemaining() > 0 && !skipTheRest) {
                            skipTheRest = GuiBase.getInterface().showBoxedProduct(booster.getName(), "You have found the following cards inside (Booster Pack " + (totalPacks - ((BoxedProduct) booster).boosterPacksRemaining() + 1) + " of " + totalPacks + "):", ((BoxedProduct) booster).getNextBoosterPack());
                        }

                        if (skipTheRest) {
                            while (((BoxedProduct) booster).boosterPacksRemaining() > 0) {
                                remainingCards.addAll(((BoxedProduct) booster).getNextBoosterPack());
                            }
                        }

                        remainingCards.addAll(((BoxedProduct) booster).getExtraCards());

                        if (!remainingCards.isEmpty()) {
                            GuiBase.getInterface().showCardList(booster.getName(), "You have found the following cards inside:", remainingCards);
                        }

                    }
                    else {
                        GuiBase.getInterface().showCardList(booster.getName(), "You have found the following cards inside:", newCards);
                    }
                }
            }
            else if (item instanceof PreconDeck) {
                final PreconDeck deck = (PreconDeck) item;
                for (int i = 0; i < qty; i++) {
                    FModel.getQuest().getCards().buyPreconDeck(deck, value);

                    itemsToAdd.addAll(deck.getDeck().getMain());
                }

                boolean one = (qty == 1);
                SOptionPane.showMessageDialog(String.format(
                        "%s '%s' %s added to your decklist.%n%n%s cards were also added to your pool.",
                        one ? "Deck" : String.format("%d copies of deck", qty),
                        deck.getName(), one ? "was" : "were", one ? "Its" : "Their"),
                        "Thanks for purchasing!", SOptionPane.INFORMATION_ICON);
            }
        }

        shopManager.removeItems(itemsToBuy);
        inventoryManager.addItems(itemsToAdd);
    }

    public static void sell(Iterable<Entry<InventoryItem, Integer>> items, IItemManager<InventoryItem> shopManager, IItemManager<InventoryItem> inventoryManager, boolean confirmSale) {
        long totalReceived = 0;
        ItemPool<InventoryItem> itemsToSell = new ItemPool<>(InventoryItem.class);
        for (Entry<InventoryItem, Integer> itemEntry : items) {
            final InventoryItem item = itemEntry.getKey();
            if (item instanceof PaperCard) {
                final int qty = itemEntry.getValue();
                itemsToSell.add(item, qty);
                totalReceived += qty * Math.min((int) (multiplier * getCardValue(item)), FModel.getQuest().getCards().getSellPriceLimit());
            }
        }
        if (itemsToSell.isEmpty()) { return; }

        if (confirmSale) {
            List<InventoryItem> itemFlatList = itemsToSell.toFlatList();
            String suffix = SItemManagerUtil.getItemDisplayString(itemFlatList, 1, true);
            String displayList = SItemManagerUtil.buildDisplayList(itemsToSell);
            String title = "Sell " + suffix;

            if (!SOptionPane.showConfirmDialog("Sell the following " + suffix.toLowerCase() + " for " + totalReceived +
                    " credit" + (totalReceived != 1 ? "s" : "") + "?\n" + displayList, title, "Sell", "Cancel")) {
                return;
            }
        }

        for (Entry<InventoryItem, Integer> itemEntry : itemsToSell) {
            final PaperCard card = (PaperCard) itemEntry.getKey();
            final int qty = itemEntry.getValue();
            final int price = Math.min((int) (multiplier * getCardValue(card)), FModel.getQuest().getCards().getSellPriceLimit());

            FModel.getQuest().getCards().sellCard(card, qty, price);
        }

        inventoryManager.removeItems(itemsToSell);
        shopManager.addItems(itemsToSell);
    }

    public static void sellExtras(IItemManager<InventoryItem> shopManager, IItemManager<InventoryItem> inventoryManager) {
        List<Entry<InventoryItem, Integer>> cardsToRemove = new LinkedList<>();
        for (Entry<InventoryItem, Integer> item : inventoryManager.getPool()) {
            PaperCard card = (PaperCard)item.getKey();
            int numToKeep = card.getRules().getType().isBasic() ?
                    FModel.getQuestPreferences().getPrefInt(QPref.PLAYSET_BASIC_LAND_SIZE) : FModel.getQuestPreferences().getPrefInt(QPref.PLAYSET_SIZE);
            if ("Relentless Rats".equals(card.getName()) || "Shadowborn Apostle".equals(card.getName())) {
                numToKeep = FModel.getQuestPreferences().getPrefInt(QPref.PLAYSET_ANY_NUMBER_SIZE);
            }
            if (numToKeep < item.getValue()) {
                cardsToRemove.add(Pair.of(item.getKey(), item.getValue() - numToKeep));
            }
        }

        inventoryManager.removeItems(cardsToRemove);
        shopManager.addItems(cardsToRemove);

        for (Entry<InventoryItem, Integer> item : cardsToRemove) {
            if (!(item.getKey() instanceof PaperCard)) {
                continue;
            }
            PaperCard card = (PaperCard)item.getKey();
            final int price = Math.min((int) (multiplier * getCardValue(card)),
                    FModel.getQuest().getCards().getSellPriceLimit());
            FModel.getQuest().getCards().sellCard(card, item.getValue(), price);
        }
    }

    // fills number of decks using each card
    public static void updateDecksForEachCard() {
        decksUsingMyCards = new ItemPool<>(InventoryItem.class);
        for (final Deck deck : FModel.getQuest().getMyDecks()) {
            CardPool main = deck.getMain();
            for (final Entry<PaperCard, Integer> e : main) {
                decksUsingMyCards.add(e.getKey());
            }
            if (deck.has(DeckSection.Sideboard)) {
                for (final Entry<PaperCard, Integer> e : deck.get(DeckSection.Sideboard)) {
                    // only add card if we haven't already encountered it in main
                    if (!main.contains(e.getKey())) {
                        decksUsingMyCards.add(e.getKey());
                    }
                }
            }
        }
    }

    public static double updateMultiplier() {
        multiplier = FModel.getQuest().getCards().getSellMultiplier();
        return multiplier;
    }
}
