package fr.jserv.androidapp;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import fr.jserv.qr_auth.QRSignIn;

public class LoginActivity extends QRSignIn {

    protected String answerChallenge(Object... args) {
        return "42";
    }

    protected void onCreate(Bundle savedInstanceState){
        this.tvMain = (TextView) findViewById(R.id.tvMain);
        this.lblName = (TextView) findViewById(R.id.lblName);
        this.lblSurname = (TextView) findViewById(R.id.lblSurname);
        this.tvName = (EditText) findViewById(R.id.tvName);
        this.tvSurname = (EditText) findViewById(R.id.tvSurname);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }




}
