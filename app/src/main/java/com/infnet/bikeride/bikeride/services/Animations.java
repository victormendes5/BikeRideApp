package com.infnet.bikeride.bikeride.services;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;

public class Animations {

    private int mDefaultAnimationSpeed = 200;
    private int mScreenBias = 1500;

    public interface AnimationCallback {
        void OnComplete();
    }

    //   /===============================================================================\
    //   |                                CONSTRUCTORS                                   |
    //   \===============================================================================/

    public Animations() {}

    public Animations(int defaultspeed) {
        mDefaultAnimationSpeed = defaultspeed;
    }


    //   /===============================================================================\
    //   |                             PUBLIC ABSTRACTIONS                               |
    //   \===============================================================================/

    // ---> Fade In
    public void fadeIn(View v)
    {fade(v, 0, mDefaultAnimationSpeed, 0, 1);}
    public void fadeIn(View v, int delay)
    {fade(v, delay, mDefaultAnimationSpeed, 0, 1);}
    public void fadeIn(View v, int delay, int speed)
    {fade(v, delay, speed, 0, 1);}

    // --- Fade Out
    public void fadeOut(View v)
    {fade(v, 0, mDefaultAnimationSpeed, 1, 0);}
    public void fadeOut(View v, int delay)
    {fade(v, delay, mDefaultAnimationSpeed, 1, 0);}
    public void fadeOut(View v, int delay, int speed)
    {fade(v, delay, speed, 1, 0);}

    // ---> Fade Custom
    public void fadeCustom(View v, int startOpacity, int endOpacity)
    {fade(v, 0, mDefaultAnimationSpeed, startOpacity, endOpacity);}
    public void fadeCustom(View v, int delay, int startOpacity, int endOpacity)
    {fade(v, delay, mDefaultAnimationSpeed, startOpacity, endOpacity);}
    public void fadeCustom(View v, int delay, int speed, int startOpacity, int endOpacity)
    {fade(v, delay, speed, startOpacity, endOpacity);}

    // ---> Translate from Bottom
    public void translateFromBottom (View v)
    { translateInVertically(v, 0, mDefaultAnimationSpeed, mScreenBias); }
    public void translateFromBottom (View v, int delay)
    { translateInVertically(v, delay, mDefaultAnimationSpeed, mScreenBias); }
    public void translateFromBottom (View v, int delay, int speed)
    { translateInVertically(v, delay, speed, mScreenBias); }

    // ---> Translate from Top
    public void translateFromTop (View v)
    { translateInVertically(v, 0, mDefaultAnimationSpeed,-1* mScreenBias); }
    public void translateFromTop (View v, int delay)
    { translateInVertically(v, delay, mDefaultAnimationSpeed,-1* mScreenBias); }
    public void translateFromTop (View v, int delay, int speed)
    { translateInVertically(v, delay, speed,-1* mScreenBias); }

    // ---> Translate to Bottom
    public void translateToBottom (View v)
    { translateOutVertically(v, 0, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToBottom (View v, int delay)
    { translateOutVertically(v, delay, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToBottom (View v, int delay, int speed)
    { translateOutVertically(v, delay, speed, mScreenBias); }

    // ---> Translate to Top
    public void translateToTop (View v)
    { translateOutVertically(v, 0, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToTop (View v, int delay)
    { translateOutVertically(v, delay, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToTop (View v, int delay, int speed)
    { translateOutVertically(v, delay, speed, mScreenBias); }

    // ---> Translate from Right
    public void translateFromRight (View v)
    { translateInHorizontally(v, 0, mDefaultAnimationSpeed, mScreenBias); }
    public void translateFromRight (View v, int delay)
    { translateInHorizontally(v, delay, mDefaultAnimationSpeed, mScreenBias); }
    public void translateFromRight (View v, int delay, int speed)
    { translateInHorizontally(v, delay, speed, mScreenBias); }

    // ---> Translate from Left
    public void translateFromLeft (View v)
    { translateInHorizontally(v, 0, mDefaultAnimationSpeed,-1* mScreenBias); }
    public void translateFromLeft (View v, int delay)
    { translateInHorizontally(v, delay, mDefaultAnimationSpeed,-1* mScreenBias); }
    public void translateFromLeft (View v, int delay, int speed)
    { translateInHorizontally(v, delay, speed,-1* mScreenBias); }

    // ---> Translate to Left
    public void translateToLeft (final View v)
    { translateOutHorizontally(v, 0, mDefaultAnimationSpeed, -1* mScreenBias); }
    public void translateToLeft (final View v, int delay)
    { translateOutHorizontally(v, delay, mDefaultAnimationSpeed,-1* mScreenBias); }
    public void translateToLeft (final View v, int delay, int speed)
    { translateOutHorizontally(v, delay, speed,-1* mScreenBias); }

    // ---> Translate to Right
    public void translateToRight (final View v)
    { translateOutHorizontally(v, 0, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToRight (final View v, int delay)
    { translateOutHorizontally(v, delay, mDefaultAnimationSpeed, mScreenBias); }
    public void translateToRight (final View v, int delay, int speed)
    { translateOutHorizontally(v, delay, speed, mScreenBias); }


    //   /========================================================================================\
    //   |                                        FADES                                           |
    //   \========================================================================================/

    public void fade (final View v, int delay, final int speed, final float startOpacity,
                      final float endOpacity) {

        if (endOpacity == 0) {
            hideElementAfterAnimation(v, speed);
        }

        else if (startOpacity == 0) {
            v.setAlpha(0);
            v.setTranslationX(0);
            v.setTranslationY(0);
            v.setVisibility(View.VISIBLE);
        }

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "alpha",
                                startOpacity, endOpacity);
                        anim.setDuration(speed);
                        anim.start();
                    }
                },
                delay);
    }

    public void selectionFader (View viewtoshow, View... viewstohide) {

        if (viewtoshow.getVisibility() == View.GONE) {
            fadeIn(viewtoshow);
        }

        for (View v : viewstohide) {
            if (v.getVisibility() == View.VISIBLE) {
                fadeOut(v);
            }
        }
    }

    public void fadeAllOut (View... viewstoclean) {
        for (View v : viewstoclean) {
            if (v.getVisibility() == View.VISIBLE) {
                fadeOut(v);
            }
        }
    }


    //   /===============================================================================\
    //   |                             VERTICAL TRANSLATIONS                             |
    //   \===============================================================================/

    private void translateInVertically (final View v, int delay, final int speed,
                                        final int position) {

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        v.setTranslationY(position);
                        v.setTranslationX(0);
                        v.setAlpha(1);
                        v.setVisibility(View.VISIBLE);
                        ObjectAnimator anim = ObjectAnimator.ofFloat(v ,
                                "translationY", 0);
                        anim.setDuration(speed);
                        anim.start();
                    }
                },
                delay);
    }

    private void translateOutVertically (final View v, int delay, final int speed,
                                         final int position) {

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(v ,
                                "translationY", position);
                        anim.setDuration(speed);
                        anim.start();
                        hideElementAfterAnimation(v, speed);
                    }
                },
                delay);
    }


