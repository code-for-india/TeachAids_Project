package com.project.TeachAids;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.project.TeachAids.GenderScreenFragment.Gender;
import com.project.TeachAids.LanguagePathUtils.Language;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.util.Log;
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

public class MainActivity extends Activity implements StartScreenFragment.StartScreenListener, 
													  GenderScreenFragment.GenderScreenListener,
													  MainVideoFragment.VideoScreenListener,
													  FinalScreenFragment.FinalScreenListener,
													  VideoDownloadManager.DownloadManagerListener {
	private static final String TAG = "MainActivity";
	private static final int DOWNLOAD_PROGRESS_TIMER_PERIOD = 2000;
	
	private StartScreenFragment mStartScreenFragment;
	private GenderScreenFragment mGenderScreenFragment;
	private MainVideoFragment mMainVideoFragment;
	private FinalScreenFragment mFinalScreenFragment;
	private VideoDownloadManager mDownloadManager;
	private boolean mTimerRunning = false;
	private Timer mTimer;
	private Handler mHandler = new Handler();
	private Language mCurrentLanguage;
	private String mCurrentDownloadUri;
	private ProgressDialog mCurrentProgressDialog;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mDownloadManager = new VideoDownloadManager(this, this);
		
		if (savedInstanceState == null) {
			mStartScreenFragment = new StartScreenFragment(this);
			mGenderScreenFragment = new GenderScreenFragment(this);
			FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mStartScreenFragment);
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.commit();
		}
	}

	@Override 
	public void onBackPressed() {
		if (getFragmentManager().getBackStackEntryCount() == 0) {
			finish();
		} else {
			getFragmentManager().popBackStack();
		}
	}
	
	@Override
	public void onLanguageTapped(Language lang) {
		mCurrentLanguage = lang;
		
		File localPathInternal = LanguagePathUtils.getLanguagePackFolderPath(this, mCurrentLanguage);
		if (localPathInternal.exists()) {
			// we have already downloaded this language pack. done
			launchGenderSelectScreen();
		} else {
			// we currently download all media files to the private storage directory within external storage
			// if the external storage medium is not available, display an error.
			if (!externalStorageAvailable()) {
				UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
				                       this.getResources().getString(R.string.externalstorage_unavailable));
				return;
			}

			File downloadPath = LanguagePathUtils.getLanguagePackDownloadPath(this, lang);
			mCurrentDownloadUri = LanguagePathUtils.getLanguagePackUrl(lang);
			if (mDownloadManager.triggerDownload(mCurrentDownloadUri, downloadPath.getPath())) {
				startDownloadProgressTimer();
			} else {
				if (downloadPath.exists()) {
					if (!UnzipUtil.extractPackToFolderAndDelete(downloadPath.getPath(), localPathInternal.getPath())) {
					    UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
	                                           this.getResources().getString(R.string.unexpected_error));
					    return;
					} else {
					    launchGenderSelectScreen();
					}
				} else {
					// This would not make sense as the language does not exist so a download has to be triggered.
					Log.e(TAG, "Unexpected: download not triggered for language: " + lang);
				}
			}
		}
	}
	
	@Override
	public void onGenderTapped(Gender gender) {
		mMainVideoFragment = new MainVideoFragment(this, new File(LanguagePathUtils.getLanguagePackFolderPath(this, mCurrentLanguage), 
		                                                          gender == Gender.MALE ? "male" : "female").getPath());
		FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.container, mMainVideoFragment);
		ft.addToBackStack(null);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	@Override
	public void onMainVideoFinished() {
		mFinalScreenFragment = new FinalScreenFragment(this);
		FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.container, mFinalScreenFragment);
		ft.addToBackStack(null);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	@Override
    public void onFinalScreenFinished() {
	    while (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStackImmediate();
        }
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDownloadFinished(String uri) {
		Log.d(TAG, "Download finished: " + uri);
		boolean errorOccurred = false;
		if (uri.equals(mCurrentDownloadUri)) {
			stopDownloadProgressTimer();
			File downloadPath = LanguagePathUtils.getLanguagePackDownloadPath(this, mCurrentLanguage);
			if (downloadPath.exists()) {
				if (!UnzipUtil.extractPackToFolderAndDelete(downloadPath.getPath(), 
													        LanguagePathUtils.getLanguagePackFolderPath(this, mCurrentLanguage).getPath())) {
				    errorOccurred = true;
				} else {
				    launchGenderSelectScreen();
				}
			} else {
				Log.e(TAG, "File was downloaded but path does not exist locally: " + uri);
				errorOccurred = true;
			}
		}
		
		if (errorOccurred) {
		    UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
                                   this.getResources().getString(R.string.download_error));
		}
	}

	@Override
	public void onDownloadError(String uri) {
		Log.e(TAG, "Download error: " + uri);
		if (uri.equals(mCurrentDownloadUri)) {
			stopDownloadProgressTimer();
			UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
					               this.getResources().getString(R.string.download_error));
		}
	} 
	
	private void launchGenderSelectScreen() {
		FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.container, mGenderScreenFragment);
		ft.addToBackStack(null);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	private void onDownloadProgressTimerTick() {
		Map<String, Integer> progressMap = mDownloadManager.getProgress();
		if (progressMap != null) {
			for (String key : progressMap.keySet()) {
				Log.d(TAG, "Progress of: " + key + " percentage completed: " + progressMap.get(key) + "%");
				if (key.equals(mCurrentDownloadUri)) {
					mCurrentProgressDialog.setProgress(progressMap.get(key));
				}
			}
		}
	}
	
	private void startDownloadProgressTimer() {
		if (!mTimerRunning) {
			final MainActivity self = this;
			mCurrentProgressDialog = new ProgressDialog(this);
			mCurrentProgressDialog.setMessage(getResources().getString(R.string.downloading));
			mCurrentProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mCurrentProgressDialog.setCancelable(true);
			mCurrentProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
	            @Override
	            public void onCancel(DialogInterface dialog) {
	                self.stopDownloadProgressTimer();
	            }
	        });
			mCurrentProgressDialog.show();
			mTimerRunning = true; 
			mTimer = new Timer();
			mTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		            self.mHandler.post(new Runnable() {
						@Override
						public void run() {
							self.onDownloadProgressTimerTick();
						}
		            });
		        }
		    }, 0, DOWNLOAD_PROGRESS_TIMER_PERIOD);
		}
	}

	private void stopDownloadProgressTimer() {
		if (mTimerRunning) {
			mTimerRunning = false;
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
		}
		
		if (mCurrentProgressDialog != null) {
			mCurrentProgressDialog.dismiss();
			mCurrentProgressDialog = null;
		}
	}
		
	private boolean externalStorageAvailable() {
		  return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
	}
}
