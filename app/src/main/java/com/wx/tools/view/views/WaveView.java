package com.wx.tools.view.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;


import com.wx.tools.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by BlueFire on 2019/9/26  10:56
 * Describe:
 */
public class WaveView extends View {

    //默认波纹颜色
    private static final int WAVE_PAINT_COLOR = 0xffDEEFFF;
    //默认外环颜色
    private static final int OUTER_RING_COLOR = 0xFFDEEFFF;
    //内环颜色
    private static final int OUTER_RING2_COLOR = 0xFFffffff;
    //进度条填充颜色
    private static final int OUTER_RING3_COLOR = 0xFF29C1A3;
    // y = Asin(wx+b)+h
    private static final float STRETCH_FACTOR_A = 15;
    // 第一条水波移动速度
    private static final int TRANSLATE_X_SPEED_ONE = 5;
    // 第二条水波移动速度
    private static final int TRANSLATE_X_SPEED_TWO = 3;

    protected Context mContext;
    private String topText, centerText, bottomText;
    private float topTextSize, centerTextSize, bottomTextSize;
    private float strokeWidth;
    private float waveHeight;
    private int topTextColor, centerTextColor, bottomTextColor, wavePaintColor, strokeColor, submergedTextColor, strokeColor2, strokeColor3;
    private int max, progress;
    private @BindingText
    int bindingText;

    private Paint mWavePaint, topPaint, centerPaint, bottomPaint, outerRingPaint, outerRingPaint2, outerRingPaint3;
    private DrawFilter mDrawFilter;
    private Rect textRect;
    private Path cirPath;

    private int width, height;
    private float[] mYPositions, mResetOneYPositions, mResetTwoYPositions;
    private int mXOffsetSpeedOne, mXOffsetSpeedTwo;
    private int mXOneOffset, mXTwoOffset;

    public static final int[] SWEEP_GRADIENT_COLORS = new int[]{0xff29C1A3, 0xff3CE6CC, 0xff57DBD0};

