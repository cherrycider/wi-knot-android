package app.com.cherrycider.android.wiknot;


import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends FragmentActivity {

    PeopleDBHelper peopleDBHelper;
    SharedPreferences sPref;

    int SCREEN_ORIENTATION = 0;
    int LOGCAT_DUMP = 0;
    static int NOTIFICATION_SOUND = 1;

    public static String currentStatus;
    public static int SERVER_PORT = 4567;
    public static String BroadcastAddress = "not set";
    public static String MyIpAddress = "not set";
    public static String MyMACAddress = "not set";
    public static String MyIpNetmask = "24";
    public static String myID = "not set";
    public static String mySSID = "not set";
    public static String myBSSID = "not set";

    public static String MESSAGE_IS_RECEIVED_INTENT = "com.cherrycider.udpX.messageIsReceived";
    public static String CHAT_STATUS_IS_CHANGED = "com.cherrycider.udpX.chatStatusIsChanged";
    public static String CHAT_STATUS = "chatStatus";
    public static String MESSAGE_STRING = "MessageString";
    public static String PREFERENCES_INTENT = "com.cherrycider.udpX.preferences";
    public static String INTRO_INTENT = "com.cherrycider.udpX.intro";
    public static String MYDETAILS_INTENT = "com.cherrycider.udpX.mydetails";


    // объявляем BroadcastReceiver для приема интента с People onDestroy в переменных  - step1.1 с people
    BroadcastReceiver peopleIntentReceiver;

    // переменные для приема интента с People onDestroy - step1.2 с people

    public static String PPLLIST_STATUS_IS_CHANGED = "com.cherrycider.udpX.peopleStatusIsChanged";
    public static String PPLLIST_STATUS = "peopleStatus";
    public static String peopleFragmentStatus = "peopleIsOnCreateView";
    public static boolean peopleIsOnCreateView = true;


    public File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");


    private boolean isWiFiChangeReceiverRegistered = false;


    ArrayList<String> onLineIPAddresses = new ArrayList<>();
    ArrayList<String> onLineUsers = new ArrayList<>();


    //Context step 1 -перед onCreate в переменных
    public static Context MainContext;


    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();


    PeopleArrayAdapter peopleArrayAdapter;


    /**
     * переменные для Chat
     */
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatText;
    private boolean side = false;

    // переменные из PersonDetails.java для проверки или заполнено имя
    SharedPreferences myProfile;
    public static String myName;
    public static String moreInfo;
    public static String myPhoto;

    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 25;

    // объявляем BroadcastReceiver в переменных  - step1 для приема сообщений message
    BroadcastReceiver messageIntentReceiver;

    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    /**
     * идентификатор первого фрагмента.
     */
    public static final int FRAGMENT_ONE = 0;
    /**
     * идентификатор третего.
     */
    public static final int FRAGMENT_THREE = 2;
    /**
     * идентификатор второго.
     */
    public static final int FRAGMENT_TWO = 1;
    /**
     * количество фрагментов.
     */
    public static final int FRAGMENTS = 3;
    /**
     * адаптер фрагментов.
     */
    private FragmentPagerAdapter _fragmentPagerAdapter;
    /**
     * список фрагментов для отображения.
     */
    private final List<Fragment> _fragments = new ArrayList<Fragment>();
    /**
     * сам ViewPager который будет все это отображать.
     */
    private ViewPager _viewPager;

    // переменная для отображения пользователей только на активной странице

    public boolean PEOPLE_PAGE_SELECTED = false;
    public boolean PAGES_SCROLLED = false;

    /**
     * переменная для вывода текста
     */

    private static TextView statusTxtView;

    public static String receivedMessage;


    ///////////////////////
    ////  onCreate
    //////////////////////

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // устанавливаем ориентацию
        loadMyProfile();
        loadPreferences();
        setOrientation();

        // Пишем logcat в файл /sdcard/logcatDump.txt

        if (LOGCAT_DUMP == 1) {
            wiknot.LogCat();
        }

        /// убираем клавиатуру

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );






        // если не создана, создаем папку wiknot



        // запрашиваем разрешение начиная с  sdk 23+
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // TODO
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

                // MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        boolean success = false;
        if (!wiknotFolder.exists()) {

            //Toast.makeText(this, "Folder wiknot does NOT exist!", Toast.LENGTH_SHORT).show();
            success = wiknotFolder.mkdirs();

            // создание файла или директори нужно подтверждать бродкастом

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(wiknotFolder); //out is your output file
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);
            } else {


                sendBroadcast(
                        new Intent(Intent.ACTION_MEDIA_MOUNTED,
                                Uri.fromFile(wiknotFolder))
                );

            }

        }


        if (!success){
            //Log.d(TAG,"Folder wiknot is already created.");

            //Toast.makeText(this, "Folder wiknot is already exists or failed to created.", Toast.LENGTH_SHORT).show();


        }
        else{
            // Log.d(TAG,"Folder created!");

            //Toast.makeText(this, "Folder wiknot just created!", Toast.LENGTH_SHORT).show();
        }



        //Context 2 внутри onCreate
        MainActivity.MainContext = getApplicationContext();


        //получаем уникальный ID пользователя, если приложение запускается впервые
        myID = wiknot.InstallationID(MainActivity.MainContext);


        // если в профайле не заполнено имя, запускаем PersonDetails.java

        loadMyProfile();

        if (myName.equals("null") || myName.equals("") || myName.equals("firsttimetest")) {

            Intent goMyProfileIntent = new Intent(MYDETAILS_INTENT);
            startActivity(goMyProfileIntent);


        }


        // создаем объект для создания и управления версиями БД
        peopleDBHelper = new PeopleDBHelper(MainActivity.MainContext);


        // создаем BroadcastReceiver в onCreate  step 2 для приема сообщений message
        messageIntentReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                receivedMessage = intent.getExtras().getString(MESSAGE_STRING);
                //Log.d(TAG, "Arrived to Main: action = " + action);
                //Log.d(TAG, "Arrived to Main: Message = " + receivedMessage);

                //printReceivedChatMessage(receivedMessage);


                //appendStatus("<<< " + receivedMessage);

                handleRecivedMessage(receivedMessage);

            }
        };

        // создаем фильтр для BroadcastReceiver в onCreate  step 3  для приема сообщений message
        IntentFilter messageIntentFilter = new IntentFilter(MESSAGE_IS_RECEIVED_INTENT);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(messageIntentReceiver, messageIntentFilter);


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
                    printOnlinePeople();
                }
                //Log.d(TAG, "Arrived to MainActivity: action = " + action);
                //Log.d(TAG, "Arrived to MainActivity: peopleFragmentStatus = " + peopleFragmentStatus + "(" + peopleIsOnCreateView + ")");


            }
        };

        // создаем фильтр для BroadcastReceiver в onCreate  step 3  с people
        IntentFilter peopleIntentFilter = new IntentFilter(PPLLIST_STATUS_IS_CHANGED);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(peopleIntentReceiver, peopleIntentFilter);

        // запускаем пульс online
        //Intent onlinePulseIntent = new Intent(this, OnlinePulse.class);
        //startService(onlinePulseIntent);

        // запускаем  FileReceiverService
        //Intent fileReceiverServiceIntent = new Intent(this, FileReceiverService.class);
        //startService(fileReceiverServiceIntent);


        ////Start of Fragments handling
        // создаем фрагменты.
        _fragments.add(FRAGMENT_ONE, new ChatFragment());
        _fragments.add(FRAGMENT_TWO, new TitlePageFragment());
        _fragments.add(FRAGMENT_THREE, new PeopleFragment());

        // Настройка фрагментов, определяющих количество фрагментов, экраны и название.
        _fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public int getCount() {

                return FRAGMENTS;
            }

            @Override
            public Fragment getItem(final int position) {

                return _fragments.get(position);
            }


            // что это, удалить?

            @Override
            public CharSequence getPageTitle(final int position) {

                switch (position) {
                    case FRAGMENT_ONE:
                        return "Title One";
                    case FRAGMENT_TWO:
                        return "Title Two";
                    case FRAGMENT_THREE:
                        return "Title three";
                    default:
                        return null;
                }
            }
        };
        _viewPager = (ViewPager) findViewById(R.id.pager);
        _viewPager.setOffscreenPageLimit(3);
        _viewPager.setAdapter(_fragmentPagerAdapter);
        _viewPager.setCurrentItem(1);


        _viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                // TODO после поворота экрана список пустой, нужно двинуть экран
                PAGES_SCROLLED = true;
                if ((position == 2)&&(positionOffsetPixels == 0)) {
                    PEOPLE_PAGE_SELECTED = true;
                    Log.d(TAG, "MainActivity onPageScrolled -----position " + position + " positionOffset " + positionOffset + " positionOffsetPixels " + positionOffsetPixels);
                    redrawOnlineUsers();
                }
                else {PEOPLE_PAGE_SELECTED = false;}

            }

            @Override
            public void onPageSelected(int position) {
                // оптимизируем ----- рисуем printOnlinePeople только если выбрана эта страница


                if (position == 2) {


                    //Log.d(TAG, "MainActivity onPageSelected ---------------------------position = " + position);
                }




            }

            @Override
            public void onPageScrollStateChanged(int state) {

                //Log.d(TAG, "MainActivity onPageScrollStateChanged -------state = " + state);
                PAGES_SCROLLED = false;

            }
        });

        ////////////////////////////////////////////////////////////////////////////////
        //// END of FRAGMENTS HANDLING
        /////////////////////////////////////////////////////////////////////////////////


    }

    ////////////////////////////////////////////////////
    ////// End of onCreate
    ///////////////////////////////////////////////////


    // this method

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(this, R.string.thank_you, Toast.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    Toast.makeText(this, R.string.sorry_wi_knot_will_not_work_properly, Toast.LENGTH_SHORT).show();


                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        //appendStatus(currentStatus);

        // Log.d(TAG, "MainActivity onRestoreInstanceState = " + currentStatus);
    }


    @Override
    protected void onResume() {
        super.onResume();
        // Log.d(TAG, "MainActivity goes onResume()");
        loadMyProfile();
        loadPreferences();
        setOrientation();

        // убираем клавиатуру

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );

        //Register wifi state change  receiver in onResume method
        /**
         *
         */
        if (!isWiFiChangeReceiverRegistered) {
            isWiFiChangeReceiverRegistered = true;
            //context.registerReceiver(wiFiChangeReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
            registerReceiver(wiFiChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            // IntentFilter to wifi state change is "android.net.conn.CONNECTIVITY_CHANGE"
        }


    }


    protected void onPause() {
        super.onPause();

        /**
         *
         */


        if (isWiFiChangeReceiverRegistered) {
            isWiFiChangeReceiverRegistered = false;
            unregisterReceiver(wiFiChangeReceiver);

            // Log.d(TAG, "MainActivity goes onPause()");
        }

    }


    ///////////////////////////////
    ////METHODS
    ////////////////////

    /**
     * этот метод интерпретирует пришедшее сообщение и выполняет соответствующие действия
     */

    private void handleRecivedMessage(String msgToInterpret) {


        // принимаем сообщения, отвечаем на них и обновляем peopleTable


        if (msgToInterpret.contains("IAmOnline, ")) {


            // выделяем ip адрес  и добавляем его в список адресов online

            // расфасовываем сообщение с запятыми в массив msgToRowList
            ArrayList msgToRowList = new ArrayList(Arrays.asList(msgToInterpret.split(",")));

            String onLineIPAddress = String.valueOf(msgToRowList.get(3));
            //String onLineIPAddress = msgToInterpret.replace("IAmOnline, ","");

            // TODO проверить нужно ли паралельно onLineIPAddresses
            if (!thisIPAddressIsOnline(onLineIPAddress)) {
                onLineIPAddresses.add(onLineIPAddress);

            }

            /////////////////////////////////////////////
            // отсылаем на печать в online list

            addOrUpdateOnlinePeople(msgToInterpret);

        }

        // удаляем из массива onLineIPAddresses адреса участника после прихода IAmOFFline

        if (msgToInterpret.contains("IAmOFFline, ")) {

            /////////////////////////////////////////////
            // отсылаем на печать в online list
            addOrUpdateOnlinePeople(msgToInterpret);


        }


        //отвечаем на запрос статуса

        if (msgToInterpret.contains("tellYourStatusTo, ")) {


            // расфасовываем сообщение с запятыми в массив msgToRowList
            ArrayList msgToRowList = new ArrayList(Arrays.asList(msgToInterpret.split(",")));

            //Log.d(TAG, "tellYourStatusTo, from "+ msgToRowList.get(3));

            // отсылаем сообщение IAmOnline тому кто просит

            MyIpAddress = wiknot.getIpAddress();
            String preparedMessage = "IAmOnline, " + myName + "," + "online" + "," + MyIpAddress + "," + currentSSID() + "," + currentAP_MacAddress() + "," + myID + "," + moreInfo;
            wiknot.SendMessage(preparedMessage, String.valueOf(msgToRowList.get(3)));

            //Log.d(TAG, "IAmOnline sent to " + returnIPAddress);
            //Log.d(TAG, " с MainActivity на запрос послано IAmOnline," + preparedMessage);


            String returnIPAddress = String.valueOf(msgToRowList.get(3));
            //String returnIPAddress = msgToInterpret.replace("tellYourStatusTo, ","");
            if (!thisIPAddressIsOnline(returnIPAddress)) {
                onLineIPAddresses.add(returnIPAddress);

            }

            // отсылаем фото себя тому кто просит (здесь и в UdpReceiverService )

            wiknot.SendFile(wiknotFolder, myPhoto, myID, String.valueOf(msgToRowList.get(3)));
            //Log.d(TAG, " с MainActivity отсылаем фото себя тому кто просит " + myPhoto);
            //Log.d(TAG, " с MainActivity отсылаем фото на адрес " + String.valueOf(msgToRowList.get(3)));

            /////////////////////////////////////////////
            // отсылаем на печать в online list

            addOrUpdateOnlinePeople(msgToInterpret);

        }

        //вызываем опрос сети при получении sendStatusPoll при запуске UdpReceiverService
        if (msgToInterpret.contains("sendStatusPoll")) {

            /////////////////////////////////////////////
            // чистим  online list
            // TODO проверить нужно ли паралельно onLineIPAddresses
            onLineIPAddresses = new ArrayList<>();

            // перед sendStatusPoll во всех строчках таблицы peopleTable  меняем статус onlineStatusToDB offline

            setPeopleOffline();


            /////////////////////////////////////////////
            // отсылаем на печать в online list
            printOnlinePeople();


            // показываем на экране состояние подключения
            showSSID_name();
            // Log.d(TAG, "показываем showSSID_name");

            //запустить сканирование пользователей в сети и прогресс бар на 9 секунд
            //OnlinePollServiceFromMainActivityWithProgressbar scanPeople = new OnlinePollServiceFromMainActivityWithProgressbar();
            //scanPeople.execute();



            onlinePollServiceFromMainActivity();


            //sendStatusPoll();
        }


    }

    // метод запускает по кнопке rescan people
    public void rescanPeople (View view) {

        //redrawOnlineUsers();

        RescanBattonProgressbar rescanBatton = new RescanBattonProgressbar();
        rescanBatton.execute();


        Toast toast = Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.rescan_people_online), Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();


        //запустить сканирование пользователей в сети и прогресс бар на 9 секунд
        //OnlinePollServiceFromMainActivityWithProgressbar scanPeople = new OnlinePollServiceFromMainActivityWithProgressbar();
        //scanPeople.execute();


        onlinePollServiceFromMainActivity();




        //peopleArrayAdapter.notifyDataSetChanged();
        //redrawOnlineUsers();

    }

    // класс асинхронно запускает прогрессбар на 1 сек
    class RescanBattonProgressbar extends AsyncTask<Void, Void, Void> {

        ImageView rescanBtn;
        ProgressBar progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rescanBtn = (ImageView)findViewById(R.id.rescan);
            progress = (ProgressBar) findViewById(R.id.progress_people_scan);
            rescanBtn.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            //redrawOnlineUsers();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();

            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            rescanBtn.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);


            //TODO после смены ориентации экрана только мигает и затем опять исчезает ???
            redrawOnlineUsers();

        }
    }



    // класс асинхронно запускает прогрессбар на 9 сек
    class OnlinePollServiceFromMainActivityWithProgressbar extends AsyncTask<Void, Void, Void> {

        ImageView rescanBtn;
        ProgressBar progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            rescanBtn = (ImageView)findViewById(R.id.rescan);
            progress = (ProgressBar) findViewById(R.id.progress_people_scan);
            rescanBtn.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            //redrawOnlineUsers();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {





                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Log.d(TAG, "OnlinePollService начал  Poll 2 сек");
            sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
            //Log.d(TAG, "OnlinePollService закончил  Poll 2 сек");

            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Log.d(TAG, "OnlinePollService начал  Poll 3 сек");
            sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
            //Log.d(TAG, "OnlinePollService закончил  Poll 3 сек");

            try {
                TimeUnit.SECONDS.sleep(4);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Log.d(TAG, "OnlinePollService начал  Poll 4 сек");
            sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
            //Log.d(TAG, "OnlinePollService закончил  Poll 4 сек");











             /*
                                TimeUnit.SECONDS.sleep(9);
            } catch (InterruptedException e) {
                e.printStackTrace();
                }
                 */


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            rescanBtn.setVisibility(View.VISIBLE);
            progress.setVisibility(View.GONE);
            //redrawOnlineUsers();

        }
    }



    ////////////////////////////////////////////////////////////////////////////////
    //     2 метода из OnlinePollService -  переносим функционал в MainActivity
    ////////////////////////////////////////////////////////////////////////////////


    protected void onlinePollServiceFromMainActivity() {

        Thread t = new Thread(new Runnable() {
            public void run() {


                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Log.d(TAG, "OnlinePollService начал  Poll 2 сек");
                sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
                //Log.d(TAG, "OnlinePollService закончил  Poll 2 сек");

                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Log.d(TAG, "OnlinePollService начал  Poll 3 сек");
                sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
                //Log.d(TAG, "OnlinePollService закончил  Poll 3 сек");

                try {
                    TimeUnit.SECONDS.sleep(4);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //Log.d(TAG, "OnlinePollService начал  Poll 4 сек");
                sendOnlinePollFromSameThread(myName, mySSID, myBSSID, myID, moreInfo);
                //Log.d(TAG, "OnlinePollService закончил  Poll 4 сек");


            }

        });

        t.start();
    }


    public void sendOnlinePollFromSameThread(String name, String ssid, String bssid, String id, String moreinfo) {

        MyIpAddress = wiknot.getIpAddress();

        if (!(MyIpAddress == "not set")) {


            String myNetwork = (MyIpAddress + "/" + MyIpNetmask);

            //Log.d(TAG, "myNetwork is " + myNetwork);

            SubnetUtils utils = new SubnetUtils(myNetwork);
            String[] allIps = utils.getInfo().getAllAddresses();
            // allIps will contain all the ip addresses in the subnet

            //Log.d(TAG, "min IP " +  allIps[0]);
            //Log.d(TAG, "max IP " + allIps[allIps.length-1]);

            for (int i = 0; i < allIps.length; i++) {


                String preparedMessage = "tellYourStatusTo, " + name + "," + "online" + "," + MyIpAddress + "," + ssid + "," + bssid + "," + id + "," + moreinfo;
                wiknot.SendMessagefromSameThread(preparedMessage, allIps[i]);

                //Log.d(TAG, "OnlinePollService  " + preparedMessage + " to " + allIps[i]);

              /*  try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */
            }


        }
    }



    ////////////////////////////////////////////////////////////////////////////////
    //
    // конец
    // 2 метода из OnlinePollService -  переносим функционал в MainActivity
    ////////////////////////////////////////////////////////////////////////////////






    // этот метод проверяет, есть ли новый ip адрес в списке online ip адресов

    public boolean thisIPAddressIsOnline(String ipaddr) {
        for (int i = 0; i < (onLineIPAddresses.size()); i++) {

            if (onLineIPAddresses.get(i).equals(ipaddr)) {

                return (true);
            }

        }

        return (false);


    }


    /////////////////////////////////////////////
    // этот метод ставит изначально всем пользователям offline

    public void setPeopleOffline() {

        //listView = (ListView) findViewById(R.id.peoplelistview);

        // if (listView != null) {

        //    peopleArrayAdapter = new PeopleArrayAdapter(this, R.layout.useronline);
        //   listView.setAdapter(peopleArrayAdapter);


        // listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //}

        AsyncTask<Void, Void, Void> async_setPeopleOffline = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {


                // создаем объект для данных
                ContentValues cv = new ContentValues();

                // получаем данные из полей ввода


                // подключаемся к БД
                SQLiteDatabase db = peopleDBHelper.getWritableDatabase();

                // Log.d(TAG, "--- onCreateView -- печатаем  peopleTable в страницу people online : ---");
                // делаем запрос всех данных из таблицы peopleTable, получаем Cursor
                Cursor c = db.query("peopleTable", null, null, null, null, null, null);


                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (c.moveToFirst()) {


                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("_id");


                    do {


                        /////////////////////////////////////////////
                        // проставляем ВСЕМ пользователям Offline
                        //
                        // меняем поле onlineStatus на "offline"

                        //Log.d(TAG, "--- Insert in peopleTable: ---");
                        // подготовим данные для вставки в виде пар: наименование столбца - значение
                        cv.put("onlineStatus", "offline");
                        db.update("peopleTable", cv, "_id=?", new String[]{c.getString(idColIndex)});
                        // Log.d(TAG, "MainActivity setPeopleOffline() прописываем \"offline\" в строке = " +  c.getString(idColIndex) + " " +c.getString(nameColIndex) + "=" + c.getString(onlineStatusColIndex));


                    } while (c.moveToNext());
                } else
                    // Log.d(TAG, "0 rows");
                    c.close();
                return null;

            }
        };


        async_setPeopleOffline.execute();
    }


    // этот метод добавляет или обновдяет строки БД
    public void addOrUpdateOnlinePeople(String msg) {


        AsyncTask<String, Void, Void> async_addOrUpdateOnlinePeople = new AsyncTask<String, Void, Void>() {

            //@Override
            protected Void doInBackground(String... params) {

                //super.doInBackground(msg);

                String m = params[0];
                //Log.d(TAG, "MainActivity addOrUpdateOnlinePeople" + m);

                // расфасовываем сообщение с запятыми в массив msgToRowList
                ArrayList msgToRowList = new ArrayList(Arrays.asList(m.split(",")));

                // готовим для ввода в базу

                String nameDB = String.valueOf(msgToRowList.get(1));
                String onlineStatusDB = String.valueOf(msgToRowList.get(2));
                String IpAddressDB = String.valueOf(msgToRowList.get(3));
                String SSIDDB = String.valueOf(msgToRowList.get(4));
                String BSSIDDB = String.valueOf(msgToRowList.get(5));
                String userIDDB = String.valueOf(msgToRowList.get(6));
                String moreInfoDB = String.valueOf(msgToRowList.get(7));

                // в методе  printOnlinePeople(); добавляем заполнение всех полей

                // создаем объект для данных
                ContentValues cv = new ContentValues();

                // получаем данные из полей ввода


                // подключаемся к БД
                SQLiteDatabase db = peopleDBHelper.getWritableDatabase();


                //Log.d(TAG, "--- Insert in peopleTable: ---");
                // подготовим данные для вставки в виде пар: наименование столбца - значение

                cv.put("name", nameDB);
                cv.put("onlineStatus", onlineStatusDB);
                cv.put("IpAddress", IpAddressDB);
                cv.put("SSID", SSIDDB);
                cv.put("BSSID", BSSIDDB);
                cv.put("userID", userIDDB);
                cv.put("moreInfo", moreInfoDB);

                //  ДОБАВЛЯЕМ ИЛИ ОБНОВЛЯЕМ СТРОКУ ПОЛЬЗОВАТЕЛЯ (уникальный userID )

                // получаем номер строчки rowID того userID, который нужно переписать
                Cursor c = db.query("peopleTable", new String[]{"_id"}, "userID =?", new String[]{userIDDB}, null, null, null, null);
                if (c.moveToFirst()) //if the row exist then return the id and update
                {
                    int rowID = c.getInt(c.getColumnIndex("_id"));
                    db.update("peopleTable", cv, "_id=?", new String[]{Integer.toString(rowID)});
                    //Log.d(TAG, "row updated, ID = " + rowID);
                    //Log.d(TAG, "row updated " + nameToDB + onlineStatusToDB + IpAddressToDB + SSIDToDB + userIDToDB + moreInfoToDB);
                } else {

                    // если строчки не существует вставляем запись и получаем ее ID
                    long rowID = db.insert("peopleTable", null, cv);
                    //Log.d(TAG, "row inserted, ID = " + rowID);
                    //Log.d(TAG, "row inserted " + nameToDB + onlineStatusToDB + IpAddressToDB + SSIDToDB + SSIDToDB+ userIDToDB + moreInfoToDB);
                }
                return null;

            }

            protected void onPostExecute(Void result) {

                super.onPostExecute(result);

                //Log.d (TAG, "MainActivity addOrUpdateOnlinePeople отправляем на printOnlinePeople()" );
                printOnlinePeople();



            }
        };


        async_addOrUpdateOnlinePeople.execute(msg);

    }


    /////////////////////////////////////////////
    // этот метод отсылает на печать в online list

    public void printOnlinePeople() {


        // в async, выводим chatArrayAdapter.add(new ChatMessage(side,....
        AsyncTask<Void, Void, ArrayList<String>> async_printOnlinePeople = new AsyncTask<Void, Void, ArrayList<String>>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected ArrayList<String> doInBackground(Void... params) {

                // в результате на onPostExecute отправляем массив из готовых пользователей
                ArrayList<String> userOnlineList = new ArrayList<>();


                // подключаемся к БД
                SQLiteDatabase db = peopleDBHelper.getWritableDatabase();


                // Log.d(TAG, "--- onCreateView -- печатаем  peopleTable в страницу people online : ---");
                // делаем запрос всех данных из таблицы peopleTable, получаем Cursor
                Cursor c = db.query("peopleTable", null, null, null, null, null, null);

                // готовим сначала ONLINE пользователей
                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false

                if (c.moveToFirst()) {


                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("_id");
                    int nameColIndex = c.getColumnIndex("name");
                    int onlineStatusColIndex = c.getColumnIndex("onlineStatus");
                    int IpAddressColIndex = c.getColumnIndex("IpAddress");
                    int SSIDColIndex = c.getColumnIndex("SSID");
                    int BSSIDColIndex = c.getColumnIndex("BSSID");
                    int userIDColIndex = c.getColumnIndex("userID");
                    int moreInfoColIndex = c.getColumnIndex("moreInfo");


                    do {


                        /////////////////////////////////////////////
                        // ОТСЫЛАЕМ НА ПЕЧАТЬ ВСЮ ТАБЛИЦУ  КТО ONLINE  в userOnlineList


                        //  проверяем если people fragment активен


                        if (peopleIsOnCreateView) {


                            if (!(c.getString(nameColIndex).equals("developerspecialname"))  // проверяем если имя пользователя developerspecialname - его не видно в списке people online
                                    &&
                                    //  (
                                    //  (c.getString(BSSIDColIndex).equals(myBSSID))              // печатаем только пользователей в текущей сети
                                    //          ||
                                    (c.getString(onlineStatusColIndex).equals("online") // или всех online
                                            //           )

                                    )

                                //TODO добавим boolean myHOTSPOT и логику (если мы hotspot to печатаем всех с SSID wiknot.getWifiApConfiguration(this).SSID

                                    ) {


                                String userOnlineToPostExecute = c.getString(nameColIndex) + "," + c.getString(onlineStatusColIndex) + "," + c.getString(IpAddressColIndex) + "," + c.getString(SSIDColIndex) + "," + c.getString(BSSIDColIndex) + "," + c.getString(userIDColIndex) + "," + c.getString(moreInfoColIndex);
                                //userOnline = new UserOnline(c.getString(nameColIndex), c.getString(onlineStatusColIndex), c.getString(IpAddressColIndex), c.getString(SSIDColIndex), c.getString(BSSIDColIndex), c.getString(userIDColIndex), c.getString(moreInfoColIndex));
                               // Log.d(TAG, "MainActivity: добавляем в userOnlineList со статусом  userOnline  = " + userOnlineToPostExecute);

                                //собираеи всех пользователей в список для передачи в IU
                                userOnlineList.add(userOnlineToPostExecute);

                                // отправляем параметры userOnline на печать в UI onProgressUpdate
                                //publishProgress(userOnlineToPrint);

                                //peopleArrayAdapter.add(new UserOnline(c.getString(nameColIndex), c.getString(onlineStatusColIndex), c.getString(IpAddressColIndex), c.getString(SSIDColIndex), c.getString(BSSIDColIndex), c.getString(userIDColIndex), c.getString(moreInfoColIndex)));
                            }


                        }
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (c.moveToNext());


                } else
                    // Log.d(TAG, "0 rows");
                    c.close();


                // Log.d(TAG, "--- onCreateView -- печатаем  peopleTable в страницу people online : ---");
                // делаем запрос всех данных из таблицы peopleTable, получаем Cursor
                c = db.query("peopleTable", null, null, null, null, null, null);

                // затем добавляем OFFLINE пользователей
                // ставим позицию курсора опять на первую строку выборки
                // если в выборке нет строк, вернется false

                if (c.moveToFirst()) {


                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("_id");
                    int nameColIndex = c.getColumnIndex("name");
                    int onlineStatusColIndex = c.getColumnIndex("onlineStatus");
                    int IpAddressColIndex = c.getColumnIndex("IpAddress");
                    int SSIDColIndex = c.getColumnIndex("SSID");
                    int BSSIDColIndex = c.getColumnIndex("BSSID");
                    int userIDColIndex = c.getColumnIndex("userID");
                    int moreInfoColIndex = c.getColumnIndex("moreInfo");


                    do {


                        /////////////////////////////////////////////
                        // ДОБАВЛЯЕМ В  ТАБЛИЦУ кто OFFLINE в userOnlineList


                        //  проверяем если people fragment активен


                        if (peopleIsOnCreateView) {


                            if (!(c.getString(nameColIndex).equals("developerspecialname"))  // проверяем если имя пользователя developerspecialname - его не видно в списке people online
                                    &&
                                    (
                                            (c.getString(BSSIDColIndex).equals(myBSSID))              // печатаем только пользователей в текущей сети
                                                    &&
                                                    (c.getString(onlineStatusColIndex).equals("offline") // и всех offline
                                                    )

                                    )


                                    ) {


                                String userOfflineToPostExecute = c.getString(nameColIndex) + "," + c.getString(onlineStatusColIndex) + "," + c.getString(IpAddressColIndex) + "," + c.getString(SSIDColIndex) + "," + c.getString(BSSIDColIndex) + "," + c.getString(userIDColIndex) + "," + c.getString(moreInfoColIndex);
                                //userOnline = new UserOnline(c.getString(nameColIndex), c.getString(onlineStatusColIndex), c.getString(IpAddressColIndex), c.getString(SSIDColIndex), c.getString(BSSIDColIndex), c.getString(userIDColIndex), c.getString(moreInfoColIndex));
                                //Log.d(TAG, "MainActivity: добавляем в userOnlineList со статусом userOffline  = " + userOfflineToPostExecute);

                                //собираеи вконце всех offline пользователей в список для передачи в IU
                                userOnlineList.add(userOfflineToPostExecute);

                                // отправляем параметры userOnline на печать в UI onProgressUpdate
                                //publishProgress(userOnlineToPrint);

                                //peopleArrayAdapter.add(new UserOnline(c.getString(nameColIndex), c.getString(onlineStatusColIndex), c.getString(IpAddressColIndex), c.getString(SSIDColIndex), c.getString(BSSIDColIndex), c.getString(userIDColIndex), c.getString(moreInfoColIndex)));
                            }


                        }
                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (c.moveToNext());


                } else
                    // Log.d(TAG, "0 rows");
                    c.close();


                // в результате на onPostExecute отправляем массив из готовых пользователей


                return userOnlineList;

            }


            //@Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);


            }

            protected void onPostExecute(ArrayList<String> result) {

                super.onPostExecute(result);

                // распечатываем список пользователей который пришел как result
                onLineUsers = result;

                if (peopleIsOnCreateView) {
                    ////////////////////////////
                    /////list part взят из  onCreateView
                    ////////////////////////////  может вынести наверх ??
                    listView = (ListView) findViewById(R.id.peoplelistview);

                    if (listView != null) {

                        if (listView.getAdapter()==null) {
                            // создаем список
                            peopleArrayAdapter = new PeopleArrayAdapter(MainActivity.MainContext, R.layout.useronline);

                            listView.setAdapter(peopleArrayAdapter);

                            //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);

                            // TODO проверить peopleArrayAdapter.getCount() на null
                            // Log.d(TAG, "Main Activity peopleArrayAdapter.getCount() = " + peopleArrayAdapter.getCount()); // не работает
                            //if (peopleArrayAdapter!=null){      // не работает


                            // TODO отображение списка переходит в конец списка - нужно оставлять фокус где был
                            // listView.setSelection(peopleArrayAdapter.getCount() - 1);


                            //Log.d(TAG, "MainActivity: printOnlinePeople() onPostExecute условие (PEOPLE_PAGE_SELECTED&&(!PAGES_SCROLLED)) = " + (PEOPLE_PAGE_SELECTED && (!PAGES_SCROLLED)));

                            for (int i = 0; i < (onLineUsers.size()); i++) {

                                // проверяем что страницы не листаются при печати
                                if (PEOPLE_PAGE_SELECTED
                                        && (!PAGES_SCROLLED)
                                        ) {

                                    // расфасовываем сообщение с запятыми в массив userOnlineArray
                                    ArrayList userOnlineArray = new ArrayList(Arrays.asList(onLineUsers.get(i).split(",")));
                                    //Log.d(TAG, "MainActivity: printOnlinePeople() onPostExecute печатаем " + onLineUsers.get(i));
                                    peopleArrayAdapter.add(new UserOnline(String.valueOf(userOnlineArray.get(0)), String.valueOf(userOnlineArray.get(1)), String.valueOf(userOnlineArray.get(2)), String.valueOf(userOnlineArray.get(3)), String.valueOf(userOnlineArray.get(4)), String.valueOf(userOnlineArray.get(5)), String.valueOf(userOnlineArray.get(6))));

                                }

                            }
                        }else {


                            peopleArrayAdapter.clear();

                            peopleArrayAdapter.notifyDataSetChanged();
                            //listView.setAdapter(null);


                            //peopleArrayAdapter.remove(peopleArrayAdapter.getItem(0));

                            //Log.d(TAG, "MainActivity: redrawOnlineUsers - кол-во записей пользователей  " + peopleArrayAdapter.getCount());


                            for (int i = 0; i < (onLineUsers.size()); i++) {

                                // проверяем что страницы не листаются при печати
                                if (PEOPLE_PAGE_SELECTED
                                        && (!PAGES_SCROLLED)
                                        ) {

                                    // расфасовываем сообщение с запятыми в массив userOnlineArray
                                    ArrayList userOnlineArray = new ArrayList(Arrays.asList(onLineUsers.get(i).split(",")));
                                    //Log.d(TAG, "MainActivity: printOnlinePeople() onPostExecute печатаем " + onLineUsers.get(i));
                                    peopleArrayAdapter.add(new UserOnline(String.valueOf(userOnlineArray.get(0)), String.valueOf(userOnlineArray.get(1)), String.valueOf(userOnlineArray.get(2)), String.valueOf(userOnlineArray.get(3)), String.valueOf(userOnlineArray.get(4)), String.valueOf(userOnlineArray.get(5)), String.valueOf(userOnlineArray.get(6))));

                                }

                            }
                            peopleArrayAdapter.notifyDataSetChanged();
                        }


                        //Log.d(TAG, "MainActivity: printOnlinePeople() напечатали --------- ");


                    }

                }

            }
        };


        async_printOnlinePeople.execute();

    }

    public void redrawOnlineUsers() {

        // Проверяем  что  именно people fragment page selected
        if (peopleIsOnCreateView) {


            ////////////////////////////
            /////list part взят из  onCreateView
            ////////////////////////////  может вынести наверх ??
            listView = (ListView) findViewById(R.id.peoplelistview);

            if (listView != null) {

                if (listView.getAdapter()==null) {

                    // чистим список
                    peopleArrayAdapter = new PeopleArrayAdapter(MainActivity.MainContext, R.layout.useronline);
                    listView.setAdapter(peopleArrayAdapter);


                    //listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                    listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_DISABLED);

                    // TODO проверить peopleArrayAdapter.getCount() на null
                    // Log.d(TAG, "Main Activity peopleArrayAdapter.getCount() = " + peopleArrayAdapter.getCount()); // не работает
                    //if (peopleArrayAdapter!=null){      // не работает


                    // TODO отображение списка переходит в конец списка - нужно оставлять фокус где был
                    // listView.setSelection(peopleArrayAdapter.getCount() - 1);


                    for (int i = 0; i < (onLineUsers.size()); i++) {

                        // проверяем что страницы не листаются при печати
                        // if (PEOPLE_PAGE_SELECTED) {

                        // расфасовываем сообщение с запятыми в массив userOnlineArray
                        ArrayList userOnlineArray = new ArrayList(Arrays.asList(onLineUsers.get(i).split(",")));
                        //Log.d(TAG, "MainActivity: redrawOnlineUsers печатаем " + onLineUsers.get(i));
                        peopleArrayAdapter.add(new UserOnline(String.valueOf(userOnlineArray.get(0)), String.valueOf(userOnlineArray.get(1)), String.valueOf(userOnlineArray.get(2)), String.valueOf(userOnlineArray.get(3)), String.valueOf(userOnlineArray.get(4)), String.valueOf(userOnlineArray.get(5)), String.valueOf(userOnlineArray.get(6))));

                        // }
                    }

                } else {

                    peopleArrayAdapter.clear();

                    peopleArrayAdapter.notifyDataSetChanged();
                    //listView.setAdapter(null);


                    //peopleArrayAdapter.remove(peopleArrayAdapter.getItem(0));

                    //Log.d(TAG, "MainActivity: redrawOnlineUsers - кол-во записей пользователей  " + peopleArrayAdapter.getCount());


                    for (int i = 0; i < (onLineUsers.size()); i++) {

                        // проверяем что страницы не листаются при печати
                        // if (PEOPLE_PAGE_SELECTED) {

                        // расфасовываем сообщение с запятыми в массив userOnlineArray
                        ArrayList userOnlineArray = new ArrayList(Arrays.asList(onLineUsers.get(i).split(",")));
                        //Log.d(TAG, "MainActivity: redrawOnlineUsers печатаем " + onLineUsers.get(i));
                        peopleArrayAdapter.add(new UserOnline(String.valueOf(userOnlineArray.get(0)), String.valueOf(userOnlineArray.get(1)), String.valueOf(userOnlineArray.get(2)), String.valueOf(userOnlineArray.get(3)), String.valueOf(userOnlineArray.get(4)), String.valueOf(userOnlineArray.get(5)), String.valueOf(userOnlineArray.get(6))));

                       // }
                    }
                    peopleArrayAdapter.notifyDataSetChanged();


                }   //Log.d(TAG, "MainActivity: redrawOnlineUsers() напечатали --------- ");
            }

        }
    }

    /**
     * этот метод останавливает сервис приема UDP пакетов
     */

    public void stopUdpReceiverService() {
        stopService(new Intent(this, UdpReceiverService.class));

        // убираем значек online с панели
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(9999);
    }


    /**
     * этот метод останавливает сервис приема файлов
     */
    public void stopFileReceiverService() {
        stopService(new Intent(this, FileReceiverService.class));


    }


    /**
     * этот метод останавливает сервис OnlinePollService
     *
    public void stopOnlinePollService() {
        stopService(new Intent(this, OnlinePollService.class));


    }

    */

    /**
     * этот метод останавливает сервис OnlinePulse
     */
    public void stopOnlinePulse() {
        stopService(new Intent(this, OnlinePulse.class));


    }


    // Create a wiFiChangeReceiver that has to run on receiving WiFi state change

    /**
     *
     */
    private BroadcastReceiver wiFiChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            //Log.e("Trigger", "getNetworkInfo");
            //  Log.d(TAG, "---- WiFi состояние поменялось ----");



            // TODO здесь запускаем Beacon


            // запускаем сервис UdpReceiverService при каждом изменении в сети wifi
            sendBroadcast(new Intent(MainActivity.MainContext, UdpReceiverServiceWakefulBroadcastReceiver.class));

            // запускаем сервис FileReceiverService при каждом изменении в сети wifi
            sendBroadcast(new Intent(MainActivity.MainContext, FileReceiverServiceWakefulBroadcastReceiver.class));


        }
    };


    /**
     * этот метод определяет подключен ли wifi, HOTSPOT или другая сеть
     */

    public boolean connectionIsSet() {


        MyIpAddress = wiknot.getIpAddress();

        if (!(MyIpAddress == "not set")) {
            return true;
        } else {

            Toast toast = Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.check_your_WiFI_connection), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return false;
        }


    }




    // Показываем имя сети под логотипом

    public void showSSID_name() {


        MyIpAddress = wiknot.getIpAddress();


        TextView textView = (TextView) findViewById(R.id.ssid);
        ImageView hotspot = (ImageView)findViewById(R.id.hotspot);

        if ((currentAP_MacAddress() == null) ||
                (currentAP_MacAddress() == "00:00:00:00:00:00") ||
                (currentSSID() == "0x") ||
                (currentSSID() == "<unknown ssid>")) {

            if (MyIpAddress == "not set") {
                hotspot.setVisibility(View.GONE);
                textView.setTextSize(22);
                textView.setText(getBaseContext().getString(R.string.check_your_WiFi_connection));

            } else {
                textView.setText(wiknot.getWifiApConfiguration(this).SSID);
                hotspot.setVisibility(View.VISIBLE);
            }

        } else {
            if (currentSSID() == null) {
                textView.setText(wiknot.getWifiApConfiguration(this).SSID);
                hotspot.setVisibility(View.VISIBLE);
            } else {
                hotspot.setVisibility(View.GONE);
                textView.setText(currentSSID().replace("\"", ""));
            }
        }
        myBSSID = currentAP_MacAddress();
        mySSID = currentSSID();
    }


    /**
     * The following methods will return the MAC address and SSID of the access point,
     * null if there is no network currently connected
     */
    public String currentAP_MacAddress() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();

    }

    public String currentSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }


