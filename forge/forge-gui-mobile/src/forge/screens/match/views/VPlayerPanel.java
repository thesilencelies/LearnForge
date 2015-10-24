package forge.screens.match.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

import forge.Forge;
import forge.Graphics;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.assets.FSkinColor.Colors;
import forge.game.card.CardView;
import forge.game.player.PlayerView;
import forge.game.zone.ZoneType;
import forge.model.FModel;
import forge.properties.ForgePreferences.FPref;
import forge.screens.match.MatchController;
import forge.screens.match.MatchScreen;
import forge.toolbox.FCardPanel;
import forge.toolbox.FContainer;
import forge.toolbox.FDisplayObject;
import forge.util.Utils;

public class VPlayerPanel extends FContainer {
    private static final FSkinFont LIFE_FONT = FSkinFont.get(18);
    private static final FSkinFont INFO_FONT = FSkinFont.get(12);
    private static final FSkinColor INFO_FORE_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor DISPLAY_AREA_BACK_COLOR = FSkinColor.get(Colors.CLR_INACTIVE).alphaColor(0.5f);
    private static final float INFO_TAB_PADDING_X = Utils.scale(2);
    private static final float INFO_TAB_PADDING_Y = Utils.scale(2);

    private final PlayerView player;
    private final VPhaseIndicator phaseIndicator;
    private final VField field;
    private final VAvatar avatar;
    private final VZoneDisplay commandZone;
    private final LifeLabel lblLife;
    private final InfoTab tabManaPool;
    private final Map<ZoneType, InfoTab> zoneTabs = new HashMap<ZoneType, InfoTab>();
    private final List<InfoTab> tabs = new ArrayList<InfoTab>();
    private InfoTab selectedTab;

    public VPlayerPanel(PlayerView player0, boolean showHand) {
        player = player0;
        phaseIndicator = add(new VPhaseIndicator());

        field = add(new VField(player));
        avatar = add(new VAvatar(player));
        lblLife = add(new LifeLabel());
        addZoneDisplay(ZoneType.Hand, FSkinImage.HAND);
        addZoneDisplay(ZoneType.Graveyard, FSkinImage.GRAVEYARD);
        addZoneDisplay(ZoneType.Library, FSkinImage.LIBRARY);
        addZoneDisplay(ZoneType.Flashback, FSkinImage.FLASHBACK);

        VManaPool manaPool = add(new VManaPool(player));
        tabManaPool = add(new InfoTab(FSkinImage.MANA_X, manaPool));
        tabs.add(tabManaPool);

        addZoneDisplay(ZoneType.Exile, FSkinImage.EXILE);

        commandZone = add(new CommandZoneDisplay(player));

        if (showHand) {
            setSelectedZone(ZoneType.Hand);
        }
    }

    public PlayerView getPlayer() {
        return player;
    }

    public void addZoneDisplay(ZoneType zoneType, FSkinImage tabIcon) {
        VZoneDisplay zoneDisplay = add(new VZoneDisplay(player, zoneType));
        InfoTab zoneTab = add(new InfoTab(tabIcon, zoneDisplay));
        zoneTabs.put(zoneType, zoneTab);
        tabs.add(zoneTab);
    }

    public Iterable<InfoTab> getTabs() {
        return tabs;
    }

    public InfoTab getSelectedTab() {
        return selectedTab;
    }

    public InfoTab getZoneTab(ZoneType zoneType) {
        return zoneTabs.get(zoneType);
    }

    public void setSelectedZone(ZoneType zoneType) {
        setSelectedTab(zoneTabs.get(zoneType));
    }

    public void setSelectedTab(InfoTab selectedTab0) {
        if (selectedTab == selectedTab0) {
            return;
        }

        if (selectedTab != null) {
            selectedTab.displayArea.setVisible(false);
        }

        selectedTab = selectedTab0;

        if (selectedTab != null) {
            selectedTab.displayArea.setVisible(true);
        }

        if (MatchController.getView() != null) { //must revalidate entire screen so panel heights updated
            MatchController.getView().revalidate();
        }
    }

    public InfoTab getManaPoolTab() {
        return tabManaPool;
    }

    public boolean isFlipped() {
        return field.isFlipped();
    }
    public void setFlipped(boolean flipped0) {
        field.setFlipped(flipped0);
    }

    @Override
    public void setRotate180(boolean b0) {
        //only rotate certain parts of panel
        avatar.setRotate180(b0);
        lblLife.setRotate180(b0);
        phaseIndicator.setRotate180(b0);
        for (InfoTab tab : tabs) {
            tab.displayArea.setRotate180(b0);
        }
        field.getRow1().setRotate180(b0);
        field.getRow2().setRotate180(b0);
    }

    public VField getField() {
        return field;
    }

    public VPhaseIndicator getPhaseIndicator() {
        return phaseIndicator;
    }

