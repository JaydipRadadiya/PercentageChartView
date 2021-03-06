package com.ramijemli.percentagechartview.renderer;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.util.TypedValue;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.ramijemli.percentagechartview.IPercentageChartView;
import com.ramijemli.percentagechartview.PercentageChartView;
import com.ramijemli.percentagechartview.R;
import com.ramijemli.percentagechartview.annotation.ProgressOrientation;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

public abstract class BaseModeRenderer {


    // CHART MODE
    public static final int MODE_RING = 0;
    public static final int MODE_PIE = 1;

    // ORIENTATION
    public static final int ORIENTATION_CLOCKWISE = 0;
    public static final int ORIENTATION_COUNTERCLOCKWISE = 1;

    // TEXT
    private static float DEFAULT_TEXT_SP_SIZE = 12;

    //ADAPTIVE MODES
    public static final int DARKER_MODE = 0;
    public static final int LIGHTER_MODE = 1;

    //ANIMATIONS
    public static final int DEFAULT_ANIMATION_INTERPOLATOR = 0;
    public static final int LINEAR = 0;
    public static final int ACCELERATE = 1;
    public static final int DECELERATE = 2;
    public static final int ACCELERATE_DECELERATE = 3;
    public static final int ANTICIPATE = 4;
    public static final int OVERSHOOT = 5;
    public static final int ANTICIPATE_OVERSHOOT = 6;
    public static final int BOUNCE = 7;
    public static final int FAST_OUT_LINEAR_IN = 8;
    public static final int FAST_OUT_SLOW_IN = 9;
    public static final int LINEAR_OUT_SLOW_IN = 10;

    public static final int DEFAULT_START_ANGLE = 0;
    public static final int DEFAULT_ANIMATION_DURATION = 400;
    static final float DEFAULT_MAX = 100;

    //##############################################################################################
    // BACKGROUND
    boolean mDrawBackground;
    Paint mBackgroundPaint;
    int mBackgroundColor;
    int mBackgroundOffset;

    int mAdaptiveBackgroundMode;
    float mAdaptiveBackgroundRatio;
    int mAdaptiveBackgroundColor;
    boolean mAdaptBackground;

    // PROGRESS
    Paint mProgressPaint;
    int mProgressColor;

    // TEXT
    Rect mTextBounds;
    Paint mTextPaint;
    float mTextSize;
    private int mTextStyle;
    int mTextColor;
    int mTextProgress;
    Typeface mTypeface;
    int mTextShadowColor;
    float mTextShadowRadius;
    float mTextShadowDistY;
    float mTextShadowDistX;
    int textHeight;
    String textValue;

    int mAdaptiveTextColor;
    int mAdaptiveTextMode;
    float mAdaptiveTextRatio;
    boolean mAdaptText;

    // COMMON
    RectF mBackgroundBounds;
    RectF mCircleBounds;
    ValueAnimator mColorAnimator;
    ValueAnimator mProgressAnimator;
    Interpolator mAnimInterpolator;
    int mAnimDuration;
    float mProgress;
    float mStartAngle;
    float mSweepAngle;

    int mAdaptiveColor;

    @ProgressOrientation
    int orientation;
    @Nullable
    PercentageChartView.AdaptiveColorProvider mAdaptiveColorProvider;

    IPercentageChartView mView;


    BaseModeRenderer(IPercentageChartView view) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = ORIENTATION_CLOCKWISE;

        //START DRAWING ANGLE
        mStartAngle = DEFAULT_START_ANGLE;

        //BACKGROUND DRAW STATE
        mDrawBackground = this instanceof PieModeRenderer;

        //BACKGROUND COLOR
        mBackgroundColor = Color.BLACK;

        //PROGRESS
        mProgress = mTextProgress = 0;

        //PROGRESS COLOR
        mProgressColor = Color.RED;

        //PROGRESS ANIMATION DURATION
        mAnimDuration = DEFAULT_ANIMATION_DURATION;

        //PROGRESS ANIMATION INTERPOLATOR
        mAnimInterpolator = new LinearInterpolator();

        //TEXT COLOR
        mTextColor = Color.WHITE;

