package com.project.TeachAids;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class UIUtils {
    public static void showMessageBox(Context context, String title, String message) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setPositiveButton(context.getResources().getString(R.string.oklabel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int which) 
                    {
                    }
                });
        alertDialog.show();
    }
}
