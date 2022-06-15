package com.hover.stax.views.staxcardstack;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Observable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.hover.stax.R;

import java.util.ArrayList;
import java.util.List;

public class StaxCardStackView extends ViewGroup {
    public static final int INVALID_TYPE = -1;
    static final int DEFAULT_SELECT_POSITION = -1;
    private static final String TAG = "CardStackView";

    private final ViewDataObserver mObserver = new ViewDataObserver();
    private int mTotalLength;
    private int mOverlapGaps;
    private StaxCardStackAdapter mStaxCardStackAdapter;
    private int mShowHeight;
    private List<ViewHolder> mViewHolders;

    public StaxCardStackView(Context context) {
        this(context, null);
    }

    public StaxCardStackView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StaxCardStackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CardStackView, defStyleAttr, defStyleRes);
        setOverlapGaps(array.getDimensionPixelSize(R.styleable.CardStackView_stackOverlapGaps, dp2px(20)));
        array.recycle();

        mViewHolders = new ArrayList<>();
    }

    private int dp2px(int value) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        checkContentHeightByParent();
        measureChild(widthMeasureSpec, heightMeasureSpec);
    }

    private void checkContentHeightByParent() {
        View parentView = (View) getParent();
        mShowHeight = parentView.getMeasuredHeight() - parentView.getPaddingTop() - parentView.getPaddingBottom();
    }

    private void measureChild(int widthMeasureSpec, int heightMeasureSpec) {
        int maxWidth = 0;
        mTotalLength = 0;
        mTotalLength += getPaddingTop() + getPaddingBottom();
        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            final int totalLength = mTotalLength;
            final LayoutParams lp =
                    (LayoutParams) child.getLayoutParams();
            if (lp.mHeaderHeight == -1) lp.mHeaderHeight = child.getMeasuredHeight();
            final int childHeight = lp.mHeaderHeight;
            mTotalLength = Math.max(totalLength, totalLength + childHeight + lp.topMargin +
                    lp.bottomMargin);
            mTotalLength -= mOverlapGaps * 2;
            final int margin = lp.leftMargin + lp.rightMargin;
            final int measuredWidth = child.getMeasuredWidth() + margin;
            maxWidth = Math.max(maxWidth, measuredWidth);
        }

        mTotalLength += mOverlapGaps * 2;
        int heightSize = mTotalLength;
        heightSize = Math.max(heightSize, mShowHeight);
        int heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0);
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
                heightSizeAndState);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        layoutChild();
    }

    private void layoutChild() {
        int childTop = getPaddingTop();
        int childLeft = getPaddingLeft();

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            final int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            final LayoutParams lp =
                    (LayoutParams) child.getLayoutParams();
            childTop += lp.topMargin;
            if (i != 0) {
                childTop -= mOverlapGaps * 2;
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            } else {
                child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }
            childTop += lp.mHeaderHeight;
        }
    }


    public void setAdapter(StaxCardStackAdapter staxCardStackAdapter) {
        mStaxCardStackAdapter = staxCardStackAdapter;
        mStaxCardStackAdapter.registerObserver(mObserver);
        refreshView();
    }

    private void refreshView() {
        removeAllViews();
        mViewHolders.clear();
        for (int i = 0; i < mStaxCardStackAdapter.getItemCount(); i++) {
            ViewHolder holder = getViewHolder(i);
            holder.position = i;
            addView(holder.itemView);
            mStaxCardStackAdapter.bindViewHolder(holder, i);
        }
        requestLayout();
    }

    ViewHolder getViewHolder(int i) {
        if (i == DEFAULT_SELECT_POSITION) return null;
        ViewHolder viewHolder;
        if (mViewHolders.size() <= i || mViewHolders.get(i).mItemViewType != mStaxCardStackAdapter.getItemViewType(i)) {
            viewHolder = mStaxCardStackAdapter.createView(this, mStaxCardStackAdapter.getItemViewType(i));
            mViewHolders.add(viewHolder);
        } else {
            viewHolder = mViewHolders.get(i);
        }
        return viewHolder;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    public void setOverlapGaps(int overlapGaps) {
        mOverlapGaps = overlapGaps;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public int mHeaderHeight;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray array = c.obtainStyledAttributes(attrs, R.styleable.CardStackView);
            mHeaderHeight = array.getDimensionPixelSize(R.styleable.CardStackView_stackHeaderHeight, -1);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public static abstract class Adapter<VH extends ViewHolder> {
        private final AdapterDataObservable mObservable = new AdapterDataObservable();

        VH createView(ViewGroup parent, int viewType) {
            VH holder = onCreateView(parent, viewType);
            holder.mItemViewType = viewType;
            return holder;
        }

        protected abstract VH onCreateView(ViewGroup parent, int viewType);

        public void bindViewHolder(VH holder, int position) {
            onBindViewHolder(holder, position);
        }

        protected abstract void onBindViewHolder(VH holder, int position);

        public abstract int getItemCount();

        public int getItemViewType(int position) {
            return 0;
        }

        public final void notifyDataSetChanged() {
            mObservable.notifyChanged();
        }

        public void registerObserver(AdapterDataObserver observer) {
            mObservable.registerObserver(observer);
        }
    }

    public static abstract class ViewHolder {

        public View itemView;
        int mItemViewType = INVALID_TYPE;
        int position;

        public ViewHolder(View view) {
            itemView = view;
        }

        public Context getContext() {
            return itemView.getContext();
        }
    }

    public static class AdapterDataObservable extends Observable<AdapterDataObserver> {
        public boolean hasObservers() {
            return !mObservers.isEmpty();
        }

        public void notifyChanged() {
            for (int i = mObservers.size() - 1; i >= 0; i--) {
                mObservers.get(i).onChanged();
            }
        }
    }

    public static abstract class AdapterDataObserver {
        public void onChanged() {
        }
    }

    private class ViewDataObserver extends AdapterDataObserver {
        @Override
        public void onChanged() {
            refreshView();
        }
    }

}