    @IntDef({NONE, TOP_TEXT, CENTER_TEXT, BOTTOM_TEXT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface BindingText {
    }

    public static final int NONE = 0;
    public static final int TOP_TEXT = 1;
    public static final int CENTER_TEXT = 2;
    public static final int BOTTOM_TEXT = 3;

    public WaveView(Context context) {
        super(context);
        init(context, null);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressView);
        bindingText = array.getInt(R.styleable.WaveProgressView_bindingText, NONE);
        wavePaintColor = array.getColor(R.styleable.WaveProgressView_wave_color, WAVE_PAINT_COLOR);
        submergedTextColor = array.getColor(R.styleable.WaveProgressView_submerged_textColor, 0xffffff0);
        max = array.getInt(R.styleable.WaveProgressView_max, 100);
        waveHeight = array.getDimension(R.styleable.WaveProgressView_wave_height, STRETCH_FACTOR_A);
        strokeWidth = array.getDimension(R.styleable.WaveProgressView_stroke_width, getResources().getDimension(R.dimen.stroke_width));
        strokeColor = array.getColor(R.styleable.WaveProgressView_stroke_color, OUTER_RING_COLOR);
        strokeColor2 = array.getColor(R.styleable.WaveProgressView_stroke_color, OUTER_RING2_COLOR);
        strokeColor3 = array.getColor(R.styleable.WaveProgressView_stroke_color, OUTER_RING3_COLOR);
        topText = array.getString(R.styleable.WaveProgressView_top_text);
        topTextColor = array.getColor(R.styleable.WaveProgressView_top_textColor, Color.BLACK);
        topTextSize = array.getDimension(R.styleable.WaveProgressView_top_textSize, getResources().getDimension(R.dimen.top_text_size));
        centerText = array.getString(R.styleable.WaveProgressView_center_text);
        centerTextColor = array.getColor(R.styleable.WaveProgressView_center_textColor, Color.BLACK);
        centerTextSize = array.getDimension(R.styleable.WaveProgressView_center_textSize, getResources().getDimension(R.dimen.center_text_size));
        bottomText = array.getString(R.styleable.WaveProgressView_bottom_text);
        bottomTextColor = array.getColor(R.styleable.WaveProgressView_bottom_textColor, Color.BLACK);
        bottomTextSize = array.getDimension(R.styleable.WaveProgressView_bottom_textSize, getResources().getDimension(R.dimen.bottom_text_size));
        if (topText == null) topText = "";
        if (centerText == null) centerText = "";
        if (bottomText == null) bottomText = "";
        outerRingPaint = new Paint();
        outerRingPaint.setAntiAlias(true);
        outerRingPaint.setStrokeWidth(strokeWidth);
        outerRingPaint.setColor(strokeColor);
        outerRingPaint.setStyle(Paint.Style.STROKE);
        outerRingPaint.setStrokeCap(Paint.Cap.ROUND);

        outerRingPaint2 = new Paint();
        outerRingPaint2.setAntiAlias(true);
        outerRingPaint2.setColor(strokeColor2);
        outerRingPaint2.setStyle(Paint.Style.FILL);


        outerRingPaint3 = new Paint();
        outerRingPaint3.setAntiAlias(true);
        outerRingPaint3.setStrokeWidth(strokeWidth);
        outerRingPaint3.setColor(strokeColor3);
        outerRingPaint3.setStyle(Paint.Style.STROKE);
        outerRingPaint3.setStrokeCap(Paint.Cap.ROUND);


        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(wavePaintColor);
        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        topPaint = new Paint();
        topPaint.setAntiAlias(true);
        topPaint.setTextSize(topTextSize);

        centerPaint = new Paint();
        centerPaint.setAntiAlias(true);
        centerPaint.setTextSize(centerTextSize);

        bottomPaint = new Paint();
        bottomPaint.setAntiAlias(true);
        bottomPaint.setTextSize(bottomTextSize);
        bottomPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        int progress = array.getInt(R.styleable.WaveProgressView_progress, 0);
        setProgress(progress);
        textRect = new Rect();
        cirPath = new Path();
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            int size = dp2px(mContext, 150);
            setMeasuredDimension(size, size);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(dp2px(mContext, 150), MeasureSpec.getSize(heightMeasureSpec));
        } else if (heightMode == MeasureSpec.AT_MOST) {
            int size = MeasureSpec.getSize(widthMeasureSpec);
            setMeasuredDimension(size, size);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w < h)
            width = height = w;
        else
            width = height = h;
        // 用于保存原始波纹的y值
        mYPositions = new float[width];
        // 用于保存波纹一的y值
        mResetOneYPositions = new float[width];
        // 用于保存波纹二的y值
        mResetTwoYPositions = new float[width];
        // 将周期定意
        float mCycleFactorW = (float) (2 * Math.PI / width);
        // 根据view总宽度得出所有对应的y值
        for (int i = 0; i < width; i++) {
            mYPositions[i] = (float) (waveHeight * Math.sin(mCycleFactorW * i) - waveHeight);
        }
        cirPath.addCircle(width / 2.0f, height / 2.0f, width / 2.0f - strokeWidth + 0.3f, Path.Direction.CW);
        // 将dp转化为px，用于控制不同分辨率上移动速度基本一致
        mXOffsetSpeedOne = dp2px(mContext, TRANSLATE_X_SPEED_ONE) * width / dp2px(mContext, 330);
        mXOffsetSpeedTwo = dp2px(mContext, TRANSLATE_X_SPEED_TWO) * width / dp2px(mContext, 330);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        canvas.save();
        // 从canvas层面去除绘制时锯齿
        canvas.clipPath(cirPath);//裁剪
        resetPositionY();
        float proHeight = height - (float) progress / max * (height - 4 * strokeWidth) - 2 * strokeWidth;
        for (int i = 0; i < width; i++) {
            // 绘制第一条水波纹
            canvas.drawLine(i, proHeight - mResetOneYPositions[i], i, height, mWavePaint);
            // 绘制第二条水波纹
            canvas.drawLine(i, proHeight - mResetTwoYPositions[i], i, height, mWavePaint);
        }
        canvas.restore();

        if (strokeWidth > 0) {
            //绘制外环
            canvas.drawCircle(width / 2.0f, height / 2.0f, width / 2.0f, outerRingPaint2);

            //绘制内环
            RectF rf1 = new RectF(1.2f * strokeWidth, 1.2f * strokeWidth, width - 1.2f * strokeWidth, width - strokeWidth);
            canvas.drawArc(rf1, 130, 280, false, outerRingPaint);

            if (progress > 0) {
                RectF rf = new RectF(1.2f * strokeWidth, 1.2f * strokeWidth, width - 1.2f * strokeWidth, width - strokeWidth);
                LinearGradient shader = new LinearGradient(0, height / 2.0f, width,
                        height / 2.0f, SWEEP_GRADIENT_COLORS, null, Shader.TileMode.MIRROR);
                outerRingPaint3.setShader(shader);
                canvas.drawArc(rf, 130, (float) (progress * 2.8), false, outerRingPaint3);

                //半径
                float radius = (width - 2 * strokeWidth) / 2.0f;
                //弧度
                float angle = (float) (progress * 2.8);
                //画弧度的小圆
//                canvas.drawCircle((float) (width / 2.0f - radius * Math.cos(angle)),
//                        (float) (width / 2.0f + radius * Math.sin(angle)), strokeWidth / 2,
//                        outerRingPaint3);
            }

            //画文字
            float centerTextY = height / 2.0f + textRect.height() / 2.0f;
            float bottomTextY = height / 2.0f + textRect.height() / 2.0f;

            //百分号
            centerPaint.setColor(centerTextY > proHeight + textRect.height() ?
                    (submergedTextColor == 0xffffff0 ? centerTextColor : submergedTextColor) : centerTextColor);
            centerPaint.getTextBounds(centerText, 0, centerText.length(), textRect);
            canvas.drawText(centerText, width / 2.0f + textRect.width() * 1.2f, centerTextY, centerPaint);

            //百分比
            bottomPaint.setColor(bottomTextY > proHeight + textRect.height() ?
                    (submergedTextColor == 0xffffff0 ? bottomTextColor : submergedTextColor) : bottomTextColor);
            bottomPaint.getTextBounds(bottomText, 0, bottomText.length(), textRect);
            canvas.drawText(bottomText, width / 2.0f - textRect.width() / 1.5f, bottomTextY, bottomPaint);

        }

        // 改变两条波纹的移动点
        mXOneOffset += mXOffsetSpeedOne;
        mXTwoOffset += mXOffsetSpeedTwo;
        // 如果已经移动到结尾处，则重头记录
        if (mXOneOffset >= width)
            mXOneOffset = 0;
        if (mXTwoOffset > width)
            mXTwoOffset = 0;
        if (waveHeight > 0)
            postInvalidate();
    }

