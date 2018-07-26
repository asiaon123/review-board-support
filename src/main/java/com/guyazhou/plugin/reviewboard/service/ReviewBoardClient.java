package com.guyazhou.plugin.reviewboard.service;

import com.google.gson.Gson;
import com.guyazhou.plugin.reviewboard.http.HttpClient;
import com.guyazhou.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.plugin.reviewboard.setting.ReviewBoardSetting;
import com.guyazhou.plugin.reviewboard.exceptions.IllegalSettingException;
import com.guyazhou.plugin.reviewboard.model.DiffVirtualFile;
import com.guyazhou.plugin.reviewboard.model.Response;
import com.guyazhou.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.plugin.reviewboard.model.draft.DraftResponse;
import com.guyazhou.plugin.reviewboard.model.review_request.ReviewRequestDraft;
import com.intellij.openapi.progress.ProgressIndicator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ReviewBoard client
 *
 * @author YaZhou.Gu 2017/1/2
 */
public class ReviewBoardClient {

    /**
     * Review board server url in setting panel
     */
    private String apiURL;

    /**
     * Review board cookie
     */
    private String cookie;

    public ReviewBoardClient() {
        this.loadApiURL();
    }

    /**
     * Load api url from setting panel
     */
    private void loadApiURL() {
        ReviewBoardSetting.State state = ReviewBoardSetting.getInstance().getState();
        if (null == state) {
            throw new IllegalSettingException("Can not get review setting state");
        }
        String serverURL = state.getServerURL();
        if (null == serverURL || "".equals(serverURL)) {
            // TODO pop up login dialog
            throw new IllegalSettingException("Server url is not configured");
        }
        this.apiURL = serverURL + "api/";
    }

    /**
     * Get cookie
     *
     * @return cookie value
     */
    private String getCookie() {
        if (null == this.cookie) {
            Map<String, String> userInfo;
            try {
                userInfo = this.loadUserInfo();
            } catch (Exception e) {
                throw new RuntimeException("Load user info from setting error, " + e.getMessage());
            }
            if (null == userInfo || 0 == userInfo.size()) {
                throw new RuntimeException("User info is empty");
            }
            ReviewBoardSetting.State state = ReviewBoardSetting.getInstance().getState();
            if (null == state) {
                throw new RuntimeException("Read state error");
            }
            return ReviewBoardClient.login(this.apiURL, userInfo.get("username"), userInfo.get("password"));
        }
        return this.cookie;
    }

    /**
     * Load user info from setting panel
     * @return user info map
     * @throws Exception exception
     */
    private Map<String, String> loadUserInfo() throws Exception {
        ReviewBoardSetting.State state = ReviewBoardSetting.getInstance().getState();
        if (null == state) {
            throw new Exception("Read state error");
        }
        String username = state.getUsername();
        String password = state.getPassword();
        if(null == username || null == password) {
            // TODO pop up login dialog
            throw new Exception("Username or password is empty");
        }
        Map<String, String> userInfoMap = new HashMap<>();
        userInfoMap.put("username", username);
        userInfoMap.put("password", password);
        return userInfoMap;
    }

