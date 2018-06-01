package app.com.cherrycider.android.wiknot;

/**
 * Created by V on 27.02.16.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class PopUp extends Activity {

    private static BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public  void onReceive(Context context, Intent intent) {

        }
    };


    private static final String TAG = "myLogs";

    //Context 1
    //private static Context context;

    private BroadcastReceiver notificationBroadcastReceiver = new NotificationBroadcastReceiver();


    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popup);

        registerReceiver(receiver, new IntentFilter(MainActivity.MESSAGE_IS_RECEIVED_INTENT));
        IntentFilter intentFilter = new IntentFilter(MainActivity.MESSAGE_IS_RECEIVED_INTENT);
        registerReceiver(notificationBroadcastReceiver, intentFilter);

        //Context 2
        //PopUp.context = getApplicationContext();


    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(MainActivity.MESSAGE_IS_RECEIVED_INTENT);
        registerReceiver(notificationBroadcastReceiver, intentFilter);
    }

    //@Override
    //protected void onStop() {
     //   super.onStop();
     //   unregisterReceiver(notificationBroadcastReceiver);
    //}

    private class NotificationBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Show popup
            // читаем из него action
            String action = intent.getAction();
            //Извлекаем сообщение из intent
            String message = intent.getStringExtra(MainActivity.MESSAGE_STRING);

            //Log.d(TAG, "Принято в PopUp: " + message);
            // в зависимости от action заполняем переменные
            String format = "", textInfo = "";
            if (action.equals("app.com.cherrycider.android.wiknot.messageIsReceived")) {
                format = "HH:mm:ss";
                textInfo = "Time: ";
            } else if (action.equals("app.com.cherrycider.android.wiknot.messageIsNotReceived")) {
                format = "dd.MM.yyyy";
                textInfo = "Date: ";

                // в зависимости от содержимого переменной format
                // получаем дату или время в переменную datetime

                SimpleDateFormat sdf = new SimpleDateFormat(format);
                String datetime = sdf.format(new Date(System.currentTimeMillis()));

                TextView tvDate = (TextView) findViewById(R.id.popUp);
                tvDate.setText(textInfo + datetime + message);
            }
        }

        /**
         *
         * @param savedInstanceState

         @Override protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.popup);

         registerReceiver(receiver, new IntentFilter(MainActivity.MESSAGE_RECEIVED));

         //Context 2
         //PopUp.context = getApplicationContext();

         // получаем Intent, который вызывал это Activity
         //Intent i = getIntent();

         //Log.d(TAG, "Принято в PopUp: ");

         // читаем из него action
         //String action = i.getAction();

         //String format = "", textInfo = "";

         //Извлекаем сообщение из intent

         //String message = i.getStringExtra(MainActivity.MESSAGE_STRING);

         //Log.d(TAG, "Принято в PopUp: " + message);

         // в зависимости от action заполняем переменные
         if (action.equals("app.com.cherrycider.android.wiknot.messageIsReceived")) {
         format = "HH:mm:ss";
         textInfo = "Time: ";
         }
         else if (action.equals("app.com.cherrycider.android.wiknot.messageIsNotReceived")) {
         format = "dd.MM.yyyy";
         textInfo = "Date: ";
         }

         // в зависимости от содержимого переменной format
         // получаем дату или время в переменную datetime
         SimpleDateFormat sdf = new SimpleDateFormat(format);
         String datetime = sdf.format(new Date(System.currentTimeMillis()));

         TextView tvDate = (TextView) findViewById(R.id.popUp);
         tvDate.setText(textInfo + datetime + message);
         }
         //Context 3
         //public static Context getAppContext() {
         //return PopUp.context;
         //}
         */
    }
}
