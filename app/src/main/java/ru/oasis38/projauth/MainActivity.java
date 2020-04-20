package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static String QR;
    private static final String SAVED_SESSION = "";
    private String session;
    public static String vError;
    private ZXingScannerView scannerView;
    private TextView txtResult;
    private Button btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scannerView = (ZXingScannerView)findViewById(R.id.zxscan);
        txtResult = (TextView)findViewById(R.id.txt_result);
        scannerStart();
}

    @Override
    protected void onDestroy() {
        scannerView.stopCamera();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();

        scannerStart();
    }

    public void scannerStart(){
        Dexter.withActivity(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        scannerView.setResultHandler(MainActivity.this);
                        scannerView.startCamera();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MainActivity.this, "You must accept this permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                })
                .check();
    }

    @Override
    public void handleResult(Result rawResult) {
        txtResult.setText(rawResult.getText());
        QR = rawResult.getText();
        btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setEnabled(true);
//        sendData();
//        if(vError == "com.android.volley.AuthFailureError" ) {
//            Intent intent = new Intent(this, AuthActivity.class);
//            startActivity(intent);
//        }

////        txtResult.setText(rawResult.getText());
//        if (checkAuth()) {
//            txtResult.setText("Авторизован");
//        } else {
//            txtResult.setText("Не авторизован");
////            Auth auth = new Auth();
//            QR = rawResult.getText();
//            Intent intent = new Intent(this, AuthActivity.class);
//            startActivity(intent);
//        }

    }

    public void onClickSend(View view) {
        sendData();
    }

    private void sendData() {
        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
        session = sPref.getString(SAVED_SESSION, "");


        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = "http://10.1.1.227/t.masha/auth/?proj_api";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("Response !!!", response);
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                txtResult.setText( error.toString());
                Log.d("error!!!", error.toString());
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse.statusCode == 401) {
                    openAuth();
                }
            }
        }) {
            @Override
            public Map getParams() {
                Map params = new HashMap();
                params.put("q","checkQr");
                params.put("qr", MainActivity.QR);
                params.put("session", session);

                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    private void openAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }


}
