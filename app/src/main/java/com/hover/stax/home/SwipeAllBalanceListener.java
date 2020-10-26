package com.hover.stax.home;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hover.stax.utils.customSwipeRefresh.CustomSwipeRefreshLayout;

interface SwipeAllBalanceListener {
	void triggerRefresh(CustomSwipeRefreshLayout swipeRefreshLayout);
}
