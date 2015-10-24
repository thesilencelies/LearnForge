package forge.screens.home.settings;

import forge.UiCommand;
import forge.assets.FSkinProp;
import forge.gui.SOverlayUtils;
import forge.gui.framework.DragCell;
import forge.gui.framework.DragTab;
import forge.gui.framework.EDocID;
import forge.properties.ForgeConstants;
import forge.screens.home.EMenuGroup;
import forge.screens.home.IVSubmenu;
import forge.screens.home.VHomeUI;
import forge.toolbox.*;
import forge.util.FileUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Assembles Swing components of utilities submenu singleton.
 *
 * <br><br><i>(V at beginning of class name denotes a view class.)</i>
 *
 */
public enum VSubmenuDownloaders implements IVSubmenu<CSubmenuDownloaders> {
    /** */
    SINGLETON_INSTANCE;

    // Fields used with interface IVDoc
    private DragCell parentCell;
    private final DragTab tab = new DragTab("Utilities");

    /** */
    private final JPanel pnlContent = new JPanel(new MigLayout("insets 0, gap 0, wrap, ay center"));
    private final FScrollPane scrContent = new FScrollPane(pnlContent, false);

    private final FLabel btnDownloadSetPics           = _makeButton("Download LQ Set Pictures");
    private final FLabel btnDownloadPics              = _makeButton("Download LQ Card Pictures");
    private final FLabel btnDownloadQuestImages       = _makeButton("Download Quest Images");
    private final FLabel btnDownloadAchievementImages = _makeButton("Download Achievement Images");
    private final FLabel btnReportBug                 = _makeButton("Report a Bug");
    private final FLabel btnImportPictures            = _makeButton("Import Data");
    private final FLabel btnHowToPlay                 = _makeButton("How To Play");
    private final FLabel btnDownloadPrices            = _makeButton("Download Card Prices");
    private final FLabel btnLicensing                 = _makeButton("License Details");

