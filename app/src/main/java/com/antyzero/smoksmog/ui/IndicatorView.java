package com.antyzero.smoksmog.ui;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ViewAnimator;

import pl.malopolska.smoksmog.model.Particulate;

public class IndicatorView extends View {

    private static final float GAP_ANGLE_DEFAULT = 90;
    private static final int STROKE_WIDTH_DEFAULT = 10;
    public static final int STEP_COLOR = 50;

    private float gapAngle = GAP_ANGLE_DEFAULT;

    private final ValueAnimator valueAnimator = ValueAnimator.ofFloat( 0f );

    private Paint paintArcBackground;
    private Paint paintArcForeground;
    private Paint paintNameShort;
    private Paint paintValue;
    private Paint paintMaxValue;

    private float startAngle;
    private float sweepAngle;
    private float strokeWidth;
    private float canvasHorizontalMiddle;
    private float canvasVerticalMiddle;

    private int currentWidth;
    private int currentHeight;

    private float value = 0f;
    private float arcValue = 0f;
    private float textNameSize = 10f;

    private RectF arcRect = new RectF();

    private int overLap = 0;

    private String valueText = "";

    /**
     * Setup variables
     */ {
        valueAnimator.setDuration( 1000L );
        valueAnimator.setInterpolator( new AccelerateDecelerateInterpolator() );
    }

    public IndicatorView( Context context ) {
        super( context );
        init( context );
    }

    public IndicatorView( Context context, AttributeSet attrs ) {
        super( context, attrs );
        init( context );
    }

    public IndicatorView( Context context, AttributeSet attrs, int defStyleAttr ) {
        super( context, attrs, defStyleAttr );
        init( context );
    }

