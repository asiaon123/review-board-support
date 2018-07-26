package com.guyazhou.tools.plugin.reviewboard.http;

import com.guyazhou.tools.plugin.reviewboard.model.DiffVirtualFile;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.Random;

/**
 * HttpClient
 *
 * @author YaZhou.Gu 2017/1/2.
 */
public class HttpClient {

    private static final String MULTI_PART_BOUNDARY = "---------MULTIPART";
    private Map<String, String> headers;

    public HttpClient() { }

    public HttpClient(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * Build a HttpURLConnection instance by url string and reuest method
     *
     * @param urlStr url string
     * @param method request method
     * @return a HttpURLConnection instance
     */
    private HttpURLConnection buildHttpURLConnection(String urlStr, HTTP_METHOD method) {
        URL url;
        try {
            url = new URL(urlStr);
        } catch (MalformedURLException e) {
            throw new RuntimeException("UrlStr is not format properly" + e.getMessage());
        }

        URLConnection urlConnection;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new RuntimeException("Open url connection error" + e.getMessage());
        }

        if (HTTP_METHOD.POST == method || HTTP_METHOD.PUT == method) {
            urlConnection.setDoOutput(true);    // default: false, set true make it possible to get a output stream
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
        try {
            httpURLConnection.setRequestMethod(method.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException("Set http url connection method error" + e.getMessage());
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
     *
     * @param httpURLConnection HttpURLConnection instance
     * @return response message
     */
    private String buildResponseMessage(HttpURLConnection httpURLConnection) {
        InputStream inputStream;
        try {
            inputStream = httpURLConnection.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException("Get inputstram from connection error, " + e.getMessage());
        }
        StringBuilder responseMessageBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                responseMessageBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Get message from inputstream error" + e.getMessage());
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
     *
     * @param urlStr url
     * @return response messages
     */
    public String get(String urlStr) {
        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, HTTP_METHOD.GET);
        // redirect automaticly
        httpURLConnection.setInstanceFollowRedirects(false);

        return this.buildResponseMessage(httpURLConnection);
    }

    /**
     * Post a request to server
     *
     * @param urlStr server url
     * @param params params
     * @return response message
     */
    public String post(String urlStr, Map<String, Object> params) {
        return this.post(urlStr, params, false);
    }

    /**
     * Post a request to server with files
     *
     * @param urlStr server url
     * @param params params
     * @param isMultiPart post files if true, default false
     * @return response string
     */
    public String post(String urlStr, Map<String, Object> params, boolean isMultiPart) {
        if (isMultiPart) {
            return this.postWithMultiplePart(urlStr, params);
        } else {
            return this.requestSimply(urlStr, HTTP_METHOD.POST, params);
        }
    }

    public String put(String urlStr, Map<String, Object> params) {
        if (null == urlStr || "".equals(urlStr) || null == params) {
            throw new RuntimeException("Url is empty or paramas is null");
        }
        if ( 0 == params.size() ) {
            return null;
        }
        return this.requestSimply(urlStr, HTTP_METHOD.PUT, params);
    }

    public String delete(String urlStr, Map<String, Object> params) {
        return null;
    }

    private String requestSimply(String urlStr, HTTP_METHOD method, Map<String, Object> params) {

        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, method);
        try {
            httpURLConnection.setRequestMethod(method.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");  // TODO refactor

        OutputStream outputStream;
        try {
            outputStream = httpURLConnection.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String postBody = "";
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if ( !"".equals(postBody) ) {
                postBody = postBody.concat( "&" + entry.getKey() + "=" + String.valueOf(entry.getValue()) );
            } else {
                postBody = entry.getKey() + "=" + String.valueOf(entry.getValue());
            }
        }
        try {
            outputStream.write( postBody.getBytes("UTF-8") );
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this.buildResponseMessage(httpURLConnection);
    }

    /**
     * Post a http request with multiple part
     *
     * @param urlStr url string
     * @param params http parmas
     * @return response message
     */
    private String postWithMultiplePart(String urlStr, Map<String, Object> params) {

        HTTP_METHOD method = HTTP_METHOD.POST;
        HttpURLConnection httpURLConnection = this.buildHttpURLConnection(urlStr, method);
        try {
            httpURLConnection.setRequestMethod(method.toString());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }

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
     *
     * @param httpURLConnection http url connection
     * @param params params
     * @param boundary boundary
     */
    private void setPostData(HttpURLConnection httpURLConnection, Map<String, Object> params, String boundary) {
        if(null == httpURLConnection || null == params) {
            throw new RuntimeException("Connection or params is null");
        }
        if( 0 != params.size() && null != boundary && !"".equals(boundary) ) {
            OutputStream outputStream;
            try {
                outputStream = httpURLConnection.getOutputStream();
            } catch (IOException e) {
                throw new RuntimeException("Get output stream error", e);
            }
            StringBuilder postDataBuilder = new StringBuilder();
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                if (entry.getValue() instanceof File) {
                    File file = (File) entry.getValue();
                    String fileName = file.getName();
                    String fileType = HttpURLConnection.guessContentTypeFromName(fileName);
                    if (null == fileType) {
                        fileType = "application/octet-stream";
                    }
                    InputStream inputStream;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException("Create inputstream from file error" + e.getMessage());
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
            try {
                outputStream.write( postDataBuilder.toString().getBytes("UTF-8") );
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new RuntimeException("Stream error", e);
            }
        }
    }

    /**
     * Get file data
     *
     * @param inputstream inputstream
     * @return file data
     */
    private String getFileData(InputStream inputstream) {
        // TODO fix for files
        StringBuilder fileDataBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
        String line;
        try {
            while ( (line = reader.readLine()) != null ) {
                fileDataBuilder.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Reading from socket error", e);
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
     *
     * @param httpURLConnection connection
     * @param boundary boundary
     */
    private void setMultiPartHeader(HttpURLConnection httpURLConnection, String boundary) {
        if (null == boundary) {
            throw new RuntimeException("Post boundary is null");
        }
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
    }

}
