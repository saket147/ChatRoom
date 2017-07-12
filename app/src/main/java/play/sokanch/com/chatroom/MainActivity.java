package play.sokanch.com.chatroom;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    RecyclerView.Adapter adapter;
    EditText enterMsg;
    Button send;
    RecyclerView msgRecyclerView;
    private ArrayList<Chats> chatsArrayList;
    private String ts;
    private Socket socket;
    private Chats chats;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ChatRoomApplication app = (ChatRoomApplication) this.getApplication();
        socket = app.getSocket();
        send = (Button)findViewById(R.id.button3);
        enterMsg = (EditText) findViewById(R.id.enter_send_text);
        msgRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on(Socket.EVENT_CONNECT,onConnect);
        socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        socket.on("rcv_msg", onNewMessege);

        socket.connect();




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
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this,
                            "Disconnected", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        Toast.makeText(MainActivity.this,
                                "Connect", Toast.LENGTH_LONG).show();

                    }
                }
            );
        }
    };
    public void attempSend(){
        if (!socket.connected()){
            Toast.makeText(MainActivity.this, "Socket not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        else{
            Toast.makeText(MainActivity.this, "Socket connected", Toast.LENGTH_SHORT).show();

        }
        String messege = enterMsg.getText().toString().trim();
        if (TextUtils.isEmpty(messege)){
            enterMsg.requestFocus();
            return;
        }
        enterMsg.setText("");
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("txt",messege);
            socket.emit("send_msg", jsonObject, new Ack() {
                @Override
                public void call(Object... args) {
                    Log.d("Ack received",args.toString());
                    JSONObject jsonObject = (JSONObject) args[0];
                    Log.d("Ack received",jsonObject.toString());

                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public void addMessege(String messege, String timeStamp){
        chats=new Chats(messege, timeStamp);
        chatsArrayList.add(chats);
        adapter.notifyItemInserted(chatsArrayList.size() - 1);
        //adapter.notifyDataSetChanged();
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

            Log.d("Args ","Args "+args.toString());
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Args ","Args "+args.toString());
            JSONObject jsonObject = (JSONObject) args[0];
            Log.d("JSON Object ","JSON "+jsonObject+"  length"+args.length);
            Log.d("JSON Object ","JSON "+jsonObject.toString());
                    try {
                        addMessege(jsonObject.getString("txt"),jsonObject.getLong("timestamp")+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }});
            }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        socket.off("rcv_msg", onNewMessege);
    }

}
