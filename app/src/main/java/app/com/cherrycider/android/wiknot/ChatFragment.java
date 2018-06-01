package app.com.cherrycider.android.wiknot;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Created by V on 10.02.16.
 */
public class ChatFragment extends Fragment {

    private Context context;

    ChatDBHelper chatDBHelper;


    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    /**
     * переменные c класса ChatMessage
     */

    public boolean left = false;
    public String message;


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

    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    //Context step 1 -перед onCreate в переменных
    public Context MainContext;

    // объявляем BroadcastReceiver в переменных  - step1
    BroadcastReceiver intentReceiver;

    public static String MESSAGE_IS_RECEIVED_INTENT = "com.cherrycider.udpX.messageIsReceived";
    public static String MESSAGE_STRING = "MessageString";
    public static String receivedMessage;

    // переменные для звука
    SoundPool sp;
    int notificationSoundId;


    /**
     * Handle the results from the ?????? voice recognition activity.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        // Log.d(TAG, "Chat Fragment1 onActivityCreated");


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Log.d(TAG, "Chat Fragment1 onCreate");


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //задаем разметку фрагменту
        final View view = inflater.inflate(R.layout.chat, container, false);
        //ну и контекст, так как фрагменты не содержат собственного
        context = view.getContext();
        //Log.d(TAG, "Chat Fragment1 onCreateView");

        // создаем объект для создания и управления версиями БД
        chatDBHelper = new ChatDBHelper(context);


        //start UDPReceiver сервер в onCreateView
        // UdpReceiver udpReceiver = new UdpReceiver();
        // udpReceiver.runUdpServer();

        //регистрируем Receiver в onCreateView  -  этот фрагмент на удаление
        //context.registerReceiver(intentReceiver, new IntentFilter(MESSAGE_IS_RECEIVED_INTENT));


        // создаем BroadcastReceiver в onCreateView  step 2
        intentReceiver = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                String action = intent.getAction();
                receivedMessage = intent.getExtras().getString(MESSAGE_STRING);
                ;
                //Log.d(TAG, "Arrived to ChatFragment: action = " + action);
                // Log.d(TAG, "Arrived to ChatFragment: Message = " + receivedMessage);


                chatTextInEditText = (EditText) view.findViewById(R.id.msgFromChat);
                printReceivedChatMessage(receivedMessage);


            }
        };


        // создаем фильтр для BroadcastReceiver в onCreateView  step 3
        IntentFilter intentFilter = new IntentFilter(MESSAGE_IS_RECEIVED_INTENT);
        // регистрируем (включаем) BroadcastReceiver
        context.registerReceiver(intentReceiver, intentFilter);


        ////////////////////////////
        /////CHAT part for onCreateView
        ////////////////////////////


        listView = (ListView) view.findViewById(R.id.msgview);

        chatArrayAdapter = new ChatArrayAdapter(view.getContext(), R.layout.right);
        listView.setAdapter(chatArrayAdapter);


        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //listView.setAdapter(chatArrayAdapter);




        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });


        ////////////////////////////
        /////end of CHAT part for onCreateView
        ////////////////////////////


        // создаем SoundPool и загружаем звук
        createSoundPool();
        //sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        notificationSoundId = sp.load(context, R.raw.pebbles, 1);


        // здесь восстанавливаем сообщения из DB в onCreateView
        //
        //
        //
        //
        //
        // Посылаем интент в UdpReceiverService, чтобы сообщения в чат приходили


        Intent chatOnResumeIntent = new Intent();

        chatOnResumeIntent.setAction(MainActivity.CHAT_STATUS_IS_CHANGED);

        chatOnResumeIntent.putExtra(MainActivity.CHAT_STATUS, "chatIsOnCreateView");

        MainActivity.MainContext.getApplicationContext().sendBroadcast(chatOnResumeIntent);

        //////////////////////////////////////////
        //  печатаем таблицу

        // TODO делаем в фсинхроне async , выводим chatArrayAdapter.add(new ChatMessage(side,....

        AsyncTask<Void, Void, ArrayList<ChatMessage>> async_print_chat_in_onCreateView = new AsyncTask<Void, Void, ArrayList<ChatMessage>>() {

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected ArrayList<ChatMessage> doInBackground(Void... params) {


                // в результате на onPostExecute отправляем массив из готовых пользователей
                ArrayList<ChatMessage> chatMessageList = new ArrayList<>();


                // подключаемся к БД
                SQLiteDatabase db = chatDBHelper.getWritableDatabase();

                // Log.d(TAG, "--- onCreateView -- печатаем  chatTable в чат : ---");
                // делаем запрос всех данных из таблицы mytable, получаем Cursor
                Cursor c = db.query("chatTable", null, null, null, null, null, null);

                // ставим позицию курсора на первую строку выборки
                // если в выборке нет строк, вернется false
                if (c.moveToFirst()) {

                    // определяем номера столбцов по имени в выборке
                    int idColIndex = c.getColumnIndex("id");
                    int sideColIndex = c.getColumnIndex("side");
                    int userIDColIndex = c.getColumnIndex("userID");
                    int nameColIndex = c.getColumnIndex("name");
                    int macAddressColIndex = c.getColumnIndex("macAddress");
                    int ssidColIndex = c.getColumnIndex("ssid");
                    int senttimeColIndex = c.getColumnIndex("senttime");
                    int timeColIndex = c.getColumnIndex("time");
                    int messageColIndex = c.getColumnIndex("message");


                    do {

                        /////////////////////////////////////////////
                        // отсылаем на печать
                        // собираем массив в doInBackground и печатаем в onPostExecute

                        /////////////////////////////////////////////
                        /// печатаем справа, если ip адрес свой
                        // берем side из BD

                        // с какой стороны печатать

                        if (c.getString(sideColIndex).contains("right")) {
                            side = !left;
                        } else {
                            side = left;
                        }

                        //chatMessage = new ChatMessage(side, c.getString(messageColIndex), c.getString(ssidColIndex), c.getString(userIDColIndex), c.getString(senttimeColIndex), c.getString(nameColIndex));

                        chatMessage = new ChatMessage(side, c.getString(messageColIndex), c.getString(ssidColIndex), c.getString(userIDColIndex), c.getString(senttimeColIndex), c.getString(nameColIndex));

                        chatMessageList.add(chatMessage);

                        //publishProgress(chatMessage);
                        //chatArrayAdapter.add(new ChatMessage(side, c.getString(messageColIndex), c.getString(ssidColIndex), c.getString(userIDColIndex), c.getString(senttimeColIndex), c.getString(nameColIndex)));


                        // переход на следующую строку
                        // а если следующей нет (текущая - последняя), то false - выходим из цикла
                    } while (c.moveToNext());
                } else
                    // Log.d(TAG, "0 rows");
                    c.close();

                return chatMessageList;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
                //chatArrayAdapter.add(chatMessage);



            }

            protected void onPostExecute(ArrayList<ChatMessage> result) {
                super.onPostExecute(result);


                listView = (ListView) view.findViewById(R.id.msgview);

                chatArrayAdapter = new ChatArrayAdapter(view.getContext(), R.layout.right);
                listView.setAdapter(chatArrayAdapter);


                listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                //listView.setAdapter(chatArrayAdapter);



                for (int i = 0; i < (result.size()); i++) {

                    chatArrayAdapter.add(result.get(i));
                }


            }

        };


        async_print_chat_in_onCreateView.execute();

        return view;
    }


    ///////////////////////////
    /////end of onCreateView
    ////////////////////////////


    public void onStart() {
        super.onStart();
        // Log.d(TAG, "Chat Fragment1 onStart");
    }

    public void onResume() {
        super.onResume();
        //Log.d(TAG, "Chat Fragment1 onResume");

    }

    public void onPause() {
        super.onPause();

        // дерегистрируем (выключаем) BroadcastReceiver
        //context.unregisterReceiver(intentReceiver);
        //Log.d(TAG, "BroadcastReceiver в Chat выключен");


        //Log.d(TAG, "Chat Fragment1 onPause");
    }

    public void onStop() {
        super.onStop();
        // Log.d(TAG, "Chat Fragment1 onStop");
    }

    public void onDestroyView() {
        super.onDestroyView();
        //Log.d(TAG, "Chat Fragment1 onDestroyView");

        // здесь включаем сохранение сообщений в DB в сервисе
        // Посылаем интент в UdpReceiverService


        Intent chatOnDestroyIntent = new Intent();

        chatOnDestroyIntent.setAction(MainActivity.CHAT_STATUS_IS_CHANGED);

        chatOnDestroyIntent.putExtra(MainActivity.CHAT_STATUS, "chatIsOnDestroyView");

        MainActivity.MainContext.getApplicationContext().sendBroadcast(chatOnDestroyIntent);


        // дерегистрируем (выключаем) BroadcastReceiver
        context.unregisterReceiver(intentReceiver);
        //Log.d(TAG, "BroadcastReceiver в Chat выключен");


        // убираем клавиатуру когда листаем
        hideKeyboardFrom(getContext(), getView());


    }

    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "Chat Fragment1 onDestroy");


    }

    public void onDetach() {
        super.onDetach();
        //Log.d(TAG, "Chat Fragment1 onDetach");
    }


    /////////////////////
    //// METHODS
    ////////////////////////


    /// этот метод печатает в Chat
    /// здесь в printReceivedChatMessage нужно передать уже принятое сообщение receivedMessage а не chatTextInEditText


    public void printReceivedChatMessage(final String messageToPrint) {

        ////////////////////////////////////////////
        //здесь анализируем пришедшее сообщение

        //  отрезаем toChatForAll
        if (messageToPrint.contains("toChatForAll, ")) {


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

                        if (MainActivity.NOTIFICATION_SOUND == 1) {
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

                        if (MainActivity.NOTIFICATION_SOUND == 1) {
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
                    SQLiteDatabase db = chatDBHelper.getWritableDatabase();


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
                        hideKeyboardFrom(getContext(), getView());
                    }
                    side = left;

                }

            };


            async_print_chat.execute();

        }
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
    protected void createNewSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        sp = new SoundPool.Builder()
                .setAudioAttributes(attributes)
                .build();
    }

    @SuppressWarnings("deprecation")
    protected void createOldSoundPool() {
        sp = new SoundPool(5, AudioManager.STREAM_SYSTEM, 0);
        // STREAM_ALARM, STREAM_DTMF, STREAM_NOTIFICATION, STREAM_RING, STREAM_SYSTEM, STREAM_VOICE_CALL
    }


/// метод для фрагмента убрать клавиатуру с фрагмента

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * метод с класса ChatMessage
     *
     *

     public void ChatMessage(boolean left, String message) {
     //super();
     this.left = left;
     this.message = message;
     }

     */
}

class ChatDBHelper extends SQLiteOpenHelper {

    private static final String TAG = "myLogs";

    public ChatDBHelper(Context context) {
        // конструктор суперкласса
        super(context, "chatDB", null, 1);
    }


    //добавляем  "toChatForAll, name, userID, Ipaddress, time, messqage"

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log.d(TAG, "--- onCreate database ---");
        // создаем таблицу с полями
        db.execSQL("create table chatTable ("
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
