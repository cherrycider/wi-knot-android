package app.com.cherrycider.android.wiknot;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * Сервис  рассылает сообщения каждые 5 секунд
 */
public class OnlinePulse extends IntentService {

    // фильтр для логов logcat
   // private static final String TAG = "myLogs";


    private static boolean serviceActive = true;

    public static String MyIpAddress = "not set";

    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();


    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    public OnlinePulse() {
        super("Online Pulse");
    }


    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "OnlinePulse onCreate");

    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        //Log.d(TAG, "OnlinePulse onStartCommand");

        onHandleIntent(intent);


        return START_STICKY;
    }


    public void onDestroy() {


        super.onDestroy();
        //Log.d(TAG, "OnlinePulse onDestroy");


    }


    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "OnlinePulse onBind");
        return null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {


        new Thread(new Runnable() {
            public void run() {

                sendBroadcastOnlinePollPulse();

                while (serviceActive) {

                    try {
                    TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                    e.printStackTrace();
                    }

                // Log.d(TAG, "---- OnlinePulse sendBroadcastOnlinePollPulse 5 cек ----");

                    sendBroadcastOnlinePollPulse();

                }
            }


        }).start();

    }






    public void sendBroadcastOnlinePollPulse() {

        MyIpAddress = wiknot.getIpAddress();

        if (!(MyIpAddress == "not set")) {


            sendUdpPollMessage("iAmOnline, " + MainActivity.myName + "," + "online" + "," + MyIpAddress + "," + MainActivity.mySSID + "," + MainActivity.myBSSID + "," + MainActivity.myID + "," + MainActivity.moreInfo, "224.0.0.3");
            //sendUdpPollMessage(("iAmOnline, " + MyIpAddress), "224.0.0.3");

            //Log.d(TAG, "OnlinePulse iAmOnline"+ MyIpAddress + " to 224.0.0.3" );

        }
    }



    /**
     * send UDP Message
     */


    private static int SEND_SERVER_PORT = 4567;




    public void sendUdpPollMessage(final String msgToSend, final String inetAddrToSendTo) {


        DatagramSocket ds = null;

        try {
            ds = new DatagramSocket();
            DatagramPacket dp;
            dp = new DatagramPacket(msgToSend.getBytes("cp1251"), msgToSend.length(), InetAddress.getByName(inetAddrToSendTo), SEND_SERVER_PORT);
            ds.setBroadcast(true);
            ds.send(dp);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }

    }


}




