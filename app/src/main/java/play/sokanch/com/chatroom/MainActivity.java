package play.sokanch.com.chatroom;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.koushikdutta.async.http.WebSocket;

import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    RecyclerView.Adapter adapter;
    EditText enterMsg;
    Button send;
    RecyclerView msgList;
    private ArrayList<Chats> chatsArrayList;
    String ts;
    private WebSocket webSocket;

    private Utils utils;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send = (Button)findViewById(R.id.button3);
        enterMsg = (EditText) findViewById(R.id.enter_send_text);
        msgList = (RecyclerView) findViewById(R.id.recycler_view);
        Long tsLong = System.currentTimeMillis()/1000;
        ts = tsLong.toString();
        utils = new Utils(getApplicationContext());
        chatsArrayList = new ArrayList<>();
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*for (i = 0; i< chatsArrayList.size(); i++){
                    chatsArrayList.add(new Chats(enterMsg.getText().toString(), ts));
                }*/
                sendMessegeToServer(utils.getSendMessegeJSON(enterMsg.getText().toString()));

                chatsArrayList.add(new Chats(enterMsg.getText().toString(), ts));
                enterMsg.setText("");
            }
        });
        msgList.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        msgList.setLayoutManager(layoutManager);
        adapter = new MessegeAdapter(chatsArrayList);
        msgList.setAdapter(adapter);

        webSocket = new WebSocket(URI.create(Constraints.URL_WEBSOCKET , new WebSocket().))

    }
    private void sendMessegeToServer(String messege){
        if (webSocket != null && webSocket.isBuffering()){
            webSocket.send(messege);
        }
    }
}
