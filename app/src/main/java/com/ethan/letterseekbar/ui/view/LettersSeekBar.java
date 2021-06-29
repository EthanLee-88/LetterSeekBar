package com.ethan.letterseekbar.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.ethan.letterseekbar.R;

import androidx.annotation.Nullable;

/*******
 * 字母索引表
 * created by Ethan Lee
 * on 2021/2/13
 *******/
public class LettersSeekBar extends View {
    private static final String TAG = "LettersSeekBar";

    /**
     * 字体颜色
     */
    private int normalColor = Color.GRAY;

    /**
     * 高亮颜色
     */
    private int highLightColor = Color.BLUE;

    /**
     * 画字母
     */
    private Paint normalPaint, highLinePaint;

    /**
     * 字体大小
     */
    private float lettersTextSize = 12;

    private float currentY = -1;

    private static final String[] lettersList = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
            "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X",
            "Y", "Z",};

    /**
     * 当前选中的字母索引
     */
    private int currentPosition = -1;

    /**
     * 字母变化回调
     */
    private LetterChangeListener mLetterChangeListener;

    public LettersSeekBar(Context context) {
        this(context, null);
    }

    public LettersSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LettersSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initRes(context, attrs, defStyleAttr);
    }

    private void initRes(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LettersSeekBar);
        normalColor = typedArray.getColor(R.styleable.LettersSeekBar_LetterNormalColor, normalColor);
        highLightColor = typedArray.getColor(R.styleable.LettersSeekBar_LetterHighLightColor, highLightColor);
        lettersTextSize = typedArray.getDimension(R.styleable.LettersSeekBar_LettersTextSize, lettersTextSize);
        Log.d(TAG, "lettersTextSize=" + lettersTextSize + "spToPx=" + spToPx(12));

        normalPaint = new Paint();
        normalPaint.setColor(normalColor);
        normalPaint.setAntiAlias(true);
        normalPaint.setDither(true);
        normalPaint.setTextSize(lettersTextSize);

        highLinePaint = new Paint();
        highLinePaint.setColor(highLightColor);
        highLinePaint.setAntiAlias(true);
        highLinePaint.setDither(true);
        highLinePaint.setTextSize(59f);
        typedArray.recycle();
    }

    private float spToPx(int sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //控件的宽度取决于字体大小，字体大小+paddingLeft+paddingRight
        int textWidth = (int) normalPaint.measureText("A");//测量字体大小
        int width = textWidth + getPaddingLeft() + getPaddingRight();//计算出控件宽度
        int height = MeasureSpec.getSize(heightMeasureSpec) + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
        int itemHeight = getHeight() / lettersList.length; //每个Item的高度等于控件高度除以字母数
        int itemWidth = getWidth(); //获取Item宽度

        //计算出Y方向基线与中线的距离dy
        Paint.FontMetrics fontMetrics = normalPaint.getFontMetrics();
        int dy = (int) ((fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom);

        int x;
        int textWidth = (int) normalPaint.measureText(lettersList[0]);//测量每个字体大小
        //计算出绘制字体X方向的起始位置
        x = itemWidth - getPaddingRight() - textWidth / 2;

        for (int i = 0; i < lettersList.length; i++) {
            //计算出每个Item的Y方向上的中心
            int itemCenterY = itemHeight * i + itemHeight / 2 + getPaddingTop();

            //基线的位置等于 中线的位置 + dy
            int baseLine = itemCenterY + dy;

            // 将与被选中的item相邻的几个item的 x位置向左移
            if ((currentY > 0) && (Math.abs(currentY - itemCenterY) < 3 * itemHeight)) {
                if (i == currentPosition) { //选中的item
                    canvas.drawText(lettersList[i],
                            (float) (x - (3.5 * itemHeight - Math.abs(currentY - itemCenterY))), baseLine, normalPaint);
                    canvas.drawText(lettersList[i],
                            (x - (6 * itemHeight - Math.abs(currentY - itemCenterY))), baseLine, highLinePaint);// 再画个大的
                }else {
                    canvas.drawText(lettersList[i], x - (3 * itemHeight - Math.abs(currentY - itemCenterY)), baseLine, normalPaint);
                }
            } else {
                canvas.drawText(lettersList[i], x, baseLine, normalPaint);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 缩小触控区域
        if (event.getX() < (getWidth() - 2 * getPaddingRight() - normalPaint.measureText(lettersList[0]))) {
            if (!((currentY == -1) && (currentPosition == -1))) {
                currentY = -1;
                currentPosition = -1;
                invalidate();
            }
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                //获取当前触点Y的值，再除以item的高度，得到触点的position
                currentY = event.getY();
                int itemHeight = getHeight() / lettersList.length;
                int newPosition = (int) (currentY / itemHeight);
                //防止数组越界
                if (newPosition < 0) newPosition = 0;
                if (newPosition > lettersList.length - 1) newPosition = lettersList.length - 1;
                //绘制优化，防止不必要的重复绘制
                if (newPosition == currentPosition) return true;
                currentPosition = newPosition;
                if (mLetterChangeListener != null) {
                    mLetterChangeListener.letterChange(lettersList[currentPosition], false);
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                currentY = -1;
                if (mLetterChangeListener != null) {
                    mLetterChangeListener.letterChange(lettersList[currentPosition], true);
                }
                invalidate();
                break;

            default:
                currentY = -1;
                currentPosition = -1;
                invalidate();
                break;
        }
        return true;
//        return super.onTouchEvent(event); // return true 表示拦截后续事件，如果是false则不会再拦截MOVE事件
    }

    public interface LetterChangeListener {
        void letterChange(String letter, boolean actionUp);//字母回调，ACTION_UP时消失
    }

    public void setLetterChangeListener(LetterChangeListener letterChangeListener) {
        this.mLetterChangeListener = letterChangeListener;
    }
}
