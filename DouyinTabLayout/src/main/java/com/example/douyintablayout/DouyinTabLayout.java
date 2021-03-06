package com.example.douyintablayout;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DouyinTabLayout extends FrameLayout {
    private static final String TAG = "DouyinTabLayout";

    private int mTouchSlop;
    private OverScroller mScroller;

    private LinearLayout mTabContainer;
    private int mCurrentIndex = 0;
    private OnTabSelectedListener mOnTabSelectedListener;

    public DouyinTabLayout(@NonNull Context context) {
        this(context, null);
    }

    public DouyinTabLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DouyinTabLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTabLayout(context);
    }

    public interface OnTabSelectedListener {
        void onTabSelected(int index);
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        mOnTabSelectedListener = onTabSelectedListener;
    }

    public void addTabs(List<String> tabs, int index) {
        if (tabs == null || tabs.isEmpty()) {
            return;
        }
        if (index < 0) {
            index = 0;
        }
        if (index >= tabs.size()) {
            index = tabs.size() - 1;
        }
        for (int i = 0; i < tabs.size(); i++) {
            View tabLayout = generateTabView(tabs.get(i));
            tabLayout.setTag(i);
            mTabContainer.addView(tabLayout,
                    new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            tabLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelect((int) v.getTag(), true);
                }
            });
        }
        updateCurrentIndex(index, false);
        post(new Runnable() {
            @Override
            public void run() {
                setSelect(mCurrentIndex, false);
            }
        });
    }

    protected View generateTabView(String tab) {
        View tabLayout = LayoutInflater.from(getContext()).inflate(R.layout.default_tab_view_layout, this, false);
        TextView tabView = tabLayout.findViewById(R.id.tab_view);
        tabView.setText(tab);
        return tabLayout;
    }

    public void setSelect(int index, boolean smooth) {
        if (mTabContainer.getChildCount() <= index || index < 0) {
            return;
        }
        updateCurrentIndex(index, true);
        View child = mTabContainer.getChildAt(index);
        mScroller.startScroll(getScrollX(), 0,
                child.getLeft() - getScrollX() + child.getWidth() / 2 - getWidth() / 2, 0,
                smooth ? 250 : 0);
        postInvalidateOnAnimation();
    }

    private void updateCurrentIndex(int index, boolean notify) {
        if (mCurrentIndex == index) {
            return;
        }
        mCurrentIndex = index;
        for (int i = 0; i < mTabContainer.getChildCount(); i++) {
            View child = mTabContainer.getChildAt(i);
            child.setSelected(i == index);
        }
        if (notify) {
            callbackTabSelected(index);
        }
    }

    private void callbackTabSelected(int index) {
        if (mOnTabSelectedListener != null) {
            mOnTabSelectedListener.onTabSelected(index);
        }
    }

    private void initTabLayout(Context context) {
        mTabContainer = new LinearLayout(context);
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mTabContainer, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new OverScroller(context);
        Log.d(TAG, "initTabLayout, mTouchSlop: " + mTouchSlop);
    }

    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("DouyinTabLayout can host only one direct child");
        }

        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("DouyinTabLayout can host only one direct child");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("DouyinTabLayout can host only one direct child");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("DouyinTabLayout can host only one direct child");
        }

        super.addView(child, index, params);
    }

    @Override
    protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();

        final int horizontalPadding = getPaddingLeft() + getPaddingRight();
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentWidthMeasureSpec) - horizontalPadding),
                MeasureSpec.UNSPECIFIED);

        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom(), lp.height);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    /*
     * ??????????????? child view ?????? spec mode ????????? MeasureSpec.UNSPECIFIED
     */
    @Override
    protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin
                        + heightUsed, lp.height);
        final int usedTotal = getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin +
                widthUsed;
        final int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                Math.max(0, MeasureSpec.getSize(parentWidthMeasureSpec) - usedTotal),
                MeasureSpec.UNSPECIFIED);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    private int mLastMotionX;
    private boolean mIsBeingDragged;

    /*
     * 1?????????child view ????????? down ???????????? onInterceptTouchEvent ???????????? down ?????????
     * onTouchEvent ??????????????? down move up???
     * ??????????????????????????? down
     * <p>
     * 2?????????child view ????????? down ???????????? onInterceptTouchEvent ??????????????? down move???
     * ?????? onInterceptTouchEvent ?????? true ?????????onInterceptTouchEvent ????????????????????????????????? onTouchEvent ??? move up???
     * ??????????????????????????? move
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.d(TAG, "onInterceptTouchEvent, action: " + ev.getActionMasked() + ", " + ev.getX());
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = (int) ev.getX();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                final int x = (int) ev.getX();
                final int xDiff = Math.abs(x - mLastMotionX);
                if (xDiff > mTouchSlop) {
                    Log.d(TAG, "onInterceptTouchEvent, xDiff: " + xDiff);
                    mIsBeingDragged = true;
                    mLastMotionX = x;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                mIsBeingDragged = false;
            }
            break;
        }
        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent, action: " + event.getActionMasked() + ", " + event.getX());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastMotionX = (int) event.getX();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                final int x = (int) event.getX();
                int deltaX = mLastMotionX - x;
                /*
                  ?????????????????????????????????
                  1????????? child view ????????? down ??????????????????????????????????????????????????? touchSlop ????????????
                  ???????????? move ????????????????????????????????? mIsBeingDragged == true ????????????
                  scroll ???????????????????????????????????? touchSlop ??????
                  2????????? child view ???????????? down ???????????????????????? 1 ??????????????????????????????????????? touchSlop ?????????
                  ????????? scroll????????? scroll ????????????????????????????????? touchSlop ??????
                 */
                if (!mIsBeingDragged && Math.abs(deltaX) > mTouchSlop) {
                    mIsBeingDragged = true;
                    if (deltaX > 0) {
                        deltaX -= mTouchSlop;
                    } else {
                        deltaX += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    mLastMotionX = x;
                    scrollBy(deltaX, 0);
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mIsBeingDragged) {
                    mScroller.startScroll(getScrollX(), 0, getScrollDelta(), 0);
                    postInvalidateOnAnimation();

                    mIsBeingDragged = false;
                }
            }
            break;
        }
        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidateOnAnimation();
        }
    }

    private int getScrollDelta() {
        int scrollX = getScrollX();
        int centerX = getWidth() / 2;
        int childCount = mTabContainer.getChildCount();
        if (childCount <= 0) {
            return 0;
        }
        //?????????view??????????????????
        View firstChild = mTabContainer.getChildAt(0);
        if (firstChild.getLeft() - scrollX >= centerX) {
            updateCurrentIndex(0, true);
            return firstChild.getLeft() - scrollX + firstChild.getWidth() / 2 - centerX;
        }
        //?????????view??????????????????
        View lastChild = mTabContainer.getChildAt(childCount - 1);
        if (lastChild.getRight() - scrollX <= centerX) {
            updateCurrentIndex(childCount - 1, true);
            return lastChild.getRight() - scrollX - lastChild.getWidth() / 2 - centerX;
        }
        //???????????????????????????view
        int minDelta = Integer.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < childCount; i++) {
            View child = mTabContainer.getChildAt(i);
            int childCenterX = child.getLeft() - scrollX + child.getWidth() / 2;
            if (Math.abs(childCenterX - centerX) < Math.abs(minDelta)) {
                minDelta = childCenterX - centerX;
                index = i;
            }
        }
        updateCurrentIndex(index, true);
        return minDelta == Integer.MAX_VALUE ? 0 : minDelta;
    }

}
