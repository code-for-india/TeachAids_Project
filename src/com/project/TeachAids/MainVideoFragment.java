package com.project.TeachAids;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Fragment;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
	private MainActivity mParent;
	private TextView mChapterLabel;
	private TextView mVideoTitleLabel;
	private View mNavBar;
	private View mQuizBar;
	private View mCorrectView;
	private View mIncorrectView;
	private VideoView mVideoView;
	private ImageView mPlayPauseView;
	private boolean mPlaying = false;
	private int mCurrentVideoIndex = 0;
	private boolean mTimerRunning = false;
	private Timer mTimer;
	private Handler mHandler = new Handler();
	private PopupWindow mPopup;
	private QuestionPoint mCurrentQp;
	private View mYesNoParent;
	private Button mYesButton;
	private Button mNoButton;
	private Button mCorrectConfirmButton;
	private Button mIncorrectConfirmButton;
	private TextView mQuizTextLabel;
	
	private final int TIMER_PERIOD = 2;
	
	private class QuestionPoint {
		public int stopPoint;
		public String questionText;
		public boolean correctAnswerIsYes;
		public boolean seen = false;
		
		public QuestionPoint(int point, String qtxt, boolean ansIsYes) {
			this.stopPoint = point;
			this.questionText = qtxt;
			this.correctAnswerIsYes = ansIsYes;
		}
	}

	private class VideoHolder {
		public String title;
		public String path;
		public List<QuestionPoint> stopPoints = new ArrayList<QuestionPoint>();
		
		public VideoHolder(String t, String p) { title = t; path = p; }
	}
	
	
	ArrayList<VideoHolder> mVideoList = new ArrayList<VideoHolder>();
	
	public MainVideoFragment() {
		
	}

	public MainVideoFragment(MainActivity parent) {
		mParent = parent;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initVideoList();
		
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
		myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).path));
		myVideoView.requestFocus();
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
					if (mCurrentQp.correctAnswerIsYes) {
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
					if (!mCurrentQp.correctAnswerIsYes) {
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
					myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).path));
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
						myVideoView.setVideoURI(Uri.parse(mVideoList.get(mCurrentVideoIndex).path));
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
					currentPosition = Math.round(currentPosition / 1000);
					if (currentPosition > 0 && (mCurrentVideoIndex < mVideoList.size())) {
						VideoHolder currentVideoHolder = mVideoList.get(mCurrentVideoIndex);
						if (currentVideoHolder.stopPoints != null) {
							for (QuestionPoint qp : currentVideoHolder.stopPoints) {
								if (!qp.seen && (Math.abs(qp.stopPoint - currentPosition) < TIMER_PERIOD)) {
									stopTimer();
									pauseVideo();
									mCurrentQp = qp;
									mCurrentQp.seen = true;
									mYesNoParent.setVisibility(View.VISIBLE);
									mCorrectView.setVisibility(View.GONE);
									mIncorrectView.setVisibility(View.GONE);
									mPopup = this.createPopup(qp.questionText);
									mQuizTextLabel.setText(qp.questionText);
									showPopup(mPopup);
									break;
								}
							}
						}
					}
				}
			}
			else {
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
	
	private void initVideoList() {
		VideoHolder vh = new VideoHolder("Prevention of HIV", "android.resource://com.project.TeachAids/" + R.raw.chapter_1);
		mVideoList.add(vh);
		vh = new VideoHolder("How does someone get infected?", "android.resource://com.project.TeachAids/" + R.raw.chapter_6);
		mVideoList.add(vh);
		vh = new VideoHolder("Doctor's Challenge", "android.resource://com.project.TeachAids/" + R.raw.chapter_7i);
		vh.stopPoints.add(new QuestionPoint(15, "Can you get HIV from someone sneezing or coughing on you?", false));
		vh.stopPoints.add(new QuestionPoint(23, "Are Saliva and Mucus high risk fluids?", false));
		vh.stopPoints.add(new QuestionPoint(38, "Can you get HIV from sharing a needle?", true));
		mVideoList.add(vh);
		vh = new VideoHolder("How do you know there is an infection?", "android.resource://com.project.TeachAids/" + R.raw.chapter_8);
		mVideoList.add(vh);
		vh = new VideoHolder("How do you protect yourself?", "android.resource://com.project.TeachAids/" + R.raw.chapter_9);
		mVideoList.add(vh);
		vh = new VideoHolder("Why is testing important?", "android.resource://com.project.TeachAids/" + R.raw.chapter_10);
		mVideoList.add(vh);
		vh = new VideoHolder("Hindi Chapter 1", "android.resource://com.project.TeachAids/" + R.raw.hindichap_1);
		mVideoList.add(vh);			
		vh = new VideoHolder("Hindi Chapter 2", "android.resource://com.project.TeachAids/" + R.raw.hindichap_2);
		mVideoList.add(vh);			
		vh = new VideoHolder("Hindi Chapter 3", "android.resource://com.project.TeachAids/" + R.raw.hindichap_3);
		mVideoList.add(vh);					
	}
	
	private void setChapterLabelText() {
		String text = "Chapter " + (mCurrentVideoIndex+1) + " of " + mVideoList.size();
		mChapterLabel.setText(text);
	}
	
	private void setVideoTitleText() {
		if (mCurrentVideoIndex < mVideoList.size()) {
			mVideoTitleLabel.setText(mVideoList.get(mCurrentVideoIndex).title);
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
