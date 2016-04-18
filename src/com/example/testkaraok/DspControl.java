package com.example.testkaraok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;
import android.util.SparseArray;


public class DspControl {
	private String TAG = "DspControl";
	
	private static DspControl instance;
	
//	private List<Integer> mRegList = new ArrayList<Integer>();
//	private Map<Integer, DSPRegHolder> mRegHolderMap = new HashMap<Integer, DSPRegHolder>();
	private SparseArray<DSPRegHolder> mRegHolderMap = new SparseArray<DSPRegHolder>();
	//String type: reg1=val1,reg2=val2,...
	
	private DspControl() {}
	
	public static DspControl getInstance() {
		if (instance == null) {
			instance = new DspControl();
		}
		return instance;
	}
	
	public int getRegVal(int reg) {
		DSPRegHolder holder = mRegHolderMap.get(reg);
		return holder.getVal();
	}
	
	public void addRegHolder(DSPRegHolder holder) {
		mRegHolderMap.put(holder.getReg(), holder);
	}
	
	public String getAllRegValString() {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < mRegHolderMap.size(); i++) {
			DSPRegHolder holder = mRegHolderMap.valueAt(i);
			int reg = holder.getReg();
			int val = holder.getVal();
			sb.append(reg + "=" + val);
			sb.append(",");
		}
		
		return sb.toString();
	}
	
	public List<DSPRegHolder> parseString(String str) {
		//string type: reg1=val1,reg2=val2,...
		List<DSPRegHolder> holderList = new ArrayList<DSPRegHolder>();
		String[] reg_val_array = str.split(",");
		for (int i = 0; i < reg_val_array.length; i++) {
			String[] reg_val = reg_val_array[i].split("=");
			String reg = reg_val[0];
			String val = reg_val[1];
			DSPRegHolder holder = mRegHolderMap.get(Integer.decode(reg));
			holder.setVal(Integer.decode(val));
			holderList.add(holder);
		}
		
		return holderList;
	}
	
}