    private void resetPositionY() {
        // mXOneOffset代表当前第一条水波纹要移动的距离
        int oneInterval = mYPositions.length - mXOneOffset;
        // 重新填充第一条波纹的数据
        System.arraycopy(mYPositions, mXOneOffset, mResetOneYPositions, 0, oneInterval);
        System.arraycopy(mYPositions, 0, mResetOneYPositions, oneInterval, mXOneOffset);

        int twoInterval = mYPositions.length - mXTwoOffset;
        System.arraycopy(mYPositions, mXTwoOffset, mResetTwoYPositions, 0, twoInterval);
        System.arraycopy(mYPositions, 0, mResetTwoYPositions, twoInterval, mXTwoOffset);
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        invalidate();
    }

    public int getProgress() {
        return progress;
    }

    /**
     * 可在子线程中刷新
     *
     * @param progress 进度
     */
    @SuppressLint("DefaultLocale")
    public void setProgress(int progress) {
        this.progress = progress;
        String content = String.valueOf(progress * 100 / max);
        switch (bindingText) {
            case TOP_TEXT:
                topText = content;
                break;
            case CENTER_TEXT:
                centerText = content;
                break;
            case BOTTOM_TEXT:
                bottomText = content;
                break;
            case NONE:
                break;
        }
        postInvalidate();
    }

    public int getBindingText() {
        return bindingText;
    }

    public void setBindingText(@BindingText int bindingText) {
        this.bindingText = bindingText;
    }

    public String getTopText() {
        return topText;
    }

    public void setTopText(String topText) {
        this.topText = topText == null ? "" : topText;
        invalidate();
    }

    public String getCenterText() {
        return centerText;
    }

    public void setCenterText(String centerText) {
        this.centerText = centerText == null ? "" : centerText;
        invalidate();
    }

    public String getBottomText() {
        return bottomText;
    }

    public void setBottomText(String bottomText) {
        this.bottomText = bottomText == null ? "" : bottomText;
        invalidate();
    }

    public float getTopTextSize() {
        return topTextSize;
    }

    public void setTopTextSize(float topTextSize) {
        topPaint.setTextSize(topTextSize);
        invalidate();
    }

    public float getCenterTextSize() {
        return centerTextSize;
    }

    public void setCenterTextSize(float centerTextSize) {
        centerPaint.setTextSize(centerTextSize);
        invalidate();
    }

    public float getBottomTextSize() {
        return bottomTextSize;
    }

    public void setBottomTextSize(float bottomTextSize) {
        bottomPaint.setTextSize(bottomTextSize);
        invalidate();
    }

    public int getTopTextColor() {
        return topTextColor;
    }

    public void setTopTextColor(int topTextColor) {
        this.topTextColor = topTextColor;
        invalidate();
    }

    public int getCenterTextColor() {
        return centerTextColor;
    }

    public void setCenterTextColor(int centerTextColor) {
        this.centerTextColor = centerTextColor;
        invalidate();
    }

    public int getBottomTextColor() {
        return bottomTextColor;
    }

    public void setBottomTextColor(int bottomTextColor) {
        this.bottomTextColor = bottomTextColor;
        invalidate();
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        invalidate();
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        outerRingPaint.setColor(strokeColor);
        invalidate();
    }

    public int getWaveColor() {
        return wavePaintColor;
    }

    public void setWaveColor(int wavePaintColor) {
        mWavePaint.setColor(wavePaintColor);
        invalidate();
    }

}

