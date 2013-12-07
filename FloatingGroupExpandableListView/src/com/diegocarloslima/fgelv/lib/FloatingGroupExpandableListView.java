package com.diegocarloslima.fgelv.lib;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class FloatingGroupExpandableListView extends ExpandableListView {

	private WrapperExpandableListAdapter mAdapter;
	private OnScrollListener mOnScrollListener;

	private boolean mFloatingGroupEnabled = true;
	private View mCurrentFloatingGroupView;
	private int mCurrentFloatingGroupPosition;
	private int mCurrentFloatingGroupScrollY;
	private OnScrollFloatingGroupListener mOnScrollFloatingGroupListener;

	public FloatingGroupExpandableListView(Context context) {
		this(context, null);
	}

	public FloatingGroupExpandableListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatingGroupExpandableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		super.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(mOnScrollListener != null) {
					mOnScrollListener.onScrollStateChanged(view, scrollState);
				}
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if(mOnScrollListener != null) {
					mOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
				}

				if(mFloatingGroupEnabled && mAdapter != null && mAdapter.getGroupCount() > 0 && visibleItemCount > 0) {
					createFloatingGroupView(firstVisibleItem);
				}
			}
		});
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if(mFloatingGroupEnabled && mCurrentFloatingGroupView != null && mCurrentFloatingGroupView.getVisibility() == View.VISIBLE) {
			canvas.save();
			canvas.clipRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom());
			drawChild(canvas, mCurrentFloatingGroupView, getDrawingTime());
			canvas.restore();
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(mCurrentFloatingGroupView != null) {
			final int screenCoords[] = new int[2];
			getLocationOnScreen(screenCoords);
			final int x0 = screenCoords[0];
			final int x1 = x0 + mCurrentFloatingGroupView.getRight() - mCurrentFloatingGroupView.getLeft();
			final int y0 = screenCoords[1];
			final int y1 = y0 + mCurrentFloatingGroupView.getBottom() - mCurrentFloatingGroupView.getTop();
			final float rawX = ev.getRawX();
			final float rawY = ev.getRawY();
			if(rawX >= x0 && rawX <= x1 && rawY >= y0 && rawY <= y1) {
				if(!mCurrentFloatingGroupView.performClick()) {
					if(mAdapter.isGroupExpanded(mCurrentFloatingGroupPosition)) {
						collapseGroup(mCurrentFloatingGroupPosition);
					} else {
						expandGroup(mCurrentFloatingGroupPosition);
					}
					setSelectedGroup(mCurrentFloatingGroupPosition);
					mCurrentFloatingGroupView = mAdapter.getGroupView(mCurrentFloatingGroupPosition, mAdapter.isGroupExpanded(mCurrentFloatingGroupPosition), mCurrentFloatingGroupView, this);
				}
				return true;
			}
		}
		return super.onInterceptTouchEvent(ev);
	}
	
	@Override
	public void setAdapter(ExpandableListAdapter adapter) {
		if(!(adapter instanceof WrapperExpandableListAdapter)) {
			throw new IllegalArgumentException("The adapter must be an instance of WrapperExpandableListAdapter");
		}
		setAdapter((WrapperExpandableListAdapter) adapter);
	}

	public void setAdapter(WrapperExpandableListAdapter adapter) {
		super.setAdapter(adapter);
		mAdapter = adapter;
	}

	@Override
	public void setOnScrollListener(OnScrollListener listener) {
		mOnScrollListener = listener;
	}

	public void setFloatingGroupEnabled(boolean floatingGroupEnabled) {
		mFloatingGroupEnabled = floatingGroupEnabled;
	}

	public void setOnScrollFloatingGroupListener(OnScrollFloatingGroupListener listener) {
		mOnScrollFloatingGroupListener = listener;
	}

	private void createFloatingGroupView(int firstVisibleFlatPosition) {
		mCurrentFloatingGroupPosition = getPackedPositionGroup(getExpandableListPosition(firstVisibleFlatPosition));
		
		final int nextGroupFlatPosition = getFlatListPosition(getPackedPositionForGroup(mCurrentFloatingGroupPosition + 1));
		final int nextGroupListPosition = nextGroupFlatPosition - firstVisibleFlatPosition;
		View nextGroupView = null;
		if(nextGroupListPosition >= 0 && nextGroupListPosition < getChildCount()) {
			nextGroupView = getChildAt(nextGroupListPosition);

			if(nextGroupView.getVisibility() == View.INVISIBLE) {
				final Object tag = nextGroupView.getTag(R.id.fgelv_tag_changed_visibility);
				if(tag instanceof Boolean) {
					final boolean changedVisibility = (Boolean) tag;
					if(changedVisibility) {
						nextGroupView.setVisibility(View.VISIBLE);
					}
				}
			}
		}
		
		if(mCurrentFloatingGroupPosition >= 0) {
			mCurrentFloatingGroupView = mAdapter.getGroupView(mCurrentFloatingGroupPosition, mAdapter.isGroupExpanded(mCurrentFloatingGroupPosition), mCurrentFloatingGroupView, this);
		} else {
			mCurrentFloatingGroupView = null;
		}
		
		if(mCurrentFloatingGroupView == null || mCurrentFloatingGroupView.getVisibility() != View.VISIBLE) {
			return;
		}

		final int currentGroupFlatPosition = getFlatListPosition(getPackedPositionForGroup(mCurrentFloatingGroupPosition));
		final int currentGroupListPosition = currentGroupFlatPosition - firstVisibleFlatPosition;
		if(currentGroupListPosition >= 0 && currentGroupListPosition < getChildCount()) {
			final View currentGroupView = getChildAt(currentGroupListPosition);

			if(currentGroupView.getVisibility() == View.VISIBLE) {
				currentGroupView.setVisibility(View.INVISIBLE);
				currentGroupView.setTag(R.id.fgelv_tag_changed_visibility, true);
			}
		}

		final int widthMeasureSpec = MeasureSpec.makeMeasureSpec(getWidth() - getPaddingLeft() - getPaddingRight(),  MeasureSpec.EXACTLY);
		final int heightMeasureSpec = MeasureSpec.makeMeasureSpec(getHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.AT_MOST);
		mCurrentFloatingGroupView.measure(widthMeasureSpec, heightMeasureSpec);

		int currentFloatingGroupScrollY = 0;
		if(nextGroupView != null && nextGroupView.getTop() < getPaddingTop() + mCurrentFloatingGroupView.getMeasuredHeight() + 2 * getDividerHeight()) {
			currentFloatingGroupScrollY = nextGroupView.getTop() - (getPaddingTop() + mCurrentFloatingGroupView.getMeasuredHeight() + 2 * getDividerHeight());
		}

		final int left = getPaddingLeft();
		final int top = getPaddingTop() + getDividerHeight() + currentFloatingGroupScrollY;
		final int right = left + mCurrentFloatingGroupView.getMeasuredWidth();
		final int bottom = top + mCurrentFloatingGroupView.getMeasuredHeight();
		mCurrentFloatingGroupView.layout(left, top, right, bottom);

		if(mCurrentFloatingGroupScrollY != currentFloatingGroupScrollY) {
			mCurrentFloatingGroupScrollY = currentFloatingGroupScrollY;
			if(mOnScrollFloatingGroupListener != null) {
				mOnScrollFloatingGroupListener.onScrollFloatingGroupListener(mCurrentFloatingGroupView, mCurrentFloatingGroupScrollY);
			}
		}
	}

	public interface OnScrollFloatingGroupListener {
		public void onScrollFloatingGroupListener(View floatingGroupView, int scrollY);
	}
}
