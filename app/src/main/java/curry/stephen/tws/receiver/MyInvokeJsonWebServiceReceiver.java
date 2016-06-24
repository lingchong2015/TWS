package curry.stephen.tws.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import curry.stephen.tws.activity.MainActivity;
import curry.stephen.tws.util.ServerHelper;
import curry.stephen.tws.webService.JsonWebService;
import cz.msebera.android.httpclient.Header;

public class MyInvokeJsonWebServiceReceiver extends BroadcastReceiver {

    private Context mContext;
    private String mLastResponseString = "";
    private static int testCounter = 0;

    public static final String ACTION_INVOKE_WEB_SERVICE = "curry.stephen.tws.service.my_service.action_invoke_web_service";
    private static final String TAG = MyInvokeJsonWebServiceReceiver.class.getSimpleName();

    public MyInvokeJsonWebServiceReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "MyInvokeJsonWebServiceReceiver#onReceive()");

        mContext = context;
        if (intent.getAction().equals(ACTION_INVOKE_WEB_SERVICE)) {
//            invokeWebService();

           test1();
        }
    }
    private void test1() {
        String responseString1 = "[{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"100\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"002\",\"reflection_power\":\"200\",\"transmission_power\":\"130\"},{\"frequency\":\"99\",\"name\":\"003\",\"reflection_power\":\"130\",\"transmission_power\":\"100\"}]";
        String responseString2 = "[{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"99\",\"transmission_power\":\"100\"},{\"frequency\":\"100\",\"name\":\"002\",\"reflection_power\":\"200\",\"transmission_power\":\"130\"},{\"frequency\":\"99\",\"name\":\"003\",\"reflection_power\":\"130\",\"transmission_power\":\"100\"}]";

        Intent intentReceiver = new Intent();
        if ((testCounter % 3) == 0) {
            intentReceiver.setAction(MainActivity.ACTION_FRESH_INFO);
            intentReceiver.putExtra(MainActivity.EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, responseString1);
        } else if ((testCounter % 3) == 1) {
            intentReceiver.setAction(MainActivity.ACTION_FRESH_INFO);
            intentReceiver.putExtra(MainActivity.EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, responseString2);
        } else {
            intentReceiver.setAction(MainActivity.ACTION_FRESH_DATETIME);
        }
        ++testCounter;

        mContext.sendBroadcast(intentReceiver);
    }

    private void invokeWebService() {
        JsonWebService jsonWebService = new JsonWebService() {
            @Override
            public void successPostCallBack(int statusCode, Header[] headers, JSONObject jsonObject) {
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
            }

            @Override
            public void successGetCallBack(int statusCode, Header[] headers, String responseString) {
                Log.i(TAG, "Get data successfully.");

                Intent intentReceiver = new Intent();
                if (mLastResponseString.equals(responseString)) {
                    intentReceiver.setAction(MainActivity.ACTION_FRESH_DATETIME);
                } else {
                    mLastResponseString = responseString;
                    intentReceiver.setAction(MainActivity.ACTION_FRESH_INFO);
                    intentReceiver.putExtra(MainActivity.EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, responseString);
                }

                mContext.sendBroadcast(intentReceiver);
            }

            @Override
            public void failureGetCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "Get data failed.");

                if (statusCode == 404) {
                    Toast.makeText(mContext, "未找到请求资源,请查看网络连接地址是否设置正确.", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(mContext, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG).show();
                }
            }
        };

        jsonWebService.invokeGetMethod(mContext, ServerHelper.getTransmitterDynamicInformationURI(), new RequestParams());
    }
}
