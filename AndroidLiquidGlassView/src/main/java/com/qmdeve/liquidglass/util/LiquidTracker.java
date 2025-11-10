package com.qmdeve.liquidglass.util;

import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;

public class LiquidTracker {
    private VelocityTracker velocityTracker;
    private final SpringAnimation springAnimX, springAnimY;
    private final Handler liquidHandler;

    public LiquidTracker(View view) {
        SpringForce springX = new SpringForce();
        springX.setStiffness(SpringForce.STIFFNESS_LOW);
        springX.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        springAnimX = new SpringAnimation(view, DynamicAnimation.SCALE_X);
        springAnimX.setSpring(springX);

        SpringForce springY = new SpringForce();
        springY.setStiffness(SpringForce.STIFFNESS_LOW);
        springY.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        springAnimY = new SpringAnimation(view, DynamicAnimation.SCALE_Y);
        springAnimY.setSpring(springY);

        liquidHandler = new Handler(Looper.getMainLooper());
    }

    public void applyMovement(@NonNull MotionEvent e) {
        int action = e.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                ensureAddMovement(e);
                break;
            case MotionEvent.ACTION_MOVE:
                ensureAddMovement(e);

                float[] scaleXY = getLiquidScale();
                animateToFinalPosition(scaleXY[0], scaleXY[1]);

                //if ACTION_UP did not happen yet (holding) and no more ACTION_MOVE then ease it back to 1.0f anyways
                liquidHandler.removeCallbacksAndMessages(null);
                liquidHandler.postDelayed(() -> {
                    animateToFinalPosition(1f, 1f);
				}, 200);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                recycle();
                animateToFinalPosition(1f, 1f);
                break;
        }
    }

    public void recycle() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private float getVelocity() {
        if (velocityTracker == null)
            return 0f;

        velocityTracker.computeCurrentVelocity(1);
        float velocityX = velocityTracker.getXVelocity();
        float velocityY = velocityTracker.getYVelocity();
        return (float)Math.sqrt(velocityX * velocityX + velocityY * velocityY) * (velocityX > 0f ? 1f : -1f);
    }

    private float[] getLiquidScale() {
        if (velocityTracker == null)
            return new float[] { 1f, 1f };

        float velocity = getVelocity();
        float scaleX = Math.clamp(1f + velocity * 0.1f, 0.8f, 1.337f);
        float scaleY = Math.clamp(1f - velocity * 0.1f, 0.8f, 1.337f);
        return new float[] { scaleX, scaleY };
    }

    private void ensureAddMovement(MotionEvent e) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(e);
    }

    private void animateToFinalPosition(float x, float y) {
        springAnimX.animateToFinalPosition(x);
        springAnimY.animateToFinalPosition(y);
    }
}
