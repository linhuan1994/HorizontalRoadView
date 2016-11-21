package lgx.horizontalview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class HorizontalRoadView extends View {
    private static final String TAG = "HorizontalRoadView";
    private Bitmap mcarBg;
    private Bitmap mlocationBg;
    private int mroadNum;
    private int mlineColor;
    private String mroadText;
    private String[] mroadName;//保存地点名
    private int currentMyLoction;
    private int currentCarLoction;
    private Paint mPaint;
    private Rect mRect;

    private final int DEFAULT_LINE_WIDTH = px2sp(30);//线的宽度
    private final int DEFAULT_LINE_LENGTH = px2sp(200);//地点间的间隔长度
    private final int DEFAULT_TEXT_SIZE = px2sp(50);//文字的大小
    private final int BgSize = px2sp(100);//背景图大小

    private int maxTextLength = 0;//保存最长的文字长度，用于measure
    private float downx = 0, downy = 0, movex = 0, movey = 0;//触摸事件x,y
    private int currentMoveX = 0;//ontouch中x方向的偏移
    private int sumMoveX = 0;  //在x方向总的偏移

    public HorizontalRoadView(Context context) {
        this(context, null);
    }

    public HorizontalRoadView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /*
      获取属性
     */
    public HorizontalRoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HorizontalRoadView, defStyleAttr, 0);
        int n = a.getIndexCount();

        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.HorizontalRoadView_lineColor:
                    mlineColor = a.getColor(attr, Color.GREEN);
                    break;
                case R.styleable.HorizontalRoadView_carBg:
                    mcarBg = BitmapFactory.decodeResource(getResources(), a.getResourceId(attr, 0));
                    break;
                case R.styleable.HorizontalRoadView_locationBg:
                    mlocationBg = BitmapFactory.decodeResource(getResources(), a.getResourceId(attr, 0));

                    break;
                case R.styleable.HorizontalRoadView_roadNum:
                    mroadNum = a.getInt(attr, 5);// 默认5
                    break;
                case R.styleable.HorizontalRoadView_roadText:
                    mroadText = a.getString(attr);
                    break;
            }
        }
        a.recycle();
        mPaint = new Paint();
        mRect = new Rect();

        mroadName = new String[mroadNum];//初始化位置数组
        //TODO 加上判断是否为空，保证程序的稳定
        if (mroadText != null) //不为空，则将地名提取出来
            for (int j = 0; j < mroadNum; j++) {
                mroadName[j] = mroadText.split(" ")[j];//获取" "分隔开的地点

                if (mroadName[j].split("").length > maxTextLength)
                    maxTextLength = mroadName[j].split("").length;//得到最大的文字长度
            }
        //车及人的位置赋初值
        currentCarLoction = 1;
        currentMyLoction = 1;
    }


    /*
    测量出视图大小
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
    /*    if (modeWidth != MeasureSpec.EXACTLY) {//不是指定或全屏模式
           //宽度不用管
        }
   */
        if (modeHeight != MeasureSpec.EXACTLY) {//不是指定或全屏模式
            //高度等于 背景高+线宽+文字高度
            sizeHeight = BgSize + DEFAULT_LINE_WIDTH + DEFAULT_TEXT_SIZE * maxTextLength;
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(sizeHeight, MeasureSpec.EXACTLY);
        }
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    /*
    根据属性去画图
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setAntiAlias(true); // 消除锯齿
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setColor(mlineColor);
        mPaint.setStrokeWidth(DEFAULT_LINE_WIDTH);
        int fullWidth = mroadNum * DEFAULT_LINE_LENGTH;

        //画出直线  -.--.--.--.-
        canvas.drawLine(0, BgSize, fullWidth - sumMoveX, BgSize, mPaint);
        //画出间隔点
        mPaint.setColor(Color.WHITE);
        for (int i = 0; i < mroadNum; i++)
            canvas.drawCircle(DEFAULT_LINE_LENGTH / 2 + i * DEFAULT_LINE_LENGTH - sumMoveX, BgSize, DEFAULT_LINE_WIDTH / 2, mPaint);
        //画车
        // TODO
        mRect.set(DEFAULT_LINE_LENGTH * currentCarLoction - DEFAULT_LINE_LENGTH / 2 - BgSize / 2 - sumMoveX, 0,
                DEFAULT_LINE_LENGTH * currentCarLoction - DEFAULT_LINE_LENGTH / 2 + BgSize / 2 - sumMoveX, BgSize);
        canvas.drawBitmap(mcarBg, null, mRect, mPaint);
        //画位置
        mRect.set(DEFAULT_LINE_LENGTH * currentMyLoction - DEFAULT_LINE_LENGTH / 2 - BgSize / 2 - sumMoveX, 0,
                DEFAULT_LINE_LENGTH * currentMyLoction - DEFAULT_LINE_LENGTH / 2 + BgSize / 2 - sumMoveX, BgSize);
        canvas.drawBitmap(mlocationBg, null, mRect, mPaint);

        mPaint.setColor(Color.BLACK);
        mPaint.setTextSize(DEFAULT_TEXT_SIZE);
        mPaint.setTextAlign(Paint.Align.CENTER);//画在中心
        //画出竖的文字
        if (mroadName != null && mroadName[0] != "" && mroadName[mroadNum - 1] != "")//预防数组还没赋值，或者数量不够
            for (int i = 0; i < mroadNum; i++) {
                //Log.e(TAG, "onDraw: " + i + mroadName[i]);
                int num = mroadName[i].split("").length;//按字分开获取字长
                for (int j = 0; j < num; j++) {
                    canvas.drawText(mroadName[i].split("")[j], DEFAULT_LINE_LENGTH / 2 + i * DEFAULT_LINE_LENGTH - sumMoveX,
                            j * DEFAULT_TEXT_SIZE + BgSize + DEFAULT_LINE_WIDTH, mPaint);
                }
            }
    }

    /**
     * 获取移动的距离
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                downx = event.getX();
                downy = event.getY();
                //TODO
                if ((BgSize - DEFAULT_TEXT_SIZE) <= downy && downy <= (BgSize + DEFAULT_TEXT_SIZE))//在线上下一个文字范围内
                {
                    currentMyLoction = (int) (downx + sumMoveX) / DEFAULT_LINE_LENGTH + 1;//最小从1开始
                    postInvalidate();//刷新
                }
                Log.e(TAG, "onTouchEvent: downx:" + downx);
                break;
            case MotionEvent.ACTION_MOVE://获取移动的距离
                movex = event.getX();
                movey = event.getY();
                Log.e(TAG, "onTouchEvent: movex" + movex);
                currentMoveX = (int) (downx - movex);//只关注x上的移动,
                downx = movex;//多次进入move,保证流畅
                sumMoveX += px2sp(currentMoveX);//保存总的x移动

                //sumMoveX最小为0，最大为总长-屏幕长
                if (sumMoveX < 0)
                    sumMoveX = 0;
                else if (sumMoveX > (Math.abs(mroadNum * DEFAULT_LINE_LENGTH - px2sp(getWidth()))))
                    sumMoveX = Math.abs(mroadNum * DEFAULT_LINE_LENGTH - px2sp(getWidth()));
                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                break;

        }

        return true;//必须返回true，否则不响应ACTION_MOVE
    }

    /**
     * @param location 设置车图片位置  1--roadNum
     */
    public void setCarLocation(int location) {
        //在数目范围内设置车位置
        if (0 < location && location <= mroadNum)
            currentCarLoction = location;
        postInvalidate();//刷新
    }

    /**
     * 获取当前车的位置
     */
    public int getCarLocation()
    {
        return this.currentCarLoction;
    }

    /**
     * 设置当前位置
     * 1--roadNum
     */
    public void setMyCurrentLocation(int location) {
        if (0 < location && location <= mroadNum)
            currentMyLoction = location;
        postInvalidate();//刷新
    }

    /**
     * @return 获得当前选择位置
     */
    public int getMyCurrentLocation()
    {
        return this.currentMyLoction;
    }

    /**
     * dp -> px
     */
    protected int dp2px(int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, getResources().getDisplayMetrics());
    }

    /**
     * sp -> px
     */
    protected int sp2px(int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, getResources().getDisplayMetrics());

    }
    /**
     * px转sp
     */

    protected int px2sp(int pxVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX,
                pxVal, getResources().getDisplayMetrics());
    }

    /**
     * px转dp
     */
    protected float px2dp(Context context, float pxVal) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

}
