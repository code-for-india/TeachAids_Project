package com.project.TeachAids;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

public class UnzipUtil {
	private String zipFile;
	private String location;

	public static boolean extractPackToFolderAndDelete(String packPath, String folderPath) {
		UnzipUtil uzutil = new UnzipUtil(packPath, folderPath);
		boolean success = uzutil.unzip();
		if (success) {
		    // delete the zip file to save space
		    success = new File(packPath).delete();
		}
		
		return success;
	}
	
	private UnzipUtil(String zipFile, String location) {
		this.zipFile = zipFile;
		this.location = location;
		dirChecker("");
	}
	
	private boolean unzip() {
		try {
			FileInputStream fin = new FileInputStream(zipFile);
			ZipInputStream zin = new ZipInputStream(fin);
			ZipEntry ze = null;
			byte[] buffer = new byte[65536];
			while ((ze = zin.getNextEntry()) != null) {
				Log.v("Decompress", "Unzipping " + ze.getName());
				if (ze.isDirectory()) {
					dirChecker(ze.getName());
				} else {
				    FileOutputStream fout = new FileOutputStream(new File(location, ze.getName()).getPath());     
					BufferedOutputStream bos = new BufferedOutputStream(fout, buffer.length);
					int len;
					while ((len = zin.read(buffer, 0, buffer.length)) != -1) {
					    bos.write(buffer, 0, len);
					}
					bos.close();
					fout.close();
					zin.closeEntry();
				}
			}
			zin.close();
			return true;
		} catch(Exception e) {
			Log.e("Decompress", "unzip", e);
			return false;
		}
	}

	private void dirChecker(String dir) {
		File f = new File(location, dir);
		if (!f.isDirectory()) {
			f.mkdirs();
		}
	}
}