    /**
     * Post submit to review board server
     *
     * @param reviewParams review params
     * @param progressIndicator indicator
     * @return true if success, otherwise false
     */
    public boolean submitReview(ReviewParams reviewParams, ProgressIndicator progressIndicator) {
        String reviewId = reviewParams.getReviewId();
        // Creating review requst draft
        if (null == reviewId || "".equals(reviewId)) {
            progressIndicator.setText("Creating Review Request Draft");
            ReviewRequestDraft reviewRequestDraft = this.createNewReviewRequest(reviewParams.getRepositoryId());

            if (null == reviewRequestDraft) {
                throw new RuntimeException("Create new review request error from server");
            } else if (!reviewRequestDraft.isOK()) {
                throw new RuntimeException(reviewRequestDraft.getErr().getCode() + ": " + reviewRequestDraft.getErr().getMsg());
            }
            reviewParams.setReviewId( String.valueOf(reviewRequestDraft.getReview_request().getId()) );
        }

        // Uploading diffs
        progressIndicator.setText("Uploading Diffs");
        Response response = this.uploadDiffs(reviewParams);
        if (null == response) {
            throw new RuntimeException("Response is null");
        }
        if(!response.isOK()) {
            throw new RuntimeException(response.getErr().getCode() + ": " + response.getErr().getMsg());
        }

        // Update review request draft
        progressIndicator.setText("Updating Review Request Draft");
        DraftResponse draftResponse = this.updateReviewRequestDraft(reviewParams);
        if (null == draftResponse) {
            throw new RuntimeException("DraftResponse is null");
        }
        if (!draftResponse.isOK()) {
            throw new RuntimeException(response.getErr().getCode() + ": " + response.getErr().getMsg());
        }
        return true;
    }

    /**
     * Create a new review request draft in review board server
     * @return a NewReviewRequestResonse instance transformed from review board server
     */
    private ReviewRequestDraft createNewReviewRequest(String repositoryId) {
        if(null == repositoryId) {
            return null;
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new RuntimeException("Get cookie error, " + e.getMessage());
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);

        Map<String, Object> params = new HashMap<>();
        params.put("repository", repositoryId);
        String responseJson = new HttpClient(headers).post(apiURL + "review-requests/", params);

        Gson gson = new Gson();
        return gson.fromJson(responseJson, ReviewRequestDraft.class);
    }

    /**
     * Update draft with several fields
     *
     * @param reviewParams fields, include summary, branch, bus_closed, description, tartget_people, tartget_groups
     * @return A DraftResponse instance
     */
    private DraftResponse updateReviewRequestDraft(ReviewParams reviewParams) {

        if (null == reviewParams) {
            throw new RuntimeException("Review params is null");
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new RuntimeException("Get cookie error, " + e.getMessage());
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);

        Map<String, Object> params = new HashMap<>();
        addParam(params, "summary", reviewParams.getSummary());
        addParam(params, "branch", reviewParams.getBranch());
        addParam(params, "bugs_closed", reviewParams.getBugsClosed());
        addParam(params, "description", reviewParams.getDescription());
        addParam(params, "target_people", reviewParams.getPerson());
        addParam(params, "target_groups", reviewParams.getGroup());
        addParam(params, "public", "1");    // make it public

        String responseJson = new HttpClient(headers).put(apiURL + "review-requests/" + reviewParams.getReviewId() + "/draft/", params);

        Gson gson = new Gson();
        return gson.fromJson(responseJson, DraftResponse.class);
    }

    /*
    POST http://demo.reviewboard.org/api/review-requests/316/reviews/

    ship_it=true&body_top=Ship+It!&public=1

     */

    /**
     * Auto review for review-request
     *
     * @param reviewId review request id
     * @return true if success, otherwise false
     */
    public Boolean autoReview(String reviewId) {

        // verify second person user info
        ReviewBoardSetting.State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            throw new RuntimeException("State is null");
        }
        String companionUsername = persistentState.getCompanionUsername();
        String companionPassword = persistentState.getCompanionPassword();
        if (null == companionUsername || "".equals(companionUsername)
                || null == companionPassword || "".equals(companionPassword)) {
            throw new RuntimeException("Companion username or password is empty");
        }

        // basic params
        Map<String, String> headers = new HashMap<>();
        Map<String, Object> params = new HashMap<>();
        params.put("ship_it", "true");
        params.put("body_top", "Ship It!");
        params.put("public", "1");

