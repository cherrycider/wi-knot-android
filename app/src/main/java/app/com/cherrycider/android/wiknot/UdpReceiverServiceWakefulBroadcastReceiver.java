package app.com.cherrycider.android.wiknot;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;


    public class UdpReceiverServiceWakefulBroadcastReceiver extends WakefulBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //
            Intent serviceintent = new Intent(context, UdpReceiverService.class);
            startWakefulService(context, serviceintent);
        }

    }

