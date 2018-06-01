package app.com.cherrycider.android.wiknot;

/**
 * class to set direction and message and call inside  customArrayAdapter
 */

public class ChatMessage {
    public boolean left;
    public String message;
    public String ssid;
    public String userID;
    public String time;
    public String name;

    // TODO перенести метод в ChatArrayAdapter
    public ChatMessage(boolean left, String message, String ssid, String userID , String time, String name) {
        super();
        this.left = left;
        this.message = message.replace("|comma|",",");
        this.ssid = ssid;
        this.userID = userID;
        this.time = time;
        this.name = name.replace("|comma|",",");
    }
}