/**
 * Copyright 2017 ChenHao Dendi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ethan.letterseekbar.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ethan.letterseekbar.R;

import java.util.Map;

public class FloatingBarItemDecoration extends RecyclerView.ItemDecoration {

    private final String TAG = FloatingBarItemDecoration.class.getSimpleName();
    private Context mContext;
    private int mTitleHeight;
    private Paint mBackgroundPaint;
    private Paint mTextPaint;
    private int mTextHeight;
    private int mTextBaselineOffset;
    private int mTextStartMargin;
    /**
     * Integer means the related position of the Recyclerview#getViewAdapterPosition()
     * (the position of the view in original adapter's list)
     * String means the title to be drawn
     */
    private Map<Integer, String> mList;

    public FloatingBarItemDecoration(Context context, Map<Integer, String> list) {
        this.mContext = context;
        Resources resources = mContext.getResources();
        this.mList = list;
        this.mTitleHeight = resources.getDimensionPixelSize(R.dimen.item_decoration_title_height);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(ContextCompat.getColor(mContext, R.color.item_decoration_title_background));

        mTextPaint = new Paint();
        mTextPaint.setColor(ContextCompat.getColor(mContext, R.color.item_decoration_title_fontcolor));
        mTextPaint.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.item_decoration_title_fontsize));

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        mTextHeight = (int) (fm.bottom - fm.top);
        mTextBaselineOffset = (int) fm.bottom;
        mTextStartMargin = resources.getDimensionPixelOffset(R.dimen.item_decoration_title_start_margin);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewAdapterPosition();
        // 设置item 的margin 偏移量
        outRect.set(0, mList.containsKey(position) ? mTitleHeight : 0, 0, 0);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDraw(c, parent, state);
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            int position = params.getViewAdapterPosition();
            if (!mList.containsKey(position)) {
                continue;
            }
            drawTitleArea(c, left, right, child, params, position);
        }
    }

    private void drawTitleArea(Canvas c, int left, int right, View child,
                               RecyclerView.LayoutParams params, int position) {
        final int rectBottom = child.getTop() - params.topMargin;
        // 画分类列表间隔处的字母
        c.drawRect(left, rectBottom - mTitleHeight, right,
                rectBottom, mBackgroundPaint);
        c.drawText(mList.get(position), child.getPaddingLeft() + mTextStartMargin,
                rectBottom - (mTitleHeight - mTextHeight) / 2 - mTextBaselineOffset, mTextPaint);
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        // 获取第一个可见 Item的 position
        final int position = ((LinearLayoutManager) parent.getLayoutManager()).findFirstVisibleItemPosition();
        if (position == RecyclerView.NO_POSITION) {
            return;
        }
        View child = parent.findViewHolderForAdapterPosition(position).itemView;
        // 获取首字母
        String initial = getTag(position);
        if (initial == null) {
            return;
        }

        boolean flag = false;
        // 屏幕内第一个可见的item 和下一个item首字母不同
        if (getTag(position + 1) != null && !initial.equals(getTag(position + 1))) {
            // 字母分隔处订到了浮标
            if (child.getHeight() + child.getTop() < mTitleHeight) {
                c.save();
                flag = true;
                // child.getTop() 不断减小，相当于浮标向上平移
                c.translate(0, child.getHeight() + child.getTop() - mTitleHeight);
            }
        }
        // 画浮标及字母
        c.drawRect(parent.getPaddingLeft(), parent.getPaddingTop(),
                parent.getRight() - parent.getPaddingRight(), parent.getPaddingTop() + mTitleHeight, mBackgroundPaint);
        c.drawText(initial, child.getPaddingLeft() + mTextStartMargin,
                parent.getPaddingTop() + mTitleHeight - (mTitleHeight - mTextHeight) / 2 - mTextBaselineOffset, mTextPaint);

        if (flag) {
            c.restore();
        }
    }

    private String getTag(int position) {
        while (position >= 0) {
            if (mList.containsKey(position)) {
                return mList.get(position);
            }
            position--;
        }
        return null;
    }
}

