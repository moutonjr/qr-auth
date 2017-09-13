package fr.jserv.myfirstapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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
    private TextView textView;

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

    private Emitter.Listener onSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addMessage();
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
                                textView.setText(message);
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

/*
    // Listen to events
    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }

                    // add the message to view
                    addMessage();
                }
            });
        }
    };
 */

    private void addMessage(){
        textView.setText("Successful login!!");
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String psk;
        setContentView(R.layout.activity_display_message);
        textView = (TextView) findViewById(R.id.textView2);
        textView.setText("Pending...");
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
            mSocket.on("Successful Bind", onSuccess);
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
        mSocket.off("Successful Bind", onSuccess);
        mSocket.off("update", onUpdate);    }
}