    /**
     * Constructor.
     */
    VSubmenuDownloaders() {
        final String constraintsLBL = "w 90%!, h 20px!, center, gap 0 0 3px 8px";
        final String constraintsBTN = "h 30px!, w 50%!, center";

        pnlContent.setOpaque(false);

        pnlContent.add(btnDownloadPics, constraintsBTN);
        pnlContent.add(_makeLabel("Download default card picture for each card."), constraintsLBL);

        pnlContent.add(btnDownloadSetPics, constraintsBTN);
        pnlContent.add(_makeLabel("Download all pictures of each card (one for each set the card appeared in)"), constraintsLBL);

        pnlContent.add(btnDownloadQuestImages, constraintsBTN);
        pnlContent.add(_makeLabel("Download tokens and icons used in Quest mode."), constraintsLBL);

        pnlContent.add(btnDownloadAchievementImages, constraintsBTN);
        pnlContent.add(_makeLabel("Download achievement images to really make your trophies stand out."), constraintsLBL);

        pnlContent.add(btnDownloadPrices, constraintsBTN);
        pnlContent.add(_makeLabel("Download up-to-date price list for in-game card shops."), constraintsLBL);

        pnlContent.add(btnImportPictures, constraintsBTN);
        pnlContent.add(_makeLabel("Import data from a local directory."), constraintsLBL);

        pnlContent.add(btnReportBug, constraintsBTN);
        pnlContent.add(_makeLabel("Something broken?"), constraintsLBL);

        pnlContent.add(btnHowToPlay, constraintsBTN);
        pnlContent.add(_makeLabel("Rules of the Game."), constraintsLBL);

        pnlContent.add(btnLicensing, constraintsBTN);
        pnlContent.add(_makeLabel("Forge legal."), constraintsLBL);
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#populate()
     */
    @Override
    public void populate() {
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().removeAll();

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().setLayout(new MigLayout("insets 0, gap 0"));
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().add(scrContent, "w 98%!, h 98%!, gap 1% 0 1% 0");

        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().revalidate();
        VHomeUI.SINGLETON_INSTANCE.getPnlDisplay().repaintSelf();
    }

    /* (non-Javadoc)
     * @see forge.view.home.IViewSubmenu#getGroup()
     */
    @Override
    public EMenuGroup getGroupEnum() {
        return EMenuGroup.SETTINGS;
    }

    public void setDownloadPicsCommand(UiCommand command)              { btnDownloadPics.setCommand(command);        }
    public void setDownloadSetPicsCommand(UiCommand command)           { btnDownloadSetPics.setCommand(command);     }
    public void setDownloadQuestImagesCommand(UiCommand command)       { btnDownloadQuestImages.setCommand(command); }
    public void setDownloadAchievementImagesCommand(UiCommand command) { btnDownloadAchievementImages.setCommand(command); }
    public void setReportBugCommand(UiCommand command)                 { btnReportBug.setCommand(command);           }
    public void setImportPicturesCommand(UiCommand command)            { btnImportPictures.setCommand(command);      }
    public void setHowToPlayCommand(UiCommand command)                 { btnHowToPlay.setCommand(command);           }
    public void setDownloadPricesCommand(UiCommand command)            { btnDownloadPrices.setCommand(command);      }
    public void setLicensingCommand(UiCommand command)                 { btnLicensing.setCommand(command);           }

    public void focusTopButton() {
        btnDownloadPics.requestFocusInWindow();
    }

    private void _showDialog(Component c, final Runnable onShow) {
        JPanel overlay = FOverlay.SINGLETON_INSTANCE.getPanel();
        overlay.setLayout(new MigLayout("insets 0, gap 0, ax center, ay center"));

        FPanel p = new FPanel(new MigLayout("insets dialog, wrap, center"));
        p.setOpaque(false);
        p.setBackgroundTexture(FSkin.getIcon(FSkinProp.BG_TEXTURE));

        final FButton btnClose = new FButton("OK");
        btnClose.addActionListener(new ActionListener() { @Override
            public void actionPerformed(final ActionEvent arg0) { SOverlayUtils.hideOverlay(); } });

        p.add(c, "w 500!");
        p.add(btnClose, "w 200!, h pref+12!, center, gaptop 30");
        overlay.add(p, "gap 0 0 10% 10%");
        SOverlayUtils.showOverlay();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (null != onShow) {
                    onShow.run();
                }
                btnClose.requestFocusInWindow();
            }
        });
    }

    public void showLicensing() {
        String license = "<html>Forge License Information<br><br>"
                + "This program is free software : you can redistribute it and/or modify "
                + "it under the terms of the GNU General Public License as published by "
                + "the Free Software Foundation, either version 3 of the License, or "
                + "(at your option) any later version.<br><br>"
                + "This program is distributed in the hope that it will be useful, "
                + "but WITHOUT ANY WARRANTY; without even the implied warranty of "
                + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the "
                + "GNU General Public License for more details.<br><br>"
                + "You should have received a copy of the GNU General Public License "
                + "along with this program.  If not, see http://www.gnu.org/licenses/.</html>";

        FLabel licenseLabel = new FLabel.Builder().text(license).fontSize(15).build();

        _showDialog(licenseLabel, null);
    }

    public void showHowToPlay() {
        FTextArea directions = new FTextArea(FileUtil.readFileToString(ForgeConstants.HOWTO_FILE));
        final FScrollPane scr = new FScrollPane(directions, false, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        _showDialog(scr, new Runnable() {
            @Override public void run() { scr.getViewport().setViewPosition(new Point(0, 0)); }
        });
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getMenuTitle()
     */
    @Override
    public String getMenuTitle() {
        return "Content Downloaders";
    }

    /* (non-Javadoc)
     * @see forge.gui.home.IVSubmenu#getItemEnum()
     */
    @Override
    public EDocID getItemEnum() {
        return EDocID.HOME_UTILITIES;
    }

    //========== Overridden from IVDoc

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getDocumentID()
     */
    @Override
    public EDocID getDocumentID() {
        return EDocID.HOME_UTILITIES;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getTabLabel()
     */
    @Override
    public DragTab getTabLabel() {
        return tab;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getLayoutControl()
     */
    @Override
    public CSubmenuDownloaders getLayoutControl() {
        return CSubmenuDownloaders.SINGLETON_INSTANCE;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#setParentCell(forge.gui.framework.DragCell)
     */
    @Override
    public void setParentCell(DragCell cell0) {
        this.parentCell = cell0;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.IVDoc#getParentCell()
     */
    @Override
    public DragCell getParentCell() {
        return parentCell;
    }

    private static FLabel _makeButton(String label) {
        return new FLabel.Builder().opaque().hoverable().text(label).fontSize(14).build();
    }

    private static FLabel _makeLabel(String label) {
        return new FLabel.Builder().fontAlign(SwingConstants.CENTER)
                .text(label).fontStyle(Font.ITALIC).build();
    }
}
