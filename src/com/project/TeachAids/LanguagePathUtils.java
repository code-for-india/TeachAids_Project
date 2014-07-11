package com.project.TeachAids;

import java.io.File;

import android.content.Context;

public class LanguagePathUtils {

	public enum Language {
		ENGLISH,
		AXOMIA,
		HINDI,
		KANNADA,
		MANDARIN,
		ODIA,
		SETSWANA,
		SWAHILI,
		SPANISH,
		TELUGU,
		TAMIL
	}
	
	public static String getLanguagePackUrl(Language lang) {
		switch (lang) {
			case ENGLISH: {
				return "https://s3.amazonaws.com/elasticbeanstalk-us-east-1-508207356148/english.zip";
			}
			default: {
				return "https://s3.amazonaws.com/elasticbeanstalk-us-east-1-508207356148/hindi.zip";
			}
		}
	}
	
	public static File getLanguagePackDownloadPath(Context context, Language lang) {
		String filename;
		switch (lang) {
			case ENGLISH: {
				filename = "english.zip";
				break;
			}
			case HINDI: {
				filename = "hindi.zip";
				break;
			}
			default: {
				filename = "hindi.zip";
				break;
			}
		}
		
		return new File(context.getExternalFilesDir(null), filename);
	}
	
	public static File getLanguagePackFolderPath(Context context, Language lang) {
		String filename;
		switch (lang) {
			case ENGLISH: {
				filename = "english";
				break;
			}
			case HINDI: {
				filename = "hindi";
				break;
			}
			default: {
				filename = "hindi";
				break;
			}
		}
		
		return new File(context.getExternalFilesDir(null), filename);
	}
}
