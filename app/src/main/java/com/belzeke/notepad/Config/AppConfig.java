package com.belzeke.notepad.Config;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import com.belzeke.notepad.Helper.SQLiteHandler;
import com.belzeke.notepad.Models.Note;
import com.belzeke.notepad.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by marko on 4.3.2015.
 */
public final class AppConfig {
    public static Integer userId = -1;
    public static boolean NavigationShown = false;
    public static SQLiteHandler db;


    public static final String URLBase = "http://bazeni-nniicc.rhcloud.com/Belezka/";
    public static final String URLFiles = URLBase + "uploads/";
    public static final String URLVideoView = "video.php?id=";
    public static final String URLPostFile = URLBase + "saveVideo.php";
    public static final String URLogin = URLBase + "login.php";
    public static final String URLRegister = URLBase + "register.php";
    public static String LastFilePathCreated = "";

    public static final int CAMERA_REQUEST = 1888;
    public static final int VIDEO_REQUEST = 1889;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int MEDIA_TYPE_AUDIO = 3;


    public static final String NoteType = "NoteType";
    public static final int NOTE_PICTURE_TYPE = 1;
    public static final int NOTE_VIDEO_TYPE = 2;
    public static final int NOTE_AUDIO_TYPE = 3;
    public static final int NOTE_TEXT_TYPE = 4;

    public static List<Note> AdapterItems = new ArrayList<>();

    public static File getOutputMediaFile(Context context, int type) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.app_name));
        if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdirs()){
                Toast.makeText(context, context.getResources().getString(R.string.cannot_create_file), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", new Locale("Si")).format(new Date());
        File mediaFile;
        if(type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+timeStamp + ".jpg");
        }else if(type == MEDIA_TYPE_VIDEO){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_"+timeStamp+".mp4");
        }else if(type == MEDIA_TYPE_AUDIO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "AUD_"+timeStamp+".3gp");
        }else{
            return null;
        }
        return mediaFile;
    }

    @SafeVarargs
    public static <T> void executeTask(AsyncTask<T, ?, ?> asyncTask, T ... params) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        else
            asyncTask.execute(params);
    }

    public static Bitmap decodeFile(File f) {
        try {
            if(!f.exists()) return null;
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // The new size we want to scale to


            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 8;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException ignored) {}
        return null;
    }

    public static void showAlert(Context mContext, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(message).setTitle("Response from Servers")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean result = networkInfo != null && networkInfo.isConnected();
        if(!result){
            Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG).show();
        }
        return result;
    }

    public static boolean isWifiAvailable(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo != null && networkInfo.isConnected();
    }
}
