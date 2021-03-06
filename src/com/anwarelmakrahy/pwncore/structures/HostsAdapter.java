package com.anwarelmakrahy.pwncore.structures;

import java.util.ArrayList;

import com.anwarelmakrahy.pwncore.R;
import com.anwarelmakrahy.pwncore.StaticClass;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HostsAdapter extends BaseAdapter {

	private static ArrayList<HostItem> itemDetailsrrayList;
	private LayoutInflater l_Inflater;
	private int selectedIndex;
	private int selectedColor = Color.TRANSPARENT;

	// private Context context;

	public HostsAdapter(Context context, ArrayList<HostItem> results) {
		itemDetailsrrayList = results;
		l_Inflater = LayoutInflater.from(context);
		selectedIndex = -1;
		// this.context = context;
	}

	public int getCount() {
		return itemDetailsrrayList.size();
	}

	public Object getItem(int position) {
		return itemDetailsrrayList.get(position);
	}

	public void setSelectedIndex(int ind) {
		selectedIndex = ind;
		notifyDataSetChanged();
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = l_Inflater.inflate(R.layout.host_item, null);

			holder = new ViewHolder();
			holder.txt_itemHost = (TextView) convertView
					.findViewById(R.id.targetHost);
			holder.txt_itemOS = (TextView) convertView
					.findViewById(R.id.targetOS);
			holder.txt_itemImage = (ImageView) convertView
					.findViewById(R.id.targetImage);
			holder.txt_itemImageBack = (ImageView) convertView
					.findViewById(R.id.targetImageBack);

			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txt_itemHost
				.setText(itemDetailsrrayList.get(position).getHost());
		holder.txt_itemOS.setText(itemDetailsrrayList.get(position).getOS());

		if (itemDetailsrrayList.get(position).isPwned())
			holder.txt_itemImageBack.setImageResource(R.drawable.hacked);
		else
			holder.txt_itemImageBack.setImageResource(R.drawable.computer);

		holder.txt_itemImage
				.setImageResource(getImageResFromOS(itemDetailsrrayList.get(
						position).getOS()));

		if (selectedIndex != -1 && position == selectedIndex)
			convertView.setBackgroundColor(Color.parseColor("#6633b5e5"));
		else
			convertView.setBackgroundColor(selectedColor);

		return convertView;
	}

	static class ViewHolder {
		TextView txt_itemHost;
		TextView txt_itemOS;
		ImageView txt_itemImage;
		ImageView txt_itemImageBack;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private int getImageResFromOS(String os) {
		for (int i = 0; i < StaticClass.osTitles.length; i++)
			if (StaticClass.osTitles[i].equals(os))
				return StaticClass.osImages[i];
		return StaticClass.osImages[StaticClass.osTitles.length - 1];
	}
}