    //   /===============================================================================\
    //   |                             HORIZONTAL TRANSLATIONS                           |
    //   \===============================================================================/


    private void translateInHorizontally (final View v, int delay, final int speed,
                                          final int position) {

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        v.setTranslationX(position);
                        v.setTranslationY(0);
                        v.setVisibility(View.VISIBLE);
                        v.setAlpha(1);
                        ObjectAnimator anim = ObjectAnimator.ofFloat(v ,
                                "translationX", 0);
                        anim.setDuration(speed);
                        anim.start();
                    }
                },
                delay);
    }

    private void translateOutHorizontally (final View v, int delay, final int speed,
                                           final int position) {

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        ObjectAnimator anim = ObjectAnimator.ofFloat(v ,
                                "translationX", position);
                        anim.setDuration(speed);
                        anim.start();
                        hideElementAfterAnimation(v, speed);
                    }
                },
                delay);
    }


    //   /===============================================================================\
    //   |                                     ROTATION                                  |
    //   \===============================================================================/


    public void rotate360Infinitely (View v, int speed) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v, "rotation", 0, 3600);
        anim.setDuration(speed*10);
        anim.setRepeatCount(ValueAnimator.INFINITE);
//        anim.setStartDelay(3000);
        anim.start();
    }


    //   /===============================================================================\
    //   |                               COMPOSITE ANIMATIONS                            |
    //   \===============================================================================/


    public void swapViewsLeft (final View oldview, final View newview) {
        translateFromRight(newview);
        translateToLeft(oldview);
    }

    public void swapViewsRight (final View oldview, final View newview) {
        translateToRight(oldview);
        translateFromLeft(newview);
    }

    public void swapViewsAimingRigthSequentiallyIfVisible (final View oldview, final View newview) {

        if (oldview.getVisibility() == View.VISIBLE) {
            translateToRight(oldview);
            translateFromRight(newview, mDefaultAnimationSpeed);
        }
    }

    public void crossFadeViews (View viewtofadeout, View viewtofadein) {
        fadeOut(viewtofadeout);
        fadeIn(viewtofadein);
    }

    public void crossFadeViews (View viewtofadeout, View viewtofadein, int delay) {
        fadeOut(viewtofadeout, delay);
        fadeIn(viewtofadein, delay);
    }

    public void crossFadeViews (View viewtofadeout, View viewtofadein, int delay, int speed) {
        fadeOut(viewtofadeout, delay, speed);
        fadeIn(viewtofadein, delay, speed);
    }

    public void translateToBottomIfVisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) {
                translateToBottom(v);
            }
        }
    }

    public void translateFromBottomIfInvisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.INVISIBLE || v.getVisibility() == View.GONE) {
                translateFromBottom(v);
            }
        }
    }

    public void translateToRightIfVisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) {
                translateToRight(v);
            }
        }
    }

    public void translateFromRightIfInvisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.GONE) {
                translateFromRight(v);
            }
        }
    }

    public void translateFromRightIfInvisible (int delay, View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.GONE) {
                translateFromRight(v, delay);
            }
        }
    }

    public void translateToLeftIfVisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) {
                translateToLeft(v);
            }
        }
    }

    public void translateFromLeftIfInvisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.GONE) {
                translateFromLeft(v);
            }
        }
    }



    public void fadeOutIfVisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.VISIBLE) {
                fadeOut(v);
            }
        }
    }

    public void fadeInIfInvisible (View ... views) {
        for (View v : views) {
            if (v.getVisibility() == View.GONE || v.getVisibility() == View.INVISIBLE) {
                fadeIn(v);
            }
        }
    }


    //   /===============================================================================\
    //   |                                     OTHER                                     |
    //   \===============================================================================/

    private void hideElementAfterAnimation (final View v, int speed) {
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        v.setVisibility(View.GONE);
                    }
                },
                speed);
    }

    public void setDefaultAnimationSpeed(int defaultspeed) {
        mDefaultAnimationSpeed = defaultspeed;
    }
}
