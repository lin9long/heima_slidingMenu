package com.example.administrator.heima_slidingmenu.Ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * 新建一个ViewGroup的方法，需要将布局内的控件添加到布局内部
 * 显示布局步骤为：先计算控件大小（OnMeasure），在摆放控件（OnLayout），最后显示界面
 * Created by Administrator on 2017/1/13.
 */

public class SlideMenu extends ViewGroup {

    private float downX;
    private float moveX;
    private int scrollX;
    private View leftMenu;
    private View mainContent;
    private int currentState;
    private static final int STATE_MENU = 1;
    private static final int STATE_MAIN = 2;
    private Scroller scroller;
    private float touchX;
    private float touchY;


    public SlideMenu(Context context) {
        super(context);
        init();
    }

    public SlideMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public SlideMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        scroller = new Scroller(getContext());
    }


    //
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 拦截主布局的触摸事件，当return true，使触摸事件不传递到子布局scrollView内
     * @param ev 触摸事件
     * @return 返回true表示拦截事件，flase表示将触摸事件传递到子布局
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchX = ev.getX();
                touchY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                //当用户左右滑动时，拦截触摸事件，在主布局内实现左右滑动业务逻辑
                int disX = (int) Math.abs(ev.getX() - touchX);
                int disY = (int) Math.abs(ev.getY() - touchY);
                if (disX > disY && disX > 5) {
                    return true;
                }
            case MotionEvent.ACTION_UP:
                break;

        }
        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 测量子布局的宽高
     *
     * @param widthMeasureSpec  返回的是当前父布局的宽
     * @param heightMeasureSpec 返回的是当前父布局的高
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //计算左边栏控件的宽高
        leftMenu = getChildAt(0);
        leftMenu.measure(leftMenu.getLayoutParams().width, heightMeasureSpec);
        //计算主内容布局的宽高
        mainContent = getChildAt(1);
        mainContent.measure(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 将控件摆放到布局内
     *
     * @param changed 界面是否发生改变
     * @param l       控件左边距离坐标轴左边距
     * @param t       控件上边距离坐标轴上边距
     * @param r       控件右边距离坐标轴左边距
     * @param b       控件下边距离坐标轴上边距
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        View leftMenu = getChildAt(0);
        leftMenu.layout(-leftMenu.getMeasuredWidth(), 0, 0, b);
        View mainContent = getChildAt(1);
        mainContent.layout(l, t, r, b);
    }

    /**
     * @param event
     * @return 如果当前控件为组合控件，需要保留super，如果为自定义控件，去掉super，返回true表示消费时间
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录手指按下屏幕的位置
                downX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //记录手指在屏幕移动的位置
                moveX = event.getX();
                //记录滚动的距离，scrollBy向左为负值，向右为正值
                scrollX = (int) (downX - moveX);
                //记录下即将滚动到的位置，用来作为边界判断
                int newScrollPosition = getScrollX() + scrollX;
                //如果下一刻滚动到的位置小于左边栏的宽度，则让其直接跳转到左边栏的界面
                if (newScrollPosition < -getChildAt(0).getMeasuredWidth()) {
                    scrollTo(-getChildAt(0).getMeasuredWidth(), 0);
                } else if (newScrollPosition > 0) {
                    scrollTo(0, 0);
                } else {
                    //
                    scrollBy(scrollX, 0);

                }
                moveX = downX;
                break;
            case MotionEvent.ACTION_UP:
                int upPosition = getScrollX();
                int centerLeftMenu = -getChildAt(0).getMeasuredWidth() / 2;
                if (upPosition < centerLeftMenu) {
                    currentState = STATE_MENU;
                    updateCurrentContent();
                } else {
                    currentState = STATE_MAIN;
                    updateCurrentContent();
                }

                break;

        }
        return true;
    }

    private void updateCurrentContent() {
        int startX = getScrollX();
        int dx = 0;

        if (currentState == STATE_MENU) {

            dx = -getChildAt(0).getMeasuredWidth() - startX;
            //    scrollTo(-getChildAt(0).getMeasuredWidth(), 0);
        } else {
            dx = 0 - startX;
            //     scrollTo(0, 0);
        }

        //1.开始平滑数据模拟，计算数据
        int time = Math.abs(dx * 5);
        //四个参数分别为，开始X位置，开始Y位置，结束X位置，结束Y位置
        scroller.startScroll(startX, 0, dx, 0, time);
        invalidate();//重绘界面
    }

    //2.维持动画的继续
    @Override
    public void computeScroll() {
        super.computeScroll();
        if (scroller.computeScrollOffset()) {
            int currX = scroller.getCurrX();
            scrollTo(currX, 0);
            invalidate();//重绘界面
        }

    }


    /**
     * 供外部使用的打开菜单方法
     */
    public void open() {
        currentState = STATE_MENU;
        updateCurrentContent();
    }

    /**
     * 供外部使用关闭菜单的方法
     */
    public void close() {
        currentState = STATE_MAIN;
        updateCurrentContent();
    }

    /**
     * 外部使用一键打开、关闭切换菜单方法
     */
    public void switchMode() {
        if (currentState == STATE_MAIN) {
            open();
        } else {
            close();
        }
    }

    public int getState() {
        return currentState;
    }
}
