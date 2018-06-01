package app.com.cherrycider.android.wiknot;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by V on 12.06.16.
 */
public class PeopleArrayAdapter extends ArrayAdapter<UserOnline> {

    private ImageView userPhoto;
    private TextView userName;
    private TextView userMoreInfo;
    private TextView userOnlineStatus;
    private TextView userIpAddress;
    private TextView userSSID;
    private TextView userBSSID;
    private TextView userID;
    private TextView userTime;

    public static String USER_INTENT = "com.cherrycider.udpX.user";
    public static String USER_ID = "User_ID";
    public static String USER_NAME = "User_Name";
    public static String USER_MOREINFO = "User_More_Info";
    public static String USER_IPADDR = "User_IP_address";


    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    private List<UserOnline> userOnlineList = new ArrayList<UserOnline>();
    private Context context;

    @Override
    public void add(UserOnline object) {
        userOnlineList.add(object);
        super.add(object);
    }

    @Override
    public void remove(UserOnline object) {
        userOnlineList.remove(object);
        super.remove(object);
    }


    @Override
    public void clear() {
        userOnlineList.clear();
        super.clear();
    }




    public PeopleArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.userOnlineList.size();
    }


    // метод удаляет все записи из адаптера
    public void removeAll(){

        //Log.d(TAG, "PeopleArrayAdapter.removeAll кол-во пользователей " + getCount());
        final int adapterCount = getCount();

        for (int i = 0; i < adapterCount; i++) {

            remove(getItem(i));
            //Log.d(TAG, "PeopleArrayAdapter - удаляем " + i + " " + userOnlineList.get(i));

            remove(userOnlineList.get(i));

            //Log.d(TAG, "PeopleArrayAdapter - удаляем " + i + " " + userOnlineList.get(i));

        }
        notifyDataSetChanged();
        //Log.d(TAG, "осталось кол-во пользователей " + getCount());
        //Log.d(TAG, "notifyDataSetChanged() ");
    }

    public UserOnline getItem(int index) {
        return this.userOnlineList.get(index);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        UserOnline userOnlineObj = getItem(position);
        View row = convertView;

        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        row = inflater.inflate(R.layout.useronline, parent, false);




        //заполняем все поля пользователя здесь

        userName = (TextView) row.findViewById(R.id.name_online);
        userName.setText(userOnlineObj.userName);



        userPhoto = (ImageView) row.findViewById(R.id.photo_online);
        File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");
        File photoFile = new File(wiknotFolder, userOnlineObj.userID);
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());

        //resize Bitmap здесь - вместо  setImageBitmap(bitmap)  вставляем setImageBitmap(getResizedBitmap(bitmap, 120dp, 160dp))
        if (bitmap!=null){


            userPhoto.setImageBitmap(wiknot.getResizedBitmap(bitmap, 240, 320));// better quality
            //userPhoto.setImageBitmap(wiknot.getResizedBitmap(bitmap, 90, 120));
            userPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        userMoreInfo = (TextView) row.findViewById(R.id.moreinfo_online);
        userMoreInfo.setText(userOnlineObj.userMoreInfo);

        if (userOnlineObj.userOnlineStatus.equals("offline")){
            userMoreInfo.setText(R.string.OFFLINE);
            userMoreInfo.setTextSize((float) 16);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                userPhoto.setAlpha((float) .10);
                userName.setAlpha((float) .10);
                userMoreInfo.setAlpha((float) .30);
            }

        }

        //userIpAddress = (TextView) row.findViewById(R.id.ipaddress_online);
        //userIpAddress.setText(userOnlineObj.userIpAddress);


        //userOnlineStatus = (TextView) row.findViewById(R.id.onlinestatus_online);
        //userOnlineStatus.setText(userOnlineObj.userOnlineStatus);

        row.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


                // берем userID нажатого пользователя и запускаем activity User
                String userIDFromClick = getItem(position).userID;
                String nameFromClick = getItem(position).userName;
                String moreInfoFromClick = getItem(position).userMoreInfo;
                String ipAddressFromClick = getItem(position).userIpAddress;

                //Toast.makeText(this, userFromClick, Toast.LENGTH_SHORT).show();
                //Log.d(TAG, "PeopleArrayAdapter - нажато на user s ID " + userIDFromClick);

                Intent intent = new Intent(USER_INTENT);

                intent.putExtra(USER_ID, userIDFromClick);
                intent.putExtra(USER_NAME, nameFromClick);
                intent.putExtra(USER_MOREINFO, moreInfoFromClick);
                intent.putExtra(USER_IPADDR, ipAddressFromClick);

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                v.getContext().startActivity(intent);

            }
        });



        return row;
    }

}

