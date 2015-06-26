package com.belzeke.notepad.Helper;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.belzeke.notepad.Listeners.MultipartProgressListener;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.util.CharsetUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by marko on 2.5.2015.
 */
public class MultipartRequest extends Request<String> {

    MultipartEntityBuilder entity = MultipartEntityBuilder.create();
    HttpEntity httpentity;
    private String FILE_PART_NAME = "files";

    private final Response.Listener<String> mListener;
    private final File mFilePart;
    private final Map<String, String> mStringPart   ;
    private Map<String, String> headerParams;
    private final MultipartProgressListener multipartProgressListener;
    private long fileLength = 0L;
    private int contentType;

    public MultipartRequest(String url, Response.ErrorListener errorListener,
                            Response.Listener<String> listener,File file, long fileLength,
                            Map<String, String> mStringPart,
                            final Map<String, String> headerParams, String partName,
                            MultipartProgressListener progLitener, int contentType) {

        super(Method.POST, url, errorListener);
        this.FILE_PART_NAME = partName;
        this.fileLength = fileLength;
        this.headerParams = headerParams;
        this.mFilePart = file;
        this.mListener = listener;
        this.mStringPart = mStringPart;
        this.multipartProgressListener = progLitener;
        this.contentType = contentType;

        entity.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        try{
            entity.setCharset(CharsetUtils.get("UTF-8"));
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        buildMultipartEntity();
        httpentity = entity.build();
    }

    private void buildMultipartEntity() {
        String create = "image/gif";
        switch (contentType){
            case 1:
                create = "image/gif";
                break;
            case 2:
                create = "video/mp4";
                break;
            case 3:
                create = "video/3gpp";
                break;
            default:
                create = "image/gif";
        }

        entity.addPart(FILE_PART_NAME, new FileBody(mFilePart, ContentType.create(create), mFilePart.getName()));
        if(mStringPart != null){
            for (Map.Entry<String, String> entry : mStringPart.entrySet()){
                entity.addTextBody(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public String getBodyContentType() {
        return httpentity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try{
            httpentity.writeTo(new CountingOutputStream(bos, fileLength, multipartProgressListener));
        }catch (IOException e){
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse networkResponse) {
        return null;
    }

    @Override
    protected void deliverResponse(String s) {
        mListener.onResponse(s);
    }

    private class CountingOutputStream extends FilterOutputStream {
        private final MultipartProgressListener progListener;
        private long transferred;
        private long fileLength;

        public CountingOutputStream(final OutputStream out, long fileLength,
                                    final MultipartProgressListener listener) {
            super(out);
            this.fileLength = fileLength;
            this.progListener = listener;
            this.transferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            if(progListener != null){
                this.transferred += len;
                int prog = (int)(transferred * 100 / fileLength);
                this.progListener.transferred(this.transferred, prog);
            }
        }

        public void write(byte[] b) throws IOException{
            out.write(b);
            if (progListener != null) {
                this.transferred++;
                int prog = (int) (transferred * 100 / fileLength);
                this.progListener.transferred(this.transferred, prog);
            }
        }
    }
}
