package com.guyazhou.tools.plugin.reviewboard.http;

import com.guyazhou.tools.plugin.reviewboard.service.MemoryFile;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Client http request
 * This class helps to send POST http requests with various forms data, including files. And cookies can be added to the request.
 * Created by Yakov on 2017/1/2.
 */
public class ClientHttpRequest {

    private URLConnection urlConnection;
    private OutputStream outputStream;
    private Map<String, Object> cookiesMap = new HashMap<>();

    /**
     * 建立连接
     * @throws IOException io
     */
    protected void connect() throws IOException {
        if (null == outputStream) {
            outputStream = urlConnection.getOutputStream();
        }
    }

    /**
     * 写字符
     * @param c char
     * @throws IOException io
     */
    protected void write(char c) throws IOException {
        connect();
        outputStream.write(c);
    }

    /**
     * 写字符串
     * @param str string
     * @throws IOException io
     */
    protected void write(String str) throws IOException {
        connect();
        outputStream.write(str.getBytes("UTF-8"));
    }

    /**
     * 换行
     * @throws IOException io
     */
    protected void newLine() throws IOException {
        connect();
        write("\r\n");
    }

    /**
     * 字符串写入并换行
     * @param str string
     * @throws IOException io
     */
    protected void writeLine(String str) throws IOException {
        connect();
        write(str);
        newLine();
    }

    /**
     * 分割线
     */
    private void boundary() {

    }

    /**
     * Create a new multipart POST HTTP request on a freshly opened URLConnecion
     * @param urlConnection an already opened URL connection
     */
    public ClientHttpRequest(URLConnection urlConnection) throws IOException {
        this.urlConnection = urlConnection;
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "multipart/forms-data; boundary=" + "" );
    }

    /**
     * Create a new multipart POST HTTP request for a specific URL
     * @param url the URL to send request to
     */
    public ClientHttpRequest(URL url) throws IOException {
        this(url.openConnection());
    }

    /**
     * Create a new multipart POST HTTP request for a pecific URL string
     * @param urlStr the URL string
     */
    public ClientHttpRequest(String urlStr) throws IOException {
        this(new URL(urlStr));
    }

    private void postCookies() {
        StringBuffer cookies = new StringBuffer();
        for (Iterator iterator = cookiesMap.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            cookies.append(entry.getKey().toString() + "=" + entry.getValue());
            if (iterator.hasNext()) {
                cookies.append("; ");
            }
        }
        if (cookies.length() > 0) {
            urlConnection.setRequestProperty("Cookie", cookies.toString());
        }
    }

    /**
     * Add a cookie to the request
     * @param name cookie name
     * @param value cookie value
     */
    public void setCookie(String name, String value) {
        cookiesMap.put(name, value);
    }

    /**
     * Add cookies to the request
     * @param cookiesMap the cookie name-value map
     */
    public void setCookies(Map<String, String> cookiesMap) {
        if (null != cookiesMap) {
            this.cookiesMap.putAll(cookiesMap);
        }
    }

    /**
     * adds cookies to the request
     * @param strings array of cookie names and values (cookies[2*i] is a name, cookies[2*i + 1] is a value)
     */
    public void setCookies(String[] strings) {

    }

    private void writeName(String name) throws IOException {
        newLine();
        write("Content-Disposition: forms-data; name=\"");
        write(name.trim());
        write("\"");
    }

    /**
     * Add a string parameter to the request
     * @param name parameter name
     * @param value parameter value
     */
    public void setParameter(String name, String value) throws IOException {
        boundary();
        writeName(name);
        newLine();
        newLine();
        writeLine(value);
    }

    private static void pipe(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bytes = new byte[500000];
        int nextRead;
        int nextAvailable;
        int total = 0;
        synchronized (inputStream) {
            while ( (nextRead = inputStream.read(bytes, 0, bytes.length)) >= 0 ) {
                outputStream.write(bytes, 0, nextRead);
                total += nextRead;
            }
        }
        outputStream.flush();
        bytes = null;
    }

    /**
     * Add a file parameter to the request
     * @param name          parameter name
     * @param fileName      the name of the file
     * @param inputStream   input stream to read the contents of the file from
     */
    public void setFileParameter(String name, String fileName, InputStream inputStream) throws IOException {
        boundary();
        write(name);
        write("; filename=\"");
        write(fileName);
        write("\"");
        newLine();
        write("Content-Type: ");
        String type = URLConnection.guessContentTypeFromName(fileName);
        if (null == type) {
            type = "application/octet-stream";
        }
        writeLine(type);
        newLine();
        pipe(inputStream, outputStream);
        newLine();
    }

    /**
     * Add a file parameter to the request
     * @param name  parameter name
     * @param file  the file
     */
    public void setFileParameter(String name, File file) throws IOException {
        setFileParameter(name, file.getPath(), new FileInputStream(file));
    }

    /**
     * Add a parameter to the request.
     * if the parameter is a file, the file is uploaded;
     * otherwise, the string value of the parameter is passed in the request
     * @param name      paramter name
     * @param object    parameter value, a file or anything else that can be stringfied
     */
    public void setParameter(String name, Object object) throws IOException {
        if (object instanceof File) {
            setFileParameter(name, (File) object);
        } else if (object instanceof MemoryFile) {
            MemoryFile memoryFile = (MemoryFile) object;
            //setFileParameter();
        } else {
            setParameter(name, object.toString());
        }
    }

    /**
     * Add a parameter to the request
     * if the value is a file, the file is uploaded. otherwise it is stringified and sent in the request.
     * @param parameters "name-value" parameters
     */
    public void setParameters(Map<String, Object> parameters) throws IOException {
        if (null == parameters) {
            return;
        }
        for (Iterator iterator = parameters.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) iterator.next();
            setParameter(entry.getKey(), entry.getValue());
        }
    }

    public void setParameters(Object[] objects) {

    }

    /**
     * Post the requests to the server, with all the cookies and parameters added
     * @return Input stream that the server response
     * @throws IOException io
     */
    public InputStream post() throws IOException {
        boundary();
        write("--");
        outputStream.close();
        return urlConnection.getInputStream();
    }



}
