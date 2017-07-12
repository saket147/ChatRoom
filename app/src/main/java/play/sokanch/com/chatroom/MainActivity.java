package play.sokanch.com.chatroom;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapter;
    private EditText enterMsg;
    private ImageButton send;
    private RecyclerView msgRecyclerView;
    private ArrayList<Chats> chatsArrayList;
    private Socket socket;
    private Chats chats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ChatRoomApplication app = (ChatRoomApplication) this.getApplication();
        socket = app.getSocket();
        initialize();
        isInternetOn();
        socketOn();
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
    public void initialize(){
        send = (ImageButton)findViewById(R.id.button3);
        enterMsg = (EditText) findViewById(R.id.enter_send_text);
        msgRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    }
    private void socketOn(){
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        socket.on("rcv_msg", onNewMessege);

        socket.connect();

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
                                "Connected to server", Toast.LENGTH_LONG).show();

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

                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Acknowledgement received", Toast.LENGTH_SHORT).show();
                        }
                    });
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
                            "Failed to connect to server", Toast.LENGTH_LONG).show();
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
                    //long batch_date = jsonObject.getLong("timestamp");


                    try {
                        long batch_date = jsonObject.getLong("timestamp");
                        Date dt = new Date (batch_date * 1000);

                        SimpleDateFormat sfd = new SimpleDateFormat("hh:mm aa");
                        addMessege(jsonObject.getString("txt"), sfd.format(dt));
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
    public final boolean isInternetOn() {

        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // connected to wifi
                Toast.makeText(getApplicationContext(), "Connected to " + activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                Toast.makeText(getApplicationContext(), " Connected to " + activeNetwork.getTypeName(), Toast.LENGTH_SHORT).show();
                return true;
            }

        } else {
            Toast.makeText(getApplicationContext(), "Not connected to internet", Toast.LENGTH_SHORT).show();
            // not connected to the internet
            return false;
        }
        return false;
    }



}
