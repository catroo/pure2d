/**
 * 
 */
package com.funzio.pure2D.shapes;

import java.util.Arrays;

import javax.microedition.khronos.opengles.GL10;

import com.funzio.pure2D.BaseDisplayObject;
import com.funzio.pure2D.InvalidateFlags;
import com.funzio.pure2D.Pure2D;
import com.funzio.pure2D.Scene;
import com.funzio.pure2D.containers.Container;
import com.funzio.pure2D.gl.GLColor;
import com.funzio.pure2D.gl.gl10.ColorBuffer;
import com.funzio.pure2D.gl.gl10.GLState;
import com.funzio.pure2D.gl.gl10.VertexBuffer;
import com.funzio.pure2D.gl.gl10.textures.Texture;
import com.funzio.pure2D.gl.gl10.textures.TextureCoordBuffer;

/**
 * @author long
 */
public class Shape extends BaseDisplayObject {
    public final static String TAG = Shape.class.getSimpleName();

    protected VertexBuffer mVertexBuffer;

    protected Texture mTexture;
    protected TextureCoordBuffer mTextureCoordBuffer;
    protected TextureCoordBuffer mTextureCoordBufferScaled;
    protected ColorBuffer mColorBuffer;

    // for axis system
    private boolean mTextureFlippedForAxis = false;

    public void setVertexBuffer(final VertexBuffer buffer) {
        mVertexBuffer = buffer;
    }

    public VertexBuffer getVertexBuffer() {
        return mVertexBuffer;
    }

    /**
     * @return the texture
     */
    public Texture getTexture() {
        return mTexture;
    }

