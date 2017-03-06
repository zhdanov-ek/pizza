package com.example.gek.pizza.helpers;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Use for show list dishes and group of dishes
 * Create fine spacing between items
 */
public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;
    private boolean includeEdge;
    public static final String TAG = "2222222";

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view);    // item position
        int column = position % spanCount;                      // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount;
            outRect.right = (column + 1) * spacing / spanCount;
            if (position < spanCount) {
                outRect.top = spacing;
            }
            outRect.bottom = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            if (position >= spanCount) {
                outRect.top = spacing;
            }
        }
        String s = "LEFT = " + outRect.left + "\n" +
                "RIGHT = " + outRect.right + "\n" +
                "TOP = " + outRect.top + "\n" +
                "BOTTOM = " + outRect.bottom + "\n";
        Log.d(TAG, "getItemOffsets: position = " + position + ", spacing = " + spacing + "\n" + s);
    }
}