package com.project.TeachAids;

import java.io.File;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.project.TeachAids.GenderScreenFragment.Gender;
import com.project.TeachAids.LanguagePathUtils.Language;
import com.project.TeachAids.UnzipUtil.UnzipFinishedListener;

public class MainActivity extends Activity implements StartScreenFragment.StartScreenListener, 
													  GenderScreenFragment.GenderScreenListener,
													  MainVideoFragment.VideoScreenListener,
													  FinalScreenFragment.FinalScreenListener,
													  VideoDownloadManager.DownloadManagerListener,
													  UnzipFinishedListener {
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
	private UnzipUtil mUnzipUtil;
	PowerManager.WakeLock mWakeLock;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d("TeachAids", "Design resolution: " + getDeviceResolution());
		Log.d("TeachAids", "Design heights: " + getDeviceDips());
		
	    this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);

		mDownloadManager = new VideoDownloadManager(this, this);
		
		if (savedInstanceState == null) {
		    onFinalScreenFinished();
		}
		{
			mStartScreenFragment = new StartScreenFragment(this);
			mGenderScreenFragment = new GenderScreenFragment(this);
			FragmentTransaction ft = getFragmentManager().beginTransaction().add(R.id.container, mStartScreenFragment);
			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
			ft.commit();
		}
		
		AnalyticsUtil.LogPageView(((TeachAidsApplication) getApplication()).getTracker(), "MainActivity");
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
	    super.onSaveInstanceState(savedInstanceState);
	}
	
	@Override
	public void onStop() {
	    try {
    	    if (mWakeLock != null) {
    	        mWakeLock.release();
    	    }
	    } catch (Exception e) {}
	    super.onStop();
	}
	
	@Override
    public void onPause() {
	    try {
            if (mWakeLock != null) {
                mWakeLock.release();
            }
        } catch (Exception e) {}
        super.onPause();
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
		if (!oobeHasRun()) {
		    setOobeHasRun();
		    showOrganizationPopup();
		} else {
		    onLanguageTappedInternal(lang);
		}
	}
	
	@Override
	public void onGenderTapped(Gender gender) {
	    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
	    mWakeLock.acquire();
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
			    extractLanguagePackAndLaunch(downloadPath, LanguagePathUtils.getLanguagePackFolderPath(this, mCurrentLanguage));
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
	
    @Override
    public void unzipFinished(boolean success) {
        if (success) {
            launchGenderSelectScreen();
        } else {
            UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
                    this.getResources().getString(R.string.unexpected_error));            
        }
    }
	
	private void launchGenderSelectScreen() {
		FragmentTransaction ft = getFragmentManager().beginTransaction().replace(R.id.container, mGenderScreenFragment);
		ft.addToBackStack(null);
		ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}
	
	private void onLanguageTappedInternal(Language lang) {
	    File localPathInternal = LanguagePathUtils.getLanguagePackFolderPath(this, mCurrentLanguage);
        File downloadPath = LanguagePathUtils.getLanguagePackDownloadPath(this, lang);
        if ((localPathInternal == null) || (downloadPath == null)) {
            UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
                    this.getResources().getString(R.string.externalstorage_unavailable));
            return;
        }
        
        if (localPathInternal.exists()) {
            if (downloadPath.exists()) {
                extractLanguagePackAndLaunch(downloadPath, localPathInternal);
            } else {
                // we have already downloaded this language pack. done
                launchGenderSelectScreen();             
            }
        } else {
            // we currently download all media files to the private storage directory within external storage
            // if the external storage medium is not available, display an error.
            if (!externalStorageAvailable()) {
                UIUtils.showMessageBox(this, this.getResources().getString(R.string.error_title), 
                                       this.getResources().getString(R.string.externalstorage_unavailable));
                return;
            }

            mCurrentDownloadUri = LanguagePathUtils.getLanguagePackUrl(lang);
            if (mDownloadManager.triggerDownload(mCurrentDownloadUri, downloadPath.getPath())) {
                startDownloadProgressTimer();
            } else {
                if (downloadPath.exists()) {
                    extractLanguagePackAndLaunch(downloadPath, localPathInternal);
                } else {
                    // This would not make sense as the language does not exist so a download has to be triggered.
                    Log.e(TAG, "Unexpected: download not triggered for language: " + lang);
                }
            }
        }	    
	}
	
    private boolean oobeHasRun() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.oobe_experience_key), Context.MODE_PRIVATE);
        return sharedPref.getBoolean("ooberun", false);
    }

    private void setOobeHasRun() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.oobe_experience_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("ooberun", true);
        editor.commit();
    }
    
    private String getOrganizationName() {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.organization_key), Context.MODE_PRIVATE);
        return sharedPref.getString("orgname", null);
    }
    
    private void setOrganizationName(String name) {
        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.organization_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("orgname", name);
        editor.commit();
    }
	
	private void showOrganizationPopup() {
	    // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        
        View promptsView = li.inflate(R.layout.organization_prompt, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        TextUtil.SetStandardTextStyle((TextView) promptsView.findViewById(R.id.mainText), Color.parseColor("#808285"));
        TextUtil.SetThinTextStyle((TextView) promptsView.findViewById(R.id.subText), Color.parseColor("#2b2e2e"));
        
        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
            .setCancelable(false)
            .setPositiveButton("Submit",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    Log.d(TAG, userInput.getText().toString());
                    setOrganizationName(userInput.getText().toString());
                    onLanguageTappedInternal(mCurrentLanguage);
                }
              })
            .setNegativeButton("Skip",
              new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    dialog.cancel();
                    onLanguageTappedInternal(mCurrentLanguage);
                }
              });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        TextUtil.SetStandardTextStyle(alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE), Color.parseColor("#808285"));
        TextUtil.SetStandardTextStyle(alertDialog.getButton(AlertDialog.BUTTON_POSITIVE), Color.parseColor("#808285"));
	}
	
	private void extractLanguagePackAndLaunch(File downloadPath, File localPath) {
	    mUnzipUtil = new UnzipUtil(this, this, downloadPath.getPath(), localPath.getPath());
	    mUnzipUtil.execute();
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
	
	private String getDeviceResolution()
    {
        int density = getResources().getDisplayMetrics().densityDpi;
        switch (density)
        {
            case DisplayMetrics.DENSITY_MEDIUM:
                return "MDPI";
            case DisplayMetrics.DENSITY_HIGH:
                return "HDPI";
            case DisplayMetrics.DENSITY_LOW:
                return "LDPI";
            case DisplayMetrics.DENSITY_XHIGH:
                return "XHDPI";
            case DisplayMetrics.DENSITY_TV:
                return "TV";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "XXHDPI";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "XXXHDPI";
            default:
                return "Unknown";
        }
    }
	
	private String getDeviceDips() {
	    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return "Height: " + dpHeight + "dp. Width: " + dpWidth + "dp";
	}
}
