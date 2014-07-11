package com.project.TeachAids;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

public class VideoListModel {
    private static final String TAG = "VideoListModel";
    
    public static class QuestionPoint {
        private String mQuestionText; 
        private int mQuestionStopPoint; // stop point for the question in milliseconds
        private boolean mCorrectAnswerIsYes;
        private int mCorrectAnswerSeekPoint;
        private int mIncorrectAnswerSeekPoint;
        private boolean mQuestionActive = true;
                
        private QuestionPoint(String questionText, 
                             int questionStopPoint,
                             boolean correctAnswerIsYes,
                             int correctAnswerSeekPoint,
                             int incorrectAnswerSeekPoint) {
            mQuestionText = questionText;
            mQuestionStopPoint = questionStopPoint;
            mCorrectAnswerIsYes = correctAnswerIsYes;
            mCorrectAnswerSeekPoint = correctAnswerSeekPoint;
            mIncorrectAnswerSeekPoint = incorrectAnswerSeekPoint;
        }
        
        public String getQuestionText() {
            return mQuestionText;
        }

        public int getStopPoint() {
            return mQuestionStopPoint;
        }

        public boolean correctAnswerIsYes() {
            return mCorrectAnswerIsYes;
        }

        public int getCorrectAnswerSeekPoint() {
            return mCorrectAnswerSeekPoint;
        }

        public int getIncorrectAnswerSeekPoint() {
            return mIncorrectAnswerSeekPoint;
        }

        public boolean questionActive() {
            return mQuestionActive;
        }

        public void setQuestionActive(boolean questionActive) {
            mQuestionActive = questionActive;
        }
    }

    public static class VideoHolder {
        private String mTitle;
        private String mPath;
        private List<QuestionPoint> mStopPoints = new ArrayList<QuestionPoint>();
        
        private VideoHolder(String title, String path) { 
            mTitle = title;
            mPath = path;
            if (!mPath.startsWith("file:")) {
                mPath = (mPath.startsWith("/") ? "file://" : "file://") + mPath;
            }
        }

        public String getTitle() {
            return mTitle;
        }

        public String getPath() {
            return mPath;
        }

        public final List<QuestionPoint> getStopPoints() {
            return mStopPoints;
        }

        public void addStopPoint(QuestionPoint point) {
            mStopPoints.add(point);
        }
    }
    
    public static List<VideoHolder> initVideoList(String path) {
        List<VideoHolder> videoList = parseVideoList(path);
        if (videoList != null) {
            parseQuestionList(path, videoList);
        }
        return videoList;
    }
    
    private static List<VideoHolder> parseVideoList(String path) {
        final String videoListMetadataFileName = "videolist.csv";
        try {
            ArrayList<VideoHolder> videoList = new ArrayList<>();
            CSVReader csvReader = new CSVReader(new FileReader(new File(path, videoListMetadataFileName).getPath()));
            Map<String, String> dict = null;
            while ((dict = csvReader.readNextMap()) != null) {
                String videoTitle = null, videoPath = null;
                for (String key : dict.keySet()) {
                    if (key.equalsIgnoreCase("title")) {
                        videoTitle = dict.get(key);
                    }
                    if (key.equalsIgnoreCase("filename")) {
                        videoPath = new File(path, dict.get(key)).getPath();
                    }
                }
                if (videoTitle != null && videoPath != null) {
                    videoList.add(new VideoHolder(videoTitle, videoPath));
                }
            }
            return videoList;
        } catch (FileNotFoundException fex) {
            Log.e(TAG, fex.getMessage());
            return null;
        } catch (IOException ioe) {
            Log.e(TAG, ioe.getMessage());
            return null;
        }
    }
    
    private static void parseQuestionList(String path, List<VideoHolder> videoList) {
        final String questionListMetadataFileName = "questionlist.csv";
        
        if (!new File(path, questionListMetadataFileName).exists()) {
            return;
        }
        
        try {
            CSVReader csvReader = new CSVReader(new FileReader(new File(path, questionListMetadataFileName).getPath()));
            Map<String, String> dict = null;
            while ((dict = csvReader.readNextMap()) != null) {
                String videoTitle = null, questionText = null;
                int questionStopPoint = -1, correctAnswerSeekPoint = -1, incorrectAnswerSeekPoint = -1;
                boolean correctAnswerIsYes = false;
                
                for (String key : dict.keySet()) {
                    //Question text   Time of stop    If Right go to time If Wrong go to time Correct answer is Yes
                    if (key.equalsIgnoreCase("Video Title")) {
                        videoTitle = dict.get(key);
                    }
                    if (key.equalsIgnoreCase("Question text")) {
                        questionText = dict.get(key);
                    }
                    if (key.equalsIgnoreCase("Time of stop")) {
                        questionStopPoint = Integer.parseInt(dict.get(key));
                    }
                    if (key.equalsIgnoreCase("If Right go to time")) {
                        correctAnswerSeekPoint = Integer.parseInt(dict.get(key));
                    }
                    if (key.equalsIgnoreCase("If Wrong go to time")) {
                        incorrectAnswerSeekPoint = Integer.parseInt(dict.get(key));
                    }
                    if (key.equalsIgnoreCase("Correct answer is Yes")) {
                        String value = dict.get(key);
                        if (value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true")) {
                            correctAnswerIsYes = true;
                        } else if (value.equalsIgnoreCase("no") || value.equalsIgnoreCase("false")) {
                            correctAnswerIsYes = false;
                        }
                    }
                }
                
                if (videoTitle!= null && questionText != null) {
                    VideoHolder videoHolder = getVideoHolderForTitle(videoTitle, videoList);
                    if (videoHolder != null) {
                        videoHolder.addStopPoint(new QuestionPoint(questionText, 
                                                                   questionStopPoint, 
                                                                   correctAnswerIsYes, 
                                                                   correctAnswerSeekPoint, 
                                                                   incorrectAnswerSeekPoint));
                    }
                }
            }
        } catch (FileNotFoundException fex) {
        } catch (IOException ioe) {
        } catch (NumberFormatException nfe) {
        }
    }
    
    private static VideoHolder getVideoHolderForTitle(String title, List<VideoHolder> videoList) {
        if (videoList != null) {
            for (VideoHolder holder : videoList) {
                if (holder.getTitle().equalsIgnoreCase(title)) {
                    return holder;
                }
            }
        }
        return null;
    }
}
