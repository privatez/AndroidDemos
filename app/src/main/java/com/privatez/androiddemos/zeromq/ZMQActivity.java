package com.privatez.androiddemos.zeromq;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.privatez.androiddemos.R;
import com.privatez.androiddemos.base.BaseActivity;
import com.privatez.androiddemos.util.LogHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by private on 2017/7/19.
 */

public class ZMQActivity extends BaseActivity {

    private TextView textView;
    private EditText editText;

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");

    private static String getTimeString() {
        return DATE_FORMAT.format(new Date());
    }

    private void serverMessageReceived(String messageBody) {
        textView.append(getTimeString() + " - server received: " + messageBody + "\n");
    }

    private void clientMessageReceived(String messageBody) {
        textView.append(getTimeString() + " - client received: " + messageBody + "\n");
        //Toast.makeText(mContext, messageBody, Toast.LENGTH_SHORT).show();
    }

    private final MessageListenerHandler serverMessageHandler = new MessageListenerHandler(
            new IMessageListener() {
                @Override
                public void messageReceived(String messageBody) {
                    serverMessageReceived(messageBody);
                }
            },
            Util.MESSAGE_PAYLOAD_KEY);

    private final MessageListenerHandler clientMessageHandler = new MessageListenerHandler(
            new IMessageListener() {
                @Override
                public void messageReceived(String messageBody) {
                    clientMessageReceived(messageBody);
                }
            },
            Util.MESSAGE_PAYLOAD_KEY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zmq);

        LogHelper.log(System.getProperty("os.name"));
        textView = (TextView) findViewById(R.id.text_console);
        editText = (EditText) findViewById(R.id.text_message);

        //new Thread(new ZeroMQServer(serverMessageHandler)).start();

        findViewById(R.id.button_send_message).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ZeroMQMessageTask(clientMessageHandler).execute("");
                    }
                });
    }

}
