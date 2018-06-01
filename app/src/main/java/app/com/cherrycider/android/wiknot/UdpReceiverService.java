package app.com.cherrycider.android.wiknot;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;


public class UdpReceiverService extends IntentService {

    final String TAG = "myLogs";

    public String MyIpAddress = "not set";
    private static boolean serviceActive = true;
    public static boolean beaconActive = true;
    public String msgReceived;
    private static int LISTEN_SERVER_PORT = 4567;

    public String mySSID;
    public String myBSSID;

    private boolean side = false;
    public boolean left = false;



    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    // переменные из PersonDetails.java
    SharedPreferences myProfile;
    public static String myName;
    public static String moreInfo;
    public static String myPhoto;

    public static String myInstallationID;

    public File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");

    // объявляем BroadcastReceiver для приема интента с Chat onDestroy в переменных  - step1.1
    BroadcastReceiver chatIntentReceiver;

    // переменные для приема интента с Chat onDestroy - step1.2
    public static String CHAT_STATUS_IS_CHANGED = "com.cherrycider.udpX.chatStatusIsChanged";
    public static String CHAT_STATUS = "chatStatus";
    public static String chatFragmentStatus = "chatIsOnCreateView";
    public static boolean chatIsOnCreateView = true;


    // объявляем BroadcastReceiver для приема интента с People onDestroy в переменных  - step1.1 с people
    BroadcastReceiver peopleIntentReceiver;

    // переменные для приема интента с People onDestroy - step1.2 с people

    public static String PPLLIST_STATUS_IS_CHANGED = "com.cherrycider.udpX.peopleStatusIsChanged";
    public static String PPLLIST_STATUS = "peopleStatus";
    public static String peopleFragmentStatus = "peopleIsOnCreateView";
    public static boolean peopleIsOnCreateView = true;


    public static String MESSAGE_IS_RECEIVED_INTENT = "com.cherrycider.udpX.messageIsReceived";
    public static String MESSAGE_STRING = "MessageString";

    ChatDBHelper chatDBHelper;

    // Рисуем иконку вверху если сервис online

    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

    // для VERSION 10
    // NotificationManager nm;

    // переменные для звука
    SoundPool sp;
    int notificationSoundId;



    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     * <p/>
     * parametre name Used to name the worker thread, important only for debugging.
     */
    public UdpReceiverService() {
        super("Udp Receiver Service");
    }

