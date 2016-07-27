package curry.stephen.tws.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import curry.stephen.tws.R;
import curry.stephen.tws.adapter.MyRecyclerViewAdapter;
import curry.stephen.tws.constant.GlobalVariables;
import curry.stephen.tws.model.MyRecyclerViewModel;
import curry.stephen.tws.model.TransmitterDynamicInformationModel;
import curry.stephen.tws.model.TransmitterTotalInformationModel;
import curry.stephen.tws.service.MyInvokeJsonWebService;
import curry.stephen.tws.util.DateTimeHelper;
import curry.stephen.tws.util.DividerItemDecoration;
import curry.stephen.tws.util.JsonHelper;
import curry.stephen.tws.util.ServerHelper;
import curry.stephen.tws.util.SharedPreferencesHelper;
import curry.stephen.tws.webService.JsonWebService;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

public class MainActivity extends AppCompatActivity implements Handler.Callback,
        MyRecyclerViewAdapter.OnItemClickListener {

    // UI variables declaration.
    private RecyclerView mRecyclerView;
    private Toolbar mToolbar;
    private ProgressBar mProgressBarNetworkDataProcessing;
    private TextView mTextViewNetWorkDataProcessing;

    private String mTransmitterTotalInformationJsonString = "";// Transmitter dynamic information json string received from server which used to pop up dialog.
    private String mLastTransmitterDynamicInformationJsonString = "";// Last json string received from server which used to display on UI when network is disconnected.
    private List<TransmitterTotalInformationModel> mTransmitterTotalInformationModelList = new ArrayList<>();// Transmitter total information model list parsed by json string return from server at first time.
    private List<TransmitterDynamicInformationModel> mLastTransmitterDynamicInformationModelList = new ArrayList<>();// Last Transmitter dynamic information list parsed by json string return from server at last time.

    private long mExitTime = 0;// Time to control whether to exit.

    // Constant variables for communicating with login activity.
    public static final int REQUEST_CODE_FOR_LOGIN_ACTIVITY = 0;
    public static final int RESULT_CODE_FOR_LOGIN_ACTIVITY_TRUE = 1;
    public static final int RESULT_CODE_FOR_LOGIN_ACTIVITY_FALSE = 0;

    // Constant variables for communicating with mBroadcastReceiver.
    public static final String ACTION_FRESH_INFO = "curry.stephen.tws.activity.main_activity.fresh_info";
    public static final String ACTION_FRESH_DATETIME = "curry.stephen.tws.activity.main_activity.fresh_datetime";
    public static final String EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO = "extrasForReceiverTransmitterModelList";

    // Constant variables for alarm.
    private static final int ALARM_INTERVAL = 5500;
    public static final int MESSAGE_FOR_SEND_BROADCAST_TO_MY_INVOKE_JSON_WEB_SERVICE_RECEIVER = 1;

    private static final String TAG = MainActivity.class.getSimpleName();

    // Handler with main looper that handles message from mThreadAlarm, it is used to send broadcast to MyInvokeJsonWebServiceReceiver.
    private Handler mHandlerAlarm;
    // Timer for alarm.
    private Timer mTimerAlarm;

    // BroadcastReceiver for processing json string received from server and updating UI.
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_FRESH_INFO)) {
                String responseJsonString = intent.getStringExtra(EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO);
//                if (responseJsonString != null) {
//                    updateUIInfo(responseJsonString);
//                } else {
//                    updateUIDatetime();
//                }

                updateUIInfo(responseJsonString);
            }
        }
    };

    // TimerTask as a alarm used to send message repeatedly or once to handlerMessage() method of current MainActivity instance(known as this).
    private TimerTask mTimerTaskAlarm = new TimerTask() {
        @Override
        public void run() {
            Log.i(TAG, String.format("Thread ID of MainActivity#mTimerTaskAlarm.run():%d.", Thread.currentThread().getId()));
            Message message = new Message();
            message.what = MESSAGE_FOR_SEND_BROADCAST_TO_MY_INVOKE_JSON_WEB_SERVICE_RECEIVER;
            mHandlerAlarm.sendMessage(message);
        }
    };

    private void updateUIInfo(String responseJsonString) {
        mLastTransmitterDynamicInformationJsonString = responseJsonString;

        List<TransmitterDynamicInformationModel> transmitterModelList = JsonHelper.<List<TransmitterDynamicInformationModel>>fromJson(responseJsonString,
                new TypeToken<List<TransmitterDynamicInformationModel>>() {
                }.getType());
        List<MyRecyclerViewModel> myRecyclerViewModelList =
                transmitterDynamicInformationModelListToMyRecyclerViewModelList(transmitterModelList);
        mLastTransmitterDynamicInformationModelList = transmitterModelList;

        ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).setMyRecyclerViewModelList(myRecyclerViewModelList);
        mRecyclerView.getAdapter().notifyDataSetChanged();

        mToolbar.setSubtitle(String.format("更新时间: %s", DateTimeHelper.getDateTimeNow()));
        mToolbar.setSubtitleTextAppearance(this, R.style.Theme_ToolBar_Base_Subtitle);
    }

    private void updateUIDatetime() {
        MyRecyclerViewAdapter myCurrentRecyclerViewAdapter =
                ((MyRecyclerViewAdapter) mRecyclerView.getAdapter());

        String info = myCurrentRecyclerViewAdapter.getMyRecyclerViewModelList().get(0)
                .getInfo();
        int trimIndex = info.indexOf(':');
        info = info.substring(0, trimIndex);
        info += DateTimeHelper.getDateTimeNow();

        myCurrentRecyclerViewAdapter.getMyRecyclerViewModelList().get(0).setInfo(info);
        myCurrentRecyclerViewAdapter.notifyDataSetChanged();
    }

    private List<MyRecyclerViewModel> transmitterDynamicInformationModelListToMyRecyclerViewModelList(
            List<TransmitterDynamicInformationModel> transmitterDynamicInformationModelList) {
        List<MyRecyclerViewModel> myRecyclerViewModelList = new ArrayList<>();

        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapNormal = new HashMap<>();
        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapTrouble = new HashMap<>();
        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapStop = new HashMap<>();

        labeledTransmitterDynamicInformationModelTag(transmitterDynamicInformationModelList);

        for (TransmitterDynamicInformationModel transmitterDynamicInformationModel : transmitterDynamicInformationModelList) {
            MyRecyclerViewModel myRecyclerViewModel = new MyRecyclerViewModel();

            myRecyclerViewModel.setUUID(transmitterDynamicInformationModel.getUUID());

            myRecyclerViewModel.setTransmitterName(String.format(getString(R.string.
                    transmitter_name_formatter), transmitterDynamicInformationModel.getName()));

            if (getTransmitterStatus(transmitterDynamicInformationModel) == 0) {
                myRecyclerViewModel.setInfo(String.format(getString(
                        R.string.transmitter_main_parameters_info_formatter),
                        transmitterDynamicInformationModel.getFrequency(), transmitterDynamicInformationModel.getTransmission_power(),
                        transmitterDynamicInformationModel.getReflection_power()));
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball_gray));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball));
                myRecyclerViewModelMapNormal.put(transmitterDynamicInformationModel.getId(), myRecyclerViewModel);
            } else if (getTransmitterStatus(transmitterDynamicInformationModel) == 1) {
                myRecyclerViewModel.setInfo(String.format(getString(
                        R.string.transmitter_main_parameters_info_formatter),
                        transmitterDynamicInformationModel.getFrequency(), transmitterDynamicInformationModel.getTransmission_power(),
                        transmitterDynamicInformationModel.getReflection_power()));
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball_gray));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball));
                myRecyclerViewModelMapTrouble.put(transmitterDynamicInformationModel.getId(), myRecyclerViewModel);
            } else {
                myRecyclerViewModel.setInfo(transmitterDynamicInformationModel.getTransmission_power());
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball_gray));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball_gray));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball_gray));
                myRecyclerViewModelMapStop.put(transmitterDynamicInformationModel.getId(), myRecyclerViewModel);
            }
        }

        Integer[] normalKeys = myRecyclerViewModelMapNormal.keySet().toArray(new Integer[myRecyclerViewModelMapNormal.size()]);
        Integer[] troubleKeys = myRecyclerViewModelMapTrouble.keySet().toArray(new Integer[myRecyclerViewModelMapTrouble.size()]);
        Integer[] stopKeys = myRecyclerViewModelMapStop.keySet().toArray(new Integer[myRecyclerViewModelMapStop.size()]);

        Arrays.sort(normalKeys);
        Arrays.sort(troubleKeys);
        Arrays.sort(stopKeys);

        for (Integer key : troubleKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapTrouble.get(key));
        }

        for (Integer key : normalKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapNormal.get(key));
        }

        for (Integer key : stopKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapStop.get(key));
        }

        return myRecyclerViewModelList;
    }

    private List<MyRecyclerViewModel> transmitterTotalInformationModelListToMyRecyclerViewModelList(
            List<TransmitterTotalInformationModel> transmitterTotalInformationModelList) {
        List<MyRecyclerViewModel> myRecyclerViewModelList = new ArrayList<>();

        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapNormal = new HashMap<>();
        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapTrouble = new HashMap<>();
        Map<Integer, MyRecyclerViewModel> myRecyclerViewModelMapStop = new HashMap<>();

        labeledTransmitterTotalInformationModelTag(transmitterTotalInformationModelList);

        for (TransmitterTotalInformationModel transmitterTotalInformationModel : transmitterTotalInformationModelList) {
            MyRecyclerViewModel myRecyclerViewModel = new MyRecyclerViewModel();

            myRecyclerViewModel.setUUID(transmitterTotalInformationModel.getUUID());

            myRecyclerViewModel.setTransmitterName(String.format(getString(R.string.
                    transmitter_name_formatter), transmitterTotalInformationModel.getName()));

            if (getTransmitterStatus(transmitterTotalInformationModel) == 0) {
                myRecyclerViewModel.setInfo(String.format(getString(
                        R.string.transmitter_main_parameters_info_formatter),
                        transmitterTotalInformationModel.getFrequency(), transmitterTotalInformationModel.getTransmission_power(),
                        transmitterTotalInformationModel.getReflection_power()));
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball_gray));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball));
                myRecyclerViewModelMapNormal.put(transmitterTotalInformationModel.getId(), myRecyclerViewModel);
            } else if (getTransmitterStatus(transmitterTotalInformationModel) == 1) {
                myRecyclerViewModel.setInfo(String.format(getString(
                        R.string.transmitter_main_parameters_info_formatter),
                        transmitterTotalInformationModel.getFrequency(), transmitterTotalInformationModel.getTransmission_power(),
                        transmitterTotalInformationModel.getReflection_power()));
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball_gray));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball));
                myRecyclerViewModelMapTrouble.put(transmitterTotalInformationModel.getId(), myRecyclerViewModel);
            } else {
                myRecyclerViewModel.setInfo(transmitterTotalInformationModel.getTransmission_power());
                myRecyclerViewModel.setDrawableStatusGreen(getResources().getDrawable(R.drawable.green_ball_gray));
                myRecyclerViewModel.setDrawableStatusRed(getResources().getDrawable(R.drawable.red_ball_gray));
                myRecyclerViewModel.setDrawableStatusWhite(getResources().getDrawable(R.drawable.blue_ball_gray));
                myRecyclerViewModelMapStop.put(transmitterTotalInformationModel.getId(), myRecyclerViewModel);
            }
        }

        Integer[] normalKeys = myRecyclerViewModelMapNormal.keySet().toArray(new Integer[myRecyclerViewModelMapNormal.size()]);
        Integer[] troubleKeys = myRecyclerViewModelMapTrouble.keySet().toArray(new Integer[myRecyclerViewModelMapTrouble.size()]);
        Integer[] stopKeys = myRecyclerViewModelMapStop.keySet().toArray(new Integer[myRecyclerViewModelMapStop.size()]);

        Arrays.sort(normalKeys);
        Arrays.sort(troubleKeys);
        Arrays.sort(stopKeys);

        for (Integer key : troubleKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapTrouble.get(key));
        }

        for (Integer key : normalKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapNormal.get(key));
        }

        for (Integer key : stopKeys) {
            myRecyclerViewModelList.add(myRecyclerViewModelMapStop.get(key));
        }

        return myRecyclerViewModelList;
    }

    private void labeledTransmitterTotalInformationModelTag(
            List<TransmitterTotalInformationModel> transmitterTotalInformationModelList) {
        for (TransmitterTotalInformationModel transmitterTotalInformationModel :
                transmitterTotalInformationModelList) {
            transmitterTotalInformationModel.setUUID(UUID.randomUUID());
        }
    }

    private void labeledTransmitterDynamicInformationModelTag(
            List<TransmitterDynamicInformationModel> transmitterDynamicInformationModelList) {
        for (TransmitterDynamicInformationModel transmitterDynamicInformationModel :
                transmitterDynamicInformationModelList) {
            transmitterDynamicInformationModel.setUUID(UUID.randomUUID());
        }
    }

    /**
     * 1: 发射机正常.<br/>
     * 2: 发射机故障.<br/>
     * 3: 发射机停止.
     */
    private int getTransmitterStatus(TransmitterDynamicInformationModel transmitterDynamicInformationModel) {
        switch (transmitterDynamicInformationModel.getStatus()) {
            case "1":
                return 0;
            case "2":
                return 1;
            case "3":
                return 2;
            default:
                return 1;
        }
    }

    /**
     * 1: 发射机正常.<br/>
     * 2: 发射机故障.<br/>
     * 3: 发射机停止.
     */
    private int getTransmitterStatus(TransmitterTotalInformationModel transmitterTotalInformationModel) {
        switch (transmitterTotalInformationModel.getStatus()) {
            case "1":
                return 0;
            case "2":
                return 1;
            case "3":
                return 2;
            default:
                return 1;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!hasLogin()) {
            startLoginActivity();
        } else {
            initView();
            initVariables();
            initReceiver();
            initRecyclerView();
        }
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showParameterInfo();
            }
        });

        mProgressBarNetworkDataProcessing = (ProgressBar) findViewById(
                R.id.progress_bar_network_data_processing);
        mTextViewNetWorkDataProcessing = (TextView) findViewById(
                R.id.text_view_network_data_processing);
    }

    private void initVariables() {
        mHandlerAlarm = new Handler(getMainLooper(), this);
        mLastTransmitterDynamicInformationJsonString = SharedPreferencesHelper.getString(this,
                GlobalVariables.LAST_TRANSMITTER_DYNAMIC_INFORMATION, "");
        mTransmitterTotalInformationJsonString = SharedPreferencesHelper.getString(this,
                GlobalVariables.TRANSMITTER_TOTAL_INFORMATION, "");
    }

    @Override
    public boolean handleMessage(Message message) {
        Log.i(TAG, String.format("Thread ID of MainActivity#handleMessage():%d.", Thread.currentThread().getId()));

        if (message.what == MESSAGE_FOR_SEND_BROADCAST_TO_MY_INVOKE_JSON_WEB_SERVICE_RECEIVER) {
            Intent intent = new Intent(this, MyInvokeJsonWebService.class);
            intent.setAction(MyInvokeJsonWebService.ACTION_INVOKE_WEB_SERVICE);
            startService(intent);
        }

        return true;
    }

    private boolean hasLogin() {
        return SharedPreferencesHelper.getBoolean(this, GlobalVariables.IS_LOGIN, false);
    }

    private void startLoginActivity() {
        startActivityForResult(new Intent(this, LoginActivity.class),
                REQUEST_CODE_FOR_LOGIN_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_FOR_LOGIN_ACTIVITY:
                requestCodeForLoginActivityHandler(resultCode);
                break;
            default:
                break;
        }
    }

    private void requestCodeForLoginActivityHandler(int resultCode) {
        estimateLoginResult(resultCode);
    }

    private void estimateLoginResult(int resultCode) {
        if (resultCode == 1) {
            initView();
            initVariables();
            initReceiver();
            initRecyclerView();
            Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show();
        } else {
            MainActivity.this.finish();
        }
    }

    private void showParameterInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("参数指标");

        builder.setItems(getItemContent(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private String[] getItemContent() {
        return new String[]{"绿色：发射机工作处于正常状态", "红色：发射机工作处于故障状态", "白色: 发射机工作处于暂停或关闭状态"};
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FRESH_INFO);
        registerReceiver(mBroadcastReceiver, intentFilter);
    }

    private void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL_LIST));

        invokeWebServiceFirstTime();
