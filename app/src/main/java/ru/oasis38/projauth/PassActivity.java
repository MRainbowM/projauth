package ru.oasis38.projauth;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass);

        tvFingerprintStatus = (TextView) findViewById(R.id.tvFingerprintStatus);
//        if(canUseFingerprint()){
//            tvFingerprintStatus.setText("Сканер есть");
//        } else {
//            tvFingerprintStatus.setText("Нет сканера");
//        }
//    }
//    public boolean canUseFingerprint(){ // Проверяем наличие сканера
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
//            return false;
//        } else {
//            keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
//            fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);
//            return keyguardManager.isKeyguardSecure() && fingerprintManager != null && fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints();
//        }
//    }
//
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tvFingerprintStatus.setText("Нет сканера");
        } else {
            tvFingerprintStatus.setText("Сканер есть");
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            if (!keyguardManager.isKeyguardSecure()) {
                printMessage("Защита экрана не включена в настройках");
                return;
            }
            if (ActivityCompat.checkSelfPermission (this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                printMessage("Разрешение аутентификации по отпечатку пальца не включено");
                return;
            }
            if (! fingerprintManager.hasEnrolledFingerprints ()) {
                printMessage("Необходимо зарегистрировать хотя бы один отпечаток в настройках");
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


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            fingerprintManager = (FingerprintManager)getSystemService(FINGERPRINT_SERVICE);
//            keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
//            if(fingerprintManager.isHardwareDetected()) {
//                tvFingerprintStatus.setText("Нет сканера отпечатка пальцев");
//            } else if(ContextCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
//                tvFingerprintStatus.setText("Не разрешено использвоать сканер");
//            } else if(!keyguardManager.isKeyguardSecure()){
//                tvFingerprintStatus.setText("Добавьте блокировку в настройках телефона");
//            } else if(fingerprintManager.hasEnrolledFingerprints()) {
//                tvFingerprintStatus.setText("Необходимо добавить хотя бы один отпечаток, чтобы использовать эту функцию");
//            } else {
//                tvFingerprintStatus.setText("Прикоснитесь к отпечатку пальцев");
//            }
//
//        }


}
