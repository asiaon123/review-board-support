package com.guyazhou.tools.plugin.reviewboard.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * HttpClient
 * Created by Yakov on 2017/1/2.
 */
public class HttpClient {

    private String cookie;

    public HttpClient() {
        this(null);
    }

    public HttpClient(String cookie) {
        this.cookie = cookie;
    }

    /**
     * HttpGet
     * @param path url
     * @return http post
     * @throws IOException io
     */
    public String httpGet(String path) throws IOException {
        System.out.println(path);
        URL url = new URL(path);

        URLConnection urlConnection = url.openConnection();
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        this.addCookie(httpURLConnection);
        httpURLConnection.setInstanceFollowRedirects(false);
        InputStream inputStream = urlConnection.getInputStream();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ( null != (line = bufferedReader.readLine()) ) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    public String httpPost(String path, Map<String, Object> params) throws IOException {
        return this.httpRequestWithMultiplePart(path, "POST", params);
    }

    public String httpRequestWithMultiplePart(String path, String method, Map<String, Object> params) throws IOException {
        URL url = new URL("");

        URLConnection urlConnection = url.openConnection();
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        this.addCookie(httpURLConnection);
        httpURLConnection.setRequestMethod(method);

        return null;
    }

    private void addCookie(URLConnection urlConnection) {
        if(null != cookie) {
            urlConnection.setRequestProperty("Cookie", this.cookie);
        }
    }

    public String getCookie() {
        return cookie;
    }

    public void setCookie(String cookie) {
        this.cookie = cookie;
    }
}
