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
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.startTeachAidsUrl), Color.parseColor("#3981ca"));
		rootView.findViewById(R.id.english_button).setOnClickListener(this);
		rootView.findViewById(R.id.english_button).setOnTouchListener(this);
		rootView.findViewById(R.id.axomia_button).setOnClickListener(this);
		rootView.findViewById(R.id.axomia_button).setOnTouchListener(this);
		rootView.findViewById(R.id.hindi_button).setOnClickListener(this);
		rootView.findViewById(R.id.hindi_button).setOnTouchListener(this);
		rootView.findViewById(R.id.kannada_button).setOnClickListener(this);
		rootView.findViewById(R.id.kannada_button).setOnTouchListener(this);
		rootView.findViewById(R.id.mandarin_button).setOnClickListener(this);
		rootView.findViewById(R.id.mandarin_button).setOnTouchListener(this);
		rootView.findViewById(R.id.odia_button).setOnClickListener(this);
		rootView.findViewById(R.id.odia_button).setOnTouchListener(this);
		rootView.findViewById(R.id.setswana_button).setOnClickListener(this);
		rootView.findViewById(R.id.setswana_button).setOnTouchListener(this);
		rootView.findViewById(R.id.spanish_button).setOnClickListener(this);
		rootView.findViewById(R.id.spanish_button).setOnTouchListener(this);
		rootView.findViewById(R.id.swahili_button).setOnClickListener(this);
		rootView.findViewById(R.id.swahili_button).setOnTouchListener(this);
		rootView.findViewById(R.id.tamil_button).setOnClickListener(this);
		rootView.findViewById(R.id.tamil_button).setOnTouchListener(this);
		rootView.findViewById(R.id.telugu_button).setOnClickListener(this);
		rootView.findViewById(R.id.telugu_button).setOnTouchListener(this);
		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (mParent != null) {
			if (v.getId() == R.id.english_button) {
				mParent.onLanguageTapped(Language.ENGLISH);
			} else if (v.getId() == R.id.axomia_button) {
				mParent.onLanguageTapped(Language.AXOMIA);
			} else if (v.getId() == R.id.hindi_button) {
				mParent.onLanguageTapped(Language.HINDI);
			} else if (v.getId() == R.id.kannada_button) {
				mParent.onLanguageTapped(Language.KANNADA);
			} else if (v.getId() == R.id.mandarin_button) {
				mParent.onLanguageTapped(Language.MANDARIN);
			} else if (v.getId() == R.id.odia_button) {
				mParent.onLanguageTapped(Language.ODIA);
			} else if (v.getId() == R.id.setswana_button) {
				mParent.onLanguageTapped(Language.SETSWANA);
			} else if (v.getId() == R.id.spanish_button) {
				mParent.onLanguageTapped(Language.SPANISH);
			} else if (v.getId() == R.id.swahili_button) {
				mParent.onLanguageTapped(Language.SWAHILI);
			} else if (v.getId() == R.id.telugu_button) {
				mParent.onLanguageTapped(Language.TELUGU);
			} else if (v.getId() == R.id.tamil_button) {
				mParent.onLanguageTapped(Language.TAMIL);
			}
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			((ImageView) v).setColorFilter(getResources().getColor(R.color.bluelight), PorterDuff.Mode.MULTIPLY);
		} else {
			((ImageView) v).clearColorFilter();
		}
		return false;
	}
}