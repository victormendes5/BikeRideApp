package com.infnet.bikeride.bikeride.dao;

import android.os.AsyncTask;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpRequest extends AsyncTask<String, Void, String> {

    private static final String TAG = "HttpRequest";

    private HttpRequest.Callbacks callback;

    public interface Callbacks {
        String OnComplete(String data);
        String OnFailure(Exception e);
    }


    public HttpRequest(Callbacks callbacks) {
        callback = callbacks;
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
            callback.OnFailure(e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        callback.OnComplete(s);

    }
}
