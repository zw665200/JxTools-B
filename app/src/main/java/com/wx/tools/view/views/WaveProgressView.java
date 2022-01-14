package com.wx.tools.view.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.Rect;
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
public class WaveProgressView extends View {

    //默认波纹颜色
    private static final int WAVE_PAINT_COLOR = 0xccffffff;
    private static final int WAVE_PAINT_COLOR2 = 0xb2ffffff;
    private static final int WAVE_PAINT_COLOR3 = 0xffffffff;
    //默认外环颜色
    private static final int OUTER_RING_COLOR = 0xFF21b792;
    //内环颜色
    private static final int OUTER_RING2_COLOR = 0xFF37ebc2;
    //进度条填充颜色
    private static final int OUTER_RING3_COLOR = 0xFFffffff;
    // y = Asin(wx+b)+h
    private static final float STRETCH_FACTOR_A = 40;
    // 第一条水波移动速度
    private static final int TRANSLATE_X_SPEED_ONE = 6;
    // 第二条水波移动速度
    private static final int TRANSLATE_X_SPEED_TWO = 5;
    // 第三条水波移动速度
    private static final int TRANSLATE_X_SPEED_THREE = 3;

    protected Context mContext;
    private float strokeWidth;
    private float waveHeight;
    private int wavePaintColor, wavePaintColor2, wavePaintColor3;
    private @BindingText
    int bindingText;

    private Paint mWavePaint, mWavePaint2, mWavePaint3;
    private DrawFilter mDrawFilter;
    private Path cirPath;
    private Rect textRect;

    private int width, height;
    private float[] mYPositions, mResetOneYPositions, mResetTwoYPositions,mResetThreeYPositions;
    private int mXOffsetSpeedOne, mXOffsetSpeedTwo, mXOffsetSpeedThree;
    private int mXOneOffset, mXTwoOffset, mXThreeOffset;