        // first person review
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new RuntimeException("Get cookie error, " + e.getMessage());
        }
        headers.put("Cookie", cookie);

        String responseJson;
        try {
            responseJson = new HttpClient(headers).post(this.apiURL + "review-requests/" + reviewId + "/reviews/", params);
        } catch (Exception e) {
            throw new RuntimeException("Post review request fails, " + e.getMessage());
        }
        Gson gson = new Gson();
        Response response = gson.fromJson(responseJson, Response.class);
        if (response.isOK()) {

            // second person
            try {
                cookie = ReviewBoardClient.login(this.apiURL, companionUsername, companionPassword);
            } catch (Exception e) {
                throw new RuntimeException("Companion login failed, " + e.getMessage());
            }
            if ("".equals(cookie)) {
                throw new RuntimeException("Companion cookie is empty");
            }
            headers.put("Cookie", cookie);
            try {
                responseJson = new HttpClient(headers).post(this.apiURL + "review-requests/" + reviewId + "/reviews/", params);
            } catch (Exception e) {
                throw new RuntimeException("Post review request fails, " + e.getMessage());
            }
            response = gson.fromJson(responseJson, Response.class);
            if (response.isOK()) {
                return true;
            } else {
                throw new RuntimeException("Second person: " + response.getErr().getCode() + ": " + response.getErr().getMsg());
            }

        } else {
            throw new RuntimeException("First Person: " + response.getErr().getCode() + ": " + response.getErr().getMsg());
        }
    }

    /**
     * Put a key-value to map
     * @param params map
     * @param key key
     * @param value value
     */
    private void addParam(Map<String, Object> params, String key, String value) {
        if (null == params || null == key || null == value || "".equals(value)) {
            return;
        }
        params.put(key, value);
    }

    /**
     * Upload diffs
     *
     * @param reviewParams review params
     * @return Response
     */
    private Response uploadDiffs(ReviewParams reviewParams) {
        if (null == reviewParams) {
            throw new RuntimeException("Review params is null");
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new RuntimeException("Get cookie error, " + e.getMessage());
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);

        Map<String, Object> params = new HashMap<>();
        params.put("basedir", reviewParams.getSvnBasePath());
        params.put("path", new DiffVirtualFile("review.diff", reviewParams.getDiff()));

        String responseJson = new HttpClient(headers).post(apiURL + "review-requests/" + reviewParams.getReviewId() + "/diffs/", params, true);

        Gson gson = new Gson();
        return gson.fromJson(responseJson, Response.class);
    }

    /**
     * Review board login based basic(username:password)
     *
     * @param urlStr reviewboard server
     * @param username username
     * @param password password
     * @return cookie
     */
    public static String login(String urlStr, String username, String password) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL path is wrong");
        }
        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Open connection fails");
        }
        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        httpURLConnection.setRequestProperty("Authorization", "Basic " + Base64.getEncoder().encodeToString( (username + ":" + password).getBytes() ));
        httpURLConnection.setInstanceFollowRedirects(false);

        int responseCode;
        try {
            responseCode = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            throw new RuntimeException("Get response code error");
        }
        if (401 == responseCode) {
            throw new RuntimeException("The username or the password is not correct!");
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
            throw new RuntimeException("Read from httpConnection stream error");
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Gson gson = new Gson();
        Response response = gson.fromJson(stringBuilder.toString(), Response.class);
        if (response.isOK()) {
            CookieManager cookieManager = new CookieManager();
            try {
                cookieManager.put(url.toURI(), httpURLConnection.getHeaderFields());
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException("Put cookie error");
            }
            List<HttpCookie> httpCookieList = cookieManager.getCookieStore().getCookies();
            for (HttpCookie httpCookie : httpCookieList) {
                if ("rbsessionid".equals(httpCookie.getName())) {
                    return "rbsessionid=" + httpCookie.getValue();
                }
            }
        } else {
            throw new RuntimeException("It's not ok??");
        }
        return "";
    }

    /**
     * Get repositories
     *
     * @return all repositories
     */
    public RepositoryResponse getRepositories() {
        String path = apiURL + "repositories/";

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", this.getCookie());

        String response = new HttpClient(headers).get(path);
        Gson gson = new Gson();
        return gson.fromJson(response, RepositoryResponse.class);
    }

}
