package fr.jserv.androidapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class DisplayMessageActivity extends AppCompatActivity {
    private Socket mSocket;
    private String serverUrl;
    private TextView tvMain, tvName, tvSurname;

    public DisplayMessageActivity getActivity() {
        return this;
    }

    private Emitter.Listener onChallenge = new Emitter.Listener() {
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

    private Emitter.Listener onSuccessSignIn = new Emitter.Listener() {
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
    private Emitter.Listener onSuccessLogIn = new Emitter.Listener() {
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
    private Emitter.Listener onSuccessBind = new Emitter.Listener() {
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
    private Emitter.Listener onUpdate = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Log.i("mSocket", "Got update request: " + data.toString());

                    String message;
                    try {
                        switch(data.getString("id")){
                            case "txtName":
                                message = data.getString("value");
                                Log.d("mSocket", "id field contains:" + message);
                                tvName.setText(message);
                            case "txtSurname":
                                message = data.getString("value");
                                Log.d("mSocket", "id field contains:" + message);
                                tvSurname.setText(message);
                            default:
                                throw new Exception("No component matching form.");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    private String answerChallenge(Object... args) {
        return "42";
    }

    private void addMessage(String message){
        tvMain.setText(message);
    }

    private void setCredsVisibility(int visibility){
        tvName.setVisibility(visibility);
        tvSurname.setVisibility(visibility);
        ((TextView) findViewById(R.id.lblName)).setText(visibility);
        ((TextView) findViewById(R.id.lblSurname)).setText(visibility);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String psk;
        setContentView(R.layout.activity_display_message);
        tvMain = (TextView) findViewById(R.id.tvMain);
        tvName = (TextView) findViewById(R.id.tvName);
        tvSurname = (TextView) findViewById(R.id.tvSurname);
        tvMain.setText("Pending...");
        Intent intent = getIntent();
        serverUrl = intent.getStringExtra(MainActivity.EXTRA_MESSAGE).toUpperCase();
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
}