    @IntDef({NONE, TOP_TEXT, CENTER_TEXT, BOTTOM_TEXT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface BindingText {
    }

    public static final int NONE = 0;
    public static final int TOP_TEXT = 1;
    public static final int CENTER_TEXT = 2;
    public static final int BOTTOM_TEXT = 3;

    public WaveProgressView(Context context) {
        super(context);
        init(context, null);
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public WaveProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.WaveProgressView);
        bindingText = array.getInt(R.styleable.WaveProgressView_bindingText, NONE);
        wavePaintColor = array.getColor(R.styleable.WaveProgressView_wave_color, WAVE_PAINT_COLOR);
        wavePaintColor2 = array.getColor(R.styleable.WaveProgressView_wave_color, WAVE_PAINT_COLOR2);
        wavePaintColor3 = array.getColor(R.styleable.WaveProgressView_wave_color, WAVE_PAINT_COLOR3);
        waveHeight = array.getDimension(R.styleable.WaveProgressView_wave_height, STRETCH_FACTOR_A);
        strokeWidth = array.getDimension(R.styleable.WaveProgressView_stroke_width, getResources().getDimension(R.dimen.stroke_width));

        mWavePaint = new Paint();
        mWavePaint.setAntiAlias(true);
        mWavePaint.setStyle(Paint.Style.FILL);
        mWavePaint.setColor(wavePaintColor);

        mWavePaint2 = new Paint();
        mWavePaint2.setAntiAlias(true);
        mWavePaint2.setStyle(Paint.Style.FILL);
        mWavePaint2.setColor(wavePaintColor2);

        mWavePaint3 = new Paint();
        mWavePaint3.setAntiAlias(true);
        mWavePaint3.setStyle(Paint.Style.FILL);
        mWavePaint3.setColor(wavePaintColor3);


        mDrawFilter = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

        textRect = new Rect();
        cirPath = new Path();
        array.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
//            int size = dp2px(mContext, 150);
//            setMeasuredDimension(size, size);
//        } else if (widthMode == MeasureSpec.AT_MOST) {
//            setMeasuredDimension(dp2px(mContext, 150), MeasureSpec.getSize(heightMeasureSpec));
//        } else if (heightMode == MeasureSpec.AT_MOST) {
//            int size = MeasureSpec.getSize(widthMeasureSpec);
//            setMeasuredDimension(size, size);
//        } else {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        // 用于保存原始波纹的y值
        mYPositions = new float[width];
        // 用于保存波纹一的y值
        mResetOneYPositions = new float[width];
        // 用于保存波纹二的y值
        mResetTwoYPositions = new float[width];
        //用于保存波纹三的y值
        mResetThreeYPositions = new float[width];

        // 将周期定意
        float mCycleFactorW = (float) (2 * Math.PI / width);

        // 根据view总宽度得出所有对应的y值
        for (int i = 0; i < width; i++) {
            mYPositions[i] = (float) (waveHeight * Math.sin(mCycleFactorW * i) - waveHeight);
        }

        // 将dp转化为px，用于控制不同分辨率上移动速度基本一致
        mXOffsetSpeedOne = dp2px(mContext, TRANSLATE_X_SPEED_ONE) * width / dp2px(mContext, 330);
        mXOffsetSpeedTwo = dp2px(mContext, TRANSLATE_X_SPEED_TWO) * width / dp2px(mContext, 330);
        mXOffsetSpeedThree = dp2px(mContext, TRANSLATE_X_SPEED_THREE) * width / dp2px(mContext, 330);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.setDrawFilter(mDrawFilter);
        canvas.save();

        resetPositionY();
        float proHeight = dp2px(mContext, 50);
        for (int i = 0; i < width; i++) {
            // 绘制第一条水波纹
            canvas.drawLine(i, proHeight - mResetOneYPositions[i], i, height, mWavePaint);
            // 绘制第二条水波纹
            canvas.drawLine(i, proHeight - mResetTwoYPositions[i], i, height, mWavePaint2);
            // 绘制第三条水波纹
            canvas.drawLine(i, proHeight - mResetThreeYPositions[i], i, height, mWavePaint3);

        }
        canvas.restore();

        // 改变两条波纹的移动点
        mXOneOffset += mXOffsetSpeedOne;
        mXTwoOffset += mXOffsetSpeedTwo;
        mXThreeOffset += mXOffsetSpeedThree;
        // 如果已经移动到结尾处，则重头记录
        if (mXOneOffset >= width)
            mXOneOffset = 0;
        if (mXTwoOffset > width)
            mXTwoOffset = 0;
        if (mXThreeOffset > width)
            mXThreeOffset = 0;
        if (waveHeight > 0)
            postInvalidate();
    }

    private void resetPositionY() {
        // mXOneOffset代表当前第一条水波纹要移动的距离
        int oneInterval = mYPositions.length - mXOneOffset;

        // 重新填充第一条波纹的数据
        System.arraycopy(mYPositions, mXOneOffset, mResetOneYPositions, 0, oneInterval);
        System.arraycopy(mYPositions, 0, mResetOneYPositions, oneInterval, mXOneOffset);

        // 重新填充第二条波纹的数据
        int twoInterval = mYPositions.length - mXTwoOffset;
        System.arraycopy(mYPositions, mXTwoOffset, mResetTwoYPositions, 0, twoInterval);
        System.arraycopy(mYPositions, 0, mResetTwoYPositions, twoInterval, mXTwoOffset);

        // 重新填充第三条波纹的数据
        int threeInterval = mYPositions.length - mXThreeOffset;
        System.arraycopy(mYPositions, mXThreeOffset, mResetThreeYPositions, 0, threeInterval);
        System.arraycopy(mYPositions, 0, mResetThreeYPositions, threeInterval, mXThreeOffset);
    }

    private int dp2px(Context context, float dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }


    public float getStrokeWidth() {
        return strokeWidth;
    }

    public int getWaveColor() {
        return wavePaintColor;
    }

    public void setWaveColor(int wavePaintColor) {
        mWavePaint.setColor(wavePaintColor);
        invalidate();
    }

}

