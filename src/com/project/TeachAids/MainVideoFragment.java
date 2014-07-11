package com.project.TeachAids;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.project.TeachAids.GenderScreenFragment.Gender;
import com.project.TeachAids.VideoListModel.QuestionPoint;
import com.project.TeachAids.VideoListModel.VideoHolder;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

@SuppressLint("ValidFragment")
public class MainVideoFragment extends Fragment {
    private static final String TAG = "MainVideoFragment";
    
	private VideoScreenListener mParent;
	private boolean mPlaying = false;
	private int mCurrentVideoIndex = 0;
	private boolean mTimerRunning = false;
	private Timer mTimer;
	private Handler mHandler = new Handler();
	private QuestionPoint mCurrentQp;
	private String mPath;
	private List<VideoHolder> mVideoList;
	
	// controls
	private TextView mChapterLabel;
    private TextView mVideoTitleLabel;
    private View mNavBar;
    private View mQuizBar;
    private View mCorrectView;
    private View mIncorrectView;
    private VideoView mVideoView;
    private ImageView mPlayPauseView;
    private PopupWindow mPopup;
    private View mYesNoParent;
    private Button mYesButton;
    private Button mNoButton;
    private Button mCorrectConfirmButton;
    private Button mIncorrectConfirmButton;
    private TextView mQuizTextLabel;
    
	private final int TIMER_PERIOD = 200;
	
	public interface VideoScreenListener {
	    public void onMainVideoFinished();
	}
	
	public MainVideoFragment() {
	}

