package fr.jserv.qr_authserver;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Created by moutonjr on 09/10/2017.
 */

public abstract class QRSignIn extends AppCompatActivity {
    private final String LOGFACILITY = this.getClass().toString();
    protected Socket mSocket;
    private String serverUrl;
    protected TextView tvMain;
    protected EditText tvName, tvSurname;
    protected TextView lblName, lblSurname;

    public QRSignIn getActivity() {
        return this;
    }
    protected Emitter.Listener onChallenge = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String response = answerChallenge(args);
                    mSocket.emit("uuidresponse", response);
                }
            });
        }
    };

    protected Emitter.Listener onSuccessSignIn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage("Successful Sign-In !");
                    setCredsVisibility(View.INVISIBLE);
                }
            });
        }
    };
    protected Emitter.Listener onSuccessLogIn = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage("Successful Log In !");
                    setCredsVisibility(View.VISIBLE);
                    JSONObject data = (JSONObject) args[0];
                    try {
                        tvName.setText(data.getString("name"));
                        tvName.setText(data.getString("Surname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };
    protected Emitter.Listener onSuccessBind = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage("Successful Bind !");
                }
            });
        }
    };
    protected Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i(LOGFACILITY, "Got update request: " + data.toString());

                    String message;
                    try {
                        switch(data.getString("id")){
                            case "txtName":
                                message = data.getString("value");
                                Log.d(LOGFACILITY, "id field contains:" + message);
                                tvName.setText(message);
                                break;
                            case "txtSurname":
                                message = data.getString("value");
                                Log.d(LOGFACILITY, "id field contains:" + message);
                                tvSurname.setText(message);
                                break;
                            default:
                                throw new Exception("No component matching form.");
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    protected void update(String id, String text){
        JSONObject data = new JSONObject();
        try {
            data.put("id", id);
            data.put("value", text);
            Log.d("mSocket", "Attempting to send Object: " + data.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mSocket.emit("broadcast", data);
    }

    protected abstract String answerChallenge(Object... args);


    private void addMessage(String message){
        tvMain.setText(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        mSocket.off("uuidquery", onChallenge);
        mSocket.off("Successful Signin", onSuccessSignIn);
        mSocket.off("Successful Bind", onSuccessBind);
        mSocket.off("Successful Login", onSuccessLogIn);
        mSocket.off("update", onUpdate);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String psk;
        tvMain.setText("Pending...");

        Intent intent = getIntent();
        serverUrl = intent.getStringExtra(QRLogin.EXTRA_MESSAGE).toUpperCase();
        URI url;
        List<NameValuePair> params = null;
        try {
            url = new URI(serverUrl);

            params = URLEncodedUtils.parse(url, "UTF-8");
            if(params.get(0).getName().toUpperCase().equals("ID")) {
                psk = params.get(0).getValue().toLowerCase();
            }
            else {
                throw new URISyntaxException(serverUrl, "id not present amongst name:" + params.get(0).getName() + " and value : " + params.get(0).getValue());
            }
            mSocket = IO.socket(url.getScheme().toLowerCase()+"://"+url.getRawAuthority().toLowerCase());
            mSocket.connect();
            Log.i("mSocket", "Connecting to URL " + url.getScheme().toLowerCase()+"://"+url.getRawAuthority().toLowerCase() + " successful.");

            mSocket.emit("authid", psk);
            Log.d("mSocket", "Sending authid with PSK : " + psk);

            mSocket.on("uuidquery", onChallenge);
            mSocket.on("Successful Signin", onSuccessSignIn);
            mSocket.on("Successful Bind", onSuccessBind);
            mSocket.on("Successful Login", onSuccessLogIn);
            mSocket.on("update", onUpdate);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        tvName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) update("txtName", tvName.getText().toString());
            }
        });
        tvSurname.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) update("txtSurname", tvSurname.getText().toString());
            }
        });

    }

    private void setCredsVisibility(int visibility){
        tvName.setVisibility(visibility);
        tvSurname.setVisibility(visibility);
        this.lblName.setText(visibility);
        this.lblSurname.setText(visibility);
    }
}
