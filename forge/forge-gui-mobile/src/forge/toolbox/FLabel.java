package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.math.Vector2;

import forge.Graphics;
import forge.UiCommand;
import forge.assets.FImage;
import forge.assets.FSkinColor;
import forge.assets.FSkinProp;
import forge.assets.TextRenderer;
import forge.assets.FSkinColor.Colors;
import forge.assets.FSkinFont;
import forge.interfaces.IButton;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FEvent.FEventType;
import forge.util.Utils;

public class FLabel extends FDisplayObject implements IButton {
    public static final float DEFAULT_INSETS = Utils.scale(3);

    public static class Builder {
        //========== Default values for FLabel are set here.
        private float      bldIconScaleFactor = 0.8f;
        private FSkinFont  bldFont            = FSkinFont.get(14);
        private float      bldAlphaComposite  = 0.7f;
        private HAlignment bldAlignment       = HAlignment.LEFT;
        private Vector2    bldInsets          = new Vector2(DEFAULT_INSETS, DEFAULT_INSETS);

        private boolean bldSelectable       = false;
        private boolean bldSelected         = false;
        private boolean bldOpaque           = false;
        private boolean bldIconInBackground = false;
        private boolean bldIconScaleAuto    = true;
        private boolean bldEnabled          = true;
        private boolean bldParseSymbols     = true;

        private String bldText;
        private FImage bldIcon;
        private FSkinColor bldTextColor = DEFAULT_TEXT_COLOR;
        private FSkinColor bldPressedColor;
        private FEventHandler bldCommand;

        public FLabel build() { return new FLabel(this); }

        // Begin builder methods.
        public Builder text(final String s0) { this.bldText = s0; return this; }
        public Builder icon(final FImage i0) { this.bldIcon = i0; return this; }
        public Builder align(final HAlignment a0) { this.bldAlignment = a0; return this; }
        public Builder insets(final Vector2 v0) { this.bldInsets = v0; return this; }
        public Builder opaque(final boolean b0) { this.bldOpaque = b0; return this; }
        public Builder opaque() { opaque(true); return this; }
        public Builder selectable(final boolean b0) { this.bldSelectable = b0; return this; }
        public Builder selectable() { selectable(true); return this; }
        public Builder selected(final boolean b0) { this.bldSelected = b0; return this; }
        public Builder selected() { selected(true); return this; }
        public Builder command(final FEventHandler c0) { this.bldCommand = c0; return this; }
        public Builder font(final FSkinFont f0) { this.bldFont = f0; return this; }
        public Builder alphaComposite(final float a0) { this.bldAlphaComposite = a0; return this; }
        public Builder enabled(final boolean b0) { this.bldEnabled = b0; return this; }
        public Builder iconScaleAuto(final boolean b0) { this.bldIconScaleAuto = b0; return this; }
        public Builder iconScaleFactor(final float f0) { this.bldIconScaleFactor = f0; return this; }
        public Builder iconInBackground(final boolean b0) { this.bldIconInBackground = b0; return this; }
        public Builder iconInBackground() { iconInBackground(true); return this; }
        public Builder textColor(final FSkinColor c0) { this.bldTextColor = c0; return this; }
        public Builder pressedColor(final FSkinColor c0) { this.bldPressedColor = c0; return this; }
        public Builder parseSymbols() { parseSymbols(true); return this; }
        public Builder parseSymbols(final boolean b0) { this.bldParseSymbols = b0; return this; }
    }

    // sets better defaults for button labels
    public static class ButtonBuilder extends Builder {
        public ButtonBuilder() {
            opaque();
            align(HAlignment.CENTER);
        }
    }

    public static void drawButtonBackground(Graphics g, float w, float h, boolean pressed) {
        if (pressed) {
            g.fillGradientRect(d50, d10, true, 0, 0, w, h);
            g.drawRect(BORDER_THICKNESS, d50, 0, 0, w, h);
        }
        else {
            g.fillGradientRect(d10, l20, true, 0, 0, w, h);
            g.drawRect(BORDER_THICKNESS, d10, 0, 0, w, h);
        }
    }

    public static final FSkinColor DEFAULT_TEXT_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    public static final FSkinColor INLINE_LABEL_COLOR = DEFAULT_TEXT_COLOR.alphaColor(0.7f);
    private static final FSkinColor clrMain = FSkinColor.get(Colors.CLR_INACTIVE);
    private static final FSkinColor d50 = clrMain.stepColor(-50);
    private static final FSkinColor d30 = clrMain.stepColor(-30);
    private static final FSkinColor d10 = clrMain.stepColor(-10);
    private static final FSkinColor l10 = clrMain.stepColor(10);
    private static final FSkinColor l20 = clrMain.stepColor(20);
    public static final float BORDER_THICKNESS = Utils.scale(1);

