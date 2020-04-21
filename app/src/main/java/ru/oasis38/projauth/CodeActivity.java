package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

//import static ru.oasis38.projauth.MainActivity.SAVED_SESSION;

public class CodeActivity extends AppCompatActivity {
    private TextView tvTimer;
    private Button btnRepeatSend;
    private Integer waiting;
    private String session;
    private EditText etCode;
    private String code;

    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        tvTimer = (TextView)findViewById(R.id.tvTimer);
        btnRepeatSend = (Button)findViewById(R.id.btnRepeatSend);
        btnRepeatSend.setEnabled(false);
        etCode = (EditText)findViewById(R.id.etCode);
//        Intent intent = getIntent();
//        waiting = intent.getIntExtra("waiting", 0);
        waiting = AuthActivity.waiting;
        session = AuthActivity.session;


        runTimer();
    }

    private void runTimer() {
        btnRepeatSend.setEnabled(false);
        Thread thread = new Thread() {
            @Override
            public void run() {
            while (waiting > 1) {
                try {
                    Thread.sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        waiting = waiting - 1;
                        tvTimer.setText(waiting.toString());
                        if(waiting == 0) {
                            btnRepeatSend.setEnabled(true);
                        }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            }
        };
        thread.start();
    }

    public void onClickSendCode(View view) {
        code = etCode.getText().toString();
        postValidCode();
    }

    public void onClickRepeatSend(View view) {
        postStartLogin();
    }

    public void onClickEditPhone(View view) { openAuthActivity(); }


    public void postStartLogin() {
        RequestQueue requestQueue = Volley.newRequestQueue(CodeActivity.this);
        String url = "http://10.1.1.227/t.masha/auth/?proj_api";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response!!!", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        session = jsonResponse.getString("session");
                        waiting =  jsonResponse.getInt("waiting");
                        runTimer();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error.Response!!!", error.toString());
            }
        }) {
            @Override
            public Map getParams() {
                Map params = new HashMap();
                params.put("q", "startLogin");
                params.put("ph", AuthActivity.phone);
                params.put("fio", AuthActivity.fio);
                params.put("guid", AuthActivity.guid);
                return params;
            }
        };
        Log.d("stringRequest!!!", stringRequest.toString());
        requestQueue.add(stringRequest);
    }

    public void postValidCode() {
        RequestQueue requestQueue = Volley.newRequestQueue(CodeActivity.this);
        String url = "http://10.1.1.227/t.masha/auth/?proj_api";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("ValidCode: Response!", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        saveSession(jsonResponse.getString("session"));
                        printMessage("Успешно");
                        openMainActivity();
                    } else {
                        String msg = jsonResponse.getString("message");
                        printMessage(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ValidCode: Er.Response!", error.toString());
                printMessage("Ошибка");
            }
        }) {
            @Override
            public Map getParams() {
                Map params = new HashMap();
                params.put("q", "validCode");
                params.put("code", code);
                params.put("session", session);
                return params;
            }
        };
        Log.d("ValidCode: strRequest!", stringRequest.toString());
        requestQueue.add(stringRequest);
    }

    private void saveSession(String session) {
        pref = getSharedPreferences("saved_data", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("saved_session", session);
        edit.apply();
    }

    private void printMessage(String msg) {
        Toast.makeText(CodeActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
    private void openAuthActivity(){
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

}
