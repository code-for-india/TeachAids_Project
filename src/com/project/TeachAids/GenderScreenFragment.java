package com.project.TeachAids;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("ValidFragment")
public class GenderScreenFragment extends Fragment implements OnClickListener, OnTouchListener {
	private GenderScreenListener mParent;
	private boolean mSetBounds;
	
	public enum Gender {
		MALE,
		FEMALE
	}
	
	public interface GenderScreenListener {
		public void onGenderTapped(Gender gender);
	}
	
	public GenderScreenFragment() {
		
	}
	
	public GenderScreenFragment(GenderScreenListener parent) {
		mParent = parent;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.gender_view, container, false);
		final View maleButton = rootView.findViewById(R.id.gender_male_button);
		final View femaleButton = rootView.findViewById(R.id.gender_female_button);
		rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!mSetBounds) {
                    mSetBounds = true;
                    final int containerPadding = 40;
                    int containerWidth = rootView.findViewById(R.id.genderRightContainer).getWidth();
                    int maleWidth = maleButton.getWidth();
                    int femaleWidth = femaleButton.getWidth();
                    if (maleWidth != femaleWidth) {
                        containerWidth -= containerPadding; // padding
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) maleButton.getLayoutParams();
                        params.leftMargin += containerPadding / 2;
                        params.width = containerWidth / 2;
                        maleButton.setLayoutParams(params);
                        params = (RelativeLayout.LayoutParams) femaleButton.getLayoutParams();
                        params.width = containerWidth / 2;
                        femaleButton.setLayoutParams(params);
                    }
                }
            }
		});
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.startTeachAidsUrl), Color.parseColor("#3981ca"));
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.genderSelectText), Color.parseColor("#2b2e2e"));
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.maleText), Color.parseColor("#2b2e2e"));
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.femaleText), Color.parseColor("#2b2e2e"));
		
		maleButton.setOnClickListener(this);
		maleButton.setOnTouchListener(this);
		femaleButton.setOnClickListener(this);
		femaleButton.setOnTouchListener(this);
		return rootView;
	}
	
	@Override
	public void onClick(View v) {
		if (mParent != null) {
			if (v.getId() == R.id.gender_male_button) {
				mParent.onGenderTapped(Gender.MALE);
			} else if (v.getId() == R.id.gender_female_button) {
				mParent.onGenderTapped(Gender.FEMALE);
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