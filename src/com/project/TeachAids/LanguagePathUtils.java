package com.project.TeachAids;

import java.io.File;

import android.content.Context;
import android.os.Environment;

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
		
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
		    externalDir = new File(Environment.getExternalStorageDirectory(), "TeachAids");
		}
		return externalDir != null ? new File(externalDir, filename) : null;
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
		
		File externalDir = context.getExternalFilesDir(null);
        if (externalDir == null) {
            externalDir = new File(Environment.getExternalStorageDirectory(), "TeachAids");
        }
        return externalDir != null ? new File(externalDir, filename) : null;
	}
}
