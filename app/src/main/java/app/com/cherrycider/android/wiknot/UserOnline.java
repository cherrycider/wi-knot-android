package app.com.cherrycider.android.wiknot;

/**
 * Created by V on 12.06.16.
 */
public class UserOnline {


    public String userName;
    public String userOnlineStatus;
    public String userIpAddress;
    public String ssid;
    public String bssid;
    public String userID;
    public String userMoreInfo;



    public UserOnline(String userName, String userOnlineStatus, String userIpAddress, String ssid, String bssid, String userID, String userMoreInfo) {
        super();

        this.userName = userName.replace("|comma|",",");
        this.userOnlineStatus = userOnlineStatus;
        this.userIpAddress = userIpAddress;
        this.ssid = ssid;
        this.bssid = bssid;
        this.userID = userID;
        this.userMoreInfo = userMoreInfo.replace("|comma|",",");
        this.userMoreInfo = userMoreInfo.replace("|no info|","");
    }
}