package com.infnet.bikeride.bikeride;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;

public class AsyncReflectedHttpRequest extends AsyncTask<String, Void, String> {

    private static final String TAG = "AsyncReflectedHttpReq";

    Context context;
    String mOnCompleteMethodName;

    public AsyncReflectedHttpRequest(Context context, String onCompleteMethodName) {
        this.context = context;
        mOnCompleteMethodName = onCompleteMethodName;
    }

    @Override
    protected String doInBackground(String... urls)
    {


        String result = "";
        URL url;
        HttpURLConnection urlConnection = null;

        try {

            url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = urlConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(in);
            int data = reader.read();

            while (data != -1) {
                char current = (char) data;
                result += current;
                data = reader.read();
            }

            return result;


        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Log.i(TAG, "onPostExecute: DATA RECEIVED - " + s);

        if (mOnCompleteMethodName.equals("")) {
            Log.i(TAG, "onPostExecute: error, no OnCompleteMethodName passed.");
            return;
        }

        if (s.equals("") || s.equals(null)) {
            Log.i(TAG, "onPostExecute: error, request returned nothing.");
            return;
        }

        Class[] parameterTypes = new Class[] {String.class};

        try {
            Method sentMethod = context.getClass().getDeclaredMethod(mOnCompleteMethodName,
                    parameterTypes);
            sentMethod.setAccessible(true);
            sentMethod.invoke(context, s);
        }

        catch (Exception e) {
            Log.i(TAG, "onPostExecute: error, no OnCompleteMethodName passed might be wrong " +
                    "or non-existent. Check stacktrace below.");
            e.printStackTrace();
        }

    }
}