    private float iconScaleFactor;
    private FSkinFont font;
    private float alphaComposite;
    private HAlignment alignment;
    private Vector2 insets;
    private boolean selectable, selected, opaque, iconInBackground, iconScaleAuto, pressed;

    private String text;
    private FImage icon;
    private FSkinColor textColor, pressedColor;
    private FEventHandler command;
    private TextRenderer textRenderer;

    // Call this using FLabel.Builder()...
    protected FLabel(final Builder b0) {
        iconScaleFactor = b0.bldIconScaleFactor;
        font = b0.bldFont;
        alphaComposite = b0.bldAlphaComposite;
        alignment = b0.bldAlignment;
        insets = b0.bldInsets;
        selectable = b0.bldSelectable;
        selected = b0.bldSelected;
        opaque = b0.bldOpaque;
        iconInBackground = b0.bldIconInBackground;
        iconScaleAuto = b0.bldIconScaleAuto;
        text = b0.bldText != null ? b0.bldText : "";
        icon = b0.bldIcon;
        textColor = b0.bldTextColor;
        pressedColor = b0.bldPressedColor;
        command = b0.bldCommand;
        if (b0.bldParseSymbols) {
            textRenderer = new TextRenderer();
        }
        setEnabled(b0.bldEnabled);
    }

    public boolean isSelected() {
        return selected;
    }
    public void setSelected(final boolean b0) {
        selected = b0;
    }

    public String getText() {
        return text;
    }
    public void setText(final String text0) {
        text = text0;
    }

    public FSkinColor getTextColor() {
        return textColor;
    }
    public void setTextColor(final FSkinColor textColor0) {
        textColor = textColor0;
    }

    public FSkinFont getFont() {
        return font;
    }
    public void setFont(FSkinFont font0) {
        font = font0;
    }

    public FImage getIcon() {
        return icon;
    }
    public void setIcon(final FImage icon0) {
        icon = icon0;
    }

    public boolean getIconScaleAuto() {
        return iconScaleAuto;
    }
    public void setIconScaleAuto(boolean b0) {
        iconScaleAuto = b0;
    }

    public Vector2 getInsets() {
        return insets;
    }
    public void setInsets(Vector2 insets0) {
        insets = insets0;
    }

    public HAlignment getAlignment() {
        return alignment;
    }
    public void setAlignment(final HAlignment alignment0) {
        alignment = alignment0;
    }

    public void setCommand(final FEventHandler command0) {
        command = command0;
    }

    public float getAlphaComposite() {
        return alphaComposite;
    }

    public boolean isPressed() {
        return pressed;
    }

    @Override
    public final boolean press(float x, float y) {
        if (opaque || selectable || pressedColor != null) {
            pressed = true;
            return true;
        }
        return false;
    }

