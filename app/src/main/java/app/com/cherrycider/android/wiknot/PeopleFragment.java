package app.com.cherrycider.android.wiknot;

import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by V on 10.02.16.
 */
public class PeopleFragment extends Fragment {

    private Context context;

    // фильтр для логов logcat
    private static final String TAG = "myLogs";

    // коструктор вспомогательного класса WiKnotUtils
    WiKnotUtils wiknot = new WiKnotUtils();

    private ListView listView;
    private PeopleArrayAdapter peopleArrayAdapter;

    ArrayList<String> onLineIPAddresses = new ArrayList<>();

    /** Handle the results from the voice recognition activity. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //задаем разметку фрагменту
        final View view = inflater.inflate(R.layout.people, container, false);
        //ну и контекст, так как фрагменты не содержат собственного
        context = view.getContext();
        //выводим текст который хотим
        TextView wordsCount = (TextView) view.findViewById(R.id.list_of_people);
        wordsCount.setText(R.string.people_online);


        // Посылаем интент в MainActivity чтобы печатать список людей online


        Intent peopleOnResumeIntent = new Intent();

        peopleOnResumeIntent.setAction(MainActivity.PPLLIST_STATUS_IS_CHANGED);

        peopleOnResumeIntent.putExtra(MainActivity.PPLLIST_STATUS, "peopleIsOnCreateView");

        MainActivity.MainContext.getApplicationContext().sendBroadcast(peopleOnResumeIntent);

        /**
        *  удаляем, это есть в MainActivity
        *


        ////////////////////////////
        /////list part for onCreateView
        ////////////////////////////


        listView = (ListView) view.findViewById(R.id.peoplelistview);

        peopleArrayAdapter = new PeopleArrayAdapter(view.getContext(), R.layout.useronline);
        listView.setAdapter(peopleArrayAdapter);


        listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        //listView.setAdapter(chatArrayAdapter);

        //to scroll the list view to bottom on data change
        peopleArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(peopleArrayAdapter.getCount() - 1);
            }
        });


        ////////////////////////////
        /////end of list part for onCreateView
        ////////////////////////////


        */




        return view;



    }


    public void onResume() {
        super.onResume();
        //Log.d(TAG, "People Fragment onResume");

        ((MainActivity)getActivity()).printOnlinePeople();


    }


    public void onDestroyView() {
        super.onDestroyView();

        // Посылаем интент в MainActivity чтобы не печатать список людей online


        Intent peopleOnDestroyIntent = new Intent();

        peopleOnDestroyIntent.setAction(MainActivity.PPLLIST_STATUS_IS_CHANGED);

        peopleOnDestroyIntent.putExtra(MainActivity.PPLLIST_STATUS, "peopleIsOnDestroyView");

        MainActivity.MainContext.getApplicationContext().sendBroadcast(peopleOnDestroyIntent);

    }



}
