package ru.oasis38.projauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EditPassActivity extends AppCompatActivity {
    private EditText etPass1, etPass2;
    private SharedPreferences pref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pass);

        etPass1 = (EditText)findViewById(R.id.etPass1);
        etPass2 = (EditText)findViewById(R.id.etPass2);
    }

    public void onClickSavePass(View view) {
        String pass1 = etPass1.getText().toString();
        String pass2 = etPass2.getText().toString();
        if(pass1.length() == 4 && pass2.length() == 4 && pass1.equals(pass2)) {
            printMessage("ok");
        } else if(!pass1.equals(pass2)) {
            printMessage("пароли не совпадают!");
            return;
        } else if (pass1.length() < 4) {
            printMessage("пароль должен состоять из 4 цифр!");
            return;
        }
        savePass(pass2);
        openPass();
    }

    private void savePass(String pass) {
        pref = getSharedPreferences("saved_data", MODE_PRIVATE);
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("saved_pass", pass);
        edit.apply();
    }

    private void openPass() {
        Intent intent = new Intent(this, PassActivity.class);
        startActivity(intent);
    }

    private void printMessage(String msg) {
        Toast.makeText(EditPassActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
