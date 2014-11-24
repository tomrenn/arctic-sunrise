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

import java.io.UnsupportedEncodingException;
import java.util.Map;

public class XMLRequest<T> extends Request<T> {
    private final Serializer serializer;
    private final Class<T> clazz;
    private final Map<String, String> headers;
    private final Response.Listener<T> listener;


    /**
     * Make a GET request and return a parsed object from JSON.
     *
     * @param url URL of the request to make
     * @param clazz Relevant class object, for Gson's reflection
     * @param headers Map of request headers
     */
    public XMLRequest(String url, Class<T> clazz, Map<String, String> headers,
                      Response.Listener<T> listener, Response.ErrorListener errorListener) {
        super(Method.GET, url, errorListener);
        Strategy strategy = new TreeStrategy("SectionPage", "ArticleWrapper");
        this.serializer = new Persister(strategy);
        this.clazz = clazz;
        this.headers = headers;
        this.listener = listener;
    }

    public XMLRequest(String url, Class<T> clazz,
                      Response.Listener<T> listener, Response.ErrorListener errorListener) {
        this(url, clazz, null, listener, errorListener);
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
                    serializer.read(clazz, xml),
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }
}
