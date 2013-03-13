/**
 * 
 */
package com.funzio.pure2D.particles.nova;

import java.util.List;
import java.util.Random;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

import com.funzio.pure2D.gl.gl10.BlendFunc;

/**
 * @author long
 */
public class NovaConfig {
    public static final Random RANDOM = new Random();

    // interpolators
    public static final DecelerateInterpolator INTER_DECELERATION = new DecelerateInterpolator();
    public static final AccelerateInterpolator INTER_ACCELARATION = new AccelerateInterpolator();
    public static final BounceInterpolator INTER_BOUNCE = new BounceInterpolator();

    public static final BlendFunc BF_ADD = BlendFunc.getAdd();
    public static final BlendFunc BF_SCREEN = BlendFunc.getScreen();
    public static final BlendFunc BF_MULTIPLY = BlendFunc.getMultiply();

    public static float getRandomFloat(final List<Float> values) {
        if (values == null) {
            return 0;
        }

        final int size = values.size();
        if (size == 0) {
            // default value
            return 0;
        } else if (size == 1) {
            // fixed value
            return values.get(0);
        } else if (size == 2) {
            // random value within a range
            return values.get(0) + RANDOM.nextFloat() * (values.get(1) - values.get(0));
        } else {
            // randomly pick one of the given values
            return values.get(RANDOM.nextInt(size));
        }
    }

    public static int getRandomInt(final List<Integer> values) {
        if (values == null) {
            return 0;
        }

        final int size = values.size();
        if (size == 0) {
            // default value
            return 0;
        } else if (size == 1) {
            // fixed value
            return values.get(0);
        } else if (size == 2) {
            // random value within a range
            return values.get(0) + (int) (RANDOM.nextFloat() * (values.get(1) - values.get(0)));
        } else {
            // randomly pick one of the given values
            return values.get(RANDOM.nextInt(size));
        }
    }

    public static Interpolator getInterpolator(final String name) {
        if ("accelerate".equalsIgnoreCase(name)) {
            return NovaConfig.INTER_ACCELARATION;
        } else if ("decelerate".equalsIgnoreCase(name)) {
            return NovaConfig.INTER_DECELERATION;
        } else if ("bounce".equalsIgnoreCase(name)) {
            return NovaConfig.INTER_BOUNCE;
        }

        return null;
    }

    public static BlendFunc getBlendFunc(final String mode) {
        if ("add".equalsIgnoreCase(mode)) {
            return BF_ADD;
        } else if ("screen".equalsIgnoreCase(mode)) {
            return BF_SCREEN;
        } else if ("multiply".equalsIgnoreCase(mode)) {
            return BF_MULTIPLY;
        }

        return null;
    }
}