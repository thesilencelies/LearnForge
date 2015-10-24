package forge.screens.quest;

import java.util.List;

import forge.assets.FSkinFont;
import forge.assets.FSkinImage;
import forge.interfaces.IButton;
import forge.interfaces.ICheckBox;
import forge.interfaces.IComboBox;
import forge.model.FModel;
import forge.quest.QuestUtil;
import forge.quest.bazaar.QuestItemType;
import forge.quest.bazaar.QuestPetController;
import forge.screens.FScreen;
import forge.toolbox.FCheckBox;
import forge.toolbox.FComboBox;
import forge.toolbox.FDisplayObject;
import forge.toolbox.FEvent;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FLabel;
import forge.toolbox.FOptionPane;
import forge.toolbox.FScrollPane;
import forge.util.Utils;

public class QuestStatsScreen extends FScreen {
    private static final float PADDING = FOptionPane.PADDING;

    private final FScrollPane scroller = add(new FScrollPane() {
        @Override
        protected ScrollBounds layoutAndGetScrollBounds(float visibleWidth, float visibleHeight) {
            float x = PADDING;
            float y = PADDING;
            float w = visibleWidth - 2 * PADDING;
            float h = lblWins.getAutoSizeBounds().height;
            for (FDisplayObject lbl : getChildren()) {
                if (lbl.isVisible()) {
                    lbl.setBounds(x, y, w, lbl.getHeight() == 0 ? h : lbl.getHeight()); //respect height override if set
                    y += lbl.getHeight() + PADDING;
                }
            }
            return new ScrollBounds(visibleWidth, y);
        }
    });
    private final FLabel lblWins = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_PLUS)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FLabel lblLosses = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_MINUS)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FLabel lblCredits = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_COINSTACK)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FLabel lblWinStreak = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_PLUSPLUS)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FLabel lblLife = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_LIFE)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FLabel lblWorld = scroller.add(new FLabel.Builder()
        .icon(FSkinImage.QUEST_MAP)
        .font(FSkinFont.get(16)).iconScaleFactor(1).build());
    private final FComboBox<String> cbxPet = scroller.add(new FComboBox<String>());
    private final FCheckBox cbCharm = scroller.add(new FCheckBox("Use Charm of Vigor"));
    private final FCheckBox cbPlant = scroller.add(new FCheckBox("Summon Plant"));
    private final FLabel lblZep = scroller.add(new FLabel.Builder().text("Launch Zeppelin").icon(FSkinImage.QUEST_ZEP).font(FSkinFont.get(16)).opaque().build());

    public FLabel getLblWins() {
        return lblWins;
    }
    public FLabel getLblLosses() {
        return lblLosses;
    }
    public FLabel getLblCredits() {
        return lblCredits;
    }
    public FLabel getLblWinStreak() {
        return lblWinStreak;
    }
    public FLabel getLblLife() {
        return lblLife;
    }
    public FLabel getLblWorld() {
        return lblWorld;
    }
    public IComboBox<String> getCbxPet() {
        return cbxPet;
    }
    public ICheckBox getCbPlant() {
        return cbPlant;
    }
    public ICheckBox getCbCharm() {
        return cbCharm;
    }
    public IButton getLblZep() {
        return lblZep;
    }

    public QuestStatsScreen() {
        super("Quest Statistics", QuestMenu.getMenu());
        lblZep.setHeight(Utils.scale(60));

        cbxPet.setDropDownChangeHandler(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                final int slot = 1;
                final int index = cbxPet.getSelectedIndex();
                List<QuestPetController> pets = FModel.getQuest().getPetsStorage().getAvaliablePets(slot, FModel.getQuest().getAssets());
                String petName = index <= 0 || index > pets.size() ? null : pets.get(index - 1).getName();
                FModel.getQuest().selectPet(slot, petName);
                FModel.getQuest().save();
            }
        });
        cbCharm.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                FModel.getQuest().setCharmState(cbCharm.isSelected());
                FModel.getQuest().save();
            }
        });
        cbPlant.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                FModel.getQuest().selectPet(0, cbPlant.isSelected() ? "Plant" : null);
                FModel.getQuest().save();
            }
        });
        lblZep.setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (!QuestUtil.checkActiveQuest("Launch a Zeppelin.")) {
                    return;
                }
                FModel.getQuest().getAchievements().setCurrentChallenges(null);
                FModel.getQuest().getAssets().setItemLevel(QuestItemType.ZEPPELIN, 2);
                update();
            }
        });
    }

    @Override
    public void onActivate() {
        update();
    }

    public void update() {
        QuestUtil.updateQuestView(QuestMenu.getMenu());
        setHeaderCaption(FModel.getQuest().getName() + " - Statistics\n(" + FModel.getQuest().getRank() + ")");
        scroller.revalidate(); //revalidate to account for changes in label visibility
    }

    @Override
    protected void doLayout(float startY, float width, float height) {
        scroller.setBounds(0, startY, width, height - startY);
    }
}