    public VAvatar getAvatar() {
        return avatar;
    }

    public VZoneDisplay getCommandZone() {
        return commandZone;
    }

    public void updateLife() {
        lblLife.update();
    }

    public void updateManaPool() {
        tabManaPool.update();
    }

    public void updateZone(ZoneType zoneType) {
        if (zoneType == ZoneType.Battlefield ) {
            field.update();
        }
        else if (zoneType == ZoneType.Command) {
            commandZone.update();
        }
        else {
            InfoTab zoneTab = zoneTabs.get(zoneType);
            if (zoneTab != null) { //TODO: Support Ante somehow
                zoneTab.update();
            }

            //update flashback zone when graveyard, library, or exile zones updated
            switch (zoneType) {
            case Graveyard:
            case Library:
            case Exile:
                zoneTabs.get(ZoneType.Flashback).update();
                break;
            default:
                break;
            }
        }
    }

    @Override
    protected void doLayout(float width, float height) {
        if (Forge.isLandscapeMode()) {
            doLandscapeLayout(width, height);
            return;
        }

        //layout for bottom panel by default
        float x = VAvatar.WIDTH;
        float w = width - VAvatar.WIDTH;
        float h = phaseIndicator.getPreferredHeight(w);
        phaseIndicator.setBounds(x, height - h, w, h);

        float y = height - VAvatar.HEIGHT;
        float displayAreaHeight = y / 3;
        y -= displayAreaHeight;
        for (InfoTab tab : tabs) {
            tab.displayArea.setBounds(0, y, width, displayAreaHeight);
        }

        y = height - VAvatar.HEIGHT;
        avatar.setPosition(0, y);

        float lifeLabelWidth = LIFE_FONT.getBounds("99").width * 1.2f; //make just wide enough for 2-digit life totals
        float infoLabelHeight = VAvatar.HEIGHT - phaseIndicator.getHeight();
        lblLife.setBounds(x, y, lifeLabelWidth, infoLabelHeight);
        x += lifeLabelWidth;

        float infoTabWidth = (getWidth() - x) / tabs.size();
        for (InfoTab tab : tabs) {
            tab.setBounds(x, y, infoTabWidth, infoLabelHeight);
            x += infoTabWidth;
        }

        if (selectedTab != null) {
            y -= displayAreaHeight;
        }

        //account for command zone if needed
        int commandZoneCount = commandZone.getCount();
        if (commandZoneCount > 0) {
            float commandZoneHeight = y / 2;
            float commandZoneWidth = Math.min(commandZoneCount, 2) * commandZone.getCardWidth(commandZoneHeight);
            commandZone.setBounds(width - commandZoneWidth, y - commandZoneHeight, commandZoneWidth, commandZoneHeight);

            field.setCommandZoneWidth(commandZoneWidth + 1); //ensure second row of field accounts for width of command zone and its border
        }
        else {
            field.setCommandZoneWidth(0);
        }

        field.setBounds(0, 0, width, y);

        if (isFlipped()) { //flip all positions across x-axis if needed
            for (FDisplayObject child : getChildren()) {
                child.setTop(height - child.getBottom());
            }
        }
    }

    private void doLandscapeLayout(float width, float height) {
        float x = 0;
        float y = 0;
        avatar.setPosition(x, y);
        y += avatar.getHeight();
        lblLife.setBounds(x, y, avatar.getWidth(), LIFE_FONT.getLineHeight());
        y += lblLife.getHeight();
        float infoTabWidth = avatar.getWidth();
        float infoTabHeight = (height - y) / tabs.size();
        for (InfoTab tab : tabs) {
            tab.setBounds(x, y, infoTabWidth, infoTabHeight);
            y += infoTabHeight;
        }
        x = avatar.getRight();
        phaseIndicator.resetFont();
        phaseIndicator.setBounds(x, 0, avatar.getWidth() * 0.6f, height);
        x += phaseIndicator.getWidth();

        float fieldWidth = width - x;
        float displayAreaWidth = height / FCardPanel.ASPECT_RATIO;
        if (selectedTab != null) {
            fieldWidth -= displayAreaWidth;
        }

        //account for command zone if needed
        int commandZoneCount = commandZone.getCount();
        if (commandZoneCount > 0) {
            float commandZoneHeight = height / 2;
            float commandZoneWidth = Math.min(commandZoneCount, 2) * commandZone.getCardWidth(commandZoneHeight);
            commandZone.setBounds(x + fieldWidth - commandZoneWidth, height - commandZoneHeight, commandZoneWidth, commandZoneHeight);
            if (isFlipped()) { //flip across x-axis if needed
                commandZone.setTop(height - commandZone.getBottom());
            }

            field.setCommandZoneWidth(commandZoneWidth + 1); //ensure second row of field accounts for width of command zone and its border
        }
        else {
            field.setCommandZoneWidth(0);
        }

        field.setBounds(x, 0, fieldWidth, height);

        x = width - displayAreaWidth;
        for (InfoTab tab : tabs) {
            tab.displayArea.setBounds(x, 0, displayAreaWidth, height);
        }
    }

