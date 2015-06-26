package com.belzeke.notepad.Asyncs;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.belzeke.notepad.Config.AppConfig;
import com.belzeke.notepad.Helper.SQLiteHandler;
import com.belzeke.notepad.Models.VideoMultiPartEntity;
import com.belzeke.notepad.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

/**
 * Created by marko on 15.4.2015.
 */

public class PostVideoAsyncTask extends AsyncTask <String, Integer, String> {
    private RoundCornerProgressBar mProgressBar;
    private long totalSize = 0;
    private Context mContext;
    private SQLiteHandler db;
    private long noteId;
    private ImageView uploadImage;

    public PostVideoAsyncTask(Context mContext, RoundCornerProgressBar mProgressBar, SQLiteHandler db, long noteId) {
        this.mProgressBar = mProgressBar;
        this.mContext = mContext;
        this.db = db;
        this.noteId = noteId;
    }

    public PostVideoAsyncTask(Context mContext, ViewGroup parent, SQLiteHandler db, long noteId) {
        this.mProgressBar = (RoundCornerProgressBar)parent.findViewById(R.id.gridViewItemProgressBar);
        this.uploadImage = (ImageView)parent.findViewWithTag("uploadTag");
        this.mContext = mContext;
        this.db = db;
        this.noteId = noteId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
        mProgressBar.setProgress(progress[0]);
    }

    @Override
    protected String doInBackground(String... names) {
        return uploadFile(names[0]);
    }
    @SuppressWarnings("deprecation")
    private String uploadFile(String fileName) {
        String responseString;

        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(AppConfig.URLPostFile);

        try {
            VideoMultiPartEntity entity = new VideoMultiPartEntity(
                    new VideoMultiPartEntity.ProgressListener() {

                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

            File sourceFile = new File(fileName);

            // Adding file data to http body
            entity.addPart("image", new FileBody(sourceFile));

            // Extra parameters if you want to pass to server
            entity.addPart("user_id", new StringBody(Integer.toString(AppConfig.userId)));
            entity.addPart("note_type", new StringBody(Integer.toString(AppConfig.NOTE_VIDEO_TYPE)));

            totalSize = entity.getContentLength();
            httppost.setEntity(entity);

            // Making server call
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity r_entity = response.getEntity();

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                // Server response
                responseString = EntityUtils.toString(r_entity);
            } else {
                responseString = "Error occurred! Http Status Code: "
                        + statusCode;
            }

        } catch (IOException e) {
            responseString = e.toString();
        }

        return responseString;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try{
            JSONObject json = new JSONObject(s);
            boolean success = json.getBoolean("success");
            if(success){
                if(uploadImage != null) uploadImage.setImageResource(R.drawable.check_mark);
                db.noteUploaded(noteId);
            }else{
                AppConfig.showAlert(mContext, json.getString("message"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
            AppConfig.showAlert(mContext, e.getMessage());
        }
        mProgressBar.setVisibility(View.GONE);

    }
}
