package app.com.cherrycider.android.wiknot;

/**
 * Created by V on 27.02.16.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;


public class Preferences extends Activity {

    SharedPreferences sPref;
    public int SCREEN_ORIENTATION = 0;
    public int LOGCAT_DUMP = 0;
    public int NOTIFICATION_SOUND = 1;

    String[] screenOrientationOptions = {"portrait", "landscape", "auto"};
    String[] logcatDumpOptions = {"no dump", "dump to file wiknot/LogcatDump.txt"};
    String[] notificationSoundOptions = {"disabled", "enabled", "only personal messages"};

    private static final String TAG = "myLogs";

    @Override
    protected  void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);


        loadPreferences();

        // адаптер
        ArrayAdapter<String> orientationAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, screenOrientationOptions);
        orientationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> logcatDumpAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, logcatDumpOptions);
        logcatDumpAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ArrayAdapter<String> notificationSoundAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, notificationSoundOptions);
        notificationSoundAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner orientationSpinner = (Spinner) findViewById(R.id.orientation_choice);
        orientationSpinner.setAdapter(orientationAdapter);

        Spinner logcatDumpSpinner = (Spinner) findViewById(R.id.logcatdump_choice);
        logcatDumpSpinner.setAdapter(logcatDumpAdapter);

        Spinner notificationSoundSpinner = (Spinner) findViewById(R.id.notification_choice);
        notificationSoundSpinner.setAdapter(notificationSoundAdapter);


        // заголовок
        orientationSpinner.setPrompt("Screen Orientation Options");
        // выделяем элемент
        orientationSpinner.setSelection(SCREEN_ORIENTATION);
        // устанавливаем обработчик нажатия
        orientationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int orientationPosition, long id) {

                SCREEN_ORIENTATION = orientationPosition;


                savePreferences();
                setOrientation();

                //MainActivity ma = new MainActivity();
                //ma.goLandscape();

                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        // заголовок
        logcatDumpSpinner.setPrompt("Logcat Dump Options");
        // выделяем элемент
        logcatDumpSpinner.setSelection(LOGCAT_DUMP);
        // устанавливаем обработчик нажатия
        logcatDumpSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int logcatDumpPosition, long id) {

                LOGCAT_DUMP = logcatDumpPosition;


                savePreferences();


                //MainActivity ma = new MainActivity();
                //ma.goLandscape();

                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });


        // заголовок
        notificationSoundSpinner.setPrompt("Notification Sound Options");
        // выделяем элемент
        notificationSoundSpinner.setSelection(NOTIFICATION_SOUND);
        // устанавливаем обработчик нажатия
        notificationSoundSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int notificationSoundPosition, long id) {

                NOTIFICATION_SOUND = notificationSoundPosition;


                savePreferences();


                //MainActivity ma = new MainActivity();
                //ma.goLandscape();

                // показываем позиция нажатого элемента
                //Toast.makeText(getBaseContext(), getBaseContext().getString(R.string.saved), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

    }


    void savePreferences() {
        sPref = getSharedPreferences("wiknot_preferences", MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putString("Screen orientation", String.valueOf(SCREEN_ORIENTATION));
        ed.commit();

        ed.putString("Logcat Dump", String.valueOf(LOGCAT_DUMP));
        ed.commit();

        ed.putString("Notification Sound", String.valueOf(NOTIFICATION_SOUND));
        ed.commit();

        //Toast.makeText(this, "Preferences saved", Toast.LENGTH_SHORT).show();
    }

    void loadPreferences() {
        sPref = getSharedPreferences("wiknot_preferences",MODE_PRIVATE);

        String savedTextScreenOrientation = sPref.getString("Screen orientation", String.valueOf(SCREEN_ORIENTATION));
        SCREEN_ORIENTATION = Integer.parseInt(savedTextScreenOrientation);

        String savedTextLogcatDump = sPref.getString("Logcat Dump", String.valueOf(LOGCAT_DUMP));
        LOGCAT_DUMP = Integer.parseInt(savedTextLogcatDump);

        String savedTextNotificationSound = sPref.getString("Notification Sound", String.valueOf(NOTIFICATION_SOUND));
        NOTIFICATION_SOUND = Integer.parseInt(savedTextNotificationSound);

        //Toast.makeText(this, "Preferences loaded", Toast.LENGTH_SHORT).show();
    }


    public void setOrientation() {
        if (SCREEN_ORIENTATION == 0)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        if (SCREEN_ORIENTATION == 1)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        if (SCREEN_ORIENTATION == 2)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    public void backArrowPreferences(View view){

        onBackPressed();
    }

}





