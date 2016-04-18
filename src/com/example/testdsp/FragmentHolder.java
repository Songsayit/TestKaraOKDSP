package com.example.testdsp;

import android.support.v4.app.Fragment;

public class FragmentHolder {

	private String content;
	private Class<? extends Fragment> fragmentClass;
	
	public FragmentHolder(String content, Class<? extends Fragment> clss) {
		this.content = content;
		this.fragmentClass = clss;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Class<? extends Fragment>  getFragment() {
		return fragmentClass;
	}
	public void setFragment(Class<? extends Fragment>  clss) {
		this.fragmentClass = clss;
	}
	
	
	
}
