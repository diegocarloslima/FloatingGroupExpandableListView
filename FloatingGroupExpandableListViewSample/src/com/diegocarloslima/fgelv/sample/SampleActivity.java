package com.diegocarloslima.fgelv.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.diegocarloslima.fgelv.lib.FloatingGroupExpandableListView;
import com.diegocarloslima.fgelv.lib.WrapperExpandableListAdapter;

public class SampleActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.sample_activity);
		
		final FloatingGroupExpandableListView list = (FloatingGroupExpandableListView) findViewById(R.id.sample_activity_list);

        final LayoutInflater inflater = getLayoutInflater();

        final View header = inflater.inflate(R.layout.sample_activity_list_header, list, false);
		list.addHeaderView(header);

        final View footer = inflater.inflate(R.layout.sample_activity_list_footer, list, false);
        footer.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/diegocarloslima/FloatingGroupExpandableListView"));
                startActivity(intent);
            }
        });
		list.addFooterView(footer);
		
		// Even though the child divider has already been set on the layout file, we have to set it again here
		// This prevents a bug where the background turns to the color of the child divider when the list is expanded
		list.setChildDivider(new ColorDrawable(Color.BLACK));
		
		final SampleAdapter adapter = new SampleAdapter(this);
		final WrapperExpandableListAdapter wrapperAdapter = new WrapperExpandableListAdapter(adapter);
		list.setAdapter(wrapperAdapter);
		
		for(int i = 0; i < wrapperAdapter.getGroupCount(); i++) {
			list.expandGroup(i);
		}
		
		list.setOnScrollFloatingGroupListener(new FloatingGroupExpandableListView.OnScrollFloatingGroupListener() {
			
			@Override
			public void onScrollFloatingGroupListener(View floatingGroupView, int scrollY) {
				float interpolation = - scrollY / (float) floatingGroupView.getHeight();
				
				// Changing from RGB(162,201,85) to RGB(255,255,255)
				final int greenToWhiteRed = (int) (162 + 93 * interpolation);
				final int greenToWhiteGreen = (int) (201 + 54 * interpolation);
				final int greenToWhiteBlue = (int) (85 + 170 * interpolation);
				final int greenToWhiteColor = Color.argb(255, greenToWhiteRed, greenToWhiteGreen, greenToWhiteBlue);
				
				// Changing from RGB(255,255,255) to RGB(0,0,0)
				final int whiteToBlackRed = (int) (255 - 255 * interpolation);
				final int whiteToBlackGreen = (int) (255 - 255 * interpolation);
				final int whiteToBlackBlue = (int) (255 - 255 * interpolation);
				final int whiteToBlackColor = Color.argb(255, whiteToBlackRed, whiteToBlackGreen, whiteToBlackBlue);
				
				final ImageView image = (ImageView) floatingGroupView.findViewById(R.id.sample_activity_list_group_item_image);
				image.setBackgroundColor(greenToWhiteColor);
				
				final Drawable imageDrawable = image.getDrawable().mutate();
				imageDrawable.setColorFilter(whiteToBlackColor, PorterDuff.Mode.SRC_ATOP);
				
				final View background = floatingGroupView.findViewById(R.id.sample_activity_list_group_item_background);
				background.setBackgroundColor(greenToWhiteColor);
				
				final TextView text = (TextView) floatingGroupView.findViewById(R.id.sample_activity_list_group_item_text);
				text.setTextColor(whiteToBlackColor);
				
				final ImageView expanded = (ImageView) floatingGroupView.findViewById(R.id.sample_activity_list_group_expanded_image);
				final Drawable expandedDrawable = expanded.getDrawable().mutate();
				expandedDrawable.setColorFilter(whiteToBlackColor, PorterDuff.Mode.SRC_ATOP);
			}
		});
	}
}
