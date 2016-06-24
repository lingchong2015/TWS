package curry.stephen.tws.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import curry.stephen.tws.activity.MainActivity;
import curry.stephen.tws.util.ServerHelper;
import curry.stephen.tws.webService.JsonWebService;
import cz.msebera.android.httpclient.Header;

public class MyInvokeJsonWebService extends Service implements Handler.Callback {

    private String mLastResponseString = "";
    private Handler mHandler;

    private static int testCounter = 0;

    public static final int MESSAGE_FOR_HANDLER = 1;
    public static final String ACTION_INVOKE_WEB_SERVICE = "curry.stephen.tws.service.my_service.action_invoke_web_service";
    private static final String TAG = MyInvokeJsonWebService.class.getSimpleName();

    public MyInvokeJsonWebService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "MyInvokeJsonWebService#MyInvokeJsonWebService()");
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper(), this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, String.format("Thread ID of MyInvokeJsonWebService#onStartCommand():%d", Thread.currentThread().getId()));

        if (intent.getAction().equals(ACTION_INVOKE_WEB_SERVICE)) {
            Message message = new Message();
            message.what = MESSAGE_FOR_HANDLER;
            mHandler.sendMessage(message);
        }

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
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

        sendBroadcast(intentReceiver);
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

                sendBroadcast(intentReceiver);
            }

            @Override
            public void failureGetCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "Get data failed.");

                if (statusCode == 404) {
                    Toast.makeText(MyInvokeJsonWebService.this, "未找到请求资源,请查看网络连接地址是否设置正确.", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(MyInvokeJsonWebService.this, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MyInvokeJsonWebService.this, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG).show();
                }
            }
        };

        jsonWebService.invokeGetMethod(MyInvokeJsonWebService.this, ServerHelper.getTransmitterDynamicInformationURI(), new RequestParams());
    }

    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MESSAGE_FOR_HANDLER:
                Log.i(TAG, String.format("Thread ID of MyInvokeJsonWebServiceReceiver#handlerMessage():%d", Thread.currentThread()
                        .getId()));
//                invokeWebService();
                test1();
                break;
        }

        return true;
    }
}
