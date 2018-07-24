package com.guyazhou.tools.plugin.reviewboard.service;

import com.google.gson.Gson;
import com.guyazhou.tools.plugin.reviewboard.http.HttpClient;
import com.guyazhou.tools.plugin.reviewboard.model.DiffVirtualFile;
import com.guyazhou.tools.plugin.reviewboard.model.Response;
import com.guyazhou.tools.plugin.reviewboard.model.ReviewParams;
import com.guyazhou.tools.plugin.reviewboard.model.draft.DraftResponse;
import com.guyazhou.tools.plugin.reviewboard.model.repository.RepositoryResponse;
import com.guyazhou.tools.plugin.reviewboard.model.review_request.ReviewRequestDraft;
import com.guyazhou.tools.plugin.reviewboard.setting.ReviewBoardSetting;
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

    public ReviewBoardClient() throws Exception {
        this.loadApiURL();
    }

    /**
     * Load api url from setting panel
     * @throws Exception exception
     */
    private void loadApiURL() throws Exception {
        ReviewBoardSetting.State state = ReviewBoardSetting.getInstance().getState();
        if (null == state) {
            throw new Exception("Read state error");
        }
        String serverURL = state.getServerURL();
        if (null == serverURL || "".equals(serverURL)) {
            // TODO pop up login dialog
            throw new Exception("Server url is empty");
        }
        this.apiURL = serverURL + "api/";
    }

    /**
     * Get cookie
     * @return cookie value
     * @throws Exception exception
     */
    private String getCookie() throws Exception {
        if (null == this.cookie) {
            Map<String, String> userInfo;
            try {
                userInfo = this.loadUserInfo();
            } catch (Exception e) {
                throw new Exception("Load user info from setting error, " + e.getMessage());
            }
            if (null == userInfo || 0 == userInfo.size()) {
                throw new Exception("User info is empty");
            }
            ReviewBoardSetting.State state = ReviewBoardSetting.getInstance().getState();
            if (null == state) {
                throw new Exception("Read state error");
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
     * Post files to reviewboard server
     * @param reviewParams review params
     * @param progressIndicator indicator
     * @return true if success, otherwise false
     */
    public static boolean submitReview(ReviewParams reviewParams, ProgressIndicator progressIndicator) throws Exception {

        ReviewBoardClient reviewBoardClient;
        try {
            reviewBoardClient = new ReviewBoardClient();
        } catch (Exception e) {
            throw new Exception("ReviewBoardClient init error, " + e.getMessage());
        }

        String reviewId = reviewParams.getReviewId();
        // generate a new review request
        if (null == reviewId || "".equals(reviewId)) {
            progressIndicator.setText("Creating review draft...");
            ReviewRequestDraft reviewRequestDraft;
            try {
                reviewRequestDraft = reviewBoardClient.createNewReviewRequest(reviewParams.getRepositoryId());
            } catch (Exception e) {
                throw new Exception("Create new review request draft error, " + e.getMessage());
            }

            if (null == reviewRequestDraft) {
                throw new Exception("Create new review request error from server");
            } else if (!reviewRequestDraft.isOK()) {
                throw new Exception(reviewRequestDraft.getErr().getCode() + ": " + reviewRequestDraft.getErr().getMsg());
            }
            progressIndicator.setText("Created new review request draft: " + reviewRequestDraft.getReview_request());
            reviewParams.setReviewId( String.valueOf(reviewRequestDraft.getReview_request().getId()) );
        }

        // upload diff virtual files
        progressIndicator.setText("Uploading diff virtual file(s)...");
        Response response;
        try {
            response = reviewBoardClient.uploadVirtualDifferencesFile(reviewParams);
        } catch (Exception e) {
            throw new Exception("Uploading diff virtual file error, " + e.getMessage());
        }
        if (null == response) {
            throw new Exception("Response is null");
        }
        if( !response.isOK() ) {
            throw new Exception(response.getErr().getCode() + ": " + response.getErr().getMsg());
        }

        // update draft
        progressIndicator.setText("Updating review request draft...");
        DraftResponse draftResponse;
        try {
            draftResponse = reviewBoardClient.updateReviewRequestDraft(reviewParams);
        } catch (Exception e) {
            throw new Exception("Update review request error, " + e.getMessage());
        }
        if (null == draftResponse) {
            throw new Exception("DraftResponse is null");
        }
        if ( !draftResponse.isOK() ) {
            throw new Exception(response.getErr().getCode() + ": " + response.getErr().getMsg());
        }
        progressIndicator.setText("Review request draft is updated");
        return true;
    }

    /**
     * Create a new review request draft in review board server
     * @return a NewReviewRequestResonse instance transformed from review board server
     */
    private ReviewRequestDraft createNewReviewRequest(String repositoryId) throws Exception {
        if(null == repositoryId) {
            return null;
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new Exception("Get cookie error, " + e.getMessage());
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
     * @param reviewParams fields, include summary, branch, bus_closed, description, tartget_people, tartget_groups
     * @return A DraftResponse instance
     */
    private DraftResponse updateReviewRequestDraft(ReviewParams reviewParams) throws Exception {

        if (null == reviewParams) {
            throw new Exception("Review params is null");
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new Exception("Get cookie error, " + e.getMessage());
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
     * @param reviewId review request id
     * @return true if success, otherwise false
     * @throws Exception exception
     */
    public Boolean autoReview(String reviewId) throws Exception {

        // verify second person user info
        ReviewBoardSetting.State persistentState = ReviewBoardSetting.getInstance().getState();
        if (null == persistentState) {
            throw new Exception("State is null");
        }
        String companionUsername = persistentState.getCompanionUsername();
        String companionPassword = persistentState.getCompanionPassword();
        if (null == companionUsername || "".equals(companionUsername)
                || null == companionPassword || "".equals(companionPassword)) {
            throw new Exception("Companion username or password is empty");
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
            throw new Exception("Get cookie error, " + e.getMessage());
        }
        headers.put("Cookie", cookie);

        String responseJson;
        try {
            responseJson = new HttpClient(headers).post(this.apiURL + "review-requests/" + reviewId + "/reviews/", params);
        } catch (Exception e) {
            throw new Exception("Post review request fails, " + e.getMessage());
        }
        Gson gson = new Gson();
        Response response = gson.fromJson(responseJson, Response.class);
        if (response.isOK()) {

            // second person
            try {
                cookie = ReviewBoardClient.login(this.apiURL, companionUsername, companionPassword);
            } catch (Exception e) {
                throw new Exception("Companion login failed, " + e.getMessage());
            }
            if ("".equals(cookie)) {
                throw new Exception("Companion cookie is empty");
            }
            headers.put("Cookie", cookie);
            try {
                responseJson = new HttpClient(headers).post(this.apiURL + "review-requests/" + reviewId + "/reviews/", params);
            } catch (Exception e) {
                throw new Exception("Post review request fails, " + e.getMessage());
            }
            response = gson.fromJson(responseJson, Response.class);
            if (response.isOK()) {
                return true;
            } else {
                throw new Exception("Second person: " + response.getErr().getCode() + ": " + response.getErr().getMsg());
            }

        } else {
            throw new Exception("First Person: " + response.getErr().getCode() + ": " + response.getErr().getMsg());
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
     * Upload diff vitrual file
     * @param reviewParams review params
     * @return Response
     * @throws Exception exception
     */
    private Response uploadVirtualDifferencesFile(ReviewParams reviewParams) throws Exception {
        if (null == reviewParams) {
            throw new Exception("Review params is null");
        }
        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new Exception("Get cookie error, " + e.getMessage());
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
     * ReviewBoard user login based username:password
     * @param urlStr reviewboard server
     * @param username username
     * @param password password
     * @return cookie
     * @throws Exception exception
     */
    public static String login(String urlStr, String username, String password) throws Exception {
        URL url;
        try {
            url = new URL(urlStr);
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
                    return "rbsessionid=" + httpCookie.getValue();
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
        String response;

        String cookie;
        try {
            cookie = this.getCookie();
        } catch (Exception e) {
            throw new Exception("Get cookie error, " + e.getMessage());
        }
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", cookie);

        try {
            response = new HttpClient(headers).get(path);
        } catch (Exception e) {
            throw new Exception("Get repositories fails, " + e.getMessage());
        }
        Gson gson = new Gson();
        return gson.fromJson(response, RepositoryResponse.class);
    }

}