    @Override
    public void drawBackground(Graphics g) {
        float y;
        if (selectedTab != null) { //draw background and border for selected zone if needed
            VDisplayArea selectedDisplayArea = selectedTab.displayArea;
            float x = selectedDisplayArea.getLeft();
            float w = selectedDisplayArea.getWidth();
            g.fillRect(DISPLAY_AREA_BACK_COLOR, x, selectedDisplayArea.getTop(), w, selectedDisplayArea.getHeight());

            if (Forge.isLandscapeMode()) {
                g.drawLine(1, MatchScreen.BORDER_COLOR, x, selectedDisplayArea.getTop(), x, selectedDisplayArea.getBottom());
            }
            else {
                y = isFlipped() ? selectedDisplayArea.getTop() + 1 : selectedDisplayArea.getBottom();
                //leave gap at selected zone tab
                g.drawLine(1, MatchScreen.BORDER_COLOR, x, y, selectedTab.getLeft(), y);
                g.drawLine(1, MatchScreen.BORDER_COLOR, selectedTab.getRight(), y, w, y);
            }
        }
        if (commandZone.isVisible()) { //draw border for command zone if needed
            float x = commandZone.getLeft();
            y = commandZone.getTop();
            g.drawLine(1, MatchScreen.BORDER_COLOR, x, y, x, y + commandZone.getHeight());
            if (isFlipped()) {
                y += commandZone.getHeight();
            }
            g.drawLine(1, MatchScreen.BORDER_COLOR, x, y, x + commandZone.getWidth(), y);
        }
    }

    private class LifeLabel extends FDisplayObject {
        private int life = player.getLife();
        private int poisonCounters = player.getPoisonCounters();
        private String lifeStr = String.valueOf(life);

        private LifeLabel() {
        }

        private void update() {
            int vibrateDuration = 0;
            int delta = player.getLife() - life;
            if (delta != 0) {
                if (delta < 0) {
                    //TODO: Show animation on avatar for life loss
                    vibrateDuration += delta * -100;
                }
                life = player.getLife();
                lifeStr = String.valueOf(life);
            }

            delta = player.getPoisonCounters() - poisonCounters;
            if (delta != 0) {
                if (delta > 0) {
                    //TODO: Show animation on avatar for gaining poison counters
                    vibrateDuration += delta * 200;
                }
                poisonCounters = player.getPoisonCounters();
            }

            //when gui player loses life, vibrate device for a length of time based on amount of life lost
            if (vibrateDuration > 0 && MatchController.instance.isLocalPlayer(player) &&
                    FModel.getPreferences().getPrefBoolean(FPref.UI_VIBRATE_ON_LIFE_LOSS)) {
                //never vibrate more than two seconds regardless of life lost or poison counters gained
                Gdx.input.vibrate(Math.min(vibrateDuration, 2000)); 
            }
        }

        @Override
        public boolean tap(float x, float y, int count) {
            MatchController.instance.getGameController().selectPlayer(player, null); //treat tapping on life the same as tapping on the avatar
            return true;
        }

        @Override
        public void draw(Graphics g) {
            if (poisonCounters == 0) {
                g.drawText(lifeStr, LIFE_FONT, INFO_FORE_COLOR, 0, 0, getWidth(), getHeight(), false, HAlignment.CENTER, true);
            }
            else {
                float halfHeight = getHeight() / 2;
                float textStart = halfHeight + Utils.scale(1);
                float textWidth = getWidth() - textStart;
                g.drawImage(FSkinImage.QUEST_LIFE, 0, 0, halfHeight, halfHeight);
                g.drawText(lifeStr, INFO_FONT, INFO_FORE_COLOR, textStart, 0, textWidth, halfHeight, false, HAlignment.CENTER, true);
                g.drawImage(FSkinImage.POISON, 0, halfHeight, halfHeight, halfHeight);
                g.drawText(String.valueOf(poisonCounters), INFO_FONT, INFO_FORE_COLOR, textStart, halfHeight, textWidth, halfHeight, false, HAlignment.CENTER, true);
            }
        }
    }

    public class InfoTab extends FDisplayObject {
        private String value = "0";
        private final FSkinImage icon;
        private final VDisplayArea displayArea;

        private InfoTab(FSkinImage icon0, VDisplayArea displayArea0) {
            icon = icon0;
            displayArea = displayArea0;
        }

        public FSkinImage getIcon() {
            return icon;
        }
        public VDisplayArea getDisplayArea() {
            return displayArea;
        }

