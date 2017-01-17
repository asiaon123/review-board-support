package com.guyazhou.tools.plugin.reviewboard.http;

import com.guyazhou.tools.plugin.reviewboard.model.DiffVirtualFile;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * HttpClient
 * Created by Yakov on 2017/1/2.
 */
public class HttpClient {

    private static final String MULTI_PART_BOUNDARY = "---------MULTIPART";
    private Map<String, String> headers;

    public HttpClient() {

    }

    public HttpClient(Map<String, String> headers) {
        this.headers = headers;
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

        if (HTTP_REQUEST_METHOD.POST == method || HTTP_REQUEST_METHOD.PUT == method) {
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
        InputStream inputStream;
        try {
            inputStream = httpURLConnection.getInputStream();
        } catch (IOException e) {
            throw new Exception("Get inputstram from connection error, " + e.getMessage());
        }
        StringBuilder responseMessageBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                responseMessageBuilder.append(line);
            }
        } catch (IOException e) {
            throw new Exception("Get message from inputstream error" + e.getMessage());
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return responseMessageBuilder.toString();
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
     * Post a request to server
     * @param urlStr server url
     * @param params params
     * @return response message
     * @throws Exception exception
     */
    public String post(String urlStr, Map<String, Object> params) throws Exception {
        return this.post(urlStr, params, false);
    }

    /**
     * Post a request to server with files
     * @param urlStr server url
     * @param params params
     * @param isMultiPart post files if true, default false
     * @return response string
     * @throws Exception exception
     */
    public String post(String urlStr, Map<String, Object> params, boolean isMultiPart) throws Exception {
        if (isMultiPart) {
            return this.postWithMultiplePart(urlStr, params);
        } else {
            return this.requestSimply(urlStr, HTTP_REQUEST_METHOD.POST, params);
        }
    }

    public String put(String urlStr, Map<String, Object> params) throws Exception {
        if (null == urlStr || "".equals(urlStr) || null == params) {
            throw new Exception("Url is empty or paramas is null");
        }
        if ( 0 == params.size() ) {
            return null;
        }
        return this.requestSimply(urlStr, HTTP_REQUEST_METHOD.PUT, params);
    }

    public String delete(String urlStr, Map<String, Object> params) {
        return null;
    }

    private String requestSimply(String urlStr, HTTP_REQUEST_METHOD method, Map<String, Object> params) throws Exception {

        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, method);
        httpURLConnection.setRequestMethod(method.toString());

        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  // TODO refactor

        OutputStream outputStream = httpURLConnection.getOutputStream();
        String postBody = "";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ( !"".equals(postBody) ) {
                postBody = postBody.concat( "&" + entry.getKey() + "=" + String.valueOf(entry.getValue()) );
            } else {
                postBody = entry.getKey() + "=" + String.valueOf(entry.getValue());
            }
        }
        outputStream.write( postBody.getBytes("UTF-8") );
        outputStream.flush();
        outputStream.close();

