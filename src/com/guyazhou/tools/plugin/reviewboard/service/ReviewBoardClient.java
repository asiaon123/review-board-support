package com.guyazhou.tools.plugin.reviewboard.service;

import com.google.gson.Gson;
import com.guyazhou.tools.plugin.reviewboard.http.HttpClient;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.io.BufferedReader;
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

    public ReviewBoardClient() throws Exception {
        String server = ReviewBoardSettings.getSettings().getState().getServer();
        if (null == server || server.trim().isEmpty()) {
            Messages.showMessageDialog((Project) null, "Please set the review board server address in config panel!", "info", Messages.getInformationIcon());
            ShowSettingsUtil.getInstance().showSettingsDialog(null, ReviewBoardSettings.getSettingName());
            throw new Exception("Please set the review board server address in config panel!");
        }

    }

    public static boolean postReview(ReviewSettings reviewBoardSettings, ProgressIndicator progressIndicator) {
        try {
            ReviewBoardClient reviewBoardClient = new ReviewBoardClient();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String reviewId = reviewBoardSettings.getReviewId();


        return false;
    }

    public static String login(String server, String username, String password) {
        // 去除掉后面的"/"
        if (null != server && server.endsWith("/")) {
            server = server.substring(0, server.length() - 1);
        }

        String apiURL = server + "/api/";
        try {
            URL url = new URL(apiURL);
            System.out.println("Api url:" + url);

            URLConnection urlConnection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            httpURLConnection.setRequestProperty("Authorization", "Basic" + Base64.getEncoder().encodeToString( (username + ":" + password).getBytes() ));
            httpURLConnection.setInstanceFollowRedirects(false);

            int responseCode = httpURLConnection.getResponseCode();
            if (401 == responseCode) {
                throw new Exception("The username or the password is not correct!");
            }

            InputStream inputStream = httpURLConnection.getInputStream();
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while (null != ( line = bufferedReader.readLine() )) {
                stringBuilder.append(line);
            }

            Gson gson = new Gson();
            Response response = gson.fromJson(stringBuilder.toString(), Response.class);
            if (response.isOK()) {
                CookieManager cookieManager = new CookieManager();
                cookieManager.put(url.toURI(), httpURLConnection.getHeaderFields());
                List<HttpCookie> httpCookieList = cookieManager.getCookieStore().getCookies();
                for (HttpCookie httpCookie : httpCookieList) {
                    if ("reviewBoardSessionId".equals(httpCookie.getName())) {
                        return "reviewBoardSessionId" + httpCookie.getValue();
                    }
                }
            } else {
                throw new Exception(response.getErrorMsg());
            }
            throw new Exception("The username or password is incorrect!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public RepositoryResponse getRepositories() throws MalformedURLException {
        String path = "repositories/";
        String json = new HttpClient().httpGet("");
        return null;
    }

}
