package com.example.rennt.arcticsunrise.data.api.requests;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Strategy;
import org.simpleframework.xml.strategy.TreeStrategy;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class XMLRequest<T> extends Request<T> {
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;
    private final XMLParser<T> xmlParser;

    public interface XMLParser<T> {
        public T parse(String xml) throws XmlPullParserException, IOException;
    }

    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param headers Map of request headers
     */
    public XMLRequest(String url, Map<String, String> headers,
                      XMLParser<T> xmlParser,
                      Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        Strategy strategy = new TreeStrategy("SectionPage", "ArticleWrapper");
        this.headers = headers;
        this.listener = listener;
        this.xmlParser = xmlParser;
    }

    public XMLRequest(String url, XMLParser<T> xmlParser,
                      Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(url, null, xmlParser, listener, errorListener);
    }


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        return headers != null ? headers : super.getHeaders();
    }

    @Override
    protected void deliverResponse(T response) {
        listener.onResponse(response);
    }

    @Override
    protected Response<T> parseNetworkResponse(NetworkResponse response) {
        try {
            String xml = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            return Response.success(
                    xmlParser.parse(xml),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
