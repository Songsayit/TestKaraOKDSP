package com.example.testkaraok;

import java.util.List;

import com.example.testdsp.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DspRegControlListViewAdapter extends ArrayAdapter<DSPRegHolder> 
	implements OnClickListener{

	private String TAG = "DspRegControlListViewAdapter";

	private List<DSPRegHolder> items;
	private final Context mContext;

	public DspRegControlListViewAdapter(Context context, int resource,
			List<DSPRegHolder> objects) {
		super(context, resource, objects);
		// TODO Auto-generated constructor stub
		mContext = context;
		items = objects;
	}
	
	
	@Override
	public DSPRegHolder getItem(int position) {
		// TODO Auto-generated method stub
		return items.get(position);
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
			view = vi.inflate(R.layout.regcontrol_items, null);

			holder.regName = (TextView) view.findViewById(R.id.reg_name);
			holder.min = (TextView) view.findViewById(R.id.min);
			holder.val = (TextView) view.findViewById(R.id.val);
			holder.max = (TextView) view.findViewById(R.id.max);
			holder.add = (Button) view.findViewById(R.id.btn_add);
			holder.sub = (Button) view.findViewById(R.id.btn_sub);
			
			view.setTag(holder);

		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		
		if (items == null) {
			holder.regName.setText(null);
			holder.min.setText(null);
			holder.val.setText(null);
			holder.max.setText(null);
			holder.add.setText(null);
			holder.sub.setText(null);
		} else {
			if (position >= items.size()) {
				holder.regName.setText(null);
				holder.min.setText(null);
				holder.val.setText(null);
				holder.max.setText(null);
				holder.add.setText(null);
				holder.sub.setText(null);

			} else {
				final DSPRegHolder gh =  items.get(position);
				holder.regName.setText(gh.getRegName()+ "->");
				holder.min.setText("" + gh.getMin());
				holder.val.setText("" + gh.getVal());
				holder.max.setText("" + gh.getMax());
				holder.add.setOnClickListener(this);
				holder.sub.setOnClickListener(this);
				holder.add.setTag(gh);
				holder.sub.setTag(gh);
			}
		}
		
		return view;
	}
	
	final static class ViewHolder {
		TextView regName;
		TextView min;
		TextView val;
		TextView max;
		Button add;
		Button sub;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		final int id = v.getId();
		final DSPRegHolder gh = (DSPRegHolder) v.getTag();
		int val = gh.getVal();
		boolean write = true;
		switch (id) {
		case R.id.btn_sub:
			val--;
			if(val < gh.getMin()) {
				val = gh.getMin();
				write = false;
			}
			break;
		case R.id.btn_add:
			val++;
			if(val > gh.getMax()) {
				val = gh.getMax();
				write = false;
			}
			break;

		default:
			write = false;
			break;
		}
		if (write) {
			gh.setVal(val);
			gh.writeVal();
			
			notifyDataSetChanged();
		}
	}

//	@Override
//	public void notifyDataSetChanged() {
//		// TODO Auto-generated method stub
//		for (int i = 0; i < items.size(); i++) {
//			DSPRegHolder rg = items.get(i);
//			int val = DspControl.getInstance().getRegVal(rg.getReg());
//			rg.setVal(val);
//		}
//		
//		super.notifyDataSetChanged();
//	}
	

}
