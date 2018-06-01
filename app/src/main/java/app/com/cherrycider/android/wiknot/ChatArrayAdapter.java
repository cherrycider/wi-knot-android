package app.com.cherrycider.android.wiknot;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;




/**
 * this is the class which do all work you need to load data from service and set that data in Chat listview.
 */

class ChatArrayAdapter extends ArrayAdapter<ChatMessage> {

    private TextView chatText;
    private ImageView chatPhoto;
    private TextView chatName;
    private TextView chatSSID;
    private TextView chatTime;

    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();


    private List<ChatMessage> chatMessageList = new ArrayList<ChatMessage>();
    private Context context;

    @Override
    public void add(ChatMessage object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ChatArrayAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
        this.context = context;
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public ChatMessage getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // TODO попробовать использовать viewHolder - может нормалзует потребление памяти

        ChatMessage chatMessageObj = getItem(position);
        View row = convertView;
        LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (chatMessageObj.left) {
            row = inflater.inflate(R.layout.right, parent, false);
        }else{
            row = inflater.inflate(R.layout.left, parent, false);
        }

        //заполняем все поля чата здесь

        chatText = (TextView) row.findViewById(R.id.msgr);
        chatText.setText(chatMessageObj.message);

        chatPhoto = (ImageView) row.findViewById(R.id.photo_r);
        File wiknotFolder = new File(Environment.getExternalStorageDirectory() + "/wiknot");
        File photoFile = new File(wiknotFolder, chatMessageObj.userID);
        Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getPath());

        //resize Bitmap здесь - вместо  setImageBitmap(bitmap)  вставляем setImageBitmap(getResizedBitmap(bitmap, 45dp, 60dp))
        if (bitmap!=null)
            {chatPhoto.setImageBitmap(wiknot.getResizedBitmap(bitmap, 45, 60));
            chatPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        }

        chatName = (TextView) row.findViewById(R.id.name_r);
        chatName.setText(chatMessageObj.name);

        chatSSID = (TextView) row.findViewById(R.id.SSID_r);
        if (!chatMessageObj.ssid.equals("<unknown ssid>")){chatSSID.setText(chatMessageObj.ssid);}
        else {chatSSID.setText("");}

        chatTime = (TextView) row.findViewById(R.id.time_r);
        chatTime.setText(chatMessageObj.time);





        return row;
    }
}
