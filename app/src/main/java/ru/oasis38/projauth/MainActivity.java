package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class MainActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    public static String QR;
    private String session;
    private ZXingScannerView scannerView;
    private Button btnSend;

    public static SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scannerView = (ZXingScannerView)findViewById(R.id.zxscan);
        scannerStart();
        init();
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
        QR = rawResult.getText();
        String strQR = "";
        try {
            byte[] qr = Base64.decode(MainActivity.QR, Base64.DEFAULT);
            strQR = new String(qr);
        } catch (Exception e) {
            printMessage("Некорректный QR-код!");
        }

        try {
            JSONObject jsonQR = new JSONObject(strQR);
            String guid = jsonQR.getString("guid");
            sendData();
        } catch (JSONException e) {
            printMessage("Некорректный QR-код!");
            scannerStart();
            e.printStackTrace();
        }
    }

    private void init() {
        pref = getSharedPreferences("saved_data", MODE_PRIVATE);
    }

    private void getSession() {
        session = pref.getString("saved_session", "");
    }

    private void sendData() {
        getSession();
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        String url = getResources().getString(R.string.app_url);
        url = url + "&q=checkQr&qr=" + MainActivity.QR + "&session=" + session.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("checkQr: Response!", response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    Boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                        printMessage("Успешно");
                        openPass();
                    } else {
                        String msg = jsonResponse.getString("message");
                        printMessage(msg);
                        scannerStart();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener(){
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("checkQr: error!", error.toString());
                NetworkResponse networkResponse = error.networkResponse;
                if(networkResponse != null && networkResponse.statusCode == 401) {
                    printMessage("Необходима авторизация");
                    openAuth();
                } else if(networkResponse != null && networkResponse.statusCode != 200) {
                    printMessage("Сервер недоступен  (Status code: " + networkResponse.statusCode + ")");
                    scannerStart();
                } else if(networkResponse == null ) {
                    printMessage("Ошибка URL");
                }
            }
        }) {};
        requestQueue.add(stringRequest);
    }

    private void openAuth() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
    }

    private void openPass() {
        Intent intent = new Intent(this, PassActivity.class);
        startActivity(intent);
    }

    private void printMessage(String msg) {
        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
