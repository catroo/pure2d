package com.funzio.pure2D.samples.activities.animations;

import javax.microedition.khronos.opengles.GL10;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.funzio.pure2D.Playable;
import com.funzio.pure2D.R;
import com.funzio.pure2D.Scene;
import com.funzio.pure2D.animators.BezierAnimator;
import com.funzio.pure2D.gl.GLColor;
import com.funzio.pure2D.gl.gl10.textures.Texture;
import com.funzio.pure2D.samples.activities.StageActivity;
import com.funzio.pure2D.shapes.Rectangular;
import com.funzio.pure2D.shapes.Sprite;

public class BezierAnimationActivity extends StageActivity {
    private static final AccelerateInterpolator ACCELERATE = new AccelerateInterpolator();
    private static final DecelerateInterpolator DECELERATE = new DecelerateInterpolator();
    private static final AccelerateDecelerateInterpolator ACCELERATE_DECELERATE = new AccelerateDecelerateInterpolator();
    private static final BounceInterpolator BOUNCE = new BounceInterpolator();

    private Texture mTexture;
    private BezierAnimator mAnimator = new BezierAnimator(null);
    private Rectangular mControl1;
    private Rectangular mControl2;
    private int mPointer1 = -1;
    private int mPointer2 = -1;

    @Override
    protected int getLayout() {
        return R.layout.stage_tween_animations;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAnimator.setDuration(1000);

        // need to get the GL reference first
        mScene.setListener(new Scene.Listener() {

            @Override
            public void onSurfaceCreated(final GL10 gl) {

                // load the textures
                loadTexture();

                mControl1 = new Rectangular();
                mControl1.setAutoUpdateBounds(true);
                mControl1.setSize(100, 100);
                mControl1.setColor(new GLColor(1f, 0, 0, 1f));
                mControl1.setOriginAtCenter();
                mControl1.setPosition(mDisplaySizeDiv2.x, mDisplaySizeDiv2.y);
                mScene.addChild(mControl1);

                mControl2 = new Rectangular();
                mControl2.setAutoUpdateBounds(true);
                mControl2.setSize(100, 100);
                mControl2.setColor(new GLColor(0, 0, 1f, 1f));
                mControl2.setOriginAtCenter();
                mControl2.setPosition(mDisplaySizeDiv2.x, mDisplaySizeDiv2.y);
                mScene.addChild(mControl2);

                mAnimator.setControlPoints(mControl1.getPosition(), mControl2.getPosition());

                // generate a lot of squares
                addObject(mDisplaySizeDiv2.x, mDisplaySizeDiv2.y);
            }
        });
    }

    private void loadTexture() {
        // create texture
        mTexture = mScene.getTextureManager().createDrawableTexture(R.drawable.cc_128, null);
    }

    private void addObject(final float x, final float y) {
        // create object
        Sprite obj = new Sprite();
        obj.setTexture(mTexture);
        // center origin
        obj.setOriginAtCenter();
        // position
        obj.setPosition(x, y);

        // animation
        obj.addManipulator(mAnimator);

        // add to scene
        mScene.addChild(obj);

        mAnimator.start(0, 0, mDisplaySize.x, mDisplaySize.y);
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        final int action = event.getAction() & MotionEvent.ACTION_MASK;
        final int len = event.getPointerCount();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {

            for (int i = 0; i < len; i++) {
                int pointerId = event.getPointerId(i);
                float x = event.getX(i);
                float y = mDisplaySize.y - event.getY(i);

                if (mControl1.getBounds().contains(x, y)) {
                    mPointer1 = pointerId;
                } else if (mControl2.getBounds().contains(x, y)) {
                    mPointer2 = pointerId;
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            for (int i = 0; i < len; i++) {
                int pointerId = event.getPointerId(i);
                float x = event.getX(i);
                float y = mDisplaySize.y - event.getY(i);

                if (mPointer1 == pointerId) {
                    mControl1.setPosition(x, y);
                } else if (mPointer2 == pointerId) {
                    mControl2.setPosition(x, y);
                }

            }
            mAnimator.setControlPoints(mControl1.getPosition(), mControl2.getPosition());
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            for (int i = 0; i < len; i++) {
                int pointerId = event.getPointerId(i);
                if (mPointer1 == pointerId) {
                    mPointer1 = -1;
                } else if (mPointer2 == pointerId) {
                    mPointer2 = -1;
                }
            }
        }

        return true;
    }

    public void onClickRadio(final View view) {

        switch (view.getId()) {
            case R.id.radio_linear:
                mAnimator.setInterpolator(null);
                break;

            case R.id.radio_accelarate:
                mAnimator.setInterpolator(ACCELERATE);
                break;

            case R.id.radio_decelarate:
                mAnimator.setInterpolator(DECELERATE);
                break;

            case R.id.radio_accelerate_decelarate:
                mAnimator.setInterpolator(ACCELERATE_DECELERATE);
                break;

            case R.id.radio_bounce:
                mAnimator.setInterpolator(BOUNCE);
                break;

            case R.id.radio_once:
                mAnimator.setLoop(Playable.LOOP_NONE);
                break;

            case R.id.radio_repeat:
                mAnimator.setLoop(Playable.LOOP_REPEAT);
                break;

            case R.id.radio_reverse:
                mAnimator.setLoop(Playable.LOOP_REVERSE);
                break;
        }

        mAnimator.stop();
        mAnimator.start();
    }
}