        @Override
        public boolean tap(float x, float y, int count) {
            if (selectedTab == this) {
                setSelectedTab(null);
            }
            else {
                setSelectedTab(this);
            }
            return true;
        }

        public void update() {
            displayArea.update();
            value = String.valueOf(displayArea.getCount());
        }

        @Override
        public void draw(Graphics g) {
            float x, y, w, h;

            if (selectedTab == this) {
                y = 0;
                w = getWidth();
                h = getHeight();
                float yAcross;
                if (isFlipped()) {
                    y += INFO_TAB_PADDING_Y;
                    yAcross = y;
                    y--;
                    h++;
                }
                else {
                    h -= INFO_TAB_PADDING_Y;
                    yAcross = h;
                    y--;
                    h += 2;
                }
                g.fillRect(DISPLAY_AREA_BACK_COLOR, 0, isFlipped() ? INFO_TAB_PADDING_Y : 0, w, getHeight() - INFO_TAB_PADDING_Y);
                if (!Forge.isLandscapeMode()) {
                    if (isFlipped()) { //use clip to ensure all corners connect
                        g.startClip(-1, y, w + 2, h);
                    }
                    else {
                        g.startClip(-1, y, w + 2, yAcross - y);
                    }
                    g.drawLine(1, MatchScreen.BORDER_COLOR, 0, yAcross, w, yAcross);
                    g.drawLine(1, MatchScreen.BORDER_COLOR, 0, y, 0, h);
                    g.drawLine(1, MatchScreen.BORDER_COLOR, w, y, w, h);
                    g.endClip();
                }
            }

            //show image left of text if wider than tall
            if (getWidth() > getHeight()) {
                float maxImageWidth = getWidth() - INFO_FONT.getBounds("0").width - 3 * INFO_TAB_PADDING_X;
                w = icon.getNearestHQWidth(maxImageWidth);
                if (w > maxImageWidth) {
                    w /= 2;
                }
                h = icon.getHeight() * w / icon.getWidth();
                float maxImageHeight = getHeight() - 2 * INFO_TAB_PADDING_Y;
                if (h > maxImageHeight) {
                    h = icon.getNearestHQHeight(maxImageHeight);
                    if (h > maxImageWidth) {
                        h /= 2;
                    }
                    w = icon.getWidth() * h / icon.getHeight();
                }
                x = INFO_TAB_PADDING_X + (maxImageWidth - w) / 2;
                y = (getHeight() - h) / 2;
                if (lblLife.getRotate180()) {
                    g.startRotateTransform(x + w / 2, y + h / 2, 180);
                }
                g.drawImage(icon, x, y, w, h);
                if (lblLife.getRotate180()) {
                    g.endTransform();
                }

                x += w + INFO_TAB_PADDING_X;
                HAlignment alignX = HAlignment.LEFT;
                if (lblLife.getRotate180()) {
                    g.startRotateTransform(x + (getWidth() - x + 1) / 2, getHeight() / 2, 180);
                    alignX = HAlignment.RIGHT;
                }
                g.drawText(value, INFO_FONT, INFO_FORE_COLOR, x, 0, getWidth() - x + 1, getHeight(), false, alignX, true);
                if (lblLife.getRotate180()) {
                    g.endTransform();
                }
            }
            else { //show image above text if taller than wide
                if (lblLife.getRotate180()) {
                    g.startRotateTransform(getWidth() / 2, getHeight() / 2, 180);
                }
                float maxImageWidth = getWidth() - 2 * INFO_TAB_PADDING_X;
                w = icon.getNearestHQWidth(maxImageWidth);
                if (w > maxImageWidth) {
                    w /= 2;
                }
                h = icon.getHeight() * w / icon.getWidth();
                x = (getWidth() - w) / 2;
                y = INFO_TAB_PADDING_Y;
                g.drawImage(icon, x, y, w, h);

                y += h + INFO_TAB_PADDING_Y;
                g.drawText(value, INFO_FONT, INFO_FORE_COLOR, 0, y, getWidth(), getHeight() - y + 1, false, HAlignment.CENTER, false);
                if (lblLife.getRotate180()) {
                    g.endTransform();
                }
            }
        }
    }

    private class CommandZoneDisplay extends VZoneDisplay {
        private CommandZoneDisplay(PlayerView player0) {
            super(player0, ZoneType.Command);
        }

        @Override
        protected void refreshCardPanels(Iterable<CardView> model) {
            int oldCount = getCount();
            super.refreshCardPanels(model);
            int newCount = getCount();
            if (newCount != oldCount) {
                setVisible(newCount > 0);
                VPlayerPanel.this.revalidate(); //need to revalidated entire panel when command zone size changes
            }
        }

        @Override
        protected boolean layoutVerticallyForLandscapeMode() {
            return false;
        }
    }
}
