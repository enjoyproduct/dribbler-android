package com.android.volley.request;

import android.graphics.Bitmap;
import android.provider.SyncStateContract;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.error.AuthFailureError;
import com.android.volley.error.ParseError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.multipart.FilePart;
import com.android.volley.toolbox.multipart.MultipartEntity;
import com.android.volley.toolbox.multipart.StringPart;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class CustomMultipartRequest extends Request<JSONObject> {
    public static final String KEY_PICTURE = "mypicture";
    public static final String KEY_PICTURE_NAME = "filename";
    public static final String KEY_ROUTE_ID = "route_id";

//    private HttpEntity mHttpEntity;
    private MultipartEntity multipartEntity;

    private String mRouteId;
    private Response.Listener mListener;

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////start
    public CustomMultipartRequest(String url, Response.Listener<JSONObject> listener,
                                  Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        multipartEntity = new MultipartEntity();
        mListener = listener;
//        mHttpEntity = new MultipartEntity() ;

    }
    public CustomMultipartRequest addStringPart(String key, String value) {
        StringPart stringPart = new StringPart(key, value, "UTF-8");
        multipartEntity.addPart(stringPart);
        return this;
    }
    public CustomMultipartRequest addImagePart(String key, String filePath) {
        FilePart filePart = new FilePart(key, new File(filePath), filePath.substring(filePath.lastIndexOf("/") + 1), "image/jpg");
        multipartEntity.addPart(filePart);
        return this;
    }

    public CustomMultipartRequest addVideoPart(String key, String filePath) {
        FilePart filePart = new FilePart(key, new File(filePath), filePath.substring(filePath.lastIndexOf("/") + 1), "video/mp4");
        multipartEntity.addPart(filePart);
        return this;
    }

    @Override
    public String getBodyContentType() {
        return multipartEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(bos);
        try {
            multipartEntity.writeTo(outputStream);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
        String jsonString = new String(response.data,
                HttpHeaderParser.parseCharset(response.headers));
        return Response.success(new JSONObject(jsonString),
                HttpHeaderParser.parseCacheHeaders(response));
    } catch (UnsupportedEncodingException e) {
        return Response.error(new ParseError(e));
    } catch (JSONException je) {
        return Response.error(new ParseError(je));
    }
}

    @Override
    protected void deliverResponse(JSONObject response) {
        // TODO Auto-generated method stub
        mListener.onResponse(response);
    }
}