//////////////////////////////////////////////////////////////////////////////////////

    /**
     * The following method prints test info on clicking LOGO BUTTON
     */
////////////////////////////////////////////////////////////////////////////////////////
    public void showBSSID(View view) {

        appendStatus("BSSID: " + myBSSID);

        appendStatus("IP: " + MyIpAddress + "/" + MyIpNetmask);

        MyMACAddress = wiknot.getMacAddress(MainActivity.MainContext);

        appendStatus("MAC: " + MyMACAddress);

        appendStatus("myID: " + myID);

        appendStatus("my directory mounted : " + Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED));

        appendStatus("myPath: " + wiknotFolder);

        appendStatus("myFile: " + myPhoto);

        appendStatus("my HOTSPOT SSID: " + wiknot.getWifiApConfiguration(this).SSID);

        //onLineIPAddresses = wiknot.allCurrentUserIPAddresses();
       // for (int i = 0; i < (onLineIPAddresses.size()); i++) {

       //     appendStatus("SendFile to " + onLineIPAddresses.get(i));
      //      wiknot.SendFile(
       //             wiknotFolder,
       //             myPhoto,
       //             myID,
       //             onLineIPAddresses.get(i)
       //     );

       // }


    }


    /**
     * The following method adds the header and sends the edited message from Chat
     */

    public void udpSendFromChat(View view) {
        connectionIsSet();

        EditText editText = (EditText) findViewById(R.id.msgFromChat);
        String m = editText.getText().toString();
        // проверяем чтобы строка небыла пуста
        if (!m.equals("")) {

            //подменяем все запятые на |comma|
            m = m.replace(",", "|comma|");

            // add header toChatForAll and return IP address

            //добавляем "toChatForAll, name, userID, time, Ipaddress, messqage"
            //String toChatForAllMessage = "toChatForAll, " + myName + "," + myID + "," + "sentTime" + "," + "ssid" + ","+  MyIpAddress + "," + m;
            String toChatForAllMessage = "toChatForAll, " + myName + "," + myID + "," + wiknot.getTimeAndDate() + "," + currentSSID() + "," + MyIpAddress + "," + m;

            //Log.d(TAG, "сообщение послано по кнопке send с MainActivity toChatForAll, " + m);

            onLineIPAddresses = wiknot.allCurrentUserIPAddresses();
            for (int i = 0; i < (onLineIPAddresses.size()); i++) {

                wiknot.SendMessage(toChatForAllMessage, onLineIPAddresses.get(i));


            }

            // после отправления чистим строку чтобы не отправлялось повторно при повторном нажатии

            if (connectionIsSet()) {
                editText.setText("");
            }

        }

    }




    /**
     * добавляем в строчку статус
     */


    public void appendStatus(String status) {
        statusTxtView = (TextView) findViewById(R.id.status_text);
        if (statusTxtView != null) {
            currentStatus = statusTxtView.getText().toString();
        }
        if ((statusTxtView != null) || (currentStatus != null) || (status != null)) {
            statusTxtView.setText(currentStatus + "\n" + status);
        }

    }

    /**
     * The following method opens Chat
     */

    public void goChat(View view) {


        _viewPager = (ViewPager) findViewById(R.id.pager);
        _viewPager.setAdapter(_fragmentPagerAdapter);
        _viewPager.setCurrentItem(0);

        //Log.d(TAG, "запущено окно чата");

    }

    // Этот метод запускает окно PopUp
    public void goPopUp(View view) {
        //////This invokes PopUp Activity


        Intent intent = new Intent(MESSAGE_IS_RECEIVED_INTENT);
        startActivity(intent);

    }



    // Этот метод запускает просмотр Интро

    public void goIntro(View view) {


        onBackPressed();


        Intent intent = new Intent(INTRO_INTENT);
        startActivity(intent);
        //Log.d(TAG, "запущено окно WelcomeIntro");

    }



    // Этот метод запускает окно PersonDetails
    public void goMyProfile(View view) {
        //////This invokes PersonDetailes Activity

        Intent goMyProfileIntent = new Intent(MYDETAILS_INTENT);
        startActivity(goMyProfileIntent);

    }


    // Этот метод запускает окно Preferences

    public void goPreferences(View view) {


        Intent intent = new Intent(PREFERENCES_INTENT);
        startActivity(intent);
        //Log.d(TAG, "запущено окно preferences");

    }





    /**
     * the following methods changes the screen orientation
     */

    public void setOrientation() {
        if (SCREEN_ORIENTATION == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        if (SCREEN_ORIENTATION == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        if (SCREEN_ORIENTATION == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }


    /**
     * the following method exits application
     */
    public void goExit(View view) {
        //UDP_Server Server = new UDP_Server();
        //Server.stop_UDP_Server();
        //Log.d(TAG, "UDP Server остановлен");


        // выключаем OnlinePulse
        stopOnlinePulse();
        //Log.d(TAG, "OnlinePulse выключен");


        for (int i = 0; i < (onLineIPAddresses.size()); i++) {

            String preparedMessage = "IAmOFFline, " + myName + "," + "offline" + "," + MyIpAddress + "," + currentSSID() + "," + currentAP_MacAddress() + "," + myID + "," + moreInfo;
            wiknot.SendMessage(preparedMessage, onLineIPAddresses.get(i));
            //wiknot.SendMessage("IAmOFFline, " + MyIpAddress, onLineIPAddresses.get(i));
            //Log.d(TAG, "MainActivity SendMessage " + preparedMessage + " to " + onLineIPAddresses.get(i));
        }


        // выключаем UdpReceiverService
        stopUdpReceiverService();
        //Log.d(TAG, "UdpReceiverService выключен");

        // выключаем UdpReceiverService
        stopFileReceiverService();
        //Log.d(TAG, "UdpReceiverService выключен");

        // выключаем OnlinePollService
        //stopOnlinePollService();
        //Log.d(TAG, "OnlinePollService выключен");

        // выключаем OnlinePulse
        stopOnlinePulse();
        //Log.d(TAG, "OnlinePulse выключен");

        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(messageIntentReceiver);
        //Log.d(TAG, "BroadcastReceiver выключен");

        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(peopleIntentReceiver);
        //Log.d(TAG, "BroadcastReceiver выключен");

        //UdpReceiver.stop_UDP_Server();
        //Log.d(TAG, "UdpReceiver выключен");

        //Log.d(TAG, "Wi Knot остановлен");
        finish();
        System.exit(0);
        //Log.d(TAG, "Wi Knot остановлен");
    }

    void loadPreferences() {
        sPref = getSharedPreferences("wiknot_preferences", MODE_PRIVATE);

        String orientationSavedText = sPref.getString("Screen orientation", String.valueOf(SCREEN_ORIENTATION));
        SCREEN_ORIENTATION = Integer.parseInt(orientationSavedText);

        String logcatDumpSavedText = sPref.getString("Logcat Dump", String.valueOf(LOGCAT_DUMP));
        LOGCAT_DUMP = Integer.parseInt(logcatDumpSavedText);

        String notificationSoundSavedText = sPref.getString("Notification Sound", String.valueOf(NOTIFICATION_SOUND));
        NOTIFICATION_SOUND = Integer.parseInt(notificationSoundSavedText);


        //Toast.makeText(this, "Preferences loaded", Toast.LENGTH_SHORT).show();
    }

    static public boolean publicChatNotifEnabled() {


        boolean play = false;
        if (NOTIFICATION_SOUND != 0) {
            play = true;
        }
        return play;
    }

    void loadMyProfile() {
        myProfile = getSharedPreferences("myProfile", MODE_PRIVATE);

        myName = myProfile.getString("myName", String.valueOf(myName));
        moreInfo = myProfile.getString("moreInfo", String.valueOf(moreInfo));
        myPhoto = myProfile.getString("myPhoto", String.valueOf(myPhoto));


        //Toast.makeText(this, "My Profile loaded", Toast.LENGTH_SHORT).show();
    }


    // При нажатии клавиши НАЗАД если открыто меню - закрываем
    @Override
    public void onBackPressed() {

        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        int visibility = menu6_layout.getVisibility();
        if (visibility == View.VISIBLE) {

            menu6_layout.setVisibility(View.GONE);
            menu6.setImageResource(R.drawable.menu_wite);
            return;

        } else super.onBackPressed();
    }


    ///////////////////////////////////////////////////////
    //////////////
    //////////////  обработка клавиш и MENU в TitlePage
    /////////////
    ////////////////////////////////////////////////////////


    /**
     * The method called when menu 1 pressed
     */
    public void show_menu_1(View view) {


        LinearLayout menu1_layout = (LinearLayout) findViewById(R.id.menu1_layout);
        LinearLayout menu2_layout = (LinearLayout) findViewById(R.id.menu2_layout);
        LinearLayout menu3_layout = (LinearLayout) findViewById(R.id.menu3_layout);
        LinearLayout menu4_layout = (LinearLayout) findViewById(R.id.menu4_layout);
        LinearLayout menu5_layout = (LinearLayout) findViewById(R.id.menu5_layout);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        ImageButton menu1 = (ImageButton) findViewById(R.id.menu_1);
        ImageButton menu2 = (ImageButton) findViewById(R.id.menu_2);
        // ImageButton menu3 = (ImageButton) findViewById(R.id.menu_3);
        ImageButton menu4 = (ImageButton) findViewById(R.id.menu_4);
        ImageButton menu5 = (ImageButton) findViewById(R.id.menu_5);
        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);


        /**
         * Patch for GXV
         *

         //menu1.setBackgroundResource(R.color.colorWite);
         menu2.setBackgroundResource(R.color.colorWite);
         // menu3.setBackgroundResource(R.color.colorWite);
         menu4.setBackgroundResource(R.color.colorWite);
         menu5.setBackgroundResource(R.color.colorWite);
         menu6.setBackgroundResource(R.color.colorWite);

         /**
         * End of Patch for GXV
         */


        int visibility = menu1_layout.getVisibility();
        if (visibility == View.GONE) {
            menu1_layout.setVisibility(View.VISIBLE);
            menu1.setImageResource(R.drawable.people_tag);


            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }


        if (visibility == View.VISIBLE) {
            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }
    }

    /**
     * The method called when menu 2 pressed
     */
    public void show_menu_2(View view) {
        LinearLayout menu1_layout = (LinearLayout) findViewById(R.id.menu1_layout);
        LinearLayout menu2_layout = (LinearLayout) findViewById(R.id.menu2_layout);
        LinearLayout menu3_layout = (LinearLayout) findViewById(R.id.menu3_layout);
        LinearLayout menu4_layout = (LinearLayout) findViewById(R.id.menu4_layout);
        LinearLayout menu5_layout = (LinearLayout) findViewById(R.id.menu5_layout);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        ImageButton menu1 = (ImageButton) findViewById(R.id.menu_1);
        ImageButton menu2 = (ImageButton) findViewById(R.id.menu_2);
        // ImageButton menu3 = (ImageButton) findViewById(R.id.menu_3);
        ImageButton menu4 = (ImageButton) findViewById(R.id.menu_4);
        ImageButton menu5 = (ImageButton) findViewById(R.id.menu_5);
        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);

        /**
         * Patch for GXV
         *

         menu1.setBackgroundResource(R.color.colorWite);
         menu2.setBackgroundResource(R.color.colorWite);
         //menu3.setBackgroundResource(R.color.colorWite);
         menu4.setBackgroundResource(R.color.colorWite);
         menu5.setBackgroundResource(R.color.colorWite);
         menu6.setBackgroundResource(R.color.colorWite);

         /**
         * End of Patch for GXV
         */


        int visibility = menu2_layout.getVisibility();
        if (visibility == View.GONE) {
            menu2_layout.setVisibility(View.VISIBLE);
            menu2.setImageResource(R.drawable.folders_tag);

            menu1_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);

            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }

        if (visibility == View.VISIBLE) {
            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }
    }


    /**
     * The method called when menu 4 pressed
     */
    public void show_menu_4(View view) {
        LinearLayout menu1_layout = (LinearLayout) findViewById(R.id.menu1_layout);
        LinearLayout menu2_layout = (LinearLayout) findViewById(R.id.menu2_layout);
        LinearLayout menu3_layout = (LinearLayout) findViewById(R.id.menu3_layout);
        LinearLayout menu4_layout = (LinearLayout) findViewById(R.id.menu4_layout);
        LinearLayout menu5_layout = (LinearLayout) findViewById(R.id.menu5_layout);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        ImageButton menu1 = (ImageButton) findViewById(R.id.menu_1);
        ImageButton menu2 = (ImageButton) findViewById(R.id.menu_2);
        // ImageButton menu3 = (ImageButton) findViewById(R.id.menu_3);
        ImageButton menu4 = (ImageButton) findViewById(R.id.menu_4);
        ImageButton menu5 = (ImageButton) findViewById(R.id.menu_5);
        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);

        /**
         * Patch for GXV
         *

         menu1.setBackgroundResource(R.color.colorWite);
         menu2.setBackgroundResource(R.color.colorWite);
         //menu3.setBackgroundResource(R.color.colorWite);
         menu4.setBackgroundResource(R.color.colorWite);
         menu5.setBackgroundResource(R.color.colorWite);
         menu6.setBackgroundResource(R.color.colorWite);

         /**
         * End of Patch for GXV
         */


        int visibility = menu4_layout.getVisibility();
        if (visibility == View.GONE) {
            menu4_layout.setVisibility(View.VISIBLE);
            menu4.setImageResource(R.drawable.service_tag);

            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);

            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }

        if (visibility == View.VISIBLE) {
            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }
    }


    /**
     * The method called when menu 5 pressed
     */
    public void show_menu_5(View view) {
        LinearLayout menu1_layout = (LinearLayout) findViewById(R.id.menu1_layout);
        LinearLayout menu2_layout = (LinearLayout) findViewById(R.id.menu2_layout);
        LinearLayout menu3_layout = (LinearLayout) findViewById(R.id.menu3_layout);
        LinearLayout menu4_layout = (LinearLayout) findViewById(R.id.menu4_layout);
        LinearLayout menu5_layout = (LinearLayout) findViewById(R.id.menu5_layout);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        ImageButton menu1 = (ImageButton) findViewById(R.id.menu_1);
        ImageButton menu2 = (ImageButton) findViewById(R.id.menu_2);
        // ImageButton menu3 = (ImageButton) findViewById(R.id.menu_3);
        ImageButton menu4 = (ImageButton) findViewById(R.id.menu_4);
        ImageButton menu5 = (ImageButton) findViewById(R.id.menu_5);
        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);

        /**
         * Patch for GXV
         *

         menu1.setBackgroundResource(R.color.colorWite);
         menu2.setBackgroundResource(R.color.colorWite);
         //menu3.setBackgroundResource(R.color.colorWite);
         menu4.setBackgroundResource(R.color.colorWite);
         menu5.setBackgroundResource(R.color.colorWite);
         menu6.setBackgroundResource(R.color.colorWite);

         /**
         * End of Patch for GXV
         */


        int visibility = menu5_layout.getVisibility();
        if (visibility == View.GONE) {
            menu5_layout.setVisibility(View.VISIBLE);
            menu5.setImageResource(R.drawable.event_baloon_tag);

            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);

            menu6.setImageResource(R.drawable.menu_wite);
        }

        if (visibility == View.VISIBLE) {
            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }
    }


    /**
     * The method called when menu 6 pressed
     */
    public void show_menu_6(View view) {
        LinearLayout menu1_layout = (LinearLayout) findViewById(R.id.menu1_layout);
        LinearLayout menu2_layout = (LinearLayout) findViewById(R.id.menu2_layout);
        LinearLayout menu3_layout = (LinearLayout) findViewById(R.id.menu3_layout);
        LinearLayout menu4_layout = (LinearLayout) findViewById(R.id.menu4_layout);
        LinearLayout menu5_layout = (LinearLayout) findViewById(R.id.menu5_layout);
        LinearLayout menu6_layout = (LinearLayout) findViewById(R.id.menu6_layout);
        ImageButton menu1 = (ImageButton) findViewById(R.id.menu_1);
        ImageButton menu2 = (ImageButton) findViewById(R.id.menu_2);
        // ImageButton menu3 = (ImageButton) findViewById(R.id.menu_3);
        ImageButton menu4 = (ImageButton) findViewById(R.id.menu_4);
        ImageButton menu5 = (ImageButton) findViewById(R.id.menu_5);
        ImageButton menu6 = (ImageButton) findViewById(R.id.menu_6);

        /**
         * Patch for GXV
         *

         menu1.setBackgroundResource(R.color.colorWite);
         menu2.setBackgroundResource(R.color.colorWite);
         //menu3.setBackgroundResource(R.color.colorWite);
         menu4.setBackgroundResource(R.color.colorWite);
         menu5.setBackgroundResource(R.color.colorWite);
         menu6.setBackgroundResource(R.color.colorWite);

         /**
         * End of Patch for GXV
         */


        int visibility = menu6_layout.getVisibility();
        if (visibility == View.GONE) {
            menu6_layout.setVisibility(View.VISIBLE);
            menu6.setImageResource(R.drawable.menu_tag);
            ;

            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
        }

        if (visibility == View.VISIBLE) {
            menu1_layout.setVisibility(View.GONE);
            menu2_layout.setVisibility(View.GONE);
            menu3_layout.setVisibility(View.GONE);
            menu4_layout.setVisibility(View.GONE);
            menu5_layout.setVisibility(View.GONE);
            menu6_layout.setVisibility(View.GONE);

            menu1.setImageResource(R.drawable.people_wite);
            menu2.setImageResource(R.drawable.folders_wite);
            menu4.setImageResource(R.drawable.service_wite);
            menu5.setImageResource(R.drawable.event_baloon_wite);
            menu6.setImageResource(R.drawable.menu_wite);
        }
    }


    ///////////////////////////////////////////////////////
    //////////////
    //////////////  MENU в People Fragment
    /////////////
    ////////////////////////////////////////////////////////

    public void showFriendKnotsOnline(View view) {

        ImageButton friend_knots = (ImageButton) findViewById(R.id.friend_knots);
        ImageButton friend_knots_menu = (ImageButton) findViewById(R.id.friends_online);
        TextView list_of_friend_knots = (TextView) findViewById(R.id.list_of_friend_knots);

        ImageButton folders = (ImageButton) findViewById(R.id.folders);
        ImageButton folders_menu = (ImageButton) findViewById(R.id.folders_online);
        TextView list_of_folders = (TextView) findViewById(R.id.list_of_folders);

        ImageButton service_knots = (ImageButton) findViewById(R.id.service_knots);
        ImageButton service_knots_menu = (ImageButton) findViewById(R.id.service_online);
        TextView list_of_service_knots = (TextView) findViewById(R.id.list_of_service_knots);

        ImageButton events = (ImageButton) findViewById(R.id.events);
        ImageButton events_menu = (ImageButton) findViewById(R.id.events_online);
        TextView list_of_events = (TextView) findViewById(R.id.list_of_events);

        int visibility = friend_knots.getVisibility();
        if (visibility == View.GONE) {
            friend_knots.setVisibility(View.VISIBLE);
            list_of_friend_knots.setVisibility(View.VISIBLE);
            friend_knots_menu.setImageResource(R.drawable.people_tag);


            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);

        } else if (visibility == View.VISIBLE) {

            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);


        }


    }


    public void showFoldersOnline(View view) {

        ImageButton friend_knots = (ImageButton) findViewById(R.id.friend_knots);
        ImageButton friend_knots_menu = (ImageButton) findViewById(R.id.friends_online);
        TextView list_of_friend_knots = (TextView) findViewById(R.id.list_of_friend_knots);

        ImageButton folders = (ImageButton) findViewById(R.id.folders);
        ImageButton folders_menu = (ImageButton) findViewById(R.id.folders_online);
        TextView list_of_folders = (TextView) findViewById(R.id.list_of_folders);

        ImageButton service_knots = (ImageButton) findViewById(R.id.service_knots);
        ImageButton service_knots_menu = (ImageButton) findViewById(R.id.service_online);
        TextView list_of_service_knots = (TextView) findViewById(R.id.list_of_service_knots);

        ImageButton events = (ImageButton) findViewById(R.id.events);
        ImageButton events_menu = (ImageButton) findViewById(R.id.events_online);
        TextView list_of_events = (TextView) findViewById(R.id.list_of_events);

        int visibility = folders.getVisibility();
        if (visibility == View.GONE) {
            folders.setVisibility(View.VISIBLE);
            list_of_folders.setVisibility(View.VISIBLE);
            folders_menu.setImageResource(R.drawable.folders_tag);


            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);


        } else if (visibility == View.VISIBLE) {
            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);
        }


    }

    public void showServiceKnotsOnline(View view) {

        ImageButton friend_knots = (ImageButton) findViewById(R.id.friend_knots);
        ImageButton friend_knots_menu = (ImageButton) findViewById(R.id.friends_online);
        TextView list_of_friend_knots = (TextView) findViewById(R.id.list_of_friend_knots);

        ImageButton folders = (ImageButton) findViewById(R.id.folders);
        ImageButton folders_menu = (ImageButton) findViewById(R.id.folders_online);
        TextView list_of_folders = (TextView) findViewById(R.id.list_of_folders);

        ImageButton service_knots = (ImageButton) findViewById(R.id.service_knots);
        ImageButton service_knots_menu = (ImageButton) findViewById(R.id.service_online);
        TextView list_of_service_knots = (TextView) findViewById(R.id.list_of_service_knots);

        ImageButton events = (ImageButton) findViewById(R.id.events);
        ImageButton events_menu = (ImageButton) findViewById(R.id.events_online);
        TextView list_of_events = (TextView) findViewById(R.id.list_of_events);

        int visibility = service_knots.getVisibility();
        if (visibility == View.GONE) {
            service_knots.setVisibility(View.VISIBLE);
            list_of_service_knots.setVisibility(View.VISIBLE);
            service_knots_menu.setImageResource(R.drawable.service_tag);


            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);

        } else if (visibility == View.VISIBLE) {

            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);
        }


    }

    public void showEventsOnline(View view) {

        ImageButton friend_knots = (ImageButton) findViewById(R.id.friend_knots);
        ImageButton friend_knots_menu = (ImageButton) findViewById(R.id.friends_online);
        TextView list_of_friend_knots = (TextView) findViewById(R.id.list_of_friend_knots);

        ImageButton folders = (ImageButton) findViewById(R.id.folders);
        ImageButton folders_menu = (ImageButton) findViewById(R.id.folders_online);
        TextView list_of_folders = (TextView) findViewById(R.id.list_of_folders);

        ImageButton service_knots = (ImageButton) findViewById(R.id.service_knots);
        ImageButton service_knots_menu = (ImageButton) findViewById(R.id.service_online);
        TextView list_of_service_knots = (TextView) findViewById(R.id.list_of_service_knots);

        ImageButton events = (ImageButton) findViewById(R.id.events);
        ImageButton events_menu = (ImageButton) findViewById(R.id.events_online);
        TextView list_of_events = (TextView) findViewById(R.id.list_of_events);

        int visibility = events.getVisibility();
        if (visibility == View.GONE) {
            events.setVisibility(View.VISIBLE);
            list_of_events.setVisibility(View.VISIBLE);
            events_menu.setImageResource(R.drawable.event_baloon_tag);


            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

        } else if (visibility == View.VISIBLE) {

            friend_knots.setVisibility(View.GONE);
            list_of_friend_knots.setVisibility(View.GONE);
            friend_knots_menu.setImageResource(R.drawable.people_wite);

            folders.setVisibility(View.GONE);
            list_of_folders.setVisibility(View.GONE);
            folders_menu.setImageResource(R.drawable.folders_wite);

            service_knots.setVisibility(View.GONE);
            list_of_service_knots.setVisibility(View.GONE);
            service_knots_menu.setImageResource(R.drawable.service_wite);

            events.setVisibility(View.GONE);
            list_of_events.setVisibility(View.GONE);
            events_menu.setImageResource(R.drawable.event_baloon_wite);

        }
    }


}

//В самом конце MainActivity добавляем класс

class PeopleDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myLogs";

    public PeopleDBHelper(Context context) {
        // конструктор суперкласса
        super(context, "peopleDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log.d(TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table peopleTable ("
                + "_id integer primary key autoincrement, "
                + "name text, "
                + "onlineStatus text, "
                + "IpAddress text, "
                + "SSID text, "
                + "BSSID text, "
                + "userID text unique, "
                + "moreInfo text "
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}





