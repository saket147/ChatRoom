package play.sokanch.com.chatroom;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;

/**
 * Created by saket on 8/7/17.
 */

public class MessegeAdapter extends RecyclerView.Adapter<MessegeAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<Chats> chatMsgs;
    public MessegeAdapter(ArrayList<Chats> chat){
        this.chatMsgs = chat;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        TextView receivedText = holder.receivedText;
        TextView timeStamps = holder.timeStamps;
        receivedText.setText(chatMsgs.get(position).getReceivedText());
        timeStamps.setText(chatMsgs.get(position).getTimeStamps());

    }

    @Override
    public int getItemCount() {
        return chatMsgs.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView receivedText;
        TextView timeStamps;
        public MyViewHolder(View view){
            super(view);
            receivedText = (TextView)view.findViewById(R.id.received_text);
            timeStamps = (TextView)view.findViewById(R.id.timestamp);
        }
    }
}
