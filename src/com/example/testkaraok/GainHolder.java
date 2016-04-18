package com.example.testkaraok;

public class GainHolder {
	private final int reg;
	private int val;
	private final int type;
	private final int max;
	private final int min;
	private final String regName;
	
	public GainHolder(int reg, String regName, int type,  int min, int max) {
		this.reg = reg;
		this.max = max;
		this.min = min;
		this.regName = regName;
		this.type = type;
	}

	public int getType() {
		return type;
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
	
	
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

	public void writeVal() {
		String wreg = type + ":" + getVal();
		FileOp.writeGain(wreg);
	}
	
}