        return this.buildResponseMessage(httpURLConnection);
    }

    /**
     * POST a http request with multiple part
     * @param urlStr url string
     * @param params http parmas
     * @return response message
     * @throws IOException exception
     */
    private String postWithMultiplePart(String urlStr, Map<String, Object> params) throws Exception {

        HTTP_REQUEST_METHOD method = HTTP_REQUEST_METHOD.POST;
        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, method);
        httpURLConnection.setRequestMethod(method.toString());

        String boundary = this.getBoundary();
        this.setMultiPartHeader(httpURLConnection, boundary);
        this.setPostData(httpURLConnection, params, boundary);
        return this.buildResponseMessage(httpURLConnection);
    }

    /*
     * Http multipart/form-data
     * header: Content-Type: multipart/form-data; boundary=--%{boundary}
     * body:
     *      --${boundary}
     *      Content-Dispositoty: form-data; name="textField"
     *
     *      plain text
     *      --${boundary}
     *      Content-Disposition: form-data; name=""; filename=""
     *      Content-Type: application/octer-stream (image/gif)(image/jpeg)
     *
     *      %{file[filename] content}
     *      {   --%{boundary}               // if more files
     *          .
     *          .
     *          .
     *      }
     *      --${boundary}--
     */

    /**
     * Set post body data with file and plaintext
     * @param httpURLConnection http url connection
     * @param params params
     * @param boundary boundary
     * @throws Exception exception
     */
    private void setPostData(HttpURLConnection httpURLConnection, Map<String, Object> params, String boundary) throws Exception {
        if(null == httpURLConnection || null == params) {
            throw new Exception("Connection or params is null");
        }
        if( 0 != params.size() && null != boundary && !"".equals(boundary) ) {
            OutputStream outputStream = httpURLConnection.getOutputStream();
            StringBuilder postDataBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof File) {
                    File file = (File) entry.getValue();
                    if (null == file) {
                        continue;
                    }
                    String fileName = file.getName();
                    String fileType = HttpURLConnection.guessContentTypeFromName(fileName);
                    if (null == fileType) {
                        fileType = "application/octet-stream";
                    }
                    InputStream inputStream;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new Exception("Create inputstream from file error" + e.getMessage());
                    }
                    postDataBuilder.append("--").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"; filename=\"").append(fileName).append("\r\n")
                            .append("Content-Type: ").append(fileType).append("\r\n")
                            .append("\r\n")
                            .append( this.getFileData(inputStream) ).append("\r\n");
                } else if (entry.getValue() instanceof DiffVirtualFile) {
                    DiffVirtualFile file = (DiffVirtualFile) entry.getValue();
                    String fileName = file.getName();
                    String fileType = HttpURLConnection.guessContentTypeFromName(fileName);
                    if (null == fileType) {
                        fileType = "application/octet-stream";
                    }
                    postDataBuilder.append("--").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"; filename=\"").append(fileName).append("\"\r\n")
                            .append("Content-Type: ").append(fileType).append("\r\n")
                            .append("\r\n")
                            .append( file.getContent() ).append("\r\n");
                } else {
                    postDataBuilder.append("--").append(boundary).append("\r\n")
                            .append("Content-Disposition: form-data; name=\"").append(entry.getKey()).append("\"\r\n")
                            .append("\r\n")
                            .append( String.valueOf(entry.getValue()) ).append("\r\n");
                }
            }
            postDataBuilder.append("--").append(boundary).append("--");
            outputStream.write( postDataBuilder.toString().getBytes("UTF-8") );
            outputStream.flush();
            outputStream.close();
        }
    }

    /**
     * Get file data
     * @param inputstream inputstream
     * @return file data
     * @throws Exception exception
     */
    private String getFileData(InputStream inputstream) throws Exception {
        // TODO fix for files
        StringBuilder fileDataBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
        String line;
        while ( (line = reader.readLine()) != null ) {
            fileDataBuilder.append(line);
        }
        /*byte[] buffer = new byte[500000];
        int readCount;
        try {
            while ( (readCount = inputstream.read(buffer)) >= 0 ) {
                //System.out.println(Arrays.toString(buffer));
                //System.exit(0);
                //fileDataBuilder.append( Arrays.toString(buffer) );
            }
            System.out.println("-><>><><><: " + buffer[5]) ;
        } catch (IOException e) {
            throw new Exception("Read from inputstream or write to output stream error");
        }*/
        System.out.println(fileDataBuilder.toString());
        return fileDataBuilder.toString();
    }

    /**
     * Get multipart boundary
     * @return multipart boundary string
     */
    private String getBoundary() {
        return MULTI_PART_BOUNDARY + Long.toString(new Random().nextLong(), 36);
    }

    /**
     * Set multipart header
     * @param httpURLConnection connection
     * @param boundary boundary
     * @throws Exception exception
     */
    private void setMultiPartHeader(HttpURLConnection httpURLConnection, String boundary) throws Exception {
        if (null == boundary) {
            throw new Exception("Post boundary is null");
        }
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

}
