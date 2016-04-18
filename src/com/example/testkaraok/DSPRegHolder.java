package com.example.testkaraok;

import com.nidu.robot.provider.settings.NiduSettings;

import android.content.Context;
import android.util.Log;


public class DSPRegHolder {
	private final String TAG = "DSPRegHolder";
	private final int reg;
	private int val;
	private final int max;
	private final int min;
	private final String regName;
	private Context mContext;
	private static final String REG_NAME = "reg_";
	
	public DSPRegHolder(Context context, int reg, int min, int max, int init_val, String regName) {
		this.reg = reg;
		this.max = max;
		this.min = min;
		this.regName = regName;
		mContext = context;
		val = NiduSettings.Robot.getInt(mContext.getContentResolver(), getRegString(), init_val);
		if (val != init_val) {
			writeVal();
		}
	}

	private String getRegString() {
		return REG_NAME + Integer.toHexString(reg) + "H";
	}
	
	public int getVal() {
		return val;
	}

	public void setVal(int val) {
		this.val = val;
	}
	
	public int getReg() {
		return reg;
	}

	public int getMax() {
		return max;
	}

	public int getMin() {
		return min;
	}

	public String getRegName() {
		return regName;
	}

	public void writeVal() {
		NiduSettings.Robot.putInt(mContext.getContentResolver(), getRegString(), val);
		Log.e(TAG, "write->reg:" + Integer.toHexString(reg) + "H = " + val);
		String value = Integer.toHexString(reg) + " " + Integer.toHexString(val);
		FileOp.writeDsp(value);
	}
}
