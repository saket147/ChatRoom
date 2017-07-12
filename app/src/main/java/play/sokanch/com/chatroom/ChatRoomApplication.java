package play.sokanch.com.chatroom;

import android.app.Application;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by saket on 12/7/17.
 */

public class ChatRoomApplication extends Application{
    private Socket mSocket;
    {
        try {
            mSocket = IO.socket(Constraints.URL_WEBSOCKET);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
