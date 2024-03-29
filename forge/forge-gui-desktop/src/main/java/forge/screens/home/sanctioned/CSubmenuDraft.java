package forge.screens.home.sanctioned;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import forge.GuiBase;
import forge.Singletons;
import forge.UiCommand;
import forge.deck.Deck;
import forge.deck.DeckGroup;
import forge.deck.DeckProxy;
import forge.game.GameType;
import forge.game.player.RegisteredPlayer;
import forge.gui.GuiChoose;
import forge.gui.SOverlayUtils;
import forge.gui.framework.FScreen;
import forge.gui.framework.ICDoc;
import forge.itemmanager.ItemManagerConfig;
import forge.limited.BoosterDraft;
import forge.limited.LimitedPoolType;
import forge.match.HostedMatch;
import forge.model.FModel;
import forge.player.GamePlayerUtil;
import forge.properties.ForgePreferences.FPref;
import forge.screens.deckeditor.CDeckEditorUI;
import forge.screens.deckeditor.controllers.CEditorDraftingProcess;
import forge.toolbox.FOptionPane;

/**
 * Controls the draft submenu in the home UI.
 *
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 */
@SuppressWarnings("serial")
public enum CSubmenuDraft implements ICDoc {
    SINGLETON_INSTANCE;

    private final UiCommand cmdDeckSelect = new UiCommand() {
        @Override
        public void run() {
            VSubmenuDraft.SINGLETON_INSTANCE.getBtnStart().setEnabled(true);
            fillOpponentComboBox();
        }
    };

    private final ActionListener radioAction = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
            fillOpponentComboBox();
        }
    };

    @Override
    public void register() {
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void initialize() {
        final VSubmenuDraft view = VSubmenuDraft.SINGLETON_INSTANCE;

        view.getLstDecks().setSelectCommand(cmdDeckSelect);

        view.getBtnBuildDeck().setCommand(new UiCommand() {
            @Override
            public void run() {
                setupDraft();
            }
        });

        view.getBtnStart().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                startGame(GameType.Draft);
            }
        });

        view.getRadSingle().addActionListener(radioAction);

        view.getRadAll().addActionListener(radioAction);
    }

    /* (non-Javadoc)
     * @see forge.control.home.IControlSubmenu#update()
     */
    @Override
    public void update() {
        final VSubmenuDraft view = VSubmenuDraft.SINGLETON_INSTANCE;
        final JButton btnStart = view.getBtnStart();

        view.getLstDecks().setPool(DeckProxy.getAllDraftDecks());
        view.getLstDecks().setup(ItemManagerConfig.DRAFT_DECKS);

        if (!view.getLstDecks().getPool().isEmpty()) {
            btnStart.setEnabled(true);
            fillOpponentComboBox();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                if (btnStart.isEnabled()) {
                    view.getBtnStart().requestFocusInWindow();
                } else {
                    view.getBtnBuildDeck().requestFocusInWindow();
                }
            }
        });
    }

    private void startGame(final GameType gameType) {
        final boolean gauntlet = !VSubmenuDraft.SINGLETON_INSTANCE.isSingleSelected();
        final DeckProxy humanDeck = VSubmenuDraft.SINGLETON_INSTANCE.getLstDecks().getSelectedItem();

        if (humanDeck == null) {
            FOptionPane.showErrorDialog("No deck selected for human.\n(You may need to build a new deck)", "No Deck");
            return;
        }

        if (FModel.getPreferences().getPrefBoolean(FPref.ENFORCE_DECK_LEGALITY)) {
            final String errorMessage = gameType.getDeckFormat().getDeckConformanceProblem(humanDeck.getDeck());
            if (null != errorMessage) {
                FOptionPane.showErrorDialog("Your deck " + errorMessage + " Please edit or choose a different deck.", "Invalid Deck");
                return;
            }
        }

        FModel.getGauntletMini().resetGauntletDraft();
        String duelType = (String)VSubmenuDraft.SINGLETON_INSTANCE.getCbOpponent().getSelectedItem();
        final DeckGroup opponentDecks = FModel.getDecks().getDraft().get(humanDeck.getName());
        if (gauntlet) {
            if ("Gauntlet".equals(duelType)) {
                final int rounds = opponentDecks.getAiDecks().size();
                FModel.getGauntletMini().launch(rounds, humanDeck.getDeck(), gameType);
            } else if ("Tournament".equals(duelType)) {
                // TODO Allow for tournament style draft, instead of always a gauntlet
            }
            return;
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.startGameOverlay();
                SOverlayUtils.showOverlay();
            }
        });

        // Restore Zero Indexing
        final int aiIndex = Integer.parseInt(duelType)-1;
        final Deck aiDeck = opponentDecks.getAiDecks().get(aiIndex);
        if (aiDeck == null) {
            throw new IllegalStateException("Draft: Computer deck is null!");
        }

        final List<RegisteredPlayer> starter = new ArrayList<RegisteredPlayer>();
        final RegisteredPlayer human = new RegisteredPlayer(humanDeck.getDeck()).setPlayer(GamePlayerUtil.getGuiPlayer());
        starter.add(human);
        starter.add(new RegisteredPlayer(aiDeck).setPlayer(GamePlayerUtil.createAiPlayer()));
        for (final RegisteredPlayer pl : starter) {
            pl.assignConspiracies();
        }

        final HostedMatch hostedMatch = GuiBase.getInterface().hostMatch();
        hostedMatch.startMatch(GameType.Draft, null, starter, human, GuiBase.getInterface().getNewGuiGame());

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SOverlayUtils.hideOverlay();
            }
        });
    }

    /** */
    private void setupDraft() {
        // Determine what kind of booster draft to run
        final LimitedPoolType poolType = GuiChoose.oneOrNone("Choose Draft Format", LimitedPoolType.values());
        if (poolType == null) { return; }

        final BoosterDraft draft = BoosterDraft.createDraft(poolType);
        if (draft == null) { return; }

        final CEditorDraftingProcess draftController = new CEditorDraftingProcess(CDeckEditorUI.SINGLETON_INSTANCE.getCDetailPicture());
        draftController.showGui(draft);

        Singletons.getControl().setCurrentScreen(FScreen.DRAFTING_PROCESS);
        CDeckEditorUI.SINGLETON_INSTANCE.setEditorController(draftController);
    }

    private void fillOpponentComboBox() {
        final VSubmenuDraft view = VSubmenuDraft.SINGLETON_INSTANCE;
        JComboBox<String> combo = view.getCbOpponent();
        combo.removeAllItems();

        final DeckProxy humanDeck = view.getLstDecks().getSelectedItem();
        if (humanDeck == null) {
            return;
        }

        if (VSubmenuDraft.SINGLETON_INSTANCE.isSingleSelected()) {
            // Single opponent
            final DeckGroup opponentDecks = FModel.getDecks().getDraft().get(humanDeck.getName());
            int indx = 0;
            for (Deck d : opponentDecks.getAiDecks()) {
                indx++;
                // 1-7 instead of 0-6
                combo.addItem(String.valueOf(indx));
            }
        } else {
            // Gauntlet/Tournament
            combo.addItem("Gauntlet");
            //combo.addItem("Tournament");
        }
    }

}
