package forge.screens.match;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Rectangle;
import com.google.common.collect.Maps;

import forge.Forge;
import forge.Forge.KeyInputAdapter;
import forge.Graphics;
import forge.animation.AbilityEffect;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinTexture;
import forge.game.GameEntityView;
import forge.game.GameView;
import forge.game.card.CardView;
import forge.game.combat.CombatView;
import forge.game.phase.PhaseType;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.interfaces.IGameController;
import forge.menu.FDropDown;
import forge.menu.FDropDownMenu;
import forge.menu.FMenuBar;
import forge.menu.FMenuItem;
import forge.menu.FMenuTab;
import forge.model.FModel;
import forge.player.PlayerZoneUpdate;
import forge.properties.ForgePreferences;
import forge.properties.ForgePreferences.FPref;
import forge.screens.FScreen;
import forge.screens.match.views.VAvatar;
import forge.screens.match.views.VCardDisplayArea.CardAreaPanel;
import forge.screens.match.views.VDevMenu;
import forge.screens.match.views.VGameMenu;
import forge.screens.match.views.VLog;
import forge.screens.match.views.VManaPool;
import forge.screens.match.views.VPhaseIndicator.PhaseLabel;
import forge.screens.match.views.VPlayerPanel;
import forge.screens.match.views.VPlayerPanel.InfoTab;
import forge.screens.match.views.VPlayers;
import forge.screens.match.views.VPrompt;
import forge.screens.match.views.VStack;
import forge.sound.MusicPlaylist;
import forge.sound.SoundSystem;
import forge.toolbox.FCardPanel;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FScrollPane;
import forge.util.Callback;

public class MatchScreen extends FScreen {
    public static FSkinColor BORDER_COLOR = FSkinColor.get(Colors.CLR_BORDERS);

    private final Map<PlayerView, VPlayerPanel> playerPanels = Maps.newHashMap();
    private final VGameMenu gameMenu;
    private final VPlayers players;
    private final VLog log;
    private final VStack stack;
    private final VDevMenu devMenu;
    private final FieldScroller scroller;
    private final VPrompt bottomPlayerPrompt, topPlayerPrompt;
    private VPlayerPanel bottomPlayerPanel, topPlayerPanel;
    private AbilityEffect activeEffect;

