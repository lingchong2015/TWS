package curry.stephen.tws.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.support.v7.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import curry.stephen.tws.R;
import curry.stephen.tws.constant.GlobalVariables;
import curry.stephen.tws.model.UserInfoModel;
import curry.stephen.tws.util.JsonHelper;
import curry.stephen.tws.util.MD5Helper;
import curry.stephen.tws.util.SearchAdapter;
import curry.stephen.tws.util.ServerHelper;
import curry.stephen.tws.util.SharedPreferencesHelper;
import curry.stephen.tws.webService.JsonWebService;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.ContentType;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    private AutoCompleteTextView mAutoCompleteTextViewUsername;
    private EditText mEditTextPassword;
    private View mProgressView;
    private View mTextViewNetworkDataProcessing;
    private View mViewLoginForm;
    private Set<String> mStringSetSuggestions = new HashSet<>();

    private static final String TAG = LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
    }

    private void initViews() {
        mAutoCompleteTextViewUsername = (AutoCompleteTextView) findViewById(R.id.username);
        populateAutoComplete();

        mEditTextPassword = (EditText) findViewById(R.id.password);
        mEditTextPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    login();
                    return true;
                }
                return false;
            }
        });

        Button buttonSignIn = (Button) findViewById(R.id.button_sign_in);
        buttonSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        Button buttonQuit = (Button) findViewById(R.id.button_quit);
        buttonQuit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        mProgressView = findViewById(R.id.login_progress);
        mViewLoginForm = findViewById(R.id.login_form);
        mTextViewNetworkDataProcessing = findViewById(R.id.text_view_network_data_processing);
    }

    private void populateAutoComplete() {
        Set<String> listUsername = loadUsername();
        if (!listUsername.isEmpty()) {
            addSuggestionsToAutoComplete(listUsername);
        }
    }

    private void addSuggestionsToAutoComplete(Set<String> suggestions) {
        mAutoCompleteTextViewUsername.setThreshold(1);
        SearchAdapter searchAdapter = new SearchAdapter<>(this, android.R.layout.simple_dropdown_item_1line,
                suggestions.toArray(), SearchAdapter.ALL);
        mAutoCompleteTextViewUsername.setAdapter(searchAdapter);
    }

    private void login() {
        mAutoCompleteTextViewUsername.setError(null);
        mEditTextPassword.setError(null);

        String username = mAutoCompleteTextViewUsername.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            mEditTextPassword.setError(getString(R.string.password_must_not_empty));
            focusView = mEditTextPassword;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mEditTextPassword.setError(getString(R.string.invalid_password));
            focusView = mEditTextPassword;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            mAutoCompleteTextViewUsername.setError(getString(R.string.username_must_not_empty));
            focusView = mAutoCompleteTextViewUsername;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            Log.i(TAG, getUserInfoJsonString());
            invokeWebService(ServerHelper.getLoginURI(), new StringEntity(getUserInfoJsonString(), ContentType.APPLICATION_JSON));
//            test1();
        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mViewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            mViewLoginForm.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mViewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            mTextViewNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mTextViewNetworkDataProcessing.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mTextViewNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mTextViewNetworkDataProcessing.setVisibility(show ? View.VISIBLE : View.GONE);
            mViewLoginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void invokeWebService(String uri, StringEntity stringEntity) {
        JsonWebService jsonWebService = new JsonWebService() {

            @Override
            public void successPostCallBack(int statusCode, Header[] headers, JSONObject jsonObject) {
                showProgress(false);

                try {
                    if (jsonObject.getString("result").equals("1")) {
                        Log.i(TAG, "Login successfully.");

                        saveUsername();

                        navigateToMainActivity();
                    } else {
                        Log.i(TAG, "Login failed, username or password may be wrong.");

                        SharedPreferencesHelper.putBoolean(LoginActivity.this, GlobalVariables.IS_LOGIN, false);
                        mAutoCompleteTextViewUsername.requestFocus();
                        Toast.makeText(LoginActivity.this, "用户名或密码错误!", Toast.LENGTH_LONG).show();
                        setResult(MainActivity.RESULT_CODE_FOR_LOGIN_ACTIVITY_FALSE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "解析服务器返回数据异常.", Toast.LENGTH_LONG).show();
                    setResult(MainActivity.RESULT_CODE_FOR_LOGIN_ACTIVITY_FALSE);
                }
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable,
                                            JSONObject jsonObject) {
                Log.i(TAG, "Network communication failed.");

                showProgress(false);

                setResult(MainActivity.RESULT_CODE_FOR_LOGIN_ACTIVITY_FALSE);

                if (statusCode == 404) {
                    Toast.makeText(LoginActivity.this, "未找到请求资源,请查看网络连接地址是否设置正确.", Toast.LENGTH_LONG).show();
                } else if (statusCode == 500) {
                    Toast.makeText(LoginActivity.this, "服务器出现错误.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "未处理异常抛出,连接终止.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void successPostCallBack(int statusCode, Header[] headers, JSONArray response) {
            }

            @Override
            public void failurePostCallBack(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
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

            }
        };

        showProgress(true);
        Log.i(TAG, uri);
        jsonWebService.invokePostMethod(this, uri, stringEntity);
    }

    private Set<String> loadUsername() {
        mStringSetSuggestions = SharedPreferencesHelper.getStringSet(this, GlobalVariables.AUTO_COMPLETE_CONTENT, new HashSet<String>());
        return mStringSetSuggestions;
    }

    private void saveUsername() {
        String username = mAutoCompleteTextViewUsername.getText().toString().trim();
        if (!mStringSetSuggestions.contains(username)) {
            mStringSetSuggestions.add(username);
        }
        SharedPreferencesHelper.putStringSet(this, GlobalVariables.AUTO_COMPLETE_CONTENT, mStringSetSuggestions);
    }

    private void navigateToMainActivity() {
        SharedPreferencesHelper.putBoolean(LoginActivity.this,
                GlobalVariables.IS_LOGIN, true);
        setResult(MainActivity.RESULT_CODE_FOR_LOGIN_ACTIVITY_TRUE);
        this.finish();
    }

    private String getUserInfoJsonString() {
        String username = mAutoCompleteTextViewUsername.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        UserInfoModel userInfoModel = new UserInfoModel();
        userInfoModel.setUsername(username);
        userInfoModel.setPassword(password);
        return JsonHelper.toJson(userInfoModel);
    }

    private void test1() {
        if (isUserInfoCorrect()) {
            saveUsername();
            navigateToMainActivity();
        } else {
            SharedPreferencesHelper.putBoolean(this, GlobalVariables.IS_LOGIN, false);
            Toast.makeText(this, "用户名或密码错误!", Toast.LENGTH_LONG).show();
            mAutoCompleteTextViewUsername.requestFocus();
        }
    }

    private boolean isUserInfoCorrect() {
        String username = mAutoCompleteTextViewUsername.getText().toString().trim();
        String password = mEditTextPassword.getText().toString().trim();

        if (password.equals("123")) {
            return true;
        } else {
            return false;
        }
    }
}

