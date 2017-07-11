package play.sokanch.com.chatroom;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by saket on 11/7/17.
 */

public class Utils {
    private Context context;
    private SharedPreferences sharedPref;

    private static final String KEY_SHARED_PREF = "ANDROID_WEB_CHAT";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_SESSION_ID = "sessionId",
            FLAG_MESSAGE = "message";
    public Utils(Context context){
        this.context = context;
        sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF,  KEY_MODE_PRIVATE);

    }
    public void storeSessionID(String sessionID){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_SESSION_ID, null);
        editor.commit();
    }
    public String getSessionID(){
        return sharedPref.getString(KEY_SESSION_ID, null);
    }
    public String getSendMessegeJSON(String messege){
        String json = null;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("flag", FLAG_MESSAGE);
            jsonObject.put("sessionId", getSessionID());
            jsonObject.put("message", messege);

        }catch (JSONException e){
            e.printStackTrace();
        }
        return json;
    }
}
