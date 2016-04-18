package com.example.testdsp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.testdsp.R;
import com.example.testkaraok.GainControl;
import com.example.testkaraok.GainControlListViewAdapter;
import com.example.testkaraok.GainHolder;
import com.example.testkaraok.GainSaveListViewAdapter;
import com.example.testkaraok.GainSaveListViewAdapter.OnItemClickListener;
import com.example.testkaraok.provider.GainSave;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

public class CodecFragment extends Fragment {
	private final String TAG = "CodecFragment";
	private View mContentView = null;
	
	private Button mBtnSaveGain;
	private ListView mGainControlListView;
	private GainControlListViewAdapter mGainControlAdapter;
	private ListView mGainSaveListView;
	private GainSaveListViewAdapter mGainSaveAdapter;
	
	private List<GainHolder> mGainControlList;
	private List<String> mGainSaveList;
	
	private Context mContext;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = getActivity();
		mContentView = inflater.inflate(R.layout.codec_layout, null);

		mGainControlListView = (ListView)mContentView.findViewById(R.id.gain_control_list);
		mGainSaveListView = (ListView)mContentView.findViewById(R.id.gain_save_list);
		mBtnSaveGain = (Button)mContentView.findViewById(R.id.btn_save_gain);
		mBtnSaveGain.setOnClickListener(mClickListener);
		
		initListView();
		return mContentView;
	}
	
	private void initListView() {
		mGainControlList = GainControl.getInstance().getGainHolderList();
		
		mGainControlAdapter = new GainControlListViewAdapter(mContext, -1,
				mGainControlList);
		
		mGainControlListView.setAdapter(mGainControlAdapter);
		
		mGainSaveList = new ArrayList<String>();
		List<GainSave> gainSaveList = GainSave.getGainSaveList(mContext.getContentResolver(), null);
		for (GainSave gainSave : gainSaveList) {
			mGainSaveList.add(gainSave.date);
		}
		
		mGainSaveAdapter = new GainSaveListViewAdapter(mContext, -1, mGainSaveList);
		mGainSaveListView.setAdapter(mGainSaveAdapter);
		mGainSaveAdapter.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void OnItemClick(int position) {
				// TODO Auto-generated method stub
				String date = mGainSaveList.get(position);
				GainSave gs = GainSave.getGainSave(mContext.getContentResolver(), date);
				if (gs == null) {
					return;
				}
				List<GainHolder> ghList = GainControl.getInstance().parseString(gs.holder_list_string);
				for (GainHolder gh : ghList) {
					gh.writeVal();
				}
				mGainControlAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final int id = v.getId();
			if (id == mBtnSaveGain.getId()) {
				String gainHolderString = GainControl.getInstance().getGainHolderString();
				if (gainHolderString == null) {
					return;
				}
				Date nowTime = new Date(System.currentTimeMillis());
				SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSS");
				String date = sdFormatter.format(nowTime);
				
				GainSave gain = new GainSave(date, gainHolderString);
				
				GainSave.addGainSave(mContext.getContentResolver(), gain);
				mGainSaveList.add(date);
				mGainSaveAdapter.notifyDataSetChanged();
			}
		}
	};
}