//        test1();
    }

    @Override
    public void onItemClick(View view, int position) {

        String uuid = ((MyRecyclerViewAdapter) mRecyclerView.getAdapter()).getMyRecyclerViewModelList().get(position).getUUID().toString();

        TransmitterDynamicInformationModel transmitterDynamicInformationModelSelected = null;

        for (TransmitterDynamicInformationModel transmitterDynamicInformationModel : mLastTransmitterDynamicInformationModelList) {
            if (transmitterDynamicInformationModel.getUUID().toString().equals(uuid)) {
                transmitterDynamicInformationModelSelected = transmitterDynamicInformationModel;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("发射机信息");

        builder.setItems(transmitterDynamicInformationModelSelected.getItemContent(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    private void test1() {
        String responseString = "[{\"frequency\":\"100\",\"name\":\"001\",\"reflection_power\":\"100\",\"transmission_power\":\"数据暂无更新，请检查连接是否正常\",\"status\":\"2\",\"info\":\"111\",\"number\":\"2\"},{\"frequency\":\"100\",\"name\":\"002\",\"reflection_power\":\"200\",\"transmission_power\":\"发射机处于关闭状态\",\"status\":\"2\",\"info\":\"111\",\"number\":\"3\"},{\"frequency\":\"99\",\"name\":\"003\",\"reflection_power\":\"130\",\"transmission_power\":\"100\",\"status\":\"0\",\"info\":\"111\",\"number\":\"2\"}]";

        Log.i(TAG, responseString);

        mTransmitterTotalInformationJsonString = responseString;
        List<TransmitterTotalInformationModel> transmitterTotalInformationModelList = JsonHelper.
                <List<TransmitterTotalInformationModel>>fromJson(responseString, new
                        TypeToken<List<TransmitterTotalInformationModel>>() {
                        }.getType());
        mTransmitterTotalInformationModelList = transmitterTotalInformationModelList;
        List<MyRecyclerViewModel> myRecyclerViewModelList =
                transmitterTotalInformationModelListToMyRecyclerViewModelList(transmitterTotalInformationModelList);

        MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(
                MainActivity.this, myRecyclerViewModelList);
        myRecyclerViewAdapter.setOnItemClickListener(MainActivity.this);
        mRecyclerView.setAdapter(myRecyclerViewAdapter);

        mToolbar.setSubtitle(String.format("更新时间: %s", DateTimeHelper.getDateTimeNow()));
        mToolbar.setSubtitleTextAppearance(this, R.style.Theme_ToolBar_Base_Subtitle);

        startAlarm();
    }

    private void invokeWebServiceFirstTime() {
        JsonWebService jsonWebService = new JsonWebService() {
            @Override
            public void successPostCallBack(int statusCode, Header[] headers, JSONObject jsonObject) {
                Log.i(TAG, "Get data successfully.");

                showProgress(false);
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONObject jsonObject) {
                Log.i(TAG, "Get data failed.");

                showProgress(false);

                String lastTransmitterDynamicInformationJsonString = mLastTransmitterDynamicInformationJsonString;
                String transmitterTotalInformationJsonString = mTransmitterTotalInformationJsonString;
                mTransmitterTotalInformationModelList = JsonHelper.
                        <List<TransmitterTotalInformationModel>>fromJson(transmitterTotalInformationJsonString, new
                                TypeToken<List<TransmitterTotalInformationModel>>() {
                                }.getType());

                if (!lastTransmitterDynamicInformationJsonString.equals("")) {
                    Intent intentReceiver = new Intent();
                    intentReceiver.setAction(ACTION_FRESH_INFO);
                    intentReceiver.putExtra(EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, lastTransmitterDynamicInformationJsonString);
                    MainActivity.this.sendBroadcast(intentReceiver);
                }

                if (statusCode == 404) {
                    Toast.makeText(MainActivity.this, "未找到请求资源,请查看网络连接地址是否设置正确.",
                            Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(MainActivity.this, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG)
                            .show();
                }
                showProgress(false);
            }

            @Override
            public void successPostCallBack(int statusCode, Header[] headers, JSONArray response) {
                Log.i(TAG, "Get data successfully.");

                showProgress(false);

                String responseString = response.toString();

                mTransmitterTotalInformationJsonString = responseString;
                List<TransmitterTotalInformationModel> transmitterTotalInformationModelList = JsonHelper.
                        <List<TransmitterTotalInformationModel>>fromJson(responseString, new
                                TypeToken<List<TransmitterTotalInformationModel>>() {
                                }.getType());
                mTransmitterTotalInformationModelList = transmitterTotalInformationModelList;

                mLastTransmitterDynamicInformationJsonString = responseString;
                List<TransmitterDynamicInformationModel> transmitterDynamicInformationModelList = JsonHelper.
                        <List<TransmitterDynamicInformationModel>>fromJson(responseString, new
                                TypeToken<List<TransmitterDynamicInformationModel>>() {
                                }.getType());

//                List<MyRecyclerViewModel> myRecyclerViewModelList =
//                        transmitterTotalInformationModelListToMyRecyclerViewModelList(transmitterTotalInformationModelList);
                List<MyRecyclerViewModel> myRecyclerViewModelList =
                        transmitterDynamicInformationModelListToMyRecyclerViewModelList(
                                transmitterDynamicInformationModelList
                        );
                mLastTransmitterDynamicInformationModelList = transmitterDynamicInformationModelList;

                MyRecyclerViewAdapter myRecyclerViewAdapter = new MyRecyclerViewAdapter(
                        MainActivity.this, myRecyclerViewModelList);
                myRecyclerViewAdapter.setOnItemClickListener(MainActivity.this);
                mRecyclerView.setAdapter(myRecyclerViewAdapter);

                mToolbar.setSubtitle(String.format("更新时间: %s", DateTimeHelper.getDateTimeNow()));
                mToolbar.setSubtitleTextAppearance(MainActivity.this, R.style.Theme_ToolBar_Base_Subtitle);

                startAlarm();
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
                Log.i(TAG, "Get data failed.");

                showProgress(false);

                String lastTransmitterDynamicInformationJsonString = mLastTransmitterDynamicInformationJsonString;
                String transmitterTotalInformationJsonString = mTransmitterTotalInformationJsonString;
                mTransmitterTotalInformationModelList = JsonHelper.
                        <List<TransmitterTotalInformationModel>>fromJson(transmitterTotalInformationJsonString, new
                                TypeToken<List<TransmitterTotalInformationModel>>() {
                                }.getType());

                if (!lastTransmitterDynamicInformationJsonString.equals("")) {
                    Intent intentReceiver = new Intent();
                    intentReceiver.setAction(ACTION_FRESH_INFO);
                    intentReceiver.putExtra(EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, lastTransmitterDynamicInformationJsonString);
                    MainActivity.this.sendBroadcast(intentReceiver);
                }

                if (statusCode == 404) {
                    Toast.makeText(MainActivity.this, "未找到请求资源,请查看网络连接地址是否设置正确.",
                            Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(MainActivity.this, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG)
                            .show();
                }
            }

            @Override
            public void successGetCallBack(int statusCode, Header[] headers, String responseString) {
            }

            @Override
            public void failureGetCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            }

            @Override
            public void successPostCallBack(int statusCode, Header[] headers, String responseString) {
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.i(TAG, "Get data failed.");

                showProgress(false);

                String lastTransmitterDynamicInformationJsonString = mLastTransmitterDynamicInformationJsonString;
                String transmitterTotalInformationJsonString = mTransmitterTotalInformationJsonString;
                mTransmitterTotalInformationModelList = JsonHelper.
                        <List<TransmitterTotalInformationModel>>fromJson(transmitterTotalInformationJsonString, new
                                TypeToken<List<TransmitterTotalInformationModel>>() {
                                }.getType());

                if (!lastTransmitterDynamicInformationJsonString.equals("")) {
                    Intent intentReceiver = new Intent();
                    intentReceiver.setAction(ACTION_FRESH_INFO);
                    intentReceiver.putExtra(EXTRAS_FOR_RECEIVER_TRANSMITTER_INFO, lastTransmitterDynamicInformationJsonString);
                    MainActivity.this.sendBroadcast(intentReceiver);
                }

                if (statusCode == 404) {
                    Toast.makeText(MainActivity.this, "未找到请求资源,请查看网络连接地址是否设置正确.",
                            Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(MainActivity.this, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG)
                            .show();
                }
            }
        };

        showProgress(true);

        jsonWebService.invokePostMethod(this, ServerHelper.getTransmitterTotalInformationURI(), new StringEntity("null",
                ContentType.APPLICATION_JSON));
    }

    private void startAlarm() {
        mTimerAlarm = new Timer(true);
        mTimerAlarm.schedule(mTimerTaskAlarm, ALARM_INTERVAL, ALARM_INTERVAL);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            if ((System.currentTimeMillis() - mExitTime) > 2000) {
//                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//                mExitTime = System.currentTimeMillis();
//            } else {
//                SharedPreferencesHelper.putBoolean(MainActivity.this, GlobalVariables.IS_LOGIN, false);
//                finish();
//            }
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception.
            } else {
                throw e;
            }
        }
        mTimerTaskAlarm.cancel();
        stopService(new Intent(this, MyInvokeJsonWebService.class));
        SharedPreferencesHelper.putString(this, GlobalVariables.LAST_TRANSMITTER_DYNAMIC_INFORMATION,
                mLastTransmitterDynamicInformationJsonString);
        SharedPreferencesHelper.putString(this, GlobalVariables.TRANSMITTER_TOTAL_INFORMATION,
                mTransmitterTotalInformationJsonString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_exit:
                menuActionExitHandler();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void menuActionExitHandler() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("询  问");

        builder.setItems(new String[]{"是否确定退出?"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferencesHelper.putBoolean(MainActivity.this, GlobalVariables.IS_LOGIN, false);
                MainActivity.this.finish();
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.create().show();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            mRecyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressBarNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressBarNetworkDataProcessing.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressBarNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mTextViewNetWorkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mTextViewNetWorkDataProcessing.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTextViewNetWorkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressBarNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mTextViewNetWorkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mRecyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
