/*
 * Copyright 2011 mercatis Technologies AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mercatis.lighthouse3.commons.commons;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.Credentials;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;

/**
 * This class simplifies execution of HTTP requests.
 */
public class HttpRequest {

    /**
     * HTTP timeout in msecs.
     */
    public static int TIMEOUT = 30000;
    /**
     * This property maintains a reference to the HTTP client object to use for
     * remote HTTP method calls.
     */
    private HttpClient httpClient = null;

    public enum HttpMethod {

        GET, PUT, POST, DELETE
    }

    /**
     * This method performs a file upload via multipart POST request.
     *
     * @param url            the URL against which the file upload is performed
     * @param file           the file to upload
     * @param postParameters any additional parameters to add to the POST request
     * @return the data returned by the web server
     * @throws HttpException in case a communication error occurred.
     */
    public String postFileUpload(String url, File file, Map<String, String> postParameters) {

        PostMethod filePost = null;

        try {
            filePost = new PostMethod(url);
            Part[] mimeParts = new Part[postParameters.size() + 1];
            mimeParts[0] = new FilePart(file.getName(), file);
            int p = 1;
            for (String parameterName : postParameters.keySet()) {
                mimeParts[p] = new StringPart(parameterName, postParameters.get(parameterName));
                p++;
            }

            filePost.setRequestEntity(new MultipartRequestEntity(mimeParts, filePost.getParams()));
        } catch (Exception anyProblem) {
            throw new HttpException("A problem occurred while constructing file upload request", anyProblem);
        }


        int resultCode = 0;
        String resultBody = null;

        try {
            resultCode = this.httpClient.executeMethod(filePost);
            resultBody = filePost.getResponseBodyAsString();

            if (resultCode != 200) {
                throw new HttpException(resultBody, null);
            }
        } catch (HttpException httpException) {
            throw new HttpException("HTTP file upload failed", httpException);
        } catch (IOException ioException) {
            throw new HttpException("HTTP file upload failed", ioException);
        } finally {
            filePost.releaseConnection();
        }

        return resultBody;
    }

    /**
     * This method performs an HTTP request against an URL.
     *
     * @param url         the URL to call
     * @param method      the HTTP method to execute
     * @param body        the body of a POST or PUT request, can be <code>null</code>
     * @param queryParams a Hash with the query parameter, can be <code>null</code>
     * @return the data returned by the web server
     * @throws HttpException in case a communication error occurred.
     */
    @SuppressWarnings("deprecation")
    public String execute(String url, HttpRequest.HttpMethod method,
            String body, Map<String, String> queryParams) {

        NameValuePair[] query = null;

        if (queryParams != null) {
            query = new NameValuePair[queryParams.size()];

            int counter = 0;
            for (Entry<String, String> queryParam : queryParams.entrySet()) {
                query[counter] = new NameValuePair(queryParam.getKey(),
                        queryParam.getValue());
                counter++;
            }
        }

        org.apache.commons.httpclient.HttpMethod request = null;

        if (method == HttpMethod.GET) {
            request = new GetMethod(url);
        } else if (method == HttpMethod.POST) {
            PostMethod postRequest = new PostMethod(url);
            if (body != null) {
                postRequest.setRequestBody(body);
            }
            request = postRequest;
        } else if (method == HttpMethod.PUT) {
            PutMethod putRequest = new PutMethod(url);
            if (body != null) {
                putRequest.setRequestBody(body);
            }
            request = putRequest;
        } else if (method == HttpMethod.DELETE) {
            request = new DeleteMethod(url);
        }

        request.setRequestHeader("Content-type", "application/xml;charset=utf-8");
        if (query != null) {
            request.setQueryString(query);
        }

        int resultCode = 0;
        StringBuilder resultBodyBuilder = new StringBuilder();

        try {
            resultCode = this.httpClient.executeMethod(request);
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getResponseBodyAsStream(), Charset.forName("UTF-8")));
            String line = null;
            while ((line = reader.readLine()) != null) {
            	resultBodyBuilder.append(line);
            }
            
            if (resultCode != 200) {
                throw new HttpException(resultBodyBuilder.toString(), null);
            }
        } catch (HttpException httpException) {
            throw new HttpException("HTTP request failed", httpException);
        } catch (IOException ioException) {
            throw new HttpException("HTTP request failed", ioException);
        } catch (NullPointerException npe) {
        	throw new HttpException("HTTP request failed", npe);
        } finally {
            request.releaseConnection();
        }

        return resultBodyBuilder.toString();
    }

    /**
     * This method takes an unquoted URL string and quotes it.
     * @param url the url to quote
     * @return the quoted url.
     */
    static public String quoteUrl(String unquotedUrl) {
        try {
            return new URI(unquotedUrl, false, "UTF-8").getEscapedURI();
        } catch (URIException ex) {
            throw new HttpException("Invalid URL", ex);
        }
    }

    /**
     * This method takes a quoted base URL and appends another path element (unquoted to it)
     * @param url the quoted base URL
     * @param pathElementToAppend unquoted path element to append
     * @return the quoted result URL
     */
    static public String appendPathElementToUrl(String quotedBaseUrl, String unquotedPathElementToAppend) {
        try {
                unquotedPathElementToAppend = unquotedPathElementToAppend != null ? unquotedPathElementToAppend : "";
        	String quotedPathElement = new URI("http://remove.me/".concat(unquotedPathElementToAppend), false, "UTF-8").getEscapedURI().replace("http://remove.me/", "").replace("/", "%2F");
            return quotedBaseUrl + "/" + quotedPathElement;
        } catch (URIException ex) {
            throw new HttpException("Invalid URL", ex);
        }
    }

    /**
     * Creates a HttpRequest with the default timeout of 30000ms.
     */
    public HttpRequest() {
        this(TIMEOUT);
    }
    
    public HttpRequest(int timeoutInMilliseconds) {
	this(null, null, timeoutInMilliseconds);
    }
    
    public HttpRequest(String user, String password) {
	this(user, password, TIMEOUT);
    }
    
    @SuppressWarnings("deprecation")
    public HttpRequest(String user, String password, int timeoutInMilliseconds) {
        this.httpClient = new HttpClient(new SimpleHttpConnectionManager());
        this.httpClient.setTimeout(timeoutInMilliseconds);
	
	if (user != null) {
	    this.httpClient.getParams().setAuthenticationPreemptive(true);
	    Credentials defaultcreds = new UsernamePasswordCredentials(user, password);
	    this.httpClient.getState().setCredentials(AuthScope.ANY_REALM, null, defaultcreds);
	}
    }
}
