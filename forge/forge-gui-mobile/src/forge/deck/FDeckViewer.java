package forge.deck;

import forge.Forge;
import forge.assets.FImage;
import forge.assets.FSkin;
import forge.assets.FSkinImage;
import forge.assets.FTextureRegionImage;
import forge.deck.CardPool;
import forge.deck.Deck;
import forge.deck.DeckSection;
import forge.item.PaperCard;
import forge.itemmanager.CardManager;
import forge.itemmanager.ItemManagerConfig;
import forge.itemmanager.filters.ItemFilter;
import forge.menu.FMenuItem;
import forge.menu.FPopupMenu;
import forge.screens.FScreen;
import forge.screens.match.MatchController;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FOptionPane;

import java.util.Map.Entry;

public class FDeckViewer extends FScreen {
    private static FDeckViewer deckViewer;
    private static FPopupMenu menu = new FPopupMenu() {
        @Override
        protected void buildMenu() {
            Deck deck = deckViewer.deck;
            for (Entry<DeckSection, CardPool> entry : deck) {
                final DeckSection section = entry.getKey();
                final CardPool pool = entry.getValue();
                int count = pool.countAll();
                if (count == 0) { continue; }

                final String captionPrefix;
                final FImage icon;
                switch (section) {
                default:
                case Main:
                    captionPrefix = "Main Deck";
                    icon = FDeckEditor.MAIN_DECK_ICON;
                    break;
                case Sideboard:
                    captionPrefix = "Sideboard";
                    icon = FDeckEditor.SIDEBOARD_ICON;
                    break;
                case Commander:
                    captionPrefix = "Commander";
                    icon = FSkinImage.PLANESWALKER;
                    break;
                case Avatar:
                    captionPrefix = "Avatar";
                    icon = new FTextureRegionImage(FSkin.getAvatars().get(0));
                    break;
                case Planes:
                    captionPrefix = "Planes";
                    icon = FSkinImage.CHAOS;
                    break;
                case Schemes:
                    captionPrefix = "Schemes";
                    icon = FSkinImage.POISON;
                    break;
                }

                FMenuItem item = new FMenuItem(captionPrefix + " (" + count + ")", icon, new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        deckViewer.setCurrentSection(section);
                    }
                });
                if (section == deckViewer.currentSection) {
                    item.setSelected(true);
                }
                addItem(item);
            }
            addItem(new FMenuItem("Copy to Clipboard", new FEventHandler() {
                @Override
                public void handleEvent(FEvent e) {
                    copyDeckToClipboard(deckViewer.deck);
                }
            }));
        }
    };

    public static void copyDeckToClipboard(Deck deck) {
        final String nl = System.getProperty("line.separator");
        final StringBuilder deckList = new StringBuilder();
        final String dName = deck.getName();
        deckList.append(dName == null ? "" : dName + nl + nl);

        for (DeckSection s : DeckSection.values()){
            CardPool cp = deck.get(s);
            if (cp == null || cp.isEmpty()) {
                continue;
            }
            deckList.append(s.toString()).append(": ");
            if (s.isSingleCard()) {
                deckList.append(cp.get(0).getName()).append(nl);
            }
            else {
                deckList.append(nl);
                for (final Entry<PaperCard, Integer> ev : cp) {
                    deckList.append(ev.getValue()).append(" ").append(ev.getKey()).append(nl);
                }
            }
            deckList.append(nl);
        }

        Forge.getClipboard().setContents(deckList.toString());
        FOptionPane.showMessageDialog("Deck list for '" + deck.getName() + "' copied to clipboard.");
    }

    private final Deck deck;
    private final CardManager cardManager;
    private DeckSection currentSection;

    public static void show(final Deck deck0) {
        if (deck0 == null) { return; }

        deckViewer = new FDeckViewer(deck0);
        deckViewer.setRotate180(MatchController.getView() != null && MatchController.getView().isTopHumanPlayerActive());
        Forge.openScreen(deckViewer);
    }

    private FDeckViewer(Deck deck0) {
        super(new MenuHeader(deck0.getName(), menu) {
            @Override
            protected boolean displaySidebarForLandscapeMode() {
                return false;
            }
        });
        deck = deck0;
        cardManager = new CardManager(false);
        cardManager.setPool(deck.getMain());

        currentSection = DeckSection.Main;
        updateCaption();

        add(cardManager);

        cardManager.setup(ItemManagerConfig.DECK_VIEWER);
    }

    private void setCurrentSection(DeckSection currentSection0) {
        if (currentSection == currentSection0) { return; }
        currentSection = currentSection0;
        cardManager.setPool(deck.get(currentSection));
        updateCaption();
    }

    private void updateCaption() {
        cardManager.setCaption(currentSection.name());
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        float x = 0;
        if (Forge.isLandscapeMode()) { //add some horizontal padding in landscape mode
            x = ItemFilter.PADDING;
            width -= 2 * x;
        }
        cardManager.setBounds(x, startY, width, height - startY);
    }

    @Override
    public FScreen getLandscapeBackdropScreen() {
        return null; //never use backdrop for editor
    }
}
