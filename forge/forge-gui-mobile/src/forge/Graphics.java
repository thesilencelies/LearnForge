package forge;

import java.util.Stack;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import com.badlogic.gdx.graphics.g2d.BitmapFont.TextBounds;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;

import forge.assets.FImage;
import forge.assets.FSkinColor;
import forge.assets.FSkinFont;
import forge.toolbox.FDisplayObject;
import forge.util.Utils;

public class Graphics {
    private static final int GL_BLEND = GL20.GL_BLEND;
    private static final int GL_LINE_SMOOTH = 2848; //create constant here since not in GL20

    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final Stack<Matrix4> transforms = new Stack<Matrix4>();
    private final Vector3 tmp = new Vector3();
    private float regionHeight;
    private Rectangle bounds;
    private Rectangle visibleBounds;
    private int failedClipCount;
    private float alphaComposite = 1;

    public Graphics() {
    }

    public void begin(float regionWidth0, float regionHeight0) {
        batch.begin();
        bounds = new Rectangle(0, 0, regionWidth0, regionHeight0);
        regionHeight = regionHeight0;
        visibleBounds = new Rectangle(bounds);
    }

    public void end() {
        if (batch.isDrawing()) {
            batch.end();
        }
        if (shapeRenderer.getCurrentType() != null) {
            shapeRenderer.end();
        }
    }

    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
    }

    public boolean startClip() {
        return startClip(0, 0, bounds.width, bounds.height);
    }
    public boolean startClip(float x, float y, float w, float h) {
        batch.flush(); //must flush batch to prevent other things not rendering

        Rectangle clip = new Rectangle(adjustX(x), adjustY(y, h), w, h);
        if (!transforms.isEmpty()) { //transform position if needed
            tmp.set(clip.x, clip.y, 0);
            tmp.mul(batch.getTransformMatrix());
            float minX = tmp.x;
            float maxX = minX;
            float minY = tmp.y;
            float maxY = minY;
            tmp.set(clip.x + clip.width, clip.y, 0);
            tmp.mul(batch.getTransformMatrix());
            if (tmp.x < minX) { minX = tmp.x; }
            else if (tmp.x > maxX) { maxX = tmp.x; }
            if (tmp.y < minY) { minY = tmp.y; }
            else if (tmp.y > maxY) { maxY = tmp.y; }
            tmp.set(clip.x + clip.width, clip.y + clip.height, 0);
            tmp.mul(batch.getTransformMatrix());
            if (tmp.x < minX) { minX = tmp.x; }
            else if (tmp.x > maxX) { maxX = tmp.x; }
            if (tmp.y < minY) { minY = tmp.y; }
            else if (tmp.y > maxY) { maxY = tmp.y; }
            tmp.set(clip.x, clip.y + clip.height, 0);
            tmp.mul(batch.getTransformMatrix());
            if (tmp.x < minX) { minX = tmp.x; }
            else if (tmp.x > maxX) { maxX = tmp.x; }
            if (tmp.y < minY) { minY = tmp.y; }
            else if (tmp.y > maxY) { maxY = tmp.y; }

            clip.set(minX, minY, maxX - minX, maxY - minY);
        }
        if (!ScissorStack.pushScissors(clip)) {
            failedClipCount++; //tracked failed clips to prevent calling popScissors on endClip
            return false;
        }
        return true;
    }
    public void endClip() {
        if (failedClipCount == 0) {
            batch.flush(); //must flush batch to ensure stuffed rendered during clip respects that clip
            ScissorStack.popScissors();
        }
        else {
            failedClipCount--;
        }
    }

    public void draw(FDisplayObject displayObj) {
        if (displayObj.getWidth() <= 0 || displayObj.getHeight() <= 0) {
            return;
        }

        final Rectangle parentBounds = bounds;
        bounds = new Rectangle(parentBounds.x + displayObj.getLeft(), parentBounds.y + displayObj.getTop(), displayObj.getWidth(), displayObj.getHeight());
        if (!transforms.isEmpty()) { //transform screen position if needed by applying transform matrix to rectangle
            updateScreenPosForRotation(displayObj);
        }
        else {
            displayObj.screenPos.set(bounds);
        }

        Rectangle intersection = Utils.getIntersection(bounds, visibleBounds);
        if (intersection != null) { //avoid drawing object if it's not within visible region
            final Rectangle backup = visibleBounds;
            visibleBounds = intersection;

            if (displayObj.getRotate90()) { //use top-right corner of bounds as pivot point
                startRotateTransform(displayObj.getWidth(), 0, -90);
                updateScreenPosForRotation(displayObj);
            }
            else if (displayObj.getRotate180()) { //use center of bounds as pivot point
                startRotateTransform(displayObj.getWidth() / 2, displayObj.getHeight() / 2, 180);
                //screen position won't change for this object from a 180 degree rotation 
            }

            displayObj.draw(this);

            if (displayObj.getRotate90() || displayObj.getRotate180()) {
                endTransform();
            }

            visibleBounds = backup;
        }

        bounds = parentBounds;
    }

    private void updateScreenPosForRotation(FDisplayObject displayObj) {
        tmp.set(bounds.x, regionHeight - bounds.y, 0);
        tmp.mul(batch.getTransformMatrix());
        tmp.y = regionHeight - tmp.y;
        float minX = tmp.x;
        float maxX = minX;
        float minY = tmp.y;
        float maxY = minY;
        tmp.set(bounds.x + bounds.width, regionHeight - bounds.y, 0);
        tmp.mul(batch.getTransformMatrix());
        tmp.y = regionHeight - tmp.y;
        if (tmp.x < minX) { minX = tmp.x; }
        else if (tmp.x > maxX) { maxX = tmp.x; }
        if (tmp.y < minY) { minY = tmp.y; }
        else if (tmp.y > maxY) { maxY = tmp.y; }
        tmp.set(bounds.x + bounds.width, regionHeight - bounds.y - bounds.height, 0);
        tmp.mul(batch.getTransformMatrix());
        tmp.y = regionHeight - tmp.y;
        if (tmp.x < minX) { minX = tmp.x; }
        else if (tmp.x > maxX) { maxX = tmp.x; }
        if (tmp.y < minY) { minY = tmp.y; }
        else if (tmp.y > maxY) { maxY = tmp.y; }
        tmp.set(bounds.x, regionHeight - bounds.y - bounds.height, 0);
        tmp.mul(batch.getTransformMatrix());
        tmp.y = regionHeight - tmp.y;
        if (tmp.x < minX) { minX = tmp.x; }
        else if (tmp.x > maxX) { maxX = tmp.x; }
        if (tmp.y < minY) { minY = tmp.y; }
        else if (tmp.y > maxY) { maxY = tmp.y; }

        displayObj.screenPos.set(minX, minY, maxX - minX, maxY - minY);
    }

    public void drawLine(float thickness, FSkinColor skinColor, float x1, float y1, float x2, float y2) {
        drawLine(thickness, skinColor.getColor(), x1, y1, x2, y2);
    }
    public void drawLine(float thickness, Color color, float x1, float y1, float x2, float y2) {
        batch.end(); //must pause batch while rendering shapes

        if (thickness > 1) {
            Gdx.gl.glLineWidth(thickness);
        }
        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        boolean needSmoothing = (x1 != x2 && y1 != y2);
        if (color.a < 1 || needSmoothing) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }
        if (needSmoothing) {
            Gdx.gl.glEnable(GL_LINE_SMOOTH);
        }

        startShape(ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.line(adjustX(x1), adjustY(y1, 0), adjustX(x2), adjustY(y2, 0));
        endShape();

        if (needSmoothing) {
            Gdx.gl.glDisable(GL_LINE_SMOOTH);
        }
        if (color.a < 1 || needSmoothing) {
            Gdx.gl.glDisable(GL_BLEND);
        }
        if (thickness > 1) {
            Gdx.gl.glLineWidth(1);
        }

        batch.begin();
    }

    public void drawArrow(float borderThickness, float arrowThickness, float arrowSize, FSkinColor skinColor, float x1, float y1, float x2, float y2) {
        drawArrow(borderThickness, arrowThickness, arrowSize, skinColor.getColor(), x1, y1, x2, y2);
    }
    public void drawArrow(float borderThickness, float arrowThickness, float arrowSize, Color color, float x1, float y1, float x2, float y2) {
        batch.end(); //must pause batch while rendering shapes

        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glEnable(GL_LINE_SMOOTH);

        float angle = new Vector2(x2 - x1, y2 - y1).angleRad();
        float perpRotation = (float)(Math.PI * 0.5f);
        float arrowHeadRotation = (float)(Math.PI * 0.8f);
        float arrowTipAngle = (float)(Math.PI - arrowHeadRotation);
        float halfThickness = arrowThickness / 2;

        int index = 0;
        float[] vertices = new float[14];
        Vector2 arrowCorner1 = new Vector2(x2 + arrowSize * (float)Math.cos(angle + arrowHeadRotation), y2 + arrowSize * (float)Math.sin(angle + arrowHeadRotation));
        Vector2 arrowCorner2 = new Vector2(x2 + arrowSize * (float)Math.cos(angle - arrowHeadRotation), y2 + arrowSize * (float)Math.sin(angle - arrowHeadRotation));
        float arrowCornerLen = (arrowCorner1.dst(arrowCorner2) - arrowThickness) / 2;
        float arrowHeadLen = arrowSize * (float)Math.cos(arrowTipAngle);
        index = addVertex(arrowCorner1.x, arrowCorner1.y, vertices, index);
        index = addVertex(x2, y2, vertices, index);
        index = addVertex(arrowCorner2.x, arrowCorner2.y, vertices, index);
        index = addVertex(arrowCorner2.x + arrowCornerLen * (float)Math.cos(angle + perpRotation), arrowCorner2.y + arrowCornerLen * (float)Math.sin(angle + perpRotation), vertices, index);
        index = addVertex(x1 + halfThickness * (float)Math.cos(angle - perpRotation), y1 + halfThickness * (float)Math.sin(angle - perpRotation), vertices, index);
        index = addVertex(x1 + halfThickness * (float)Math.cos(angle + perpRotation), y1 + halfThickness * (float)Math.sin(angle + perpRotation), vertices, index);
        index = addVertex(arrowCorner1.x + arrowCornerLen * (float)Math.cos(angle - perpRotation), arrowCorner1.y + arrowCornerLen * (float)Math.sin(angle - perpRotation), vertices, index);

        //draw arrow tail
        startShape(ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rectLine(adjustX(x1), adjustY(y1, 0),
                adjustX(x2 - arrowHeadLen * (float)Math.cos(angle)), //shorten tail to make room for arrow head
                adjustY(y2 - arrowHeadLen * (float)Math.sin(angle), 0), arrowThickness);

        //draw arrow head
        shapeRenderer.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
        endShape();

        //draw border around arrow
        if (borderThickness > 1) {
            Gdx.gl.glLineWidth(borderThickness);
        }
        startShape(ShapeType.Line);
        shapeRenderer.setColor(Color.BLACK);
        shapeRenderer.polygon(vertices);
        endShape();
        if (borderThickness > 1) {
            Gdx.gl.glLineWidth(1);
        }

        Gdx.gl.glDisable(GL_LINE_SMOOTH);
        Gdx.gl.glDisable(GL_BLEND);

        batch.begin();
    }

    private int addVertex(float x, float y, float[] vertices, int index) {
        vertices[index] = adjustX(x);
        vertices[index + 1] = adjustY(y, 0);
        return index + 2;
    }

    public void drawRoundRect(float thickness, FSkinColor skinColor, float x, float y, float w, float h, float cornerRadius) {
        drawRoundRect(thickness, skinColor.getColor(), x, y, w, h, cornerRadius);
    }
    public void drawRoundRect(float thickness, Color color, float x, float y, float w, float h, float cornerRadius) {
        batch.end(); //must pause batch while rendering shapes

        if (thickness > 1) {
            Gdx.gl.glLineWidth(thickness);
        }
        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        if (color.a < 1 || cornerRadius > 0) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }
        if (cornerRadius > 0) {
            Gdx.gl.glEnable(GL_LINE_SMOOTH);
        }

        //adjust width/height so rectangle covers equivalent filled area
        w = Math.round(w - 1);
        h = Math.round(h - 1);

        startShape(ShapeType.Line);
        shapeRenderer.setColor(color);

        x = adjustX(x);
        float y2 = adjustY(y, h);
        float x2 = x + w;
        y = y2 + h;
        //TODO: draw arcs at corners
        shapeRenderer.line(x, y, x, y2);
        shapeRenderer.line(x, y2, x2 + 1, y2); //+1 prevents corner not being filled
        shapeRenderer.line(x2, y2, x2, y);
        shapeRenderer.line(x2 + 1, y, x, y); //+1 prevents corner not being filled

        endShape();

        if (cornerRadius > 0) {
            Gdx.gl.glDisable(GL_LINE_SMOOTH);
        }
        if (color.a < 1 || cornerRadius > 0) {
            Gdx.gl.glDisable(GL_BLEND);
        }
        if (thickness > 1) {
            Gdx.gl.glLineWidth(1);
        }

        batch.begin();
    }

    public void drawRect(float thickness, FSkinColor skinColor, float x, float y, float w, float h) {
        drawRect(thickness, skinColor.getColor(), x, y, w, h);
    }
    public void drawRect(float thickness, Color color, float x, float y, float w, float h) {
        batch.end(); //must pause batch while rendering shapes

        if (thickness > 1) {
            Gdx.gl.glLineWidth(thickness);
        }
        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glEnable(GL_LINE_SMOOTH); //must be smooth to ensure edges aren't missed

        startShape(ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(adjustX(x), adjustY(y, h), w, h);
        endShape();

        Gdx.gl.glDisable(GL_LINE_SMOOTH);
        Gdx.gl.glDisable(GL_BLEND);
        if (thickness > 1) {
            Gdx.gl.glLineWidth(1);
        }

        batch.begin();
    }

    public void fillRect(FSkinColor skinColor, float x, float y, float w, float h) {
        fillRect(skinColor.getColor(), x, y, w, h);
    }
    public void fillRect(Color color, float x, float y, float w, float h) {
        batch.end(); //must pause batch while rendering shapes

        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        if (color.a < 1) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }

        startShape(ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(adjustX(x), adjustY(y, h), w, h);
        endShape();

        if (color.a < 1) {
            Gdx.gl.glDisable(GL_BLEND);
        }

        batch.begin();
    }

    public void drawCircle(float thickness, FSkinColor skinColor, float x, float y, float radius) {
        drawCircle(thickness, skinColor.getColor(), x, y, radius);
    }
    public void drawCircle(float thickness, Color color, float x, float y, float radius) {
        batch.end(); //must pause batch while rendering shapes

        if (thickness > 1) {
            Gdx.gl.glLineWidth(thickness);
        }
        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        Gdx.gl.glEnable(GL_BLEND);
        Gdx.gl.glEnable(GL_LINE_SMOOTH);

        startShape(ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(adjustX(x), adjustY(y, 0), radius);
        endShape();

        Gdx.gl.glDisable(GL_LINE_SMOOTH);
        Gdx.gl.glDisable(GL_BLEND);
        if (thickness > 1) {
            Gdx.gl.glLineWidth(1);
        }

        batch.begin();
    }

    public void fillCircle(FSkinColor skinColor, float x, float y, float radius) {
        fillCircle(skinColor.getColor(), x, y, radius);
    }
    public void fillCircle(Color color, float x, float y, float radius) {
        batch.end(); //must pause batch while rendering shapes

        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        if (color.a < 1) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }

        startShape(ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.circle(adjustX(x), adjustY(y, 0), radius); //TODO: Make smoother
        endShape();

        if (color.a < 1) {
            Gdx.gl.glDisable(GL_BLEND);
        }

        batch.begin();
    }

    public void fillTriangle(FSkinColor skinColor, float x1, float y1, float x2, float y2, float x3, float y3) {
        fillTriangle(skinColor.getColor(), x1, y1, x2, y2, x3, y3);
    }
    public void fillTriangle(Color color, float x1, float y1, float x2, float y2, float x3, float y3) {
        batch.end(); //must pause batch while rendering shapes

        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        if (color.a < 1) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }

        startShape(ShapeType.Filled);
        shapeRenderer.setColor(color);
        shapeRenderer.triangle(adjustX(x1), adjustY(y1, 0), adjustX(x2), adjustY(y2, 0), adjustX(x3), adjustY(y3, 0));
        endShape();

        if (color.a < 1) {
            Gdx.gl.glDisable(GL_BLEND);
        }

        batch.begin();
    }

    public void fillGradientRect(FSkinColor skinColor1, FSkinColor skinColor2, boolean vertical, float x, float y, float w, float h) {
        fillGradientRect(skinColor1.getColor(), skinColor2.getColor(), vertical, x, y, w, h);
    }
    public void fillGradientRect(FSkinColor skinColor1, Color color2, boolean vertical, float x, float y, float w, float h) {
        fillGradientRect(skinColor1.getColor(), color2, vertical, x, y, w, h);
    }
    public void fillGradientRect(Color color1, FSkinColor skinColor2, boolean vertical, float x, float y, float w, float h) {
        fillGradientRect(color1, skinColor2.getColor(), vertical, x, y, w, h);
    }
    public void fillGradientRect(Color color1, Color color2, boolean vertical, float x, float y, float w, float h) {
        batch.end(); //must pause batch while rendering shapes

        if (alphaComposite < 1) {
            color1 = FSkinColor.alphaColor(color1, color1.a * alphaComposite);
            color2 = FSkinColor.alphaColor(color2, color2.a * alphaComposite);
        }
        boolean needBlending = (color1.a < 1 || color2.a < 1);
        if (needBlending) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }

        Color topLeftColor = color1;
        Color topRightColor = vertical ? color1 : color2;
        Color bottomLeftColor = vertical ? color2 : color1;
        Color bottomRightColor = color2;

        startShape(ShapeType.Filled);
        shapeRenderer.rect(adjustX(x), adjustY(y, h), w, h, bottomLeftColor, bottomRightColor, topRightColor, topLeftColor);
        endShape();

        if (needBlending) {
            Gdx.gl.glDisable(GL_BLEND);
        }

        batch.begin();
    }

    private void startShape(ShapeType shapeType) {
        if (!transforms.isEmpty()) {
            //must copy matrix before starting shape if transformed
            shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        }
        shapeRenderer.begin(shapeType);
    }

    private void endShape() {
        shapeRenderer.end();
    }

    public void setAlphaComposite(float alphaComposite0) {
        alphaComposite = alphaComposite0;
        batch.setColor(new Color(1, 1, 1, alphaComposite));
    }
    public void resetAlphaComposite() {
        alphaComposite = 1;
        batch.setColor(Color.WHITE);
    }

    public void drawImage(FImage image, float x, float y, float w, float h) {
        image.draw(this, x, y, w, h);
    }
    public void drawImage(Texture image, float x, float y, float w, float h) {
        batch.draw(image, adjustX(x), adjustY(y, h), w, h);
    }
    public void drawImage(TextureRegion image, float x, float y, float w, float h) {
        batch.draw(image, adjustX(x), adjustY(y, h), w, h);
    }

    public void drawRepeatingImage(Texture image, float x, float y, float w, float h) {
        if (startClip(x, y, w, h)) { //only render if clip successful, otherwise it will escape bounds
            int tilesW = (int)(w / image.getWidth()) + 1;
            int tilesH = (int)(h / image.getHeight()) + 1;  
            batch.draw(image, adjustX(x), adjustY(y, h),
                    image.getWidth() * tilesW, 
                    image.getHeight() * tilesH, 
                    0, tilesH, tilesW, 0);
        }
        endClip();
    }

    //draw vertically flipped image
    public void drawFlippedImage(Texture image, float x, float y, float w, float h) {
        batch.draw(image, adjustX(x), adjustY(y, h), w, h, 0, 0, image.getWidth(), image.getHeight(), false, true);
    }

    public void drawImageWithTransforms(TextureRegion image, float x, float y, float w, float h, float rotation, boolean flipX, boolean flipY) {
        float originX = x + w / 2;
        float originY = y + h / 2;
        batch.draw(image.getTexture(), adjustX(x), adjustY(y, h), originX - x, h - (originY - y), w, h, 1, 1, rotation, image.getRegionX(), image.getRegionY(), image.getRegionWidth(), image.getRegionHeight(), flipX, flipY);
    }

    public void setProjectionMatrix(Matrix4 matrix) {
        batch.setProjectionMatrix(matrix);
        shapeRenderer.setProjectionMatrix(matrix);
    }

    public void startRotateTransform(float originX, float originY, float rotation) {
        batch.end();
        float dx = adjustX(originX);
        float dy = adjustY(originY, 0);
        transforms.add(new Matrix4(batch.getTransformMatrix())); //backup current transform matrix
        batch.getTransformMatrix().translate(dx, dy, 0);
        batch.getTransformMatrix().rotate(Vector3.Z, rotation);
        batch.getTransformMatrix().translate(-dx, -dy, 0);
        batch.begin();
    }

    public void endTransform() {
        batch.end();
        batch.getTransformMatrix().set(transforms.pop());
        shapeRenderer.setTransformMatrix(batch.getTransformMatrix());
        batch.begin();
    }

    public void drawRotatedImage(Texture image, float x, float y, float w, float h, float originX, float originY, float rotation) {
        drawRotatedImage(image, x, y, w, h, originX, originY, 0, 0, image.getWidth(), image.getHeight(), rotation);
    }
    public void drawRotatedImage(Texture image, float x, float y, float w, float h, float originX, float originY, int srcX, int srcY, int srcWidth, int srcHeight, float rotation) {
        batch.draw(image, adjustX(x), adjustY(y, h), originX - x, h - (originY - y), w, h, 1, 1, rotation, srcX, srcY, srcWidth, srcHeight, false, false);
    }

    public void drawText(String text, FSkinFont font, FSkinColor skinColor, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
        drawText(text, font, skinColor.getColor(), x, y, w, h, wrap, horzAlignment, centerVertically);
    }
    public void drawText(String text, FSkinFont font, Color color, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
        if (alphaComposite < 1) {
            color = FSkinColor.alphaColor(color, color.a * alphaComposite);
        }
        if (color.a < 1) { //enable blending so alpha colored shapes work properly
            Gdx.gl.glEnable(GL_BLEND);
        }

        TextBounds textBounds;
        if (wrap) {
            textBounds = font.getWrappedBounds(text, w);
        }
        else {
            textBounds = font.getMultiLineBounds(text);
        }
        
        boolean needClip = false;

        while (textBounds.width > w || textBounds.height > h) {
            if (font.canShrink()) { //shrink font to fit if possible
                font = font.shrink();
                if (wrap) {
                    textBounds = font.getWrappedBounds(text, w);
                }
                else {
                    textBounds = font.getMultiLineBounds(text);
                }
            }
            else {
                needClip = true;
                break;
            }
        }

        if (needClip) { //prevent text flowing outside region if couldn't shrink it to fit
            startClip(x, y, w, h);
        }

        float textHeight = textBounds.height;
        if (h > textHeight && centerVertically) {
            y += (h - textHeight) / 2;
        }

        font.draw(batch, text, color, adjustX(x), adjustY(y, 0), w, wrap, horzAlignment);

        if (needClip) {
            endClip();
        }

        if (color.a < 1) {
            Gdx.gl.glDisable(GL_BLEND);
        }
    }

    //use nifty trick with multiple text renders to draw outlined text
    public void drawOutlinedText(String text, FSkinFont skinFont, Color textColor, Color outlineColor, float x, float y, float w, float h, boolean wrap, HAlignment horzAlignment, boolean centerVertically) {
        drawText(text, skinFont, outlineColor, x - 1, y, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, outlineColor, x, y - 1, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, outlineColor, x - 1, y - 1, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, outlineColor, x + 1, y, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, outlineColor, x, y + 1, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, outlineColor, x + 1, y + 1, w, h, wrap, horzAlignment, centerVertically);
        drawText(text, skinFont, textColor, x, y, w, h, wrap, horzAlignment, centerVertically);
    }

    private float adjustX(float x) {
        return x + bounds.x;
    }

    private float adjustY(float y, float height) {
        return regionHeight - y - bounds.y - height; //flip y-axis
    }
}