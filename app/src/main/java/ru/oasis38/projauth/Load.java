package ru.oasis38.projauth;

import android.app.ProgressDialog;
import android.content.Context;

public class Load {
    static public ProgressDialog progress;
    static public void download(Context context){
        progress=new ProgressDialog(context);
        progress.setMessage("Загрузка");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
        progress.show();
    }
}
