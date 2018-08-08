package com.qiqi.inputview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.Timer;
import java.util.TimerTask;

public class InputView extends View {

    private Mode mode; //样式模式
    private int itemSize;//个数
    private int itemPadding;//间隔
    private int itemWidth;//宽度
    private int itemHeight;//高度
    private int border;//边框/下划线宽度
    private int rectColor;//边框颜色
    private int underLineColor;//下划线颜色
    private int fillColor;//填充颜色
    private int textSize;//文字大小
    private int textColor;//文字颜色
    private long cursorFlashTime;//光标闪动间隔时间
    private boolean isCursorEnable;//是否开启光标
    private boolean cipherEnable;//是否开启密文
    private int cursorColor;//光标颜色

    private int cursorPosition;//光标位置
    private int cursorWidth;//光标粗细
    private int cursorHeight;//光标长度
    private boolean isCursorShowing;//光标是否正在显示
    private boolean isInputComplete;//是否输入完毕
    private static String CIPHER_TEXT = "*"; //密文符号
    private String[] text;//文本数组
    private InputMethodManager inputManager;
    private Paint paint;
    private Timer timer;
    private TimerTask timerTask;

    public InputView(Context context) {
        super(context);
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
        postInvalidate();
    }

    public enum Mode {
        /**
         * 下划线样式
         */
        UNDERLINE(0),

        /**
         * 边框样式
         */
        RECT(1),

        /**
         * 填充样式
         */
        FILL(2);
        private int mode;

        Mode(int mode) {
            this.mode = mode;
        }

        public int getMode() {
            return this.mode;
        }

        static Mode formMode(int mode) {
            for (Mode m : values()) {
                if (mode == m.mode) {
                    return m;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    /**
     * 当前只支持从xml中构建该控件
     *
     * @param context
     * @param attrs
     */
    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        readAttribute(attrs);
    }

    private void readAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.InputView);
            itemSize = typedArray.getInteger(R.styleable.InputView_itemSize, 4);
            itemPadding = typedArray.getDimensionPixelSize(R.styleable.InputView_itemPadding, dp2px(15));
            itemWidth = typedArray.getDimensionPixelSize(R.styleable.InputView_itemWidth, dp2px(40));
            itemHeight = typedArray.getDimensionPixelSize(R.styleable.InputView_itemHeight, dp2px(40));
            border = typedArray.getDimensionPixelSize(R.styleable.InputView_border, dp2px(2));
            rectColor = typedArray.getColor(R.styleable.InputView_rectColor, Color.BLACK);
            underLineColor = typedArray.getColor(R.styleable.InputView_underLineColor, Color.BLACK);
            fillColor = typedArray.getColor(R.styleable.InputView_fillColor, Color.GRAY);
            textSize = typedArray.getDimensionPixelSize(R.styleable.InputView_textSize, sp2px(16));
            textColor = typedArray.getColor(R.styleable.InputView_textColor, Color.RED);
            cursorFlashTime = typedArray.getInteger(R.styleable.InputView_cursorFlashTime, 500);
            isCursorEnable = typedArray.getBoolean(R.styleable.InputView_isCursorEnable, true);
            cipherEnable = typedArray.getBoolean(R.styleable.InputView_cipherEnable, true);
            cursorColor = typedArray.getColor(R.styleable.InputView_cursorColor, Color.GRAY);
            mode = Mode.formMode(typedArray.getInteger(R.styleable.InputView_mode, Mode.UNDERLINE.getMode()));
            typedArray.recycle();
        }
        text = new String[itemSize];
        init();
    }

