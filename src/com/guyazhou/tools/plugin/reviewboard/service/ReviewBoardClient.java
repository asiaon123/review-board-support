package com.guyazhou.tools.plugin.reviewboard.service;

import com.google.gson.Gson;
import com.guyazhou.tools.plugin.reviewboard.http.HttpClient;
import com.guyazhou.tools.plugin.reviewboard.model.reviewboard.ReviewParams;
import com.guyazhou.tools.plugin.reviewboard.model.reviewboard.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.settings.ReviewBoardSetting;
import com.intellij.openapi.progress.ProgressIndicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.List;

/**
 * ReviewBoard client
 * Created by Yakov on 2017/1/2.
 */
public class ReviewBoardClient {

    private String apiURL;

    public ReviewBoardClient() throws Exception {
        String server = ReviewBoardSetting.getInstance().getState().getServerURL();
        if (null == server || server.trim().isEmpty()) {
            throw new Exception("Please set the review board server address in config panel!");
        }

        this.apiURL = server + "/api/";

    }

    /**
     * Post files to reviewboard server
     * @param reviewSettings review params
     * @param progressIndicator indicator
     * @return true if success, otherwise false
     */
    public static boolean submitReview(ReviewParams reviewSettings, ProgressIndicator progressIndicator) throws Exception {
        try {
            ReviewBoardClient reviewBoardClient = new ReviewBoardClient();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String reviewId = reviewSettings.getReviewId();
        if (null == reviewId || "".equals(reviewId)) {
            progressIndicator.setText("Creating review draft...");
        }


        return true;
    }

    private void createNewReviewRequest() {

    }

    /**
     * ReviewBoard user login based username:password
     * @param server reviewboard server
     * @param username username
     * @param password password
     * @return cookie
     * @throws Exception exception
     */
    public static String login(String server, String username, String password) throws Exception {
        // 去除掉后面的"/"
        if (null != server && server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }
        String apiURL = server + "/api/";
        URL url;
        try {
            url = new URL(apiURL);
        } catch (MalformedURLException e) {
            throw new Exception("URL path is wrong");
        }
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new Exception("Open connection fails");
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        httpURLConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString( (username + ":" + password).getBytes() ));
        httpURLConnection.setInstanceFollowRedirects(false);

        int responseCode;
        try {
            responseCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            throw new Exception("Get response code error");
        }
        if (401 == responseCode) {
            throw new Exception("The username or the password is not correct!");
        }

        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = httpURLConnection.getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ( (line = bufferedReader.readLine()) != null ) {  // TODO improve the performance when network is bad
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            throw new Exception("Read from httpConnection stream error");
        } finally {
            if (null != inputStream) {
                inputStream.close();
            }
            if (null != bufferedReader) {
                bufferedReader.close();
            }
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(stringBuilder.toString(), Response.class);
        if (response.isOK()) {
            CookieManager cookieManager = new CookieManager();
            try {
                cookieManager.put(url.toURI(), httpURLConnection.getHeaderFields());
            } catch (IOException | URISyntaxException e) {
                throw new Exception("Put cookie error");
            }
            List<HttpCookie> httpCookieList = cookieManager.getCookieStore().getCookies();
            for (HttpCookie httpCookie : httpCookieList) {
                if ("rbsessionid".equals(httpCookie.getName())) {
                    return "rbsessionid:" + httpCookie.getValue();
                }
            }
        } else {
            throw new Exception("It's not ok??");
        }

        return "";
    }

    /**
     * Get all rrepositories
     * @return all repositories
     * @throws IOException exception
     */
    public RepositoryResponse getRepositories() throws Exception {
        String path = apiURL + "repositories/";
        String json;
        try {
            json = new HttpClient().httpGet(path);
        } catch (Exception e) {
            throw new Exception("Get repositories fails");
        }
        System.out.println(json);
        Gson gson = new Gson();
        return gson.fromJson(json, RepositoryResponse.class);
    }

}
