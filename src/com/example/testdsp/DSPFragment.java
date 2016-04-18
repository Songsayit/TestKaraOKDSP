package com.example.testdsp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.testdsp.R;
import com.example.testkaraok.DSPRegHolder;
import com.example.testkaraok.DspControl;
import com.example.testkaraok.DspRegControlListViewAdapter;
import com.example.testkaraok.DspRegSaveListViewAdapter;
import com.example.testkaraok.GainControl;
import com.example.testkaraok.GainHolder;
import com.example.testkaraok.DspRegSaveListViewAdapter.OnItemClickListener;
import com.example.testkaraok.provider.GainSave;
import com.example.testkaraok.provider.RegSave;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DSPFragment extends Fragment {

	private final String TAG = "DSPFragment";
	private View mContentView = null;
	
	private Context mContext;
	
	private TextView mReverbTxt;
	private TextView mEchoTxt;
	private TextView mDryTxt;
	private TextView mEffectTxt;
	private TextView mMasterTxt;
	
	private ListView mReverbListView;
	private ListView mEchoListView;
	private ListView mDryListView;
	private ListView mEffectListView;
	private ListView mMasterListView;
	
	private List<DSPRegHolder> mReverbList = new ArrayList<DSPRegHolder>();
	private List<DSPRegHolder> mEchoList = new ArrayList<DSPRegHolder>();
	private List<DSPRegHolder> mDryList = new ArrayList<DSPRegHolder>();
	private List<DSPRegHolder> mEffectList = new ArrayList<DSPRegHolder>();
	private List<DSPRegHolder> mMasterList = new ArrayList<DSPRegHolder>();
	
	private DspRegControlListViewAdapter mReverbAdapter;
	private DspRegControlListViewAdapter mEchoAdapter;
	private DspRegControlListViewAdapter mDryAdapter;
	private DspRegControlListViewAdapter mEffectAdapter;
	private DspRegControlListViewAdapter mMasterAdapter;
	
	private Button mBtnRegSave;
	private ListView mRegSaveListView;
	private DspRegSaveListViewAdapter mRegSaveListViewAdapter;
	private List<String> mRegSaveList;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		mContext = getActivity();
		mContentView = inflater.inflate(R.layout.dsp_layout, null);
		
		mReverbTxt = (TextView)mContentView.findViewById(R.id.txt_reverb);
		mEchoTxt = (TextView)mContentView.findViewById(R.id.txt_echo);
		mDryTxt = (TextView)mContentView.findViewById(R.id.txt_dry);
		mEffectTxt = (TextView)mContentView.findViewById(R.id.txt_effect);
		mMasterTxt = (TextView)mContentView.findViewById(R.id.txt_master);
		
		
		mReverbListView = (ListView)mContentView.findViewById(R.id.reg_control_list_reverb);
		mEchoListView = (ListView)mContentView.findViewById(R.id.reg_control_list_echo);
		mDryListView = (ListView)mContentView.findViewById(R.id.reg_control_list_dry);
		mEffectListView = (ListView)mContentView.findViewById(R.id.reg_control_list_effect);
		mMasterListView = (ListView)mContentView.findViewById(R.id.reg_control_list_master);
		
		mBtnRegSave = (Button)mContentView.findViewById(R.id.btn_regsave);
		mBtnRegSave.setOnClickListener(mClickListener);
		mRegSaveListView = (ListView)mContentView.findViewById(R.id.dsp_regsave_list);
		
		
		initListView();
		return mContentView;
	}
	
	private void initReverbList() {
		String time = mContext.getResources().getString(R.string.time);
		DSPRegHolder timeHolder = new DSPRegHolder(mContext, 0x80, 0, 33, 20, time);
		mReverbList.add(timeHolder);
		String lpf = mContext.getResources().getString(R.string.LPF);
		DSPRegHolder lpfHolder = new DSPRegHolder(mContext, 0x81, 0, 33, 26, lpf);
		mReverbList.add(lpfHolder);
		String outputVolume = mContext.getResources().getString(R.string.output_volume);
		DSPRegHolder outputVolumeHolder = new DSPRegHolder(mContext, 0x82, 0, 33, 20, outputVolume);
		mReverbList.add(outputVolumeHolder);
		String on_off = mContext.getResources().getString(R.string.on_off);
		DSPRegHolder on_offVolumeHolder = new DSPRegHolder(mContext, 0x83, 0, 1, 1, on_off);
		mReverbList.add(on_offVolumeHolder);
		
		for (DSPRegHolder holder : mReverbList) {
			DspControl.getInstance().addRegHolder(holder);
		}
		
	}
	
	private void initEchoList() {
		String delay = mContext.getResources().getString(R.string.delay);
		DSPRegHolder delayHolder = new DSPRegHolder(mContext, 0x84, 0, 100, 60, delay);
		mEchoList.add(delayHolder);
		String repeat = mContext.getResources().getString(R.string.repeat);
		DSPRegHolder repeatHolder = new DSPRegHolder(mContext, 0x85, 0, 33, 20, repeat);
		mEchoList.add(repeatHolder);
		String outputVolume = mContext.getResources().getString(R.string.output_volume);
		DSPRegHolder outputVolumeHolder = new DSPRegHolder(mContext, 0x86, 0, 33, 20, outputVolume);
		mEchoList.add(outputVolumeHolder);
		String on_off = mContext.getResources().getString(R.string.on_off);
		DSPRegHolder on_offVolumeHolder = new DSPRegHolder(mContext, 0x87, 0, 1, 0, on_off);
		mEchoList.add(on_offVolumeHolder);
		
		for (DSPRegHolder holder : mEchoList) {
			DspControl.getInstance().addRegHolder(holder);
		}
	}
	
	private void initDryList() {
		String volume = mContext.getResources().getString(R.string.volume);
		DSPRegHolder volumeHolder = new DSPRegHolder(mContext, 0x88, 0, 33, 33, volume);
		mDryList.add(volumeHolder);
		String on_off = mContext.getResources().getString(R.string.on_off);
		DSPRegHolder on_offVolumeHolder = new DSPRegHolder(mContext, 0x89, 0, 1, 1, on_off);
		mDryList.add(on_offVolumeHolder);
		
		for (DSPRegHolder holder : mDryList) {
			DspControl.getInstance().addRegHolder(holder);
		}
	}
	private void initEffectList() {
		String volume = mContext.getResources().getString(R.string.volume);
		DSPRegHolder volumeHolder = new DSPRegHolder(mContext, 0x8a, 0, 33, 33, volume);
		mEffectList.add(volumeHolder);
		String on_off = mContext.getResources().getString(R.string.on_off);
		DSPRegHolder on_offVolumeHolder = new DSPRegHolder(mContext, 0x8b, 0, 1, 1, on_off);
		mEffectList.add(on_offVolumeHolder);
		
		for (DSPRegHolder holder : mEffectList) {
			DspControl.getInstance().addRegHolder(holder);
		}
	}
	private void initMasterList() {
		String volume = mContext.getResources().getString(R.string.volume);
		DSPRegHolder volumeHolder = new DSPRegHolder(mContext, 0x8c, 0, 33, 33, volume);
		mMasterList.add(volumeHolder);
		String on_off = mContext.getResources().getString(R.string.on_off);
		DSPRegHolder on_offVolumeHolder = new DSPRegHolder(mContext, 0x8d, 0, 1, 1, on_off);
		mMasterList.add(on_offVolumeHolder);
		
		for (DSPRegHolder holder : mMasterList) {
			DspControl.getInstance().addRegHolder(holder);
		}
	}
	
	private void initListView() {
		
		initReverbList();
		initEchoList();
		initDryList();
		initEffectList();
		initMasterList();
		
		mReverbAdapter = new DspRegControlListViewAdapter(mContext, -1, mReverbList);
		mReverbListView.setAdapter(mReverbAdapter);
		
		mEchoAdapter = new DspRegControlListViewAdapter(mContext, -1, mEchoList);
		mEchoListView.setAdapter(mEchoAdapter);
		
		mDryAdapter = new DspRegControlListViewAdapter(mContext, -1, mDryList);
		mDryListView.setAdapter(mDryAdapter);
		
		mEffectAdapter = new DspRegControlListViewAdapter(mContext, -1, mEffectList);
		mEffectListView.setAdapter(mEffectAdapter);
		
		mMasterAdapter = new DspRegControlListViewAdapter(mContext, -1, mMasterList);
		mMasterListView.setAdapter(mMasterAdapter);
		
		mRegSaveList = new ArrayList<String>();
		List<RegSave> gainSaveList = RegSave.getGainSaveList(mContext.getContentResolver(), null);
		for (RegSave gainSave : gainSaveList) {
			mRegSaveList.add(gainSave.date);
		}
		mRegSaveListViewAdapter = new DspRegSaveListViewAdapter(mContext, -1, mRegSaveList);
		mRegSaveListView.setAdapter(mRegSaveListViewAdapter);
		mRegSaveListViewAdapter.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void OnItemClick(int position) {
				// TODO Auto-generated method stub
				String date = mRegSaveList.get(position);
				RegSave gs = RegSave.getGainSave(mContext.getContentResolver(), date);
				if (gs == null) {
					return;
				}
				
				Log.e(TAG, "regsave = " + gs.holder_list_string);
				
				List<DSPRegHolder> ghList = DspControl.getInstance().parseString(gs.holder_list_string);
				for (DSPRegHolder gh : ghList) {
					gh.writeVal();
				}
				
				mReverbAdapter.notifyDataSetChanged();
				mEchoAdapter.notifyDataSetChanged();
				mDryAdapter.notifyDataSetChanged();
				mEffectAdapter.notifyDataSetChanged();
				mMasterAdapter.notifyDataSetChanged();
			}
		});
	}
	
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			final int id = v.getId();
			if (id == mBtnRegSave.getId()) {
				
				String dspRegString = DspControl.getInstance().getAllRegValString();
				if (dspRegString == null) {
					return;
				}
				
				Date nowTime = new Date(System.currentTimeMillis());
				SimpleDateFormat sdFormatter = new SimpleDateFormat("yyyy-MM-dd.HH-mm-ss.SSS");
				String date = sdFormatter.format(nowTime);
				
				RegSave regSave = new RegSave(date, dspRegString);
				RegSave.addGainSave(mContext.getContentResolver(), regSave);
				
				mRegSaveList.add(date);
				mRegSaveListViewAdapter.notifyDataSetChanged();
			}
		}
	};

}
