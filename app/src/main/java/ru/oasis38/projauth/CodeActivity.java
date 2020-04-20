package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CodeActivity extends AppCompatActivity {
    private TextView tvTimer;
    private Button btnRepeatSend;
    private Integer waiting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);
        tvTimer = (TextView)findViewById(R.id.tvTimer);
        btnRepeatSend = (Button)findViewById(R.id.btnRepeatSend);
        btnRepeatSend.setEnabled(false);
        Intent intent = getIntent();
//        waiting = intent.getIntExtra("waiting", 0);
        waiting = 5;
        String session = intent.getStringExtra("session");
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

    }
}
