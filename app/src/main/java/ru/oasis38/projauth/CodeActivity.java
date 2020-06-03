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

public class CodeActivity extends AppCompatActivity {
    private TextView tvTimer, tvPhone;
    private Button btnRepeatSend;
    private Integer waiting;
    private String session;
    private EditText etCode;
    private String code;
    private String phone;


    private SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        tvTimer = (TextView)findViewById(R.id.tvTimer);
        btnRepeatSend = (Button)findViewById(R.id.btnRepeatSend);
        btnRepeatSend.setEnabled(false);
        etCode = (EditText)findViewById(R.id.etCode);
        tvPhone = (TextView)findViewById(R.id.tvPhone);


        waiting = AuthActivity.waiting;
        session = AuthActivity.session;
        phone = AuthActivity.phone;

        tvPhone.setText(phone);

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
                        Integer min = waiting/60;
                        Integer sec = waiting - min * 60;
                        String time = min.toString() + ':' + sec.toString();
                        tvTimer.setText(time);
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
        Load.download(this);
        RequestQueue requestQueue = Volley.newRequestQueue(CodeActivity.this);
        String url = getResources().getString(R.string.app_url);
        url = url + "&q=startLogin&ph=" + AuthActivity.phone.toString() + "&fio=" + AuthActivity.fio.toString() + "&guid=" + AuthActivity.guid.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Load.progress.hide();
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
                Load.progress.hide();
                Log.d("Error.Response!!!", error.toString());
            }
        }) {};
        Log.d("stringRequest!!!", stringRequest.toString());
        requestQueue.add(stringRequest);
    }

    public void postValidCode() {
        Load.download(this);
        RequestQueue requestQueue = Volley.newRequestQueue(CodeActivity.this);
        String url = getResources().getString(R.string.app_url);
        url = url + "&q=validCode&code=" + code.toString() +"&session=" + session.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Load.progress.hide();
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
                Load.progress.hide();
                Log.d("ValidCode: Er.Response!", error.toString());
                printMessage("Ошибка");
            }
        }) {};
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