    @SuppressWarnings( "unused" )
    @TargetApi( 21 )
    public IndicatorView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes ) {
        super( context, attrs, defStyleAttr, defStyleRes );
        init( context );
    }

    private void init( Context context ) {

        strokeWidth = dipToPixels( context, STROKE_WIDTH_DEFAULT );
        textNameSize = dipToPixels( context, 32 );

        // Init paints

        paintArcBackground = new Paint();
        paintArcBackground.setAntiAlias( true );
        paintArcBackground.setStrokeCap( Paint.Cap.ROUND );
        paintArcBackground.setStrokeWidth( strokeWidth - 2 );
        paintArcBackground.setStyle( Paint.Style.STROKE );

        paintArcForeground = new Paint( paintArcBackground );
        paintArcForeground.setAntiAlias( true );
        paintArcForeground.setStrokeCap( Paint.Cap.ROUND );
        paintArcForeground.setStyle( Paint.Style.STROKE );
        paintArcForeground.setColor( Color.CYAN );
        paintArcForeground.setStrokeWidth( strokeWidth );

        paintValue = new Paint();
        paintValue.setAntiAlias( true );
        paintValue.setColor( Color.RED );
        paintValue.setStyle( Paint.Style.FILL );
        paintValue.setTypeface( Typeface.defaultFromStyle( Typeface.BOLD ) );

        paintMaxValue = new Paint();
        paintMaxValue.setAntiAlias( true );
        paintMaxValue.setColor( Color.RED );
        paintMaxValue.setStyle( Paint.Style.FILL );
        paintMaxValue.setTypeface( Typeface.defaultFromStyle( Typeface.BOLD ) );

        paintNameShort = new Paint();
        paintNameShort.setAntiAlias( true );
        paintNameShort.setColor( Color.RED );
        paintNameShort.setStyle( Paint.Style.FILL );
        paintNameShort.setTextSize( textNameSize );

        calculateMinorDrawingVariables();
    }

    @Override
    protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
        int widthMode = MeasureSpec.getMode( widthMeasureSpec );
        int widthSize = MeasureSpec.getSize( widthMeasureSpec );
        int heightMode = MeasureSpec.getMode( heightMeasureSpec );
        int heightSize = MeasureSpec.getSize( heightMeasureSpec );

        int size;
        if ( widthMode == MeasureSpec.EXACTLY && widthSize > 0 ) {
            size = widthSize;
        } else if ( heightMode == MeasureSpec.EXACTLY && heightSize > 0 ) {
            size = heightSize;
        } else {
            size = widthSize < heightSize ? widthSize : heightSize;
        }

        int widthNewSpec = MeasureSpec.makeMeasureSpec( size, MeasureSpec.EXACTLY );
        int heightNewSpec = MeasureSpec.makeMeasureSpec( size, MeasureSpec.EXACTLY );
        super.onMeasure( widthNewSpec, heightNewSpec );
    }

    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh ) {
        super.onSizeChanged( w, h, oldw, oldh );
        currentWidth = w;
        currentHeight = h;
        calculateDrawingVariables();
    }

    private static float dipToPixels( Context context, float dipValue ) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics );
    }

    /**
     * As 0% -> 100% -> more
     *
     * @param newValue
     */
    public void setValue( float newValue ) {

        if ( valueAnimator.isRunning() ) {
            valueAnimator.cancel();
        }

        valueAnimator.setFloatValues( this.value, newValue );
        valueAnimator.addUpdateListener( animation -> {
            this.value = ( float ) animation.getAnimatedValue();
            recalculateDuringAnimation();
        } );
        valueAnimator.addListener( new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart( Animator animation ) {
                // Do nothing
            }

            @Override
            public void onAnimationEnd( Animator animation ) {
                // Do nothing
            }

            @Override
            public void onAnimationCancel( Animator animation ) {
                if ( animation instanceof ValueAnimator ) {
                    ValueAnimator valueAnimator = ( ValueAnimator ) animation;
                    IndicatorView.this.value = ( float ) valueAnimator.getAnimatedValue();
                }
            }

            @Override
            public void onAnimationRepeat( Animator animation ) {
                // Do nothing
            }
        } );
        valueAnimator.start();
    }

    /**
     * This should be called when animating values
     */
    private void recalculateDuringAnimation() {

        this.arcValue = ( value * sweepAngle ) % sweepAngle;
        this.valueText = String.format( "%.0f%%", value * 100 );
        this.overLap = ( int ) Math.floor( value );
        int red = ( int ) Math.min( STEP_COLOR * overLap + STEP_COLOR * value, 255 );

        this.paintArcBackground.setARGB( 255, red, 0, 0 );
        this.paintValue.setARGB( 255, red, 0, 0 ); // TODO change text color to red in case of over limits

        postInvalidateOnAnimation();
    }

    /**
     * Non ui dependant variables
     */
    private void calculateMinorDrawingVariables() {
        startAngle = 90f + gapAngle / 2;
        sweepAngle = 360 - gapAngle;
    }

    /**
     * Recalculate some variables, important when view changes its size
     */
    private void calculateDrawingVariables() {
        calculateMinorDrawingVariables();

        final float halfStrokeWidth = strokeWidth / 2;

        //noinspection SuspiciousNameCombination
        arcRect = new RectF(
                halfStrokeWidth,
                halfStrokeWidth,
                currentWidth - halfStrokeWidth,
                currentWidth - halfStrokeWidth );

        canvasHorizontalMiddle = currentWidth / 2;
        canvasVerticalMiddle = currentWidth / 2;

        paintValue.setTextSize( ( arcRect.bottom - arcRect.top ) / 4f );

        postInvalidateOnAnimation();
    }

    @Override
    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );

        canvas.drawArc( arcRect, startAngle, sweepAngle, false, paintArcBackground );

        // TODO if overlap is > 0 draw pre background ?

        canvas.drawArc( arcRect, startAngle, arcValue, false, paintArcForeground );
        drawTextCentred( canvas, paintValue, valueText, arcRect );
    }

    private void drawTextCentred( Canvas canvas, Paint paint, String text, RectF bounds ) {
        drawTextCentred( canvas, paint, text, bounds.centerX(), bounds.centerY(), new Rect() );
    }

    private void drawTextCentred( Canvas canvas, Paint paint, String text, float cx, float cy ) {
        drawTextCentred( canvas, paint, text, cx, cy, new Rect() );
    }

    private void drawTextCentred( Canvas canvas, Paint paint, String text, float cx, float cy, Rect textBounds ) {
        paint.getTextBounds( text, 0, text.length(), textBounds );
        canvas.drawText( text, cx - textBounds.exactCenterX(), cy - textBounds.exactCenterY(), paint );
    }
}
