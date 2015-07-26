package com.belzeke.notepad.Asyncs;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by marko on 19.3.2015.
 */
public class PostImageAsyncTask extends AsyncTask<Bitmap, Integer, String> {

    private RoundCornerProgressBar mProgressBar;
    private long totalSize = 0;
    private Context mContext;
    private String tempFilePath = "";
    private long noteId;
    private ImageView uploadImage;
    private SQLiteHandler db;


    public PostImageAsyncTask(Context context, RoundCornerProgressBar progressBar, SQLiteHandler db, long noteId) {
        this.mContext = context;
        this.mProgressBar = progressBar;
        this.db = db;
        this.noteId = noteId;
    }
    public PostImageAsyncTask(Context context, ViewGroup parent, SQLiteHandler db, long noteId) {
        this.mContext = context;
        this.mProgressBar = (RoundCornerProgressBar)parent.findViewById(R.id.gridViewItemProgressBar);
        this.db = db;
        this.noteId = noteId;
        this.uploadImage = (ImageView)parent.findViewWithTag("uploadTag");

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setProgress(0);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        mProgressBar.setProgress(progress[0]);
    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {

        return uploadFile(bitmaps[0]);
    }

    @SuppressWarnings("deprecation")
    private String uploadFile(Bitmap image) {
        if(image == null) return "File doesn't Exist!!";

        if(createFile(image)){
            String responseString;
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(AppConfig.URLPostFile);

            try {
                VideoMultiPartEntity entity = new VideoMultiPartEntity(
                        new VideoMultiPartEntity.ProgressListener() {
                        @Override
                        public void transferred(long num) {
                            publishProgress((int) ((num / (float) totalSize) * 100));
                        }
                    });

                File sourceFile = new File(this.tempFilePath);
                File renameTo = new File(AppConfig.LastFilePathCreated);
                if(sourceFile.renameTo(renameTo)){
                    entity.addPart("image", new FileBody(renameTo));
                }

                entity.addPart("user_id", new StringBody(Integer.toString(AppConfig.userId)));
                entity.addPart("note_type", new StringBody(Integer.toString(AppConfig.NOTE_PICTURE_TYPE)));

                totalSize = entity.getContentLength();
                httpPost.setEntity(entity);


                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if(statusCode == 200){
                    responseString = EntityUtils.toString(r_entity);
                }else{
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            }catch (IOException e) {
                responseString = e.toString();
            }

            return responseString;

        }else
            return "Can't create file!";

    }

    private boolean createFile(Bitmap image) {

        File f = new File(Environment.getExternalStorageDirectory(), mContext.getResources().getString(R.string.app_name));
        if(!f.exists()){
            if(!f.mkdir()){
                AppConfig.showAlert(mContext, mContext.getResources().getString(R.string.cannot_create_file));
                return false;
            }
        }
        String tempName = "temp.jpg";
        File temp = new File(f.getPath() + File.separator + tempName);
        try {
            if(temp.exists()){
                if(temp.delete()){
                    if(temp.createNewFile()){
                        tempFilePath = temp.getAbsolutePath();
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                        byte[] bitmapData = bos.toByteArray();

                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();
                    }
                }
            }else{
                if(temp.createNewFile()){
                    tempFilePath = temp.getAbsolutePath();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    image.compress(Bitmap.CompressFormat.JPEG, 25, bos);
                    byte[] bitmapData = bos.toByteArray();

                    FileOutputStream fos = new FileOutputStream(temp);
                    fos.write(bitmapData);
                    fos.flush();
                    fos.close();
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPreExecute();
        super.onPostExecute(s);

        try{
            JSONObject json = new JSONObject(s);
            boolean success = json.getBoolean("success");
            if(success){
                if(uploadImage != null) uploadImage.setImageResource(R.drawable.check_mark);
                db.noteUploaded(noteId);
            }else{
                Toast.makeText(mContext, json.getString("message"), Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        mProgressBar.setVisibility(View.GONE);

    }
}
