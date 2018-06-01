package app.com.cherrycider.android.wiknot;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.SoundPool;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;

public class User extends Activity {

    PersonalChatDBHelper personalChatDBHelper;


    SharedPreferences myProfile;

    private static final String TAG = "myLogs";

    public static String USER_ID = "User_ID";
    public static String USER_NAME = "User_Name";
    public static String USER_MOREINFO = "User_More_Info";
    public static String USER_IPADDR = "User_IP_address";

    public static String userID;
    public static String userName;
    public static String userMoreInfo;
    public static String userIPAddress;

    /**
     * переменные для Chat
     */
    private ChatArrayAdapter chatArrayAdapter;
    private ListView listView;
    private EditText chatTextInEditText;
    private boolean side = false;

    ChatMessage chatMessage;


    String nameToDB;
    String userIDToDB;
    String sentTimeToDB;
    String ssidToDB;
    String IpAddressToDB;
    String messageToDB;

    String sideToDB;

    // переменные для звука
    SoundPool sp;
    int notificationSoundId;


    /**
     * переменные c класса ChatMessage
     */

    public boolean left = false;
    public String message;



    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user);

        Bundle b = getIntent().getExtras();
        userID = b.getString(USER_ID);
        userName = b.getString(USER_NAME);
        userMoreInfo = b.getString(USER_MOREINFO);
        userIPAddress = b.getString(USER_IPADDR);

        // убираем клавиатуру


        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
        );


        // заполняем текстовые поля
        TextView un = (TextView) findViewById(R.id.userName);
        un.setText(userName);

        TextView mi = (TextView) findViewById(R.id.userMoreInfo);
        mi.setText(userMoreInfo);

        // растягиваем фото по ширине экрана

        ImageView imageView = (ImageView) findViewById(R.id.userPhoto);


        File userPhotoFile = new File(wiknotFolder, userID);
        if (userPhotoFile.exists()) {

            String userPhotoPath = wiknotFolder + "/" + userID;

            Bitmap bitmap = BitmapFactory.decodeFile(userPhotoPath);
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();

            int newWidth = wiknot.getScreenWidth(); //this method should return the width of device screen.
            float scaleFactor = (float) newWidth / (float) imageWidth;
            int newHeight = (int) (imageHeight * scaleFactor);

            bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            imageView.setImageBitmap(bitmap);
        }






    }


    ////////////////////////////////////////////////////////////
    ///// Methods




    /// этот метод печатает в Chat
    /// здесь в printReceivedChatMessage нужно передать уже принятое сообщение receivedMessage а не chatTextInEditText


    public void printReceivedPersonalChatMessage(final String messageToPrint) {

        ////////////////////////////////////////////
        //здесь анализируем пришедшее сообщение

        //  отрезаем toChatForAll
        if (messageToPrint.contains("toPersonalChat, ")) {


            // TODO в async, выводим chatArrayAdapter.add(new ChatMessage(side,....
            AsyncTask<Void, Void, Void> async_print_chat = new AsyncTask<Void, Void, Void>() {

                @Override
                protected void onPreExecute() {
                }

                @Override
                protected Void doInBackground(Void... params) {

                    // расфасовываем сообщение с запятыми в массив msgToRowList
                    ArrayList msgToRowList = new ArrayList(Arrays.asList(messageToPrint.split(",")));

            /*
                //String toChatForAllMessage = "toChatForAll, " + myName + "," + myID + "," + "sentTime" + "," "ssid" + ","+  MyIpAddress + "," + m;
             *

            for(int i=0;i<msgToRowList.size();i++)
            {
                Log.d(TAG, " MainActivity --> "+ i +" "+ msgToRowList.get(i));
            }
            */

                    nameToDB = String.valueOf(msgToRowList.get(1));
                    userIDToDB = String.valueOf(msgToRowList.get(2));
                    sentTimeToDB = String.valueOf(msgToRowList.get(3));
                    ssidToDB = String.valueOf(msgToRowList.get(4));
                    IpAddressToDB = String.valueOf(msgToRowList.get(5));
                    messageToDB = String.valueOf(msgToRowList.get(6));


                    // delete : messageToPrint = messageToPrint.replace("toChatForAll, ", "");

                    // с какой стороны печатать
                    if (IpAddressToDB.contains(MainActivity.MyIpAddress)) {
                        side = !left;
                        // вписываем в DB столбец side
                        sideToDB = "right";

                        // настройках делаем возможность отключения звука нотификации MainActivity.NOTIFICATION_SOUND

                        // воспроизводим звук при отправлении сообщения

                        //final MediaPlayer mp = MediaPlayer.create(context, R.raw.pebbles);
                        //mp.start();

                        if ((MainActivity.NOTIFICATION_SOUND == 1)||(MainActivity.NOTIFICATION_SOUND == 2)) {
                            sp.play(notificationSoundId, 0.1f, 0.1f, 0, 0, 1);
                        }
                        //sp.play(notificationSoundId, 1, 1, 0, 0, 1);
                        //Log.d(TAG, " Chat Fragment notification sound right");
                        //Log.d(TAG, "сообщение послано по кнопке send с MainActivity toChatForAll, " + messageToDB);


                    } else {
                        side = left;
                        // вписываем в DB столбец side
                        sideToDB = "left";


                        // в настройках делаем возможность отключения звука нотификации MainActivity.NOTIFICATION_SOUND
                        // воспроизводим звук при прихождении сообщения
                        //final MediaPlayer mp = MediaPlayer.create(context, R.raw.pebbles);
                        //mp.start();

                        if ((MainActivity.NOTIFICATION_SOUND == 1)||(MainActivity.NOTIFICATION_SOUND == 2)) {
                            sp.play(notificationSoundId, 1, 1, 0, 0, 1);
                        }
                        //Log.d(TAG, " Chat Fragment notification sound left");

                    }


                    //////////////////////////////////////////
                    // заканчиваем анализировать пришедшее сообщение


                    /////////////////////////////////////////////
                    // записываем сообщение в BD


                    // создаем объект для данных
                    ContentValues cv = new ContentValues();

                    // получаем данные из полей ввода


                    // подключаемся к БД
                    SQLiteDatabase db = personalChatDBHelper.getWritableDatabase();


                    //Log.d(TAG, "--- Insert in chatTable: ---");
                    // подготовим данные для вставки в виде пар: наименование столбца - значение

                    // TODO проставляем  текущее время time,

                    cv.put("side", sideToDB);
                    cv.put("userID", userIDToDB);
                    cv.put("name", nameToDB);
                    cv.put("macAddress", "-");
                    cv.put("ssid", ssidToDB);
                    cv.put("senttime", sentTimeToDB);
                    cv.put("time", "dd mm --:--");
                    cv.put("message", messageToDB);

                    // вставляем запись и получаем ее ID
                    long rowID = db.insert("chatTable", null, cv);
                    //Log.d(TAG, "row inserted, ID = " + rowID);


                    return null;


                }

                protected void onPostExecute(Void result) {

                    super.onPostExecute(result);

                    /////////////////////////////////////////////
                    // отсылаем на печать в чат

                    //  после того как в layout создадим поля, загружаем туда
                    //  печатаем последние значения side, name, photo, time , в messageToDB печатаем все запятые


                    //заменяем |comma| на запятые и выводим на печать
                    String m = messageToDB.replace("|comma|", ",");
                    chatArrayAdapter.add(new ChatMessage(side, m, ssidToDB, userIDToDB, sentTimeToDB, nameToDB));

                    // если это свое сообщение пришло чистим строку и убираем кравиатуру
                    if (side != left) {
                        chatTextInEditText.setText("");
                        wiknot.hideKeyboard(User.this);

                        //hideKeyboardFrom(getContext(), getView());
                    }
                    side = left;

                }

            };


            async_print_chat.execute();

        }
    }





    /**
     * The following method adds the header and sends the edited message from User activity
     */

    public void udpSendFromPersonalChat(View view) {
        connectionIsSet();

        EditText editText = (EditText) findViewById(R.id.msgFromPersonalChat);
        String m = editText.getText().toString();
        // проверяем чтобы строка небыла пуста
        if (!m.equals("")) {

            //подменяем все запятые на |comma|
            m = m.replace(",", "|comma|");

            // add header toChatForAll and return IP address

            //добавляем "toChatForAll, name, userID, time, Ipaddress, messqage"
            //String toChatForAllMessage = "toChatForAll, " + myName + "," + myID + "," + "sentTime" + "," + "ssid" + ","+  MyIpAddress + "," + m;
            String toChatForAllMessage = "toChatForAll, " + MainActivity.myName + "," + MainActivity.myID + "," + wiknot.getTimeAndDate() + "," + currentSSID() + "," + wiknot.getIpAddress() + "," + m;
                // вместо tiChatForAll подставляем toPersonalChat
            //Log.d(TAG, "сообщение послано по кнопке send с MainActivity toChatForAll, " + m);



            wiknot.SendMessage(toChatForAllMessage, userIPAddress);



            // после отправления чистим строку чтобы не отправлялось повторно при повторном нажатии

            if (connectionIsSet()) {
                editText.setText("");
            }

        }

    }

    public String currentSSID() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }


    /**
     * этот метод определяет подключен ли wifi, HOTSPOT или другая сеть
     */

    public boolean connectionIsSet() {


        String MyIpAddress = wiknot.getIpAddress();

        if (!(MyIpAddress == "not set")) {
            return true;
        } else {

            Toast toast = Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.check_your_WiFI_connection), Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            toast.show();
            return false;
        }


    }




    public void backArrowMyProfile(View view) {

        onBackPressed();
    }


}


class PersonalChatDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myLogs";

    public PersonalChatDBHelper(Context context, String userID) {
        // конструктор суперкласса
        super(context, "personalchatDB", null, 1);
    }

    String tableID;
    //tableID = userID;

    //добавляем  "toPersonalChat, name, userID, Ipaddress, time, messqage"

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log.d(TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table chatTable" + tableID + " ("
                + "id integer primary key autoincrement, "
                + "side text, "
                + "userID text, "
                + "name text, "
                + "macAddress text, "
                + "ssid text, "
                + "senttime text, "
                + "time text, "
                + "message text "
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
