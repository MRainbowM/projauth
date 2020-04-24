package ru.oasis38.projauth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

@RequiresApi(api = Build.VERSION_CODES.M)
public class PassActivity extends AppCompatActivity {
    private static final String KEY_NAME = "example_key";
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    private TextView tvFingerprintStatus;

    public static SharedPreferences pref;

    private String codePass = "";

    private ImageView ivCircle1, ivCircle2, ivCircle3, ivCircle4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        init();
        tvFingerprintStatus = (TextView) findViewById(R.id.tvFingerprintStatus);
        ivCircle1 = (ImageView)findViewById(R.id.ivCircle1);
        ivCircle2 = (ImageView)findViewById(R.id.ivCircle2);
        ivCircle3 = (ImageView)findViewById(R.id.ivCircle3);
        ivCircle4 = (ImageView)findViewById(R.id.ivCircle4);
        colorCircle();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tvFingerprintStatus.setText("Нет сканера, введите код");
        } else {
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (!keyguardManager.isKeyguardSecure()) {
                printMessage("Защита экрана не включена в настройках");
                tvFingerprintStatus.setText("Защита экрана не включена в настройках, введите код");
                return;
            }
            if (ActivityCompat.checkSelfPermission (this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                printMessage("Разрешение аутентификации по отпечатку пальца не включено");
                tvFingerprintStatus.setText("Разрешение аутентификации по отпечатку пальца не включено, введите код");
                return;
            }
            if (! fingerprintManager.hasEnrolledFingerprints ()) {
                printMessage("Необходимо зарегистрировать хотя бы один отпечаток в настройках");
                tvFingerprintStatus.setText("Нет зарегистированных отпечатков, введите код");
                return;
            }
            generateKey ();

            if (cipherInit ()) {
                cryptoObject = new FingerprintManager.CryptoObject (cipher);
                FingerprintHandler helper = new FingerprintHandler (this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }
    }

    private void init() {
        pref = getSharedPreferences("saved_data", MODE_PRIVATE);
        String pass = pref.getString("saved_pass", "no_pass");
        if (pass.equals("no_pass")) {
            openEditPass();
            printMessage("Необходимо задать пароль");
        }
    }

    private void openEditPass() {
        Intent intent = new Intent(this, EditPassActivity.class);
        startActivity(intent);
    }

    public void onClick1(View view) {
        setValueForPass(1);
    }
    public void onClick2(View view) {
        setValueForPass(2);
    }
    public void onClick3(View view) {
        setValueForPass(3);
    }
    public void onClick4(View view) {
        setValueForPass(4);
    }
    public void onClick5(View view) {
        setValueForPass(5);
    }
    public void onClick6(View view) {
        setValueForPass(6);
    }
    public void onClick7(View view) {
        setValueForPass(7);
    }
    public void onClick8(View view) {
        setValueForPass(8);
    }
    public void onClick9(View view) {
        setValueForPass(9);
    }
    public void onClick0(View view) {
        setValueForPass(0);
    }
    public void onClickDelete(View view) {
        setValueForPass(-1);
    }

    private void setValueForPass(Integer value) {
        if(value > -1) {
            if(codePass.length() < 3) {
                codePass = codePass + value;
            } else if(codePass.length() == 3) {
                codePass = codePass + value;
                colorCircle();
                CheckPass();
            }
        } else {
            if(codePass.length() > 0) {
                codePass = codePass.substring(0, codePass.length() - 1);
            }
        }
        colorCircle();
    }

    private void colorCircle() {
        if(codePass.length() == 0) {
            ivCircle1.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle2.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle3.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle4.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        } else if(codePass.length() == 1) {
            ivCircle1.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle2.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle3.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle4.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        } else if(codePass.length() == 2) {
            ivCircle1.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle2.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle3.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
            ivCircle4.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        } else if(codePass.length() == 3) {
            ivCircle1.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle2.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle3.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle4.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorGray)));
        } else if(codePass.length() == 4) {
            ivCircle1.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle2.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle3.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            ivCircle4.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }

    private void CheckPass() {
        String pass = pref.getString("saved_pass", "no_pass");
        if (pass.equals(codePass)) {
            openMain();
        } else {
            printMessage("Неверный пароль!");
            codePass = "";
        }
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @SuppressLint("NewApi")
    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Не удалось получить экземпляр KeyGenerator", e);
        }
        try {
            keyStore.load(null);
            keyGenerator.init(new KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT |  KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Не удалось получить шифр", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Не удалось инициализировать шифр", e);
        }
    }

    private void printMessage(String msg) {
        Toast.makeText(PassActivity.this, msg, Toast.LENGTH_SHORT).show();
    }
}