        //TEXT SIZE
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                DEFAULT_TEXT_SP_SIZE,
                mView.getViewContext().getResources().getDisplayMetrics());

        //TEXT STYLE
        mTextStyle = Typeface.NORMAL;

        //TEXT SHADOW
        mTextShadowColor = Color.TRANSPARENT;
        mTextShadowRadius = 0;
        mTextShadowDistX = 0;
        mTextShadowDistY = 0;

        //ADAPTIVE BACKGROUND COLOR
        mAdaptBackground = false;
        mAdaptiveBackgroundRatio = mAdaptiveBackgroundMode = -1;

        //ADAPTIVE TEXT COLOR
        mAdaptText = false;
        mAdaptiveTextRatio = mAdaptiveTextMode = -1;

        //BACKGROUND OFFSET
        mBackgroundOffset = 0;
    }

    BaseModeRenderer(IPercentageChartView view, TypedArray attrs) {
        mView = view;

        //DRAWING ORIENTATION
        orientation = attrs.getInt(R.styleable.PercentageChartView_pcv_orientation, ORIENTATION_CLOCKWISE);

        //START DRAWING ANGLE
        mStartAngle = attrs.getInt(R.styleable.PercentageChartView_pcv_startAngle, DEFAULT_START_ANGLE);
        if (mStartAngle < 0 || mStartAngle > 360) {
            mStartAngle = DEFAULT_START_ANGLE;
        }

        //BACKGROUND DRAW STATE
        mDrawBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_drawBackground, this instanceof PieModeRenderer);

        //BACKGROUND COLOR
        mBackgroundColor = attrs.getColor(R.styleable.PercentageChartView_pcv_backgroundColor, Color.BLACK);

        //PROGRESS
        mProgress = attrs.getFloat(R.styleable.PercentageChartView_pcv_progress, 0);
        if (mProgress < 0) {
            mProgress = 0;
        } else if (mProgress > 100) {
            mProgress = 100;
        }
        mTextProgress = (int) mProgress;

        //PROGRESS COLOR
        mProgressColor = attrs.getColor(R.styleable.PercentageChartView_pcv_progressColor, getThemeAccentColor());

        //PROGRESS ANIMATION DURATION
        mAnimDuration = attrs.getInt(R.styleable.PercentageChartView_pcv_animDuration, DEFAULT_ANIMATION_DURATION);

        //PROGRESS ANIMATION INTERPOLATOR
        int interpolator = attrs.getInt(R.styleable.PercentageChartView_pcv_animInterpolator, DEFAULT_ANIMATION_INTERPOLATOR);
        switch (interpolator) {
            case LINEAR:
                mAnimInterpolator = new LinearInterpolator();
                break;
            case ACCELERATE:
                mAnimInterpolator = new AccelerateInterpolator();
                break;
            case DECELERATE:
                mAnimInterpolator = new DecelerateInterpolator();
                break;
            case ACCELERATE_DECELERATE:
                mAnimInterpolator = new AccelerateDecelerateInterpolator();
                break;
            case ANTICIPATE:
                mAnimInterpolator = new AnticipateInterpolator();
                break;
            case OVERSHOOT:
                mAnimInterpolator = new OvershootInterpolator();
                break;
            case ANTICIPATE_OVERSHOOT:
                mAnimInterpolator = new AnticipateOvershootInterpolator();
                break;
            case BOUNCE:
                mAnimInterpolator = new BounceInterpolator();
                break;
            case FAST_OUT_LINEAR_IN:
                mAnimInterpolator = new FastOutLinearInInterpolator();
                break;
            case FAST_OUT_SLOW_IN:
                mAnimInterpolator = new FastOutSlowInInterpolator();
                break;
            case LINEAR_OUT_SLOW_IN:
                mAnimInterpolator = new LinearOutSlowInInterpolator();
                break;
        }

        //TEXT COLOR
        mTextColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textColor, Color.WHITE);

        //TEXT SIZE
        mTextSize = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_textSize,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                        DEFAULT_TEXT_SP_SIZE,
                        mView.getViewContext().getResources().getDisplayMetrics()
                ));

        //TEXT TYPEFACE
        String typeface = attrs.getString(R.styleable.PercentageChartView_pcv_typeface);
        if (typeface != null && !typeface.isEmpty()) {
            mTypeface = Typeface.createFromAsset(mView.getViewContext().getResources().getAssets(), typeface);
        }

        //TEXT STYLE
        mTextStyle = attrs.getInt(R.styleable.PercentageChartView_pcv_textStyle, Typeface.NORMAL);
        if (mTextStyle > 0) {
            mTypeface = (mTypeface == null) ? Typeface.defaultFromStyle(mTextStyle) : Typeface.create(mTypeface, mTextStyle);
        }

        //TEXT SHADOW
        mTextShadowColor = attrs.getColor(R.styleable.PercentageChartView_pcv_textShadowColor, Color.TRANSPARENT);
        if (mTextShadowColor != Color.TRANSPARENT) {
            mTextShadowRadius = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowRadius, 0);
            mTextShadowDistX = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistX, 0);
            mTextShadowDistY = attrs.getFloat(R.styleable.PercentageChartView_pcv_textShadowDistY, 0);
        }

        //ADAPTIVE BACKGROUND COLOR
        mAdaptBackground = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveBackground, false);
        mAdaptiveBackgroundRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundRatio, -1);
        mAdaptiveBackgroundMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveBackgroundMode, -1);


        //ADAPTIVE TEXT COLOR
        mAdaptText = attrs.getBoolean(R.styleable.PercentageChartView_pcv_adaptiveText, false);
        mAdaptiveTextRatio = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextRatio, -1);
        mAdaptiveTextMode = attrs.getInt(R.styleable.PercentageChartView_pcv_adaptiveTextMode, -1);

        //BACKGROUND OFFSET
        mBackgroundOffset = attrs.getDimensionPixelSize(
                R.styleable.PercentageChartView_pcv_backgroundOffset,
                0);
    }

    //############################################################################################## BEHAVIOR
    public abstract void mesure(int w, int h, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom);

    public abstract void draw(Canvas canvas);

    public abstract void destroy();

    abstract void updateText();

    public abstract void setOrientation(int orientation);

    public abstract void setStartAngle(float startAngle);

    int getThemeAccentColor() {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.colorAccent;
        } else {
            colorAttr = mView.getViewContext().getResources().getIdentifier("colorAccent",
                    "attr",
                    mView.getViewContext().getPackageName()
            );
        }
        TypedValue outValue = new TypedValue();
        mView.getViewContext().getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    //############################################################################################## MODIFIERS
    public abstract void setAdaptiveColorProvider(@Nullable PercentageChartView.AdaptiveColorProvider adaptiveColorProvider);

    //PROGRESS
    public float getProgress() {
        return mProgress;
    }

    public abstract void setProgress(float progress, boolean animate);

    //DRAW BACKGROUND STATE
    public boolean isDrawBackgroundEnabled() {
        return mDrawBackground;
    }

    public void setDrawBackgroundEnabled(boolean drawBackground) {
        this.mDrawBackground = drawBackground;
        mView.invalidate();
    }

    //START ANGLE
    public float getStartAngle() {
        return mStartAngle;
    }

    //ORIENTATION
    public int getOrientation() {
        return orientation;
    }

    //BACKGROUND COLOR
    public int getBackgroundColor() {
        if (!mDrawBackground) return -1;
        return (!mAdaptBackground) ? mBackgroundColor : mAdaptiveBackgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        if (mAdaptiveColorProvider != null && mAdaptBackground) return;
        this.mBackgroundColor = backgroundColor;
        if (!mDrawBackground) return;
        mBackgroundPaint.setColor(mBackgroundColor);
        mView.invalidate();
    }

    //BACKGROUND OFFSET
    public float getBackgroundOffset() {
        if (!mDrawBackground) return -1;
        return mBackgroundOffset;
    }

    public void setBackgroundOffset(int backgroundOffset) {
        if (!mDrawBackground)
            return;
        this.mBackgroundOffset = backgroundOffset;
        mView.invalidate();
        mView.requestLayout();
    }

    //PROGRESS COLOR
    public int getProgressColor() {
        return (mAdaptiveColorProvider != null) ? mAdaptiveColor : mProgressColor;
    }

    public void setProgressColor(int progressColor) {
        if (mAdaptiveColorProvider != null) return;

        this.mProgressColor = progressColor;
        mProgressPaint.setColor(progressColor);
        mView.invalidate();
    }

    //ADAPTIVE BACKGROUND
    public boolean isAdaptiveBackgroundEnabled() {
        return mAdaptBackground;
    }

    public float getAdaptiveBackgroundRatio() {
        return mAdaptiveBackgroundRatio;
    }

    public int getAdaptiveBackgroundMode() {
        return mAdaptiveBackgroundMode;
    }

    public abstract void setAdaptiveBgEnabled(boolean enable);

    public abstract void setAdaptiveBackground(float ratio, int adaptiveMode);

    //ADAPTIVE TEXT
    public boolean isAdaptiveTextEnabled() {
        return mAdaptText;
    }

    public float getAdaptiveTextRatio() {
        return mAdaptiveTextRatio;
    }

    public int getAdaptiveTextMode() {
        return mAdaptiveTextMode;
    }

    public abstract void setAdaptiveTextEnabled(boolean enable);

    public abstract void setAdaptiveText(float ratio, int adaptiveMode);

    //ANIMATION DURATION
    public int getAnimationDuration() {
        return mAnimDuration;
    }

    public void setAnimationDuration(int duration) {
        mAnimDuration = duration;
        mProgressAnimator.setDuration(mAnimDuration);
        if (mColorAnimator != null)
            mColorAnimator.setDuration(mAnimDuration);
    }

    //ANIMATION INTERPOLATOR
    public TimeInterpolator getAnimationInterpolator() {
        return mProgressAnimator.getInterpolator();
    }

    public void setAnimationInterpolator(TimeInterpolator interpolator) {
        mProgressAnimator.setInterpolator(interpolator);
    }

    //TEXT COLOR
    public int getTextColor() {
        return (!mAdaptText) ? mTextColor : mAdaptiveTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        if (mAdaptiveColorProvider != null && mAdaptText)
            return;
        this.mTextColor = textColor;
        mTextPaint.setColor(textColor);
        mView.invalidate();
    }

    //TEXT SIZE
    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        this.mTextSize = textSize;
        mTextPaint.setTextSize(textSize);
        updateText();
        mView.invalidate();
    }

    //TEXT TYPEFACE
    public Typeface getTypeface() {
        return mTypeface;
    }

    public void setTypeface(Typeface typeface) {
        this.mTypeface = (mTextStyle > 0) ?
                Typeface.create(typeface, mTextStyle) :
                typeface;
        mTextPaint.setTypeface(mTypeface);
        updateText();
        mView.invalidate();
    }

    //TEXT STYLE
    public int getTextStyle() {
        return mTextStyle;
    }

    public void setTextStyle(int mTextStyle) {
        this.mTextStyle = mTextStyle;
        mTypeface = (mTypeface == null) ? Typeface.defaultFromStyle(mTextStyle) : Typeface.create(mTypeface, mTextStyle);

        mTextPaint.setTypeface(mTypeface);
        updateText();
        mView.invalidate();
    }

    //TEXT SHADOW
    public int getTextShadowColor() {
        return mTextShadowColor;
    }

    public float getTextShadowRadius() {
        return mTextShadowRadius;
    }

    public float getTextShadowDistY() {
        return mTextShadowDistY;
    }

    public float getTextShadowDistX() {
        return mTextShadowDistX;
    }

    public void setTextShadow(int shadowColor, float shadowRadius, float shadowDistX, float shadowDistY) {
        this.mTextShadowColor = shadowColor;
        this.mTextShadowRadius = shadowRadius;
        this.mTextShadowDistX = shadowDistX;
        this.mTextShadowDistY = shadowDistY;

        mTextPaint.setShadowLayer(mTextShadowRadius, mTextShadowDistX, mTextShadowDistY, mTextShadowColor);
        updateText();
        mView.invalidate();
    }

    //COPY RENDERER
    public void mirror(BaseModeRenderer renderer) {
        renderer.mDrawBackground = mDrawBackground;
        renderer.mBackgroundPaint = mBackgroundPaint;
        renderer.mBackgroundColor = mBackgroundColor;
        renderer.mBackgroundOffset = mBackgroundOffset;
        renderer.mAdaptiveBackgroundMode = mAdaptiveBackgroundMode;
        renderer.mAdaptiveBackgroundRatio = mAdaptiveBackgroundRatio;
        renderer.mAdaptiveBackgroundColor = mAdaptiveBackgroundColor;
        renderer.mAdaptBackground = mAdaptBackground;
        renderer.mProgressPaint = mProgressPaint;
        renderer.mProgressColor = mProgressColor;
        renderer.mTextBounds = mTextBounds;
        renderer.mTextPaint = mTextPaint;
        renderer.mTextSize = mTextSize;
        renderer.mTextStyle = mTextStyle;
        renderer.mTextColor = mTextColor;
        renderer.mTextProgress = mTextProgress;
        renderer.mTypeface = mTypeface;
        renderer.mTextShadowColor = mTextShadowColor;
        renderer.mTextShadowRadius = mTextShadowRadius;
        renderer.mTextShadowDistY = mTextShadowDistY;
        renderer.mTextShadowDistX = mTextShadowDistX;
        renderer.mAdaptiveTextColor = mAdaptiveTextColor;
        renderer.mAdaptiveTextMode = mAdaptiveTextMode;
        renderer.mAdaptiveTextRatio = mAdaptiveTextRatio;
        renderer.mAdaptText = mAdaptText;
        renderer.mBackgroundBounds = mBackgroundBounds;
        renderer.mCircleBounds = mCircleBounds;
        renderer.mColorAnimator = mColorAnimator;
        renderer.mProgressAnimator = mProgressAnimator;
        renderer.mAnimInterpolator = mAnimInterpolator;
        renderer.mAnimDuration = mAnimDuration;
        renderer.mProgress = mProgress;
        renderer.mStartAngle = mStartAngle;
        renderer.mSweepAngle = mSweepAngle;
        renderer.mAdaptiveColor = mAdaptiveColor;
        renderer.orientation = orientation;
        renderer.mAdaptiveColorProvider = mAdaptiveColorProvider;
    }
}