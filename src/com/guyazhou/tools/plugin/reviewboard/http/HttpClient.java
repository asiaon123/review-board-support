package com.guyazhou.tools.plugin.reviewboard.http;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpClient
 * Created by Yakov on 2017/1/2.
 */
public class HttpClient {

    private Map<String, String> headers;

    public HttpClient() {

    }

    public HttpClient(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Request a GET http request
     * @param urlStr url
     * @return response messages
     * @throws IOException io
     */
    public String get(String urlStr) throws Exception {
        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, HTTP_REQUEST_METHOD.GET);
        // redirect automaticly
        httpURLConnection.setInstanceFollowRedirects(false);

        return this.buildResponseMessage(httpURLConnection);
    }

    /**
     * POST a http request
     * @param urlStr url string
     * @param params http params
     * @return responseMessage
     * @throws IOException exception
     */
    public String post(String urlStr, Map<String, Object> params) throws Exception {
        return this.httpRequestWithMultiplePart(urlStr, HTTP_REQUEST_METHOD.POST, params);
    }

    public String put(String urlStr, Map<String, Object> params) {
        return null;
    }

    public String delete(String urlStr, Map<String, Object> params) {
        return null;
    }

    /**
     * POST a http request with multiple part
     * @param urlStr url string
     * @param method http method
     * @param params http parmas
     * @return response message
     * @throws IOException exception
     */
    private String httpRequestWithMultiplePart(String urlStr, HTTP_REQUEST_METHOD method, Map<String, Object> params) throws Exception {

        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, method);
        httpURLConnection.setRequestMethod("");
        httpURLConnection.setRequestMethod(method.toString());

        OutputStream outputStream = httpURLConnection.getOutputStream();
        PrintWriter printWriter = new PrintWriter(outputStream, true);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            printWriter.println(entry.getKey() + "=" + entry.getValue());
        }
        printWriter.flush();

        return this.buildResponseMessage(httpURLConnection);
    }

    /**
     * Build a HttpURLConnection instance by url string and reuest method
     * @param urlStr url string
     * @param method request method
     * @return a HttpURLConnection instance
     * @throws Exception exception
     */
    private HttpURLConnection buildHttpURLConnection(String urlStr, HTTP_REQUEST_METHOD method) throws Exception {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new Exception("UrlStr is not format properly" + e.getMessage());
        }

        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new Exception("Open url connection error" + e.getMessage());
        }

        if (HTTP_REQUEST_METHOD.POST == method) {
            urlConnection.setDoOutput(true);    // default: false, set true make it possible to get a output stream
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        try {
            httpURLConnection.setRequestMethod(method.toString());
        } catch (ProtocolException e) {
            throw new Exception("Set http url connection method error" + e.getMessage());
        }

        // set headers
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpURLConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
        return httpURLConnection;
    }

    /**
     * Build response message from inputstream
     * @param httpURLConnection HttpURLConnection instance
     * @return response message
     */
    private String buildResponseMessage(HttpURLConnection httpURLConnection) throws Exception {
        InputStream inputStream = httpURLConnection.getInputStream();
        StringBuilder responseMessageBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                responseMessageBuilder.append(line);
            }
        } catch (IOException e) {
            throw new Exception("Get message from inputstream error");
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                throw new Exception("Close inputstream error");
            }
        }
        return responseMessageBuilder.toString();
    }

}
