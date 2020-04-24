package ru.oasis38.projauth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

@RequiresApi(api = Build.VERSION_CODES.M)
@SuppressLint("NewApi")
public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {
    private CancellationSignal cancellationSignal;
    private Context appContext;

    public FingerprintHandler(Context context) {
        appContext = context;
    }
    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError (int errMsgId, CharSequence errString) {
        printMessage("Ошибка аутентификации \n" + errString);
    }
    @Override
    public void onAuthenticationHelp (int helpMsgId, CharSequence helpString) {
        printMessage("Справка по аутентификации \n" + helpString);
    }

    @Override
    public void onAuthenticationFailed () {
        printMessage("Ошибка аутентификации.");
    }

    @Override
    public void onAuthenticationSucceeded (FingerprintManager.AuthenticationResult result) {
        printMessage("Аутентификация прошла успешно.");

        Intent intent = new Intent(appContext, MainActivity.class);
        appContext.startActivity(intent);
    }

    private void printMessage(String msg) {
        Toast.makeText(appContext, msg, Toast.LENGTH_SHORT).show();
    }
}

