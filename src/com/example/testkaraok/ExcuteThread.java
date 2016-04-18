package com.example.testkaraok;

import android.util.Log;

public class ExcuteThread extends Thread {
	private final String TAG = "ExcuteThread";
	private int mInDevice = 0;
	private int mOutDevice = 0;
	private boolean isRunning = false;
	private final IInitListerner mListerner;
	public ExcuteThread(int inDevice, int outDevice, IInitListerner l) {
		mInDevice = inDevice;
		mOutDevice = outDevice;
		mListerner = l;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (mOutDevice == 0 || mInDevice == 0) {
			Log.e(TAG, "mOutDevice or mInDevice is 0");
			return;
		}
		isRunning = true;
		try {
			if (mListerner != null) {
				mListerner.onPreInit();
			}
			AlsaNativeOp.getInstance().init(mInDevice, mOutDevice);
//			byte[] readBuf = new byte[256*2];
			
			if (mListerner != null) {
				mListerner.onInitFinished();
			}
			
			while(isRunning) {
//				int readSize = AlsaNativeOp.getInstance().read(readBuf, readBuf.length);
//				AlsaNativeOp.getInstance().write(readBuf, readSize);
				AlsaNativeOp.getInstance().excute();
			}
			
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			AlsaNativeOp.getInstance().deinit();
		}
		
	}
	
	public void stopExcute() {
		isRunning = false;
	}

	public interface IInitListerner {
		public void onPreInit();
		public void onInitFinished();
	}
}