    @Override
    public final boolean release(float x, float y) {
        if (pressed) {
            pressed = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean tap(float x, float y, int count) {
        boolean handled = false;
        if (selectable) {
            setSelected(!selected);
            handled = true;
        }
        if (command != null) {
            command.handleEvent(new FEvent(this, FEventType.TAP));
            handled = true;
        }
        return handled;
    }

    public boolean trigger() {
        if (isEnabled() && command != null) {
            command.handleEvent(new FEvent(this, FEventType.TAP));
            return true;
        }
        return false;
    }

    public TextBounds getAutoSizeBounds() {
        TextBounds bounds;
        if (text.isEmpty()) {
            bounds = new TextBounds();
            bounds.height += font.getLineHeight();
        }
        else {
            bounds = font.getMultiLineBounds(text);
            bounds.height += font.getLineHeight() - font.getCapHeight(); //account for height below baseline of final line
        }
        bounds.width += 2 * insets.x;
        bounds.height += 2 * insets.y;

        if (icon != null) {
            bounds.width += icon.getWidth() + insets.x + getExtraGapBetweenIconAndText();
        }

        return bounds;
    }

    @Override
    public void draw(Graphics g) {
        float w = getWidth();
        float h = getHeight();

        g.startClip(0, 0, w, h); //start clip to ensure nothing escapes bounds

        boolean applyAlphaComposite = (opaque && !pressed && isEnabled());
        if (applyAlphaComposite) {
            g.setAlphaComposite(alphaComposite);
        }

        if (pressed) {
            if (pressedColor != null) {
                g.fillRect(pressedColor, 0, 0, w, h);
            }
            else {
                g.fillGradientRect(d50, d10, true, 0, 0, w, h);
                g.drawRect(BORDER_THICKNESS, d50, 0, 0, w, h);
            }
        }
        else if (selected && (opaque || selectable)) {
            g.fillGradientRect(d30, l10, true, 0, 0, w, h);
            g.drawRect(BORDER_THICKNESS, d30, 0, 0, w, h);
        }
        else if (opaque) {
            g.fillGradientRect(d10, l20, true, 0, 0, w, h);
            g.drawRect(BORDER_THICKNESS, d10, 0, 0, w, h);
        }
        else if (selectable) {
            g.drawRect(BORDER_THICKNESS, l10, 0, 0, w, h);
        }

        drawContent(g, w, h, pressed);

        if (applyAlphaComposite) {
            g.resetAlphaComposite();
        }

        g.endClip();
    }

    protected void drawContent(Graphics g, float w, float h, final boolean pressed) {
        float x = insets.x;
        float y = insets.y;
        w -= 2 * x;
        h -= 2 * y;
        if (pressed) { //while pressed, translate graphics so icon and text appear shifted down and to the right
            x += Utils.scale(1);
            y += Utils.scale(1);
        }

        if (icon != null) {
            float iconWidth = icon.getWidth();
            float iconHeight = icon.getHeight();
            float aspectRatio = iconWidth / iconHeight;

            if (iconInBackground || iconScaleAuto) {
                iconHeight = h * iconScaleFactor;
                iconWidth = iconHeight * aspectRatio;
            }

            float iconOffset = iconWidth + insets.x + getExtraGapBetweenIconAndText();

            if (iconInBackground || text.isEmpty()) {
                if (alignment == HAlignment.CENTER) {
                    x += (w - iconWidth) / 2;
                }
                y += (h - iconHeight) / 2;
            }
            else {
                if (alignment == HAlignment.CENTER) {
                    float dx;
                    while (true) {
                        dx = (w - iconOffset - getTextWidth()) / 2;
                        if (dx > 0) {
                            x += dx;
                            break;
                        }
                        if (!font.canShrink()) {
                            break;
                        }
                        font = font.shrink();
                    }
                }
                else if (alignment == HAlignment.RIGHT) {
                    float dx;
                    while (true) {
                        dx = (w - iconWidth - getTextWidth() - insets.x);
                        if (dx > 0) {
                            x += dx;
                            break;
                        }
                        if (!font.canShrink()) {
                            break;
                        }
                        font = font.shrink();
                    }
                }
                y += (h - iconHeight) / 2;
            }

            g.drawImage(icon, x, y, iconWidth, iconHeight);

            if (!text.isEmpty()) {
                y = insets.y;
                if (pressed) {
                    y++;
                }
                x += iconOffset;
                w -= iconOffset;

                drawText(g, x, y, w, h, HAlignment.LEFT);
            }
        }
        else if (!text.isEmpty()) {
            drawText(g, x, y, w, h, alignment);
        }
    }

    private void drawText(Graphics g, float x, float y, float w, float h, HAlignment align) {
        g.startClip(x, y, w, h);
        if (textRenderer == null) {
            g.drawText(text, font, textColor, x, y, w, h, false, align, true);
        }
        else {
            textRenderer.drawText(g, text, font, textColor, x, y, w, h, y, h, false, align, true);
        }
        g.endClip();
    }

    private float getTextWidth() {
        if (textRenderer == null) {
            return font.getMultiLineBounds(text).width;
        }
        return textRenderer.getBounds(text, font).width;
    }

    //use FEventHandler one except when references as IButton
    @Override
    public void setCommand(final UiCommand command0) {
        setCommand(new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                command0.run();
            }
        });
    }

    @Override
    public boolean requestFocusInWindow() {
        return false;
    }

    protected float getExtraGapBetweenIconAndText() {
        return 0;
    }

    @Override
    public void setImage(FSkinProp color) {
        setTextColor(FSkinColor.get(Colors.fromSkinProp(color)));
    }

    @Override
    public void setTextColor(int r, int g, int b) {
        setTextColor(FSkinColor.getStandardColor(r, g, b));
    }
}
