package com.project.TeachAids;

import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

public class TextUtil {

	public static void SetStandardTextStyle(TextView tv) {
		tv.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		}
		tv.setTextColor(0xFF33B5E6);		
	}
	
	public static void SetStandardTextStyle(TextView tv, int color) {
		tv.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			tv.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		}
		tv.setTextColor(color);		
	}
	
	public static void SetMediumTextStyle(TextView tv) {
		tv.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
		}
		tv.setTextColor(0xFF33B5E6);
	}
	
	public static void SetMediumTextStyle(TextView tv, int color) {
		tv.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
		}
		tv.setTextColor(color);
	}
	
	public static void SetThinTextStyle(TextView tv, int color) {
		tv.setVisibility(View.VISIBLE);
		if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
			tv.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
		}
		tv.setTextColor(color);
	}	
	
	public static void ShrinkTextToFit(float availableWidth, TextView textView,
									   float startingTextSize, float minimumTextSize) {
		CharSequence text = textView.getText();
		float textSize = startingTextSize;
		textView.setTextSize(startingTextSize);
		while (text != (TextUtils.ellipsize(text, textView.getPaint(), availableWidth, TextUtils.TruncateAt.END))) {
			textSize -= 1;
			if (textSize < minimumTextSize) {
				break;
			} 
			else {
				textView.setTextSize(textSize);
			}
		}
	}
}