    /**
     * @param texture the texture to set
     */
    public void setTexture(final Texture texture) {
        mTexture = texture;

        invalidate(InvalidateFlags.TEXTURE | InvalidateFlags.TEXTURE_COORDS);
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.BaseDisplayObject#drawStart(com.funzio.pure2D.gl.gl10.GLState)
     */
    @Override
    protected void drawStart(final GLState glState) {
        // texture coordinates changed?
        if ((mInvalidateFlags & InvalidateFlags.TEXTURE_COORDS) != 0) {
            validateTextureCoordBuffer();
        }

        super.drawStart(glState);
    }

    /**
     * validate texture coords
     */
    protected void validateTextureCoordBuffer() {
        // match texture coordinates with the Axis system
        final Scene scene = getScene();
        if (mTextureCoordBuffer != null && scene != null && scene.getAxisSystem() == Scene.AXIS_TOP_LEFT && !mTextureFlippedForAxis) {
            // flip vertically
            mTextureCoordBuffer.flipVertical();
            mTextureFlippedForAxis = true;
        }

        // scale to match with the Texture scale, for optimization
        if (mTexture != null && mTextureCoordBuffer != null) {
            if ((mTexture.mCoordScaleX != 1 || mTexture.mCoordScaleY != 1)) {
                // scale the values
                final float[] scaledValues = mTextureCoordBuffer.getValues().clone();
                TextureCoordBuffer.scale(scaledValues, mTexture.mCoordScaleX, mTexture.mCoordScaleY);

                if (mTextureCoordBufferScaled != null && mTextureCoordBufferScaled != mTextureCoordBuffer) {
                    mTextureCoordBufferScaled.setValues(scaledValues);
                } else {
                    mTextureCoordBufferScaled = new TextureCoordBuffer(scaledValues);
                }
            } else {
                mTextureCoordBufferScaled = mTextureCoordBuffer;
            }
        } else {
            mTextureCoordBufferScaled = null;
        }

        // clear flag: texture coords
        validate(InvalidateFlags.TEXTURE_COORDS);
    }

    protected boolean setTextureCoordBuffer(final float[] values) {
        if (mTextureCoordBuffer != null) {
            // diff check
            if (Arrays.equals(mTextureCoordBuffer.getValues(), values)) {
                return false;
            }

            mTextureCoordBuffer.setValues(values);
        } else {
            mTextureCoordBuffer = new TextureCoordBuffer(values);
        }

        // invalidate texture coords
        mTextureFlippedForAxis = false;

        invalidate(InvalidateFlags.TEXTURE_COORDS);

        return true;
    }

    public void setTextureCoordBuffer(final TextureCoordBuffer coords) {
        // diff check
        if (mTextureCoordBuffer == coords) {
            return;
        }

        mTextureCoordBuffer = coords;

        // invalidate texture coords
        mTextureFlippedForAxis = false;

        invalidate(InvalidateFlags.TEXTURE_COORDS);
    }

    public TextureCoordBuffer getTextureCoordBuffer() {
        return mTextureCoordBuffer;
    }

    public void setColorBuffer(final ColorBuffer buffer) {
        mColorBuffer = buffer;
    }

    public ColorBuffer getColorBuffer() {
        return mColorBuffer;
    }

    @Override
    protected boolean drawChildren(final GLState glState) {
        if (mVertexBuffer == null) {
            return false;
        }

        // color buffer
        if (mColorBuffer == null) {
            glState.setColorArrayEnabled(false);
        } else {
            // apply color buffer
            mColorBuffer.apply(glState);
        }

        // texture
        if (mTexture != null) {
            // bind the texture
            mTexture.bind();

            // apply the coordinates
            if (mTextureCoordBufferScaled != null) {
                mTextureCoordBufferScaled.apply(glState);
            }
        } else {
            // unbind the texture
            glState.unbindTexture();
        }

        // now draw, woo hoo!
        mVertexBuffer.draw(glState);

        return true;
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.BaseDisplayObject#drawWireframe(com.funzio.pure2D.gl.gl10.GLState)
     */
    @Override
    protected void drawWireframe(final GLState glState) {

        // pre-draw
        final int primitive = mVertexBuffer.getPrimitive();
        final GLColor currentColor = glState.getColor();
        final boolean textureEnabled = glState.isTextureEnabled();
        final float currentLineWidth = glState.getLineWidth();
        glState.setLineWidth(Pure2D.DEBUG_WIREFRAME_WIDTH);
        glState.setColor(Pure2D.DEBUG_WIREFRAME_COLOR);
        glState.setTextureEnabled(false);

        // draw
        mVertexBuffer.setPrimitive(GL10.GL_LINE_STRIP);
        mVertexBuffer.draw(glState);

        // post-draw
        glState.setTextureEnabled(textureEnabled);
        glState.setColor(currentColor);
        glState.setLineWidth(currentLineWidth);
        mVertexBuffer.setPrimitive(primitive);
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.IDisplayObject#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();

        if (mVertexBuffer != null) {
            mVertexBuffer.dispose();
            mVertexBuffer = null;
        }

        if (mTextureCoordBuffer != null) {
            mTextureCoordBuffer.dispose();
            mTextureCoordBuffer = null;
            mTextureFlippedForAxis = false;
        }

        if (mTextureCoordBufferScaled != null) {
            mTextureCoordBufferScaled.dispose();
            mTextureCoordBufferScaled = null;
        }
    }

    /**
     * @param flips can be #DisplayObject.FLIP_X and/or #DisplayObject.FLIP_Y
     * @see #DisplayObject
     */
    public void flipTextureCoordBuffer(final int flips) {
        // null check
        if (mTextureCoordBuffer == null) {
            return;
        }

        boolean flipped = false;

        if ((flips & FLIP_X) > 0) {
            mTextureCoordBuffer.flipHorizontal();
            flipped = true;
        }

        if ((flips & FLIP_Y) > 0) {
            mTextureCoordBuffer.flipVertical();
            flipped = true;
        }

        if (flipped) {
            invalidate(InvalidateFlags.TEXTURE_COORDS);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.BaseDisplayObject#onAdded(com.funzio.pure2D.containers.Container)
     */
    @Override
    public void onAdded(final Container parent) {
        super.onAdded(parent);

        invalidate(InvalidateFlags.TEXTURE_COORDS);
    }
}