	public MainVideoFragment(VideoScreenListener parent, String path) {
		mParent = parent;
		mPath = path;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    super.onCreateView(inflater, container, savedInstanceState);
	    
	    mVideoList = VideoListModel.initVideoList(mPath);
	    if (mVideoList == null || mVideoList.size() == 0) {
	        // this really shouldn't be happening... there is not much we can do here other than bail.
	        Log.e(TAG, "Unexpected: video list is null");
	        UIUtils.showMessageBox(this.getActivity(), this.getResources().getString(R.string.error_title), 
                                   this.getResources().getString(R.string.unexpected_error));
	        return null;
	    }
		
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		
		mCorrectView = rootView.findViewById(R.id.correctAnswerRoot);
		mCorrectConfirmButton = (Button)mCorrectView.findViewById(R.id.correctContinueButton);
		initCorrectView();
		
		mIncorrectView = rootView.findViewById(R.id.incorrectAnswerRoot);
		mIncorrectConfirmButton = (Button)mIncorrectView.findViewById(R.id.incorrectContinueButton);
		initIncorrectView();		
		
		mChapterLabel = (TextView)rootView.findViewById(R.id.chapterLabel);
		TextUtil.SetThinTextStyle(mChapterLabel, Color.parseColor("#2b2e2e"));
		mVideoTitleLabel = (TextView)rootView.findViewById(R.id.videoTitleLabel);
		TextUtil.SetThinTextStyle(mVideoTitleLabel, Color.parseColor("#c1272d"));
		TextUtil.SetThinTextStyle((TextView)rootView.findViewById(R.id.learnMoreTeachAids), Color.parseColor("#3981ca"));
		
		mPlayPauseView = (ImageView)rootView.findViewById(R.id.playandpause);
		
		mNavBar = rootView.findViewById(R.id.navBar);
		mQuizBar = rootView.findViewById(R.id.quizbar);
		mYesNoParent = rootView.findViewById(R.id.yesnoparent);
		mQuizTextLabel = (TextView)mYesNoParent.findViewById(R.id.quizQuestionLabel);
		TextUtil.SetThinTextStyle(mQuizTextLabel, Color.parseColor("#3981ca"));
		mYesButton = (Button)rootView.findViewById(R.id.yesButton);
		mNoButton = (Button)rootView.findViewById(R.id.noButton);
		
		final VideoView myVideoView = (VideoView)rootView.findViewById(R.id.myvideoview);
		mVideoView = myVideoView;
		myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).getPath()));
		myVideoView.start();
		mPlaying = true;
		startTimer();
		
		setChapterLabelText();
		setVideoTitleText();
		
		final MainVideoFragment self = this;
		
		rootView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
		mYesButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentQp != null) {
					if (mCurrentQp.correctAnswerIsYes()) {
						mQuizBar.setVisibility(View.VISIBLE);
						mYesNoParent.setVisibility(View.GONE);
						mCorrectView.setVisibility(View.VISIBLE);
						mIncorrectView.setVisibility(View.GONE);
					}
					else {
						mQuizBar.setVisibility(View.VISIBLE);
						mYesNoParent.setVisibility(View.GONE);
						mCorrectView.setVisibility(View.GONE);
						mIncorrectView.setVisibility(View.VISIBLE);
					}
				}
			}
		});
		
		mNoButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCurrentQp != null) {
					if (!mCurrentQp.correctAnswerIsYes()) {
						mQuizBar.setVisibility(View.VISIBLE);
						mYesNoParent.setVisibility(View.GONE);
						mCorrectView.setVisibility(View.VISIBLE);
						mIncorrectView.setVisibility(View.GONE);
					}
					else {
						mQuizBar.setVisibility(View.VISIBLE);
						mYesNoParent.setVisibility(View.GONE);
						mCorrectView.setVisibility(View.GONE);
						mIncorrectView.setVisibility(View.VISIBLE);
					}
					dismissPopup();
					playVideo();
				}
			}
		});
		
		mCorrectConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissSideBar();
				dismissPopup();
				playVideo();
			}
		});
		
		mIncorrectConfirmButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dismissSideBar();
				dismissPopup();
				playVideo();
			}
		});	
		
		rootView.findViewById(R.id.skipprev).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopup == null) {
					self.mCurrentVideoIndex--;
					if (self.mCurrentVideoIndex < 0) {
						self.mCurrentVideoIndex = 0;
					}
					setChapterLabelText();
					setVideoTitleText();
					myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).getPath()));
					myVideoView.requestFocus();
					myVideoView.start();
					mPlaying = true;
					startTimer();
				}
			}
		});
		
		rootView.findViewById(R.id.skipnext).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopup == null) {
					self.mCurrentVideoIndex++;
					if (self.mCurrentVideoIndex >= self.mVideoList.size()) {
						mCurrentVideoIndex = self.mVideoList.size() - 1;
						pauseVideo();
						mParent.onMainVideoFinished();
					}
					else {
						setChapterLabelText();
						setVideoTitleText();
						myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).getPath()));
						myVideoView.requestFocus();
						myVideoView.start();
						mPlaying = true;
						startTimer();
					}
				}
			}
		});
		
		mPlayPauseView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mPopup == null) {
					if (self.mPlaying) {
						pauseVideo();
					}
					else {
						playVideo();
					}
				}
			}
		});		
		
		return rootView;
	}
	
	private void onTimerTick() {
		if (mTimerRunning) {
			if (mPlaying) {
				if (mVideoView != null) {
					int currentPosition = mVideoView.getCurrentPosition();
					if (currentPosition > 0 && (mCurrentVideoIndex < mVideoList.size())) {
						VideoHolder currentVideoHolder = mVideoList.get(mCurrentVideoIndex);
						if (currentVideoHolder.getStopPoints() != null) {
							for (QuestionPoint qp : currentVideoHolder.getStopPoints()) {
								if (qp.questionActive() && (Math.abs(qp.getStopPoint() - currentPosition) < TIMER_PERIOD)) {
									stopTimer();
									pauseVideo();
									mCurrentQp = qp;
									mCurrentQp.setQuestionActive(false);
									mYesNoParent.setVisibility(View.VISIBLE);
									mCorrectView.setVisibility(View.GONE);
									mIncorrectView.setVisibility(View.GONE);
									mPopup = this.createPopup(qp.getQuestionText());
									mQuizTextLabel.setText(qp.getQuestionText());
									showPopup(mPopup);
									break;
								}
							}
						}
					}
				}
			} else {
				stopTimer();
			}
		}
	}
	
	private void startTimer() {
		if (!mTimerRunning) {
			mTimerRunning = true; 
			mTimer = new Timer();
			final MainVideoFragment self = this;
			mTimer.scheduleAtFixedRate(new TimerTask() {
		        public void run() {
		            self.mHandler.post(new Runnable() {
						@Override
						public void run() {
							self.onTimerTick();
						}
		            });
	
		        }
		    }, 0, TIMER_PERIOD);
		}
	}

	private void stopTimer() {
		if (mTimerRunning) {
			mTimerRunning = false;
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
		}
	}
	
	private void initCorrectView() {
		TextUtil.SetMediumTextStyle((TextView)mCorrectView.findViewById(R.id.correctLabel), Color.parseColor("#6dce7c"));
		TextUtil.SetThinTextStyle((TextView)mCorrectView.findViewById(R.id.correctLabel2), Color.parseColor("#2b2e2e"));
	}
	
	private void initIncorrectView() {
		TextUtil.SetMediumTextStyle((TextView)mIncorrectView.findViewById(R.id.incorrectLabel), Color.parseColor("#c1272d"));
		TextUtil.SetThinTextStyle((TextView)mIncorrectView.findViewById(R.id.incorrectLabel2), Color.parseColor("#2b2e2e"));		
	}
	
	private void setChapterLabelText() {
		String text = "Chapter " + (mCurrentVideoIndex+1) + " of " + mVideoList.size();
		mChapterLabel.setText(text);
	}
	
	private void setVideoTitleText() {
		if (mCurrentVideoIndex < mVideoList.size()) {
			mVideoTitleLabel.setText(mVideoList.get(mCurrentVideoIndex).getTitle());
		}
	}
	
	private void pauseVideo() {
		if (mVideoView != null) {
			mVideoView.pause();
			mPlaying = false;
			mPlayPauseView.setImageResource(R.drawable.play_small);
			mPlayPauseView.invalidate();
			stopTimer();
		}
	}
	
	private void playVideo() {
		if (mVideoView != null) {
			mVideoView.start();
			mPlaying = true;
			mPlayPauseView.setImageResource(R.drawable.pause);
			mPlayPauseView.invalidate();
			startTimer();
		}
	}
	
	private PopupWindow createPopup(String text) {
		PopupWindow popUp = new PopupWindow(this.getActivity());
        LinearLayout layout = new LinearLayout(this.getActivity());
        TextView tv = new TextView(this.getActivity());
        
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        tv.setGravity(Gravity.CENTER);
        
        tv.setTextSize(40);
        TextUtil.SetMediumTextStyle(tv, Color.parseColor("#c1272d"));
        tv.setText(text);
        layout.addView(tv, params);
        popUp.setContentView(layout);
        return popUp;
	}
	
	private void showPopup(PopupWindow popUp) {
        final float scale = getActivity().getResources().getDisplayMetrics().density;
		int width = (int) (200 * scale + 0.5f);
		ResizeWidthAnimation anim = new ResizeWidthAnimation(mQuizBar, width);
	    anim.setDuration(500);
	    mQuizBar.startAnimation(anim);
	    
	    ResizeWidthAnimation anim2 = new ResizeWidthAnimation(mNavBar, 0);
	    anim2.setDuration(500);
	    mNavBar.startAnimation(anim2);
	}
	
	private void dismissPopup() {
		if (mPopup != null) {
			mPopup.dismiss();
		}
		mPopup = null;
		mCurrentQp = null;
	}
	
	private void dismissSideBar() {
		ResizeWidthAnimation anim = new ResizeWidthAnimation(mQuizBar, 0);
	    anim.setDuration(500);
	    mQuizBar.startAnimation(anim);
	    
	    final float scale = getActivity().getResources().getDisplayMetrics().density;
		int width = (int) (200 * scale + 0.5f);;
		ResizeWidthAnimation anim2 = new ResizeWidthAnimation(mNavBar, width);
	    anim2.setDuration(500);
	    mNavBar.startAnimation(anim2);	    
	}
}
