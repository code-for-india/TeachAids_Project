package com.project.TeachAids;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;
import android.os.Build;

public class MainActivity extends Activity {
	private StartScreenFragment mStartScreenFragment;
	private GenderScreenFragment mGenderScreenFragment;
	private MainVideoFragment mMainVideoFragment;
	private FinalScreenFragment mFinalScreenFragment;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			mStartScreenFragment = new StartScreenFragment(this);
			mGenderScreenFragment = new GenderScreenFragment(this);
			FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mStartScreenFragment);
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.commit();
		}
	}

	public void onStartClicked() {
		FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mGenderScreenFragment);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	public void onGenderClicked(boolean male) {
		mMainVideoFragment = new MainVideoFragment(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mMainVideoFragment);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	public void onMainVideoFinished() {
		mFinalScreenFragment = new FinalScreenFragment(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mFinalScreenFragment);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
