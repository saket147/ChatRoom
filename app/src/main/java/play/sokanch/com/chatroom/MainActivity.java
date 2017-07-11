package play.sokanch.com.chatroom;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    RecyclerView.Adapter adapter;
    EditText enterMsg;
    Button send;
    RecyclerView msgRecyclerView;
    private ArrayList<Chats> chatsArrayList;
    String ts;
    private Socket socket;
    private Context context;


    {
        try {
            socket = IO.socket(Constraints.URL_WEBSOCKET);
        }catch (URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    private Utils utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = (Button)findViewById(R.id.button3);
        enterMsg = (EditText) findViewById(R.id.enter_send_text);
        msgRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        Long tsLong = System.currentTimeMillis()/1000;
        ts = tsLong.toString();
        context = getApplicationContext();

        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on("new Messege", onNewMessege);

        enterMsg.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == R.id.send || i == EditorInfo.IME_NULL){
                    attempSend();
                    return false;
                }
                return false;
            }
        });

        utils = new Utils(getApplicationContext());
        chatsArrayList = new ArrayList<>();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attempSend();
            }
        });
        msgRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        msgRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessegeAdapter(chatsArrayList);
        msgRecyclerView.setAdapter(adapter);
    }
    public void attempSend(){
        if (!socket.connected()){
            Toast.makeText(MainActivity.this, "Socket not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        String messege = enterMsg.getText().toString().trim();
        if (TextUtils.isEmpty(messege)){
            enterMsg.requestFocus();
            return;
        }
        enterMsg.setText("");
        addMessege(messege, ts);
        socket.emit("chat", messege, ts);
    }
    public void addMessege(String messege, String timeStamp){
        chatsArrayList.add(new Chats(enterMsg.getText().toString(), ts));
        adapter.notifyItemInserted(chatsArrayList.size() - 1);
        scrollToBottom();
    }
    private void scrollToBottom() {
        msgRecyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this.getApplicationContext(),
                            "Failed to connect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onNewMessege = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String messege = (String) args[0];
                    String timeStamp = (String)args[1];

                    addMessege(messege, timeStamp);
                }
            });
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off("new Messege", onNewMessege);
    }
}
