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
    private TextView tvFIO, tvResult;

    protected String fio = "";
    protected String phone = "";
    protected String guid = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        btnSendPhone = (Button)findViewById(R.id.btnSendPhone);
        tvFIO = (TextView)findViewById(R.id.tvFIO);
        tvResult = (TextView)findViewById(R.id.tvResult);

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
                    postSendData();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void postSendData() {
        RequestQueue requestQueue = Volley.newRequestQueue(AuthActivity.this);
        String url = "http://10.1.1.227/t.masha/auth/?proj_api";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response!!!", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        openCodeActivity(jsonResponse.getString("session"), jsonResponse.getInt("waiting"));
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
                params.put("ph", phone);
                params.put("fio", fio);
                params.put("guid", guid);
                return params;
            }
        };
        Log.d("stringRequest!!!", stringRequest.toString());
        requestQueue.add(stringRequest);
    }

    private void openCodeActivity(String session, Integer waiting){

        Intent intent = new Intent(this, CodeActivity.class);
        intent.putExtra("session", session);
        intent.putExtra("waiting", waiting);
        startActivity(intent);

    }

}
