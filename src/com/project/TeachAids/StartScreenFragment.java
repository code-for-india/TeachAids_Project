package com.project.TeachAids;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class StartScreenFragment extends Fragment {
	MainActivity mParent;
	
	public StartScreenFragment() {
		
	}

	public StartScreenFragment(MainActivity parent) {
		mParent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.start_view, container, false);
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.startTeachAidsUrl), Color.parseColor("#3981ca"));
		
		rootView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mParent.onStartClicked();
			}
			
		});
		return rootView;
	}
}