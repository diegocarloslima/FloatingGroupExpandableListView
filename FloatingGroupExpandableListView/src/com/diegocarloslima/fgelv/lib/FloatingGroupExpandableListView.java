package com.diegocarloslima.fgelv.lib;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

public class FloatingGroupExpandableListView extends ExpandableListView {
	
	private static final String TAG = FloatingGroupExpandableListView.class.getName();

	private WrapperExpandableListAdapter mAdapter;
	private OnScrollListener mOnScrollListener;

	private boolean mFloatingGroupEnabled = true;
	private View mCurrentFloatingGroupView;
	private int mCurrentFloatingGroupPosition;
	private int mCurrentFloatingGroupScrollY;
	private OnScrollFloatingGroupListener mOnScrollFloatingGroupListener;
	// We need to add an AttachInfo instance to the FloatingGroupView in order to have proper touch event handling
	private Object mViewAttachInfo;

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
	public boolean dispatchTouchEvent(MotionEvent ev) {
		boolean handled = false;

		if(mCurrentFloatingGroupView != null) {
			handled = mCurrentFloatingGroupView.dispatchTouchEvent(ev);
			if(ev.getAction() == MotionEvent.ACTION_UP) {
				final int screenCoords[] = new int[2];
				getLocationOnScreen(screenCoords);
				final RectF rect = new RectF(screenCoords[0], screenCoords[1], screenCoords[0] + mCurrentFloatingGroupView.getWidth(), screenCoords[1] + mCurrentFloatingGroupView.getHeight());

				if(!handled && rect.contains(ev.getRawX(), ev.getRawY())) {
					if(mAdapter.isGroupExpanded(mCurrentFloatingGroupPosition)) {
						collapseGroup(mCurrentFloatingGroupPosition);
					} else {
						expandGroup(mCurrentFloatingGroupPosition);
					}
					setSelectedGroup(mCurrentFloatingGroupPosition);
					playSoundEffect(SoundEffectConstants.CLICK);

					mCurrentFloatingGroupView = mAdapter.getGroupView(mCurrentFloatingGroupPosition, mAdapter.isGroupExpanded(mCurrentFloatingGroupPosition), mCurrentFloatingGroupView, this);
					loadAttachInfo();
					setAttachInfo(mCurrentFloatingGroupView);
					handled = true;
				}
				// invalidate();
			}
		}

		return handled || super.dispatchTouchEvent(ev);
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
			loadAttachInfo();
			setAttachInfo(mCurrentFloatingGroupView);

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
	
	private void loadAttachInfo() {
		if(mViewAttachInfo == null) {
			try {
				final Field field = View.class.getDeclaredField("mAttachInfo");
				field.setAccessible(true);
				mViewAttachInfo = field.get(this);
			} catch (Exception e) {
				Log.w(TAG, Log.getStackTraceString(e));
			}
		}
	}
	
	private void setAttachInfo(View v) {
		if(v == null) {
			return;
		}
		if(mViewAttachInfo != null) {
			try {
				final Field field = View.class.getDeclaredField("mAttachInfo");
				field.setAccessible(true);
				field.set(v, mViewAttachInfo);
			} catch (Exception e) {
				Log.w(TAG, Log.getStackTraceString(e));
			}
		}
		if(v instanceof ViewGroup) {
			final ViewGroup viewGroup = (ViewGroup) v;
			for(int i = 0; i < viewGroup.getChildCount(); i++) {
				setAttachInfo(viewGroup.getChildAt(i));
			}
		}
	}

	public interface OnScrollFloatingGroupListener {
		public void onScrollFloatingGroupListener(View floatingGroupView, int scrollY);
	}
}
