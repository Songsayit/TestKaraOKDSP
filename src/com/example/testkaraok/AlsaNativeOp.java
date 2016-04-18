package com.example.testkaraok;

public class AlsaNativeOp {

	private static final int AUDIO_DEVICE_BIT_IN = 0x80000000;
	public static final int AUDIO_DEVICE_IN_WIRED_HEADSET = AUDIO_DEVICE_BIT_IN | 0x10;
	public static final int AUDIO_DEVICE_IN_BUILTIN_MIC = AUDIO_DEVICE_BIT_IN | 0x4;
	public static final int AUDIO_DEVICE_IN_BLUETOOTH_SCO_HEADSET = AUDIO_DEVICE_BIT_IN | 0x8;
			
	public static final int AUDIO_DEVICE_OUT_WIRED_HEADSET = 0x4;
	public static final int AUDIO_DEVICE_OUT_SPEAKER = 0x2;
	public static final int AUDIO_DEVICE_OUT_BLUETOOTH_SCO             = 0x10;
	

	private static AlsaNativeOp instance;

	public static AlsaNativeOp getInstance() {
		if (instance == null) {
			instance = new AlsaNativeOp();
		}
		return instance;
	}

	public int init(int in_device, int out_device) {
		 return native_init(in_device, out_device);
	}

	public int deinit() {
		return native_deinit();
	}

	public int read(byte[] buf, int size) {
		return native_read(buf, size);
	}

	public int write(byte[] buf, int size) {
		return native_write(buf, size);
	}

	public int excute() {
		return native_excute();
	}
	
	public String getString() {
		return stringFromJNI();
	}
	
	public void setGainVolume(float gain) {
		native_setGainVolume(gain);
	}

	private native String stringFromJNI();

	private native int native_init(int in_device, int out_device);

	private native int native_deinit();
	
	private native int native_excute();
	
	private native int native_read(byte[] buf, int size);

	private native int native_write(byte[] buf, int size);
	
	private native void native_setGainVolume(float gain);

	static {
		System.loadLibrary("jni_test");
	}
}
