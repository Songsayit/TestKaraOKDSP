package com.example.testdsp;

import java.util.ArrayList;

import com.example.testdsp.R;
import com.example.testdsp.tabindicator.TabPageIndicator;
import com.example.testdsp.tabindicator.TestFragment;
import com.example.testkaraok.AlsaNativeOp;
import com.example.testkaraok.ExcuteThread;
import com.example.testkaraok.ExcuteThread.IInitListerner;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import android.widget.Toast;

public class SampleTabsDefault extends FragmentActivity implements OnCheckedChangeListener {
    private ArrayList<FragmentHolder> mFragmentHolderList = new ArrayList<FragmentHolder>();
    private Context mContext;
    private Switch mSwitch; 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_tabs);
        
        mContext = this;

        FragmentHolder holder;
        holder = new FragmentHolder("DSP", DSPFragment.class);
        mFragmentHolderList.add(holder);
        holder = new FragmentHolder("CODEC", CodecFragment.class);
        mFragmentHolderList.add(holder);
        
        
        FragmentPagerAdapter adapter = new GoogleMusicAdapter(getSupportFragmentManager());

        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(adapter);

        TabPageIndicator indicator = (TabPageIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        
        int padding = 10;
        mSwitch = new Switch(this);
        mSwitch.setOnCheckedChangeListener(this);
        mSwitch.setPaddingRelative(0, 0, padding, 0);
        mSwitch.setText(R.string.start_playback_capture);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(mSwitch, new ActionBar.LayoutParams(
                ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_VERTICAL | Gravity.END));
    }

    class GoogleMusicAdapter extends FragmentPagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return Fragment.instantiate(mContext, mFragmentHolderList.get(position).getFragment().getName());
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentHolderList.get(position).getContent();
        }

        @Override
        public int getCount() {
          return mFragmentHolderList.size();//CONTENT.length;
        }
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		if (buttonView.getId() == mSwitch.getId()) {
			if (isChecked) {
				if (mExcuteThread != null && mExcuteThread.isAlive()) {
					mExcuteThread.stopExcute();
					mExcuteThread = null;
				}
				
				mExcuteThread = new ExcuteThread(AlsaNativeOp.AUDIO_DEVICE_IN_BUILTIN_MIC,  AlsaNativeOp.AUDIO_DEVICE_OUT_SPEAKER, mListerner);
				mExcuteThread.start();
			} else {
				if (mExcuteThread != null) {
					mExcuteThread.stopExcute();
					mExcuteThread = null;
				}
			}
		}
	}
	
	private ExcuteThread mExcuteThread;
	
	private ExcuteThread.IInitListerner mListerner = new IInitListerner() {
		
		
		@Override
		public void onInitFinished() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(INIT_FINISHED);
		}

		@Override
		public void onPreInit() {
			// TODO Auto-generated method stub
			mHandler.sendEmptyMessage(PRE_INIT);
		}
	};
	
	private final int PRE_INIT = 1;
	private final int INIT_FINISHED = 2;
	private long interval = 0;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			int what = msg.what;
			switch (what) {
			case PRE_INIT:
				String str = getResources().getString(R.string.pre_init);
				showProcessDialog(str, str);
				interval = System.currentTimeMillis();
				break;
			case INIT_FINISHED:
				dismissProgressDialog();
				interval = System.currentTimeMillis() - interval;
				Toast.makeText(mContext, getResources().getString(R.string.init_finished) + interval + " ms" ,
						Toast.LENGTH_LONG).show();
				break;

			default:
				break;
			}
		}
	};
	
	private ProgressDialog progressDialog;
	private void showProcessDialog(String title, String message) {
		dismissProgressDialog();
		progressDialog = ProgressDialog.show(mContext, title, message, false, true);
	}
	
	private void dismissProgressDialog() {
		if (progressDialog != null) {
			progressDialog.dismiss();
			progressDialog = null;
		}
	}
}
