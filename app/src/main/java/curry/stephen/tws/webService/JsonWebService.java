package curry.stephen.tws.webService;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HttpEntity;

/**
 * Web Service for json data communication class.<br/>
 * Created by lingchong on 16/6/1.
 */
public abstract class JsonWebService {

    public abstract void successPostCallBack(int statusCode, Header[] headers, JSONObject jsonObject);

    public abstract void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject);

    public abstract void successPostCallBack(int statusCode, Header[] headers, JSONArray response);

    public abstract void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse);

    public abstract void successGetCallBack(int statusCode, Header[] headers, String responseString);

    public abstract void failureGetCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable);

    public abstract void successPostCallBack(int statusCode, Header[] headers, String responseString);

    public abstract void failurePostCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable);

    public void invokePostMethod(final Context context, String uri, HttpEntity httpEntity) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.post(context, uri, httpEntity, "application/json", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                successPostCallBack(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                failurePostCallBack(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                successPostCallBack(statusCode, headers, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                failurePostCallBack(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                super.onSuccess(statusCode, headers, response);
                successPostCallBack(statusCode, headers, response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
                failurePostCallBack(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    public void invokeGetMethod(Context context, String uri, RequestParams requestParams) {
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
        asyncHttpClient.get(context, uri, requestParams, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                super.onSuccess(statusCode, headers, responseString);
                successGetCallBack(statusCode, headers, responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                failureGetCallBack(statusCode, headers, responseString, throwable);
            }
        });
    }
}
