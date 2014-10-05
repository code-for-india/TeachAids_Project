package com.project.TeachAids;

import com.project.TeachAids.LanguagePathUtils.Language;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class StartScreenFragment extends Fragment implements OnClickListener, OnTouchListener {
	private StartScreenListener mParent;
	
	public interface StartScreenListener {
		public void onLanguageTapped(Language lang);
	}
	
	public StartScreenFragment() {
		
	}

	public StartScreenFragment(StartScreenListener parent) {
		mParent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.start_view, container, false);
		rootView.findViewById(R.id.english_button).setOnClickListener(this);
		rootView.findViewById(R.id.english_button).setOnTouchListener(this);
		rootView.findViewById(R.id.hindi_button).setOnClickListener(this);
		rootView.findViewById(R.id.hindi_button).setOnTouchListener(this);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (mParent != null) {
			if (v.getId() == R.id.english_button) {
				mParent.onLanguageTapped(Language.ENGLISH);
			} else if (v.getId() == R.id.hindi_button) {
				mParent.onLanguageTapped(Language.HINDI);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return false;
	}
}