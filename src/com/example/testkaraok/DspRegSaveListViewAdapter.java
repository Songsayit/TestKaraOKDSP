package com.example.testkaraok;

import java.util.List;

import com.example.testdsp.R;
import com.example.testkaraok.provider.GainSave;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DspRegSaveListViewAdapter extends ArrayAdapter<String> 
	implements OnClickListener, OnLongClickListener {

	private String TAG = "DspRegSaveListViewAdapter";

	private List<String> items;
	private final Context mContext;
	
	private int positionColor;
	
	public static interface OnItemClickListener {
		void OnItemClick(int position);
	}
	
	private OnItemClickListener mListener;
	
	public void setOnItemClickListener(OnItemClickListener l) {
		mListener = l;
	}

	public DspRegSaveListViewAdapter(Context context, int resource,
			List<String> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		mContext = context;
		items = objects;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View view = convertView;
		ViewHolder holder = null;
		if (view == null) {
			holder = new ViewHolder();
			LayoutInflater vi = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = vi.inflate( R.layout.regsave_items, null);

			holder.gainSaveDate = (Button) view.findViewById(R.id.gain_save_date);
			
			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		
		if (items == null) {
			holder.gainSaveDate.setText(null);
		} else {
			if (position >= items.size()) {
				holder.gainSaveDate.setText(null);

			} else {
				final String date =  items.get(position);
				holder.gainSaveDate.setText(date);
				if (positionColor != position) {
					holder.gainSaveDate.setTextColor(
							mContext.getResources().getColor(android.R.color.black));
				}
				holder.gainSaveDate.setOnClickListener(this);
				holder.gainSaveDate.setOnLongClickListener(this);
				holder.gainSaveDate.setTag(position);
			}
		}
		
		return view;
	}
	
	final static class ViewHolder {
		Button gainSaveDate;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (mListener != null) {
			int position = (Integer) v.getTag();
			mListener.OnItemClick(position);
			positionColor = position;
		}
		notifyDataSetInvalidated();
		
		((Button)v).setTextColor(mContext.getResources().getColor(android.R.color.holo_red_light));
	}

	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		int position = (Integer) v.getTag();
		showMyDialog(position);
		return true;
	}

	private void showMyDialog(final int position) {
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setMessage(R.string.dialog_msg);
		builder.setTitle(R.string.dialog_warn);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				
				String date = items.get(position);
				GainSave gs = GainSave.getGainSave(mContext.getContentResolver(), date);
				GainSave.deleteGainSave(mContext.getContentResolver(),
						gs.id);
				
				items.remove(position);
				notifyDataSetChanged();
				
				
			}
		});
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
	
}
