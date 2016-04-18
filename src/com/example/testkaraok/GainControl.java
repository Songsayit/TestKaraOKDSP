package com.example.testkaraok;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class GainControl {
	private String TAG = "GainControl";
	
	private static GainControl instance;
	
	private GainControl() {
		
	}
	
	public static GainControl getInstance() {
		if (instance == null) {
			instance = new GainControl();
		}
		return instance;
	}
	
	public  List<GainHolder> getGainHolderList() {
		 List<GainHolder> ghs= getHolderByRead();
		 if (ghs == null) {
			 ghs = new ArrayList<GainHolder>();
			 GainHolder gh = new GainHolder(1, "null", 1, 0, 1);
			 ghs.add(gh);
		 }
		 return ghs;
			 
	}
	
	public List<GainHolder> getGainHolderList(String str) {
		return parseString(str);
	}
	
	public String getGainHolderString() {
		String str = FileOp.readGain();
		if (str == null) {
			Log.e(TAG, "read fail");
			return null ;
		}
		return str;
	}
	
	private List<GainHolder> getHolderByRead() {
		String str = FileOp.readGain();
		if (str == null) {
			Log.e(TAG, "read fail");
			return null ;
		}
		
		return parseString(str);
	}
	
	public List<GainHolder> parseString(String str) {
		if (str == null) {
			return null;
		}
		List<GainHolder> holders = new ArrayList<GainHolder>();
		
		String[] regHolder = str.split("####");
		
		for (String string : regHolder) {
			GainHolder holder = parseItemString(string);
			holders.add(holder);
		}
		return holders;
	}
	
	private GainHolder parseItemString(String str) {
		String tt = "(regName=in1_boost, reg=13, val=2, type=1, min=0, max=15)"; 
		
		if (str == null) {
			str = tt;
		}
		
		String holder = str.trim();
		Log.e(TAG, "holder = " + holder);
		holder = holder.replace("(", "");
		holder = holder.replace(")", "");
		holder = holder.replace(" ", "");
		String[] need = holder.split(","); 
		
		Map<String, String> map = new HashMap<String, String>();
		
		for (String string : need) {
			String[] pair = string.split("=");
			if (pair.length != 2) {
				continue;
			}
			map.put(pair[0], pair[1]);
		}
		String regName = map.get("regName");
		int reg = Integer.decode(map.get("reg"));
		int val = Integer.decode(map.get("val"));
		int type = Integer.decode(map.get("type"));
		int min =  Integer.decode(map.get("min"));
		int max =  Integer.decode(map.get("max"));
		
		GainHolder gainHolder = new GainHolder(reg, regName, type, min, max);
		gainHolder.setVal(val);
		
		return gainHolder;
		
	}
	
}
