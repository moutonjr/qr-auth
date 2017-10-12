package fr.jserv.qr_auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by moutonjr on 09/10/2017.
 */

public abstract class QRLogin extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private final String LOGFACILITY = "QRLoginActivity";
    public static final String EXTRA_MESSAGE = "fr.jserv.qr_authserver.LOGINOID";
    private ZXingScannerView mScannerView;
    private Class targetActivity;

    public QRLogin(Class targetLoginActivity){
        super();
        this.targetActivity = targetLoginActivity;
    }

    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.e(LOGFACILITY,"Starting Activity");

        mScannerView = new ZXingScannerView(this);
        // Set the scanner view as the content view
        setContentView(mScannerView);
    }

    @Override
    public final void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public final void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    @Override
    public final void handleResult(Result rawResult) {
        Intent intent = new Intent(this,this.targetActivity);
        intent.putExtra(EXTRA_MESSAGE, rawResult.getText());
        setResult(RESULT_OK, intent);
        startActivity(intent);
        finish();
    }

}
