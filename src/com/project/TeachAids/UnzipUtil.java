package com.project.TeachAids;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class UnzipUtil extends AsyncTask<Void, Integer, Boolean> {
    private Context mContext;
    private String mPackPath;
    private String mFolderPath;
    private ProgressDialog mDialog;
    private UnzipFinishedListener mListener;
    
    public interface UnzipFinishedListener {
        public void unzipFinished(boolean success);
    } 
    
    public UnzipUtil(Context context, UnzipFinishedListener listener, String packPath, String folderPath) {
        mContext = context;
        mListener = listener;
        mPackPath = packPath;
        mFolderPath = folderPath;
        mDialog = new ProgressDialog(mContext);
        deleteRecursive(new File(folderPath));
        dirChecker("");
    }
    
    public void cancel() {
        try {
            this.cancel(true);
        } catch (Exception ex) {
            
        }
    }
    
    @Override
    protected void onPreExecute() {
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDialog.setCancelable(false);
        mDialog.setMessage(mContext.getString(R.string.unzip_progress_message));
        mDialog.show();
    }

    @Override
    protected void onCancelled() {
        mDialog.cancel();
    }

    @Override
    protected Boolean doInBackground(Void... param) {
        boolean success = unzip();
        if (success) {
            // delete the zip file to save space
            success = new File(mPackPath).delete();
        }
        
        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        switch (values[0]) {
        case 0:
            mDialog.setProgress(values[1]);
            break;
        }
    }
    
    @Override
    protected void onPostExecute(Boolean result) {
        mDialog.cancel();
        if (mListener != null) {
            mListener.unzipFinished(result);
        }
    }
    
    private boolean unzip() {
        ZipInputStream zin = null;
        ZipFile zipFile = null;
        BufferedInputStream bis = null;
        try {
            FileInputStream fin = new FileInputStream(mPackPath);
            zin = new ZipInputStream(fin);
            zipFile = new ZipFile(mPackPath);
            bis = new BufferedInputStream(zin);
            ZipEntry ze = null;
            byte[] buffer = new byte[2048];
            final int numEntries = zipFile.size();
            int currentEntry = 0;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    dirChecker(ze.getName());
                } else {
                    FileOutputStream fout = new FileOutputStream(new File(mFolderPath, ze.getName()).getPath());
                    BufferedOutputStream bos = new BufferedOutputStream(fout, buffer.length);
                    int len;
                    while ((len = bis.read(buffer, 0, buffer.length)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                    //bis.close();
                    bos.close();
                    fout.close();
                    zin.closeEntry();
                }
                publishProgress(0, (int)(100.0 * (double)currentEntry / (double)numEntries));
                currentEntry++;
            }
            return true;
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
            return false;
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (zin != null) {
                    zin.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private void dirChecker(String dir) {
        File f = new File(mFolderPath, dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }
    
    void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }
}