    public void onCreate() {
        super.onCreate();
        //Log.d(TAG, "UdpReceiverService onCreate");

        // создаем SoundPool и загружаем звук
        createSoundPool();
        //sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        notificationSoundId = sp.load(this, R.raw.pebbles, 1);




        // создаем BroadcastReceiver в onCreate   step 2
        chatIntentReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                chatFragmentStatus = intent.getExtras().getString(CHAT_STATUS);
                if (chatFragmentStatus.contains("chatIsOnDestroyView")) {
                    chatIsOnCreateView = false;
                } else {
                    chatIsOnCreateView = true;
                }
                //Log.d(TAG, "Arrived to UdpReceiverService: action = " + action);
                //Log.d(TAG, "Arrived to UdpReceiverService: chatFragmentStatus = " + chatFragmentStatus + "(" + chatIsOnCreateView + ")");


            }
        };


        // создаем фильтр для BroadcastReceiver в onCreate  step 3
        IntentFilter intentFilter = new IntentFilter(CHAT_STATUS_IS_CHANGED);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(chatIntentReceiver, intentFilter);


        // создаем BroadcastReceiver в onCreate   step 2  с people
        peopleIntentReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                peopleFragmentStatus = intent.getExtras().getString(PPLLIST_STATUS);
                if (peopleFragmentStatus.contains("peopleIsOnDestroyView")) {
                    peopleIsOnCreateView = false;
                } else {
                    peopleIsOnCreateView = true;

                }
                //Log.d(TAG, "Arrived to UdpReceiverServer: action = " + action);
                //Log.d(TAG, "Arrived to UdpReceiverServer: peopleFragmentStatus = " + peopleFragmentStatus + "(" + peopleIsOnCreateView + ")");


            }
        };

        // создаем фильтр для BroadcastReceiver в onCreate  step 3  с people
        IntentFilter peopleIntentFilter = new IntentFilter(PPLLIST_STATUS_IS_CHANGED);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(peopleIntentReceiver, peopleIntentFilter);


        // создаем объект для создания и управления версиями БД
        chatDBHelper = new ChatDBHelper(this);

    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        myInstallationID = MainActivity.myID;
        mySSID = MainActivity.mySSID;
        myBSSID = MainActivity.myBSSID;



        // даем команду появиться в сети сразу после запуска UdpRceiveService
        sendBroadcastIntentMessageToApplication("sendStatusPoll");

        // TODO запускаем Online Beacon  - здесь не запускаем, нужно перезапускать каждый раз когда меняется статус wifi с main activity
        // TODO определяем чтоб beacon не светил если нет wifi

        //goBeacon();



        //Log.d(TAG, "UdpReceiverService onStartCommand");
        //receiveTask();
        onHandleIntent(intent);
        //return super.onStartCommand(intent, flags, startId);



        /**
         * // Notification рисуем в Action Bar бантик если сервис online
         *
         */
        if (Build.VERSION.SDK_INT >= 11) {
            // NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);

            mBuilder.setSmallIcon(R.drawable.logo_tilted_wite_100);
            mBuilder.setContentTitle("wi knot is online");
            //mBuilder.setContentText("сlick to open wi knot");
            mBuilder.setOngoing(true);
            //mBuilder.setAutoCancel(false);

            /**
             * не получается вызывать старое activity,
             * все время подымается новое,
             * уже после закрывается старое, вызывая проблемы в чате
             * типа : chatFragmentStatus = chatIsOnDestroyView(false)
             *
             * УДИВИТЕЛЬНО прошло само после обновления API
             */
            // открываем приложение при нажатии на notification
            Intent backToAppIntent = new Intent(this, MainActivity.class);
            backToAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            backToAppIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            //backToAppIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            //backToAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            //stackBuilder.addParentStack(MainActivity.class);

            // Adds the Intent that starts the Activity to the top of the stack
            //stackBuilder.addNextIntent(backToAppIntent);
            //PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
            //mBuilder.setContentIntent(resultPendingIntent);

            PendingIntent backToAppIntentPendingIntent = PendingIntent.getActivity(this, 0, backToAppIntent, 0);

            mBuilder.setContentIntent(backToAppIntentPendingIntent);


            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // notificationID allows you to update the notification later on.
            mNotificationManager.notify(9999, mBuilder.build());
        }

        /**
         * для Version 10
         *
         else {

         nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

         // 1-я часть
         Notification notif = new Notification(R.drawable.logo_tilted_wite_100, "Text in status bar",
         System.currentTimeMillis());

         // 3-я часть
         //Intent intent = new Intent(this, MainActivity.class);
         //intent.putExtra(MainActivity.FILE_NAME, "somefile");
         //PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

         // 2-я часть
         //notif.setLatestEventInfo(this, "Notification's title", "Notification's text", pIntent);

         // ставим флаг, чтобы уведомление пропало после нажатия
         //notif.flags |= Notification.FLAG_AUTO_CANCEL;

         // отправляем
         nm.notify(9999, notif);


         }

         */


        return START_STICKY;
    }

    public void onDestroy() {
        // onDestroy почему-то не запускается скомандой stopService !!!!!
        // по команде goExit запускается но не работает метод :public void stopUdpReceiverService()
        //                                                       {stopService(new Intent(this, UdpReceiverService.class));}

        super.onDestroy();
        //Log.d(TAG, "UdpReceiverService onDestroy");


        // дерегистрируем (выключаем) BroadcastReceiver  - step 4
        unregisterReceiver(chatIntentReceiver);
        //Log.d(TAG, "BroadcastReceiver в UdpReceiverService выключен");

        // дерегистрируем (выключаем) BroadcastReceiver  - step 4
        unregisterReceiver(peopleIntentReceiver);
        //Log.d(TAG, "BroadcastReceiver в UdpReceiverService выключен");

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(9999);

        // возвращаем  разрешение на прием Multicast UDP
        WifiManager wifi;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("MulticastLock");
        multicastLock.release();

    }


    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "UdpReceiverService onBind");
        return null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {

        // получаем разрешение на прием Multicast UDP
        WifiManager wifi;
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock multicastLock = wifi.createMulticastLock("MulticastLock");
        multicastLock.acquire();


        //void receiveTask() {
        new Thread(new Runnable() {
            public void run() {

                byte[] datagramMsg = new byte[1024];

                DatagramPacket dataPacket = new DatagramPacket(datagramMsg, datagramMsg.length);

                //Log.d(TAG, "UdpReceiverService DatagramPacket datagramMsg.length= " + datagramMsg.length);

                //DatagramSocket datagramSocket = null;
                MulticastSocket datagramSocket = null;


                try {

                    //datagramSocket = new DatagramSocket(LISTEN_SERVER_PORT);

                    datagramSocket = new MulticastSocket(LISTEN_SERVER_PORT);
                    datagramSocket.joinGroup(InetAddress.getByName("224.0.0.3"));

                    while (serviceActive) {

                        //Log.d(TAG, "UdpReceiverService готов принимать пакеты");
                        datagramSocket.receive(dataPacket);
                        msgReceived = new String(datagramMsg, 0, dataPacket.getLength(), "cp1251");

                        //Log.d(TAG, "dp.getLength = " + dp.getLength());

                        //Log.d(TAG, "UdpReceiverService Принято: " + msgReceived);


                        // если фрагмент Chat не живой, пишем сообщения в БД
                        // или отправляем и пропускаем служебные сообщения сами

                        //   if (!chatIsOnCreateView)
                        //   {
                        //Log.d(TAG, "UdpReceiverService НЕ послано в MainActivity и в ChatFragment " + msgReceived);


                        ////////////////////////////////////////////////////////////////////////////////
                        ///// если чат не живой сервис сам отвечает IAmOnline
                        ///// и отправляет сообщение все равно для отображения в People Online


                        if (msgReceived.contains("tellYourStatusTo, ")) {

                            //Log.d(TAG, "UdpReceiverService пришло сообщение tellYourStatusTo " );
                            //Log.d(TAG, " msgReceived " + msgReceived );

                            if
                             //       (
                                    (!chatIsOnCreateView)
                             //               && (!peopleIsOnCreateView))
                            {

                                // оптимизировать, чтобы не отсылался IAmOnline два раза

                                // расфасовываем сообщение с запятыми в массив msgToRowList
                                ArrayList msgToRowList = new ArrayList(Arrays.asList(msgReceived.split(",")));


                                // получаем поля myName и moreInfo из shared preferences
                                loadMyProfile();


                                // отсылаем фото себя тому кто просит

                                wiknot.SendFile(wiknotFolder, myPhoto, myInstallationID, String.valueOf(msgToRowList.get(3)));
                                Log.d(TAG, " с UdpReceiverService отсылаем фото себя тому кто просит" + myPhoto);
                                Log.d(TAG, " с UdpReceiverService отсылаем фото на адрес " + String.valueOf(msgToRowList.get(3)));

                                // отсылаем сообщение IAmOnline тому кто просит
                                MyIpAddress = wiknot.getIpAddress();
                                String preparedMessage = "IAmOnline, " + myName + "," + "online" + "," + MyIpAddress + "," + mySSID + "," + myBSSID + "," + myInstallationID + "," + moreInfo;
                                wiknot.SendMessage(preparedMessage, String.valueOf(msgToRowList.get(3)));

                                //Log.d(TAG, "UdpReceiverService SendMessage " + preparedMessage + " to " + msgToRowList.get(3));

                                // wiknot.SendMessage("IAmOnline, " + "name" + "," + "onlineStatus" + "," + wiknot.getIpAddress() + "," + wiknot.currentSSID() + "," + "BSSID" + "," + "userID" + "," + "time", msgReceived.replace("tellYourStatusTo, ", ""));

                                //wiknot.SendMessage("IAmOnline, " + wiknot.getIpAddress(), msgReceived.replace("tellYourStatusTo, ", ""));
                                //Log.d(TAG, "с UdpReceiverService на запрос послано IAmOnline,  " + preparedMessage);


                            }

                            // оптимизировать, чтобы не отсылался IAmOnline два раза
                            // посылаем в broadcast intent с принятым сообщением на случай если открыт фрагмент People Online

                            else {
                                sendBroadcastIntentMessageToApplication(msgReceived);
                            }

                            //return;

                        }
                        /**
                         *
                         *
                         // посылаем в broadcast intent с принятым сообщением

                         Intent i = new Intent();

                         i.setAction(MainActivity.MESSAGE_IS_RECEIVED_INTENT);

                         i.putExtra(MainActivity.MESSAGE_STRING, msgReceived);

                         MainActivity.MainContext.getApplicationContext().sendBroadcast(i);
                         */

                        //Log.d(TAG, "Сообщение послано от UdpReceiverService при chatFragmentStatus = " + chatFragmentStatus);


                        //Log.d(TAG, "UdpReceiverService послано в MainActivity для отображения в People Online " + msgReceived);


                        // пропускаем сообщения IAmOnline на случай, если открыт фрагмент People Online

                        if (msgReceived.contains("IAmOnline, ")) {

                            //  посылаем в broadcast intent с принятым сообщением на случай если открыт фрагмент People Online
                            // TODO иначе update db

                            if (peopleIsOnCreateView) {
                                sendBroadcastIntentMessageToApplication(msgReceived);
                            }

                            //return;



                        }


                        if (msgReceived.contains("IAmOFFline, ")) {

                            //  посылаем в broadcast intent с принятым сообщением на случай если открыт фрагмент People Online
                            // TODO иначе update db
                            if (peopleIsOnCreateView) {
                                sendBroadcastIntentMessageToApplication(msgReceived);
                            }

                            //return;


                        }


                        ////////////////////////////////////////////////////////////////////////////////
                        /////  если chat мертв write message to chat BD
                        //
                        //
                        //
                        /////////////////////////////////////////////
                        // записываем сообщение в BD

                        //  отрезаем toChatForAll
                        if (msgReceived.contains("toChatForAll, ")) {

                            if (!chatIsOnCreateView) {

                                // расфасовываем сообщение с запятыми в массив msgToRowList
                                ArrayList msgToRowList = new ArrayList(Arrays.asList(msgReceived.split(",")));
                         /*
                            //String toChatForAllMessage = "toChatForAll, " + myName + "," + myInstallationID + "," + "sentTime" + "," "ssid" + ","+  MyIpAddress + "," + m;
                            *

                            for(int i=0;i<msgToRowList.size();i++)
                            {
                             Log.d(TAG, " MainActivity --> "+ i +" "+ msgToRowList.get(i));
                            }
                          */

                                String nameToDB = String.valueOf(msgToRowList.get(1));
                                String userIDToDB = String.valueOf(msgToRowList.get(2));
                                String sentTimeToDB = String.valueOf(msgToRowList.get(3));
                                String ssidToDB = String.valueOf(msgToRowList.get(4));
                                String IpAddressToDB = String.valueOf(msgToRowList.get(5));
                                String messageToDB = String.valueOf(msgToRowList.get(6));

                                String sideToDB;


                                // Log.d(TAG, "Не отсылается в chat а пишется в DB" + msgReceived);

                                // delete: String msgToChatBD = msgReceived.replace("toChatForAll, ", "");


                                // с какой стороны печатать
                                if (IpAddressToDB.contains(MainActivity.MyIpAddress)) {
                                    side = !left;
                                    // вписываем в DB столбец side
                                    sideToDB = "right";

                                } else {
                                    side = left;
                                    // вписываем в DB столбец side
                                    sideToDB = "left";

                                }

                                // создаем объект для данных
                                ContentValues cv = new ContentValues();

                                // получаем данные из полей ввода


                                // подключаемся к БД
                                SQLiteDatabase db = chatDBHelper.getWritableDatabase();


                                //Log.d(TAG, "--- Insert in chatTable: ---");
                                // подготовим данные для вставки в виде пар: наименование столбца - значение

                                //  проставляем  текущее время time,

                                cv.put("side", sideToDB);
                                cv.put("userID", userIDToDB);
                                cv.put("name", nameToDB);
                                cv.put("macAddress", "-");
                                cv.put("ssid", ssidToDB);
                                cv.put("senttime", sentTimeToDB);
                                cv.put("time", wiknot.getTimeAndDate());
                                cv.put("message", messageToDB);

                                // вставляем запись и получаем ее ID
                                long rowID = db.insert("chatTable", null, cv);
                                //Log.d(TAG, "row inserted, ID = " + rowID);

                                sp.play(notificationSoundId, 1, 1, 0, 0, 1);
                                //Log.d(TAG, " Chat Fragment notification sound left");


                            } else {
                                //если chat жив
                                sendBroadcastIntentMessageToApplication(msgReceived);


                            }


                        }
                        //return;


                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    //Log.d(TAG, "UdpReceiverService Оппc: " + e.getMessage());

                    //выдает  Оппc: bind failed: EADDRINUSE (Address already in use)
                    // нужно будет потом  разобраться при автоматизации broadcast address

                }
                /**
                 *
                 */ finally {
                    if (datagramSocket != null) {
                        datagramSocket.close();
                        UdpReceiverServiceWakefulBroadcastReceiver.completeWakefulIntent(intent);
                    }


                }
            }


        }).start();

    }


    /**
     * send intent broadcast Message back to application
     */
    public void sendBroadcastIntentMessageToApplication(String msg) {

        Intent i = new Intent();

        i.setAction(MESSAGE_IS_RECEIVED_INTENT);

        i.putExtra(MESSAGE_STRING, msg);

        this.getApplicationContext().sendBroadcast(i);


    }


    void loadMyProfile() {
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);
        myName = myProfile.getString("myName", String.valueOf(myName));
        moreInfo = myProfile.getString("moreInfo", String.valueOf(moreInfo));
        myPhoto = myProfile.getString("myPhoto", String.valueOf(myPhoto));


        //Toast.makeText(this, "My Profile loaded", Toast.LENGTH_SHORT).show();
    }

    // методы для построения SoundPool для разных API

    protected void createSoundPool() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createNewSoundPool();
        } else {
            createOldSoundPool();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createNewSoundPool(){
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sp = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool(){
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC,0);
    }





    /////////////////////////////////////////////
    // этот метод рассылает online сообщения в сеть

    public void goBeacon() {



        AsyncTask<Void, Void, Void> async_Beacon = new AsyncTask<Void, Void, Void>() {




            protected void onPreExecute() {
            }


            protected Void doInBackground(Void... params) {

                //String m = params[0];

                while (beaconActive) {

                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //Log.d(TAG, "UdpReceiverService goBeacon 1 cек ----");

                    // получаем поля myName и moreInfo из shared preferences
                    loadMyProfile();
                    MyIpAddress = wiknot.getIpAddress();


                    //TODO проверяем ip address на условие , если нет сети выходим и останавливаем beacon

                    //TODO влияет ли получение инфы mySSID и тд на производительность?
                    String preparedMessage = "IAmOnline, " + myName + "," + "online" + "," + MyIpAddress + "," + mySSID + "," + myBSSID + "," + myInstallationID + "," + "Hi from beacon!";

                    wiknot.SendMessagefromSameThread(preparedMessage,"224.0.0.3");

                    //Log.d(TAG, "UdpReceiverService goBeacon : sent = " + preparedMessage);
                }



                return null;

            }


            protected void onPostExecute(Void result) {

                super.onPostExecute(result);



            }

        };


        async_Beacon.execute();
    }



}




