package app.com.cherrycider.android.wiknot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;


/**
 * Created by V on 10.02.16.
 */
public class TitlePageFragment extends Fragment {

    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    private boolean isWiFiChangeReceiverRegistered = false;

    private Context context;

    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();



    /** Handle the results from the voice recognition activity. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState)

        {
            super.onActivityCreated(savedInstanceState);

            //показываем на экране имя SSID при запуске
            //  ((MainActivity)getActivity()).showSSID_name();


            //опрашиваем кто в сети
            //((MainActivity)getActivity()).sendStatusPoll();


            //Log.d(TAG, "TitlePage Fragment onActivityCreated");
        }




    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //Log.d(TAG, "TitlePage Fragment onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        //задаем разметку фрагменту
        final View view = inflater.inflate(R.layout.titlepage, container, false);

        //ну и контекст, так как фрагменты не содержат собственного
        context = view.getContext();

        hideKeyboardFrom(context, view);

        //Log.d(TAG, "TitlePage Fragment onCreateView");

        //Register wifi state change  receiver in onCreateView  method in Fragment
       // if (!isWiFiChangeReceiverRegistered) {
            //isWiFiChangeReceiverRegistered = true;
            //context.registerReceiver(wiFiChangeReceiver, new IntentFilter("android.net.wifi.STATE_CHANGE"));
            //context.registerReceiver(wiFiChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
            // IntentFilter to wifi state change is "android.net.wifi.STATE_CHANGE"
        //}


        return view;

    }


    public void onStart() {
        super.onStart();

        //Log.d(TAG, "TitlePage Fragment onStart");
    }

    public void onResume() {
        super.onResume();
        //Log.d(TAG, "TitlePage Fragment onResume");
    }


    public void onDestroyView() {
        super.onDestroyView();
        //Log.d(TAG, "TitlePage Fragment onDestroyView");

       // if (isWiFiChangeReceiverRegistered) {
       //     isWiFiChangeReceiverRegistered = false;
       //     context.unregisterReceiver(wiFiChangeReceiver);

       // }
    }


    ///////////////////////////////
    //////////METHODS for TitlePage
    ////////////////////////////////

    /**
     *
     *

// сохраняются данные без последней строчки

    public void onSaveInstanceState(Bundle outState) {

        outState.putString("currentStatus", MainActivity.currentStatus);

        super.onSaveInstanceState(outState);
        //Log.d(TAG, "TitlePageFragment onSaveInstanceState");
    }

*/


        /**
         * The following methods will return the MAC address and SSID of the access point,
         * null if there is no network currently connected
         */
    public String currentAP_MacAddress() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getBSSID();
    }

    public String currentSSID() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }




    /// метод для фрагмента убрать клавиатуру с фрагмента

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

// Create a wiFiChangeReceiver that has to run on receiving WiFi state change

    private BroadcastReceiver wiFiChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            Log.e("Trigger", "getNetworkInfo");
            // Log.d(TAG, "---- WiFi состояние поменялось (TitlePage Fragment)----");

            //показываем на экране имя SSID при исменении состояния сети wifi
            // спим 1 сек чтобы отобразить правильно HOTSPOT, так как IP адрес получаем с опозданием

            /**
             *
             *
            new Thread(new Runnable() {
                public void run() {

                    try {

                        //Log.d(TAG, "---- sleep(1) (TitlePage Fragment)----");
                        TimeUnit.SECONDS.sleep(1);

                        //((MainActivity) getActivity()).showSSID_name();

                        //Log.d(TAG, "---- showSSID_name (TitlePage Fragment)----");

                        //опрашиваем кто в сети
                        ((MainActivity)getActivity()).sendStatusPoll();


                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }).start();
        }


        */



        /**
         *
         *

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ((MainActivity)getActivity()).showSSID_name();


            //опрашиваем кто в сети
            ((MainActivity)getActivity()).sendStatusPoll();


            //showSSID_name();

            //sendStatusPoll();

         */

        }
        };




    }




////////////////////////////////////////////////
//// End of Fragment Class
///////////////////////////////////////////////


