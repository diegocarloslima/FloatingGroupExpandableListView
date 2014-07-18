package com.diegocarloslima.fgelv.lib;

import android.database.DataSetObserver;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;

public class WrapperExpandableListAdapter extends BaseExpandableListAdapter {

    private final ExpandableListAdapter mWrappedAdapter;
    private final SparseBooleanArray mGroupExpandedMap = new SparseBooleanArray();

    public WrapperExpandableListAdapter(ExpandableListAdapter adapter) {
        mWrappedAdapter = adapter;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mWrappedAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mWrappedAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getGroupCount() {
        return mWrappedAdapter.getGroupCount();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mWrappedAdapter.getChildrenCount(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mWrappedAdapter.getGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mWrappedAdapter.getChild(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mWrappedAdapter.getGroupId(groupPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return mWrappedAdapter.getChildId(groupPosition, childPosition);
    }

    @Override
    public boolean hasStableIds() {
        return mWrappedAdapter.hasStableIds();
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView != null) {
            final Object tag = convertView.getTag(R.id.fgelv_tag_changed_visibility);
            if(tag instanceof Boolean) {
                final boolean changedVisibility = (Boolean) tag;
                if(changedVisibility) {
                    convertView.setVisibility(View.VISIBLE);
                }
            }
            convertView.setTag(R.id.fgelv_tag_changed_visibility, null);
        }
        mGroupExpandedMap.put(groupPosition, isExpanded);
        return mWrappedAdapter.getGroupView(groupPosition, isExpanded, convertView, parent);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        return mWrappedAdapter.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return mWrappedAdapter.isChildSelectable(groupPosition, childPosition);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mWrappedAdapter.areAllItemsEnabled();
    }

    @Override
    public boolean isEmpty() {
        return mWrappedAdapter.isEmpty();
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
        mGroupExpandedMap.put(groupPosition, true);
        mWrappedAdapter.onGroupExpanded(groupPosition);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        mGroupExpandedMap.put(groupPosition, false);
        mWrappedAdapter.onGroupCollapsed(groupPosition);
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return mWrappedAdapter.getCombinedGroupId(groupId);
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return mWrappedAdapter.getCombinedChildId(groupId, childId);
    }

    public boolean isGroupExpanded(int groupPosition) {
        final Boolean expanded = mGroupExpandedMap.get(groupPosition);
        return expanded != null ? expanded : false;
    }
}