    private void init() {
        setFocusableInTouchMode(true);
        MyKeyListener MyKeyListener = new MyKeyListener();
        setOnKeyListener(MyKeyListener);
        inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        paint = new Paint();
        paint.setAntiAlias(true);
        timerTask = new TimerTask() {
            @Override
            public void run() {
                isCursorShowing = !isCursorShowing;
                postInvalidate();
            }
        };
        timer = new Timer();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int width = 0;
        switch (widthMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                //没有指定大小，宽度 = 单个密码框大小 * 密码位数 + 密码框间距 *（密码位数 - 1）
                width = itemWidth * itemSize + itemPadding * (itemSize - 1);
                break;
            case MeasureSpec.EXACTLY:
                //指定大小，宽度 = 指定的大小
                width = MeasureSpec.getSize(widthMeasureSpec);
                //密码框大小等于 (宽度 - 密码框间距 *(密码位数 - 1)) / 密码位数
                itemWidth = (width - (itemPadding * (itemSize - 1))) / itemSize;
                break;
        }
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int height = 0;
        switch (heightMode) {
            case MeasureSpec.UNSPECIFIED:
            case MeasureSpec.AT_MOST:
                //没有指定大小，宽度 = 单个密码框大小 * 密码位数 + 密码框间距 *（密码位数 - 1）
                height = itemHeight;
                break;
            case MeasureSpec.EXACTLY:
                //指定大小，高度 = 指定的大小
                height = MeasureSpec.getSize(heightMeasureSpec);
                itemHeight = height;
                break;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //光标宽度
        cursorWidth = dp2px(2);
        //光标长度
        cursorHeight = textSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mode == Mode.UNDERLINE) {
            //绘制下划线
            drawUnderLine(canvas, paint);
        } else if (mode == Mode.RECT) {
            //绘制方框
            drawRect(canvas, paint);
        } else {
            // 绘制填充背景
            drawFill(canvas, paint);
        }
        //绘制光标
        drawCursor(canvas, paint);
        //绘制密码文本
        drawCipherText(canvas, paint);
    }

    class MyKeyListener implements OnKeyListener {

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            int action = event.getAction();
            if (action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    /**
                     * 删除操作
                     */
                    if (TextUtils.isEmpty(text[0])) {
                        return true;
                    }
                    delete();
                    postInvalidate();
                    return true;
                }
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    /**
                     * 只支持数字
                     */
                    if (isInputComplete) {
                        return true;
                    }
                    add((keyCode - 7) + "");
                    postInvalidate();
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * 删除
     */
    private String delete() {
        String deleteText = null;
        if (cursorPosition > 0) {
            deleteText = text[cursorPosition - 1];
            text[cursorPosition - 1] = null;
            cursorPosition--;
        } else if (cursorPosition == 0) {
            deleteText = text[cursorPosition];
            text[cursorPosition] = null;
        }
        isInputComplete = false;
        return deleteText;
    }

    /**
     * 增加
     */
    private String add(String c) {
        String addText = null;
        if (cursorPosition < itemSize) {
            addText = c;
            text[cursorPosition] = c;
            cursorPosition++;
            if (cursorPosition == itemSize) {
                isInputComplete = true;
            }
        }
        return addText;
    }

    /**
     * 绘制密码替代符号
     *
     * @param canvas
     * @param paint
     */
    private void drawCipherText(Canvas canvas, Paint paint) {
        //画笔初始化
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        //文字居中的处理
        Rect r = new Rect();
        canvas.getClipBounds(r);
        int cHeight = r.height();
        paint.getTextBounds(CIPHER_TEXT, 0, CIPHER_TEXT.length(), r);
        float y = cHeight / 2f + r.height() / 2f - r.bottom;

        //根据输入的密码位数，进行for循环绘制
        for (int i = 0; i < text.length; i++) {
            if (!TextUtils.isEmpty(text[i])) {
                // x = paddingLeft + 单个密码框大小/2 + ( 密码框大小 + 密码框间距 ) * i
                // y = paddingTop + 文字居中所需偏移量
                if (cipherEnable) {
                    //没有开启明文显示，绘制密码密文
                    canvas.drawText(CIPHER_TEXT,
                            (getPaddingLeft() + itemWidth / 2) + (itemWidth + itemPadding) * i,
                            getPaddingTop() + y, paint);
                } else {
                    //明文显示，直接绘制密码
                    canvas.drawText(text[i],
                            (getPaddingLeft() + itemWidth / 2) + (itemWidth + itemPadding) * i,
                            getPaddingTop() + y, paint);
                }
            }
        }
    }

    /**
     * 绘制光标
     *
     * @param canvas
     * @param paint
     */
    private void drawCursor(Canvas canvas, Paint paint) {
        //画笔初始化
        paint.setColor(cursorColor);
        paint.setStrokeWidth(cursorWidth);
        paint.setStyle(Paint.Style.FILL);
        //光标未显示 && 开启光标 && 输入位数未满 && 获得焦点
        if (!isCursorShowing && isCursorEnable && !isInputComplete && hasFocus()) {
            // 起始点x = paddingLeft + 单个密码框大小 / 2 + (单个密码框大小 + 密码框间距) * 光标下标
            // 起始点y = paddingTop + (单个密码框大小 - 光标大小) / 2
            // 终止点x = 起始点x
            // 终止点y = 起始点y + 光标高度
            canvas.drawLine((getPaddingLeft() + itemWidth / 2) + (itemWidth + itemPadding) * cursorPosition,
                    getPaddingTop() + (itemHeight - cursorHeight) / 2,
                    (getPaddingLeft() + itemWidth / 2) + (itemWidth + itemPadding) * cursorPosition,
                    getPaddingTop() + (itemHeight + cursorHeight) / 2,
                    paint);
        }
    }