    public MatchScreen(List<VPlayerPanel> playerPanels0) {
        super(new FMenuBar());

        scroller = add(new FieldScroller());

        for (VPlayerPanel playerPanel : playerPanels0) {
            playerPanels.put(playerPanel.getPlayer(), scroller.add(playerPanel));
        }
        bottomPlayerPanel = playerPanels0.get(0);
        topPlayerPanel = playerPanels0.get(1);
        topPlayerPanel.setFlipped(true);

        bottomPlayerPrompt = add(new VPrompt("", "",
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        getGameController().selectButtonOk();
                    }
                },
                new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        getGameController().selectButtonCancel();
                    }
                }));

        if (MatchController.instance.getLocalPlayerCount() <= 1 || MatchController.instance.hotSeatMode()) {
            topPlayerPrompt = null;
        }
        else { //show top prompt if multiple human players and not playing in Hot Seat mode
            topPlayerPrompt = add(new VPrompt("", "",
                    new FEventHandler() {
                        @Override
                        public void handleEvent(FEvent e) {
                            getGameController().selectButtonOk();
                        }
                    },
                    new FEventHandler() {
                        @Override
                        public void handleEvent(FEvent e) {
                            getGameController().selectButtonCancel();
                        }
                    }));
            topPlayerPrompt.setRotate180(true);
            topPlayerPanel.setRotate180(true);
            getHeader().setRotate90(true);
        }

        gameMenu = new VGameMenu();
        gameMenu.setDropDownContainer(this);
        players = new VPlayers();
        players.setDropDownContainer(this);
        log = new VLog();
        log.setDropDownContainer(this);
        devMenu = new VDevMenu();
        devMenu.setDropDownContainer(this);
        stack = new VStack();
        stack.setDropDownContainer(this);

        FMenuBar menuBar = (FMenuBar)getHeader();
        if (topPlayerPrompt == null) {
            menuBar.addTab("Game", gameMenu);
            menuBar.addTab("Players (" + playerPanels.size() + ")", players);
            menuBar.addTab("Log", log);
            menuBar.addTab("Dev", devMenu);
            menuBar.addTab("Stack (0)", stack);
        }
        else {
            menuBar.addTab("\u2022 \u2022 \u2022", new PlayerSpecificMenu(true));
            stack.setRotate90(true);
            menuBar.addTab("Stack (0)", stack);
            menuBar.addTab("\u2022 \u2022 \u2022", new PlayerSpecificMenu(false));

            //create fake menu tabs for other drop downs so they can be positioned as needed
            gameMenu.setMenuTab(new HiddenMenuTab(gameMenu));
            players.setMenuTab(new HiddenMenuTab(players));
            log.setMenuTab(new HiddenMenuTab(log));
            devMenu.setMenuTab(new HiddenMenuTab(devMenu));
        }
    }

    private IGameController getGameController() {
        return MatchController.instance.getGameController();
    }

    private class HiddenMenuTab extends FMenuTab {
        private HiddenMenuTab(FDropDown dropDown0) {
            super(null, null, dropDown0, -1);
            setVisible(false);
        }
        @Override
        public void setText(String text0) {
            //avoid trying to set text for this tab
        }
    }

    private class PlayerSpecificMenu extends FDropDownMenu {
        private PlayerSpecificMenu(boolean forTopPlayer) {
            setRotate180(forTopPlayer);
        }

        @Override
        protected void updateSizeAndPosition() {
            Rectangle menuTabPos = getMenuTab().screenPos;
            FScreen screen = Forge.getCurrentScreen();
            float maxWidth = screen.getWidth() - menuTabPos.width;
            float maxHeight = screen.getHeight() / 2;

            paneSize = updateAndGetPaneSize(maxWidth, maxHeight);

            //round width and height so borders appear properly
            paneSize = new ScrollBounds(Math.round(paneSize.getWidth()), Math.round(paneSize.getHeight()));

            float x = maxWidth - paneSize.getWidth();
            float y = getRotate180() ? menuTabPos.y + FMenuTab.PADDING : menuTabPos.y + menuTabPos.height - paneSize.getHeight() - FMenuTab.PADDING + 1;
            setBounds(Math.round(x), Math.round(y), paneSize.getWidth(), paneSize.getHeight());
        }

        private class MenuItem extends FMenuItem {
            private MenuItem(String text0, final FDropDown dropDown) {
                super(text0, new FEventHandler() {
                    @Override
                    public void handleEvent(FEvent e) {
                        dropDown.setRotate180(PlayerSpecificMenu.this.getRotate180());
                        Rectangle menuScreenPos = PlayerSpecificMenu.this.screenPos;
                        if (dropDown.getRotate180()) {
                            dropDown.getMenuTab().screenPos.setPosition(menuScreenPos.x + menuScreenPos.width, menuScreenPos.y);
                        }
                        else {
                            dropDown.getMenuTab().screenPos.setPosition(menuScreenPos.x + menuScreenPos.width, menuScreenPos.y + menuScreenPos.height);
                        }
                        dropDown.show();
                    }
                });
            }
        }

        @Override
        protected void buildMenu() {
            if (isTopHumanPlayerActive() == getRotate180()) {
                addItem(new MenuItem("Game", gameMenu));
                addItem(new MenuItem("Players (" + playerPanels.size() + ")", players));
                addItem(new MenuItem("Log", log));
                if (ForgePreferences.DEV_MODE) {
                    addItem(new MenuItem("Dev", devMenu));
                }
            }
            else { //TODO: Support using menu when player doesn't have priority
                FMenuItem item = new FMenuItem("Must wait for priority...", null);
                item.setEnabled(false);
                addItem(item);
            }
        }
    }

    @Override
    public void onActivate() {
        //update dev menu visibility here so returning from Settings screen allows update
        if (topPlayerPrompt == null) {
            devMenu.getMenuTab().setVisible(ForgePreferences.DEV_MODE);
        }
    }

    public boolean isTopHumanPlayerActive() {
        return topPlayerPrompt != null && topPlayerPanel.getPlayer() == MatchController.instance.getCurrentPlayer();
    }

    public VPrompt getActivePrompt() {
        if (isTopHumanPlayerActive()) {
            return topPlayerPrompt;
        }
        return bottomPlayerPrompt;
    }

    public VPrompt getPrompt(PlayerView playerView) {
        if (topPlayerPrompt == null || bottomPlayerPanel.getPlayer() == playerView) {
            return bottomPlayerPrompt;
        }
        return topPlayerPrompt;
    }

    public VLog getLog() {
        return log;
    }

    public VStack getStack() {
        return stack;
    }

    public VPlayerPanel getTopPlayerPanel() {
        return topPlayerPanel;
    }

    public VPlayerPanel getBottomPlayerPanel() {
        return bottomPlayerPanel;
    }

    public Map<PlayerView, VPlayerPanel> getPlayerPanels() {
        return playerPanels;
    }

    @Override
    public void onClose(Callback<Boolean> canCloseCallback) {
        MatchController.writeMatchPreferences();
        SoundSystem.instance.setBackgroundMusic(MusicPlaylist.MENUS);
        super.onClose(canCloseCallback);
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        float scrollerWidth = width;
        if (topPlayerPrompt != null) {
            topPlayerPrompt.setBounds(0, 0, width, VPrompt.HEIGHT);
            float menuBarWidth = getHeader().getHeight();
            float menuBarHeight = height - 2 * VPrompt.HEIGHT;
            getHeader().setBounds(width - menuBarHeight, height - VPrompt.HEIGHT, menuBarHeight, menuBarWidth); //adjust position prior to rotate transform
            startY = VPrompt.HEIGHT;
            scrollerWidth -= menuBarWidth;
        }
        scroller.setBounds(0, startY, scrollerWidth, height - VPrompt.HEIGHT - startY);
        bottomPlayerPrompt.setBounds(0, height - VPrompt.HEIGHT, width, VPrompt.HEIGHT);
    }

    @Override
    public FScreen getLandscapeBackdropScreen() {
        return null;
    }

    @Override
    public Rectangle getDropDownBoundary() {
        if (topPlayerPrompt == null) {
            return new Rectangle(0, 0, getWidth(), getHeight() - VPrompt.HEIGHT); //prevent covering prompt
        }
        return new Rectangle(0, VPrompt.HEIGHT, scroller.getWidth(), getHeight() - 2 * VPrompt.HEIGHT);
    }

    @Override
    protected void drawOverlay(Graphics g) {
        final GameView game = MatchController.instance.getGameView();
        if (game == null) { return; }

        //draw arrows for paired cards
        Set<CardView> pairedCards = new HashSet<CardView>();
        for (VPlayerPanel playerPanel : playerPanels.values()) {
            for (CardView card : playerPanel.getField().getRow1().getOrderedCards()) {
                if (pairedCards.contains(card)) { continue; } //prevent arrows going both ways

                CardView paired = card.getPairedWith();
                if (paired != null) {
                    TargetingOverlay.drawArrow(g, card, paired);
                }
            }
        }

        //draw arrows for combat
        final CombatView combat = game.getCombat();
        if (combat != null) {
            for (final CardView attacker : combat.getAttackers()) {
                //connect each attacker with planeswalker it's attacking if applicable
                final GameEntityView defender = combat.getDefender(attacker);
                if (defender instanceof CardView) {
                    TargetingOverlay.drawArrow(g, attacker, (CardView) defender);
                }
                final Iterable<CardView> blockers = combat.getBlockers(attacker);
                if (blockers != null) {
                    //connect each blocker with the attacker it's blocking
                    for (final CardView blocker : blockers) {
                        TargetingOverlay.drawArrow(g, blocker, attacker);
                    }
                }
                final Iterable<CardView> plannedBlockers = combat.getPlannedBlockers(attacker);
                if (plannedBlockers != null) {
                    //connect each planned blocker with the attacker it's blocking
                    for (final CardView blocker : plannedBlockers) {
                        TargetingOverlay.drawArrow(g, blocker, attacker);
                    }
                }
            }
        }

        if (activeEffect != null) {
            activeEffect.draw(g, 10, 10, 100, 100);
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
        case Keys.ENTER:
        case Keys.SPACE:
            if (getActivePrompt().getBtnOk().trigger()) { //trigger OK on Enter or Space
                return true;
            }
            return getActivePrompt().getBtnCancel().trigger(); //trigger Cancel if can't trigger OK
        case Keys.ESCAPE:
            return getActivePrompt().getBtnCancel().trigger(); //otherwise trigger Cancel
        case Keys.BACK:
            return true; //suppress Back button so it's not bumped when trying to press OK or Cancel buttons
        case Keys.A: //alpha strike on Ctrl+A
            if (KeyInputAdapter.isCtrlKeyDown()) {
                getGameController().alphaStrike();
                return true;
            }
            break;
        case Keys.E: //end turn on Ctrl+E
            if (KeyInputAdapter.isCtrlKeyDown()) {
                getGameController().passPriorityUntilEndOfTurn();
                return true;
            }
            break;
        case Keys.Q: //concede game on Ctrl+Q
            if (KeyInputAdapter.isCtrlKeyDown()) {
                MatchController.instance.concede();
                return true;
            }
            break;
        case Keys.Z: //undo on Ctrl+Z
            if (KeyInputAdapter.isCtrlKeyDown()) {
                getGameController().undoLastAction();
                return true;
            }
            break;
        }
        return super.keyDown(keyCode);
    }

    @Override
    public void showMenu() {
        //don't show menu from this screen since it's too easy to bump the menu button when trying to press OK or Cancel
    }

    public boolean stopAtPhase(final PlayerView turn, final PhaseType phase) {
        final PhaseLabel label = getPlayerPanel(turn).getPhaseIndicator().getLabel(phase);
        return label == null || label.getStopAtPhase();
    }

    public void resetAllPhaseButtons() {
        for (final VPlayerPanel panel : getPlayerPanels().values()) {
            panel.getPhaseIndicator().resetPhaseButtons();
        }
    }

    public VPlayerPanel getPlayerPanel(final PlayerView playerView) {
        return getPlayerPanels().get(playerView);
    }

    public void highlightCard(final CardView c) {
        for (VPlayerPanel playerPanel : getPlayerPanels().values()) {
            for (FCardPanel p : playerPanel.getField().getCardPanels()) {
                if (p.getCard().equals(c)) {
                    p.setHighlighted(true);
                    return;
                }
            }
        }
    }

    public void clearCardHighlights() {
        for (VPlayerPanel playerPanel : getPlayerPanels().values()) {
            for (FCardPanel p : playerPanel.getField().getCardPanels()) {
                p.setHighlighted(false);
            }
        }
    }

    public void updateZones(final Iterable<PlayerZoneUpdate> zonesToUpdate) {
        for (final PlayerZoneUpdate update : zonesToUpdate) {
            final PlayerView owner = update.getPlayer();
            final VPlayerPanel panel = getPlayerPanel(owner);
            for (final ZoneType zone : update.getZones()) {
                panel.updateZone(zone);
            }
        }
    }

    public void updateSingleCard(final CardView card) {
        final CardAreaPanel pnl = CardAreaPanel.get(card);
        if (pnl == null) { return; }
        final ZoneType zone = card.getZone();
        if (zone != null && zone == ZoneType.Battlefield) {
            pnl.updateCard(card);
        }
        else { //ensure card not on battlefield is reset such that it no longer thinks it's on the battlefield
            pnl.setTapped(false);
            pnl.getAttachedPanels().clear();
            pnl.setAttachedToPanel(null);
            pnl.setPrevPanelInStack(null);
            pnl.setNextPanelInStack(null);
        }
    }

    private class FieldScroller extends FScrollPane {
        private float extraHeight = 0;

        @Override
        public void drawBackground(Graphics g) {
            super.drawBackground(g);

            if (FModel.getPreferences().getPrefBoolean(FPref.UI_MATCH_IMAGE_VISIBLE)) {
                float midField = topPlayerPanel.getBottom();
                float x = topPlayerPanel.getField().getLeft();
                float y = midField - topPlayerPanel.getField().getHeight();
                float w = getWidth() - x;

                g.drawImage(FSkinTexture.BG_MATCH, x, y, w, midField + bottomPlayerPanel.getField().getHeight() - y);
            }
        }

        @Override
        public void drawOverlay(Graphics g) {
            float midField = topPlayerPanel.getBottom();
            float x = 0;
            float y = midField - topPlayerPanel.getField().getHeight();
            float w = getWidth();

            //field separator lines
            if (!Forge.isLandscapeMode()) {
                if (topPlayerPanel.getSelectedTab() == null) {
                    y++; //ensure border goes all the way across under avatar
                }
                g.drawLine(1, BORDER_COLOR, 0, y, w, y);
            }

            y = midField - 0.5f;
            g.drawLine(1, BORDER_COLOR, x, y, w, y);

            if (!Forge.isLandscapeMode()) {
                y = midField + bottomPlayerPanel.getField().getHeight();
                g.drawLine(1, BORDER_COLOR, 0, y, w, y);
            }
        }

        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float totalHeight = visibleHeight + extraHeight;

            //determine player panel heights based on visibility of zone displays
            float topPlayerPanelHeight, bottomPlayerPanelHeight;
            if (Forge.isLandscapeMode()) {
                topPlayerPanelHeight = totalHeight / 2;
                bottomPlayerPanelHeight = topPlayerPanelHeight;
            }
            else {
                float cardRowsHeight = totalHeight - 2 * VAvatar.HEIGHT;
                if (topPlayerPanel.getSelectedTab() == null) {
                    if (bottomPlayerPanel.getSelectedTab() != null) {
                        topPlayerPanelHeight = cardRowsHeight * 2f / 5f;
                        bottomPlayerPanelHeight = cardRowsHeight * 3f / 5f;
                    }
                    else {
                        topPlayerPanelHeight = cardRowsHeight / 2f;
                        bottomPlayerPanelHeight = topPlayerPanelHeight;
                    }
                }
                else if (bottomPlayerPanel.getSelectedTab() == null) {
                    topPlayerPanelHeight = cardRowsHeight * 3f / 5f;
                    bottomPlayerPanelHeight = cardRowsHeight * 2f / 5f;
                }
                else {
                    topPlayerPanelHeight = cardRowsHeight / 2f;
                    bottomPlayerPanelHeight = topPlayerPanelHeight;
                }
                topPlayerPanelHeight += VAvatar.HEIGHT;
                bottomPlayerPanelHeight += VAvatar.HEIGHT;
            }

            topPlayerPanel.setBounds(0, 0, visibleWidth, topPlayerPanelHeight);
            bottomPlayerPanel.setBounds(0, totalHeight - bottomPlayerPanelHeight, visibleWidth, bottomPlayerPanelHeight);
            return new ScrollBounds(visibleWidth, totalHeight);
        }

        @Override
        public boolean zoom(float x, float y, float amount) {
            //adjust position for current scroll positions
            float staticHeight = 2 * VAvatar.HEIGHT; //take out avatar rows that don't scale
            float oldScrollHeight = getScrollHeight() - staticHeight;
            float oldScrollTop = getScrollTop();
            y += oldScrollTop - VAvatar.HEIGHT;

            //build map of all horizontal scroll panes and their current scrollWidths and adjusted X values
            Map<FScrollPane, Pair<Float, Float>> horzScrollPanes = new HashMap<FScrollPane, Pair<Float, Float>>();
            backupHorzScrollPanes(topPlayerPanel, x, horzScrollPanes);
            backupHorzScrollPanes(bottomPlayerPanel, x, horzScrollPanes);

            float zoom = oldScrollHeight / (getHeight() - staticHeight);
            extraHeight += amount * zoom; //scale amount by current zoom
            if (extraHeight < 0) {
                extraHeight = 0;
            }
            revalidate(); //apply change in height to all scroll panes

            //adjust scroll top to keep y position the same
            float newScrollHeight = getScrollHeight() - staticHeight;
            float ratio = newScrollHeight / oldScrollHeight;
            float yAfter = y * ratio;
            setScrollTop(oldScrollTop + yAfter - y);

            //adjust scroll left of all horizontal scroll panes to keep x position the same
            float startX = x;
            for (Entry<FScrollPane, Pair<Float, Float>> entry : horzScrollPanes.entrySet()) {
                FScrollPane horzScrollPane = entry.getKey();
                float oldScrollLeft = entry.getValue().getLeft();
                x = startX + oldScrollLeft;
                float xAfter = x * ratio;
                horzScrollPane.setScrollLeft(oldScrollLeft + xAfter - x);
            }

            return true;
        }

        private void backupHorzScrollPanes(VPlayerPanel playerPanel, float x, Map<FScrollPane, Pair<Float, Float>> horzScrollPanes) {
            backupHorzScrollPane(playerPanel.getField().getRow1(), x, horzScrollPanes);
            backupHorzScrollPane(playerPanel.getField().getRow2(), x, horzScrollPanes);
            for (InfoTab tab : playerPanel.getTabs()) {
                if (tab.getDisplayArea() instanceof VManaPool) {
                    continue; //don't include Mana pool in this
                }
                backupHorzScrollPane(tab.getDisplayArea(), x, horzScrollPanes);
            }
            backupHorzScrollPane(playerPanel.getCommandZone(), x, horzScrollPanes);
        }
        private void backupHorzScrollPane(FScrollPane scrollPane, float x, Map<FScrollPane, Pair<Float, Float>> horzScrollPanes) {
            horzScrollPanes.put(scrollPane, Pair.of(scrollPane.getScrollLeft(), scrollPane.getScrollWidth()));
        }
    }
}
