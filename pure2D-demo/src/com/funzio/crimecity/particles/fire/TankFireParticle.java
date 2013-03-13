/**
 * 
 */
package com.funzio.crimecity.particles.fire;

import android.graphics.PointF;

import com.funzio.crimecity.particles.ParticleAdapter;
import com.funzio.pure2D.gl.gl10.textures.Texture;
import com.funzio.pure2D.particles.SpriteParticle;

/**
 * @author long
 */
public class TankFireParticle extends SpriteParticle {
    private static final int SIZE = 16;

    protected float mDeltaRotation;
    protected PointF mDeltaScale = new PointF();
    protected int mFrame;
    protected int mTotalFrames;
    protected int mLifeStage;

    public TankFireParticle(final Texture texture) {
        super();
        setTexture(texture);
        reset();
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.particles.SpriteParticle#reset()
     */
    @Override
    public void reset() {
        super.reset();

        // initial state
        setSize(SIZE, SIZE);
        mAlpha = 1;
        mPosition.x = mRandom.nextInt(7) - 3;
        mPosition.y = mRandom.nextInt(7) - 3;
        mRotation = 0;
        mScale.x = mScale.y = 1;
        mVisible = true;

        // deltas
        mVelocity.x = mRandom.nextInt(5) - 2;
        mVelocity.y = mRandom.nextInt(5) - 2;
        mDeltaRotation = mRandom.nextInt(5) - 2;
        mDeltaScale.x = mDeltaScale.y = mRandom.nextFloat() / 2;

        // frame stuff
        mFrame = 0;
        mLifeStage = 0;
        mTotalFrames = 65 + mRandom.nextInt(11);
    }

    /*
     * (non-Javadoc)
     * @see com.funzio.pure2D.particles.SpriteParticle#update(int)
     */
    @Override
    public boolean update(final int deltaTime) {
        // apply the deltas
        mRotation += mDeltaRotation;
        mScale.x += mDeltaScale.x;
        mScale.y += mDeltaScale.y;
        mPosition.x += mVelocity.x;
        mPosition.y += mVelocity.y;
        invalidate();

        mFrame += ParticleAdapter.FRAME_THROTTLE ? 2 : 1;
        if (mFrame == 6) {
            mLifeStage = 1;
        } else if (mFrame == 16) {
            mLifeStage = 2;
            mDeltaScale.x = mDeltaScale.y = 0;
        } else if (mFrame >= mTotalFrames) {
            mLifeStage = 3;
            finish();
        }

        return true;
    }
}