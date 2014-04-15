package me.crossle.vaircast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.crossle.vaircast.MainActivity.AirItem;

public class HolderView extends RelativeLayout {
	
	private TextView mAirName;
	public HolderView(Context context) {
		super(context);
		View v = LayoutInflater.from(context).inflate(R.layout.air_server_item, this);
		mAirName = (TextView) v.findViewById(R.id.tv_airserver_name);
	}

	public void bind(AirItem airItem) {
		if (airItem != null) {
			mAirName.setText(airItem.name);
		}
	}
}