    /**
     * 绘制密码框下划线
     *
     * @param canvas
     * @param paint
     */
    private void drawUnderLine(Canvas canvas, Paint paint) {
        //画笔初始化
        paint.reset();
        paint.setColor(underLineColor);
        paint.setStrokeWidth(border);
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < itemSize; i++) {
            //根据密码位数for循环绘制直线
            // 起始点x为paddingLeft + (单个密码框大小 + 密码框边距) * i , 起始点y为paddingTop + 单个密码框大小
            // 终止点x为 起始点x + 单个密码框大小 , 终止点y与起始点一样不变
            canvas.drawLine(getPaddingLeft() + (itemWidth + itemPadding) * i, getPaddingTop() + itemHeight - border / 2,
                    getPaddingLeft() + (itemWidth + itemPadding) * i + itemWidth, getPaddingTop() + itemHeight - border / 2,
                    paint);
        }
    }

    private void drawRect(Canvas canvas, Paint paint) {
        paint.reset();
        paint.setColor(rectColor);
        paint.setStrokeWidth(border);
        paint.setStyle(Paint.Style.STROKE);
        Rect rect;
        for (int i = 0; i < itemSize; i++) {
            int startX = getPaddingLeft() + (itemWidth + itemPadding) * i + border / 2;
            int startY = getPaddingTop() + border / 2;
            int stopX = getPaddingLeft() + (itemWidth + itemPadding) * i + itemWidth - border / 2;
            int stopY = getPaddingTop() + itemHeight - border / 2;

            rect = new Rect(startX, startY, stopX, stopY);
            canvas.drawRect(rect, paint);
        }
    }


    private void drawFill(Canvas canvas, Paint paint) {
        paint.reset();
        paint.setColor(fillColor);
        paint.setStyle(Paint.Style.FILL);
        Rect rect;
        for (int i = 0; i < itemSize; i++) {
            int startX = getPaddingLeft() + (itemWidth + itemPadding) * i;
            int startY = getPaddingTop();
            int stopX = getPaddingLeft() + (itemWidth + itemPadding) * i + itemWidth;
            int stopY = getPaddingTop() + itemHeight;
            rect = new Rect(startX, startY, stopX, stopY);
            canvas.drawRect(rect, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            /**
             * 弹出软键盘
             */
            requestFocus();
            inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //cursorFlashTime为光标闪动的间隔时间
        timer.scheduleAtFixedRate(timerTask, 0, cursorFlashTime);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        timer.cancel();
    }

    private int dp2px(float dp) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int sp2px(float spValue) {
        float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER; //输入类型为数字
        return super.onCreateInputConnection(outAttrs);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putStringArray("text", text);
        bundle.putInt("cursorPosition", cursorPosition);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            text = bundle.getStringArray("text");
            cursorPosition = bundle.getInt("cursorPosition");
            state = bundle.getParcelable("superState");
        }
        super.onRestoreInstanceState(state);
    }

    /**
     * 获取输入的内容
     */
    public String getText() {
        if (TextUtils.isEmpty(text[0])) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            for (String s : text) {
                if (!TextUtils.isEmpty(s)) {
                    sb.append(s);
                }
            }
            return sb.toString();
        }
    }

    public void setItemSize(int itemSize) {
        this.itemSize = itemSize;
    }

    public void setBorder(int border) {
        this.border = border;
    }

    public void setRectColor(int rectColor) {
        this.rectColor = rectColor;
        postInvalidate();
    }

    public void setUnderLineColor(int underLineColor) {
        this.underLineColor = underLineColor;
        postInvalidate();
    }

    public void setFillColor(int fillColor) {
        this.fillColor = fillColor;
        postInvalidate();
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
        postInvalidate();
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
        postInvalidate();
    }

    public void setCursorColor(int cursorColor) {
        this.cursorColor = cursorColor;
        postInvalidate();
    }
}
