package play.sokanch.com.chatroom;

/**
 * Created by saket on 8/7/17.
 */

public class Chats {

    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_LOG = 1;
    public static final int TYPE_ACTION = 2;

    private String receivedText;
    private String timeStamps;
    Chats(String receivedText, String timeStamps){
        this.receivedText = receivedText;
        this.timeStamps = timeStamps;
    }

    public String getReceivedText() {
        return receivedText;
    }

    public String getTimeStamps() {
        return timeStamps;
    }
}
