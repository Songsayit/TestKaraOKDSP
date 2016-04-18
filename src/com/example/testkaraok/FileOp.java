package com.example.testkaraok;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

import android.util.Log;

public class FileOp {
	private static final String TAG = "FileOp";
	private static final String GAIN_PATH = "/sys/class/rt3261_test/gain_control";
	private static final String DSP_PATH = "/sys/class/rt3261_test/dsp_control";

	public static boolean writeDsp(String value) {
		return writeFile(DSP_PATH, value);
	}
	
	public static boolean writeGain(String value) {
		return writeFile(GAIN_PATH, value);
	}

	public static String readGain() {
		return readPeri(GAIN_PATH);
	}

	private static boolean writeFile(String path, String value) {
		boolean ret = false;

		File file = new File(path);

		if (!file.exists()) {
			Log.e(TAG, path + "not exist!!");
			return ret;
		}

		try {
			FileOutputStream outStream = new FileOutputStream(file);
			OutputStreamWriter wr = new OutputStreamWriter(outStream);
			wr.write(value);
			wr.close();
			outStream.close();
			ret = true;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return ret;
		}
		return ret;
	}

	private static String readPeri(String path) {
		File file = new File(path);

		if (!file.exists()) {
			Log.e(TAG, path + "not exist!!");
			return null;
		}
		StringBuilder sb = new StringBuilder();
		try {

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			String line = "";

			while ((line = br.readLine()) != null) {
				sb.append(line);
				sb.append("####");
			}

			br.close();
			fr.close();

		} catch (Exception e) {
			// TODO: handle exception
			return null;
		}

		return sb.toString();
	}

}
