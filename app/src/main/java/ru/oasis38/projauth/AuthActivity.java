package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private Button btnSendPhone;
    private EditText etFIO, etPhone;
    private TextView tvFIO;

    public static String fio = "", phone = "", guid = "";

    public static String session = "";
    public static Integer waiting = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnSendPhone = (Button)findViewById(R.id.btnSendPhone);
        tvFIO = (TextView)findViewById(R.id.tvFIO);
        etFIO = (EditText)findViewById(R.id.etFIO);
        etPhone = (EditText)findViewById(R.id.etPhone);

        btnSendPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fio = etFIO.getText().toString();
                phone = etPhone.getText().toString();
                byte[] qr = Base64.decode(MainActivity.QR, Base64.DEFAULT);
                String strQR = new String(qr);
                try {
                    JSONObject jsonQR = new JSONObject(strQR);
                    guid = jsonQR.getString("guid");
                    postStartLogin();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postStartLogin() {
        Load.download(this);
        RequestQueue requestQueue = Volley.newRequestQueue(AuthActivity.this);
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
                        printMessage("Введите код авторизации");
                        openCodeActivity();
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
                Log.d("Error.Response!!!", error.toString());
            }
        }) {};
        Log.d("stringRequest!!!", stringRequest.toString());
        requestQueue.add(stringRequest);
    }

    private void openCodeActivity(){
        Intent intent = new Intent(this, CodeActivity.class);
        startActivity(intent);
    }

    private void printMessage(String msg) {
        Toast.makeText(AuthActivity.this, msg, Toast.LENGTH_SHORT).show();
    }

}
