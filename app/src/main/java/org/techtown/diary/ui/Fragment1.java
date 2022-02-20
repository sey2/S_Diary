package org.techtown.diary.ui;

import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.adapter.NoteAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.adapter.Note;
import org.techtown.diary.data.AppConstants;
import org.techtown.diary.listener.OnTabSelectedListener;
import org.techtown.diary.R;

import java.util.ArrayList;
import java.util.Date;

import lib.kingja.switchbutton.SwitchMultiButton;

public class Fragment1 extends Fragment {

    RecyclerView recyclerView;
    NoteAdapter adapter;

    public Context context;
    OnTabSelectedListener listener;

    @Override   /* Acticity 에서 프래그먼트를 호출하면 호출되는 메서드 */
    public void onAttach(Context context){
        super.onAttach(context);

        this.context = context;

        if(context instanceof OnTabSelectedListener)
            listener = (OnTabSelectedListener) context;
    }

    @Override /* Activity에서 프래그먼트가 제거될 때 호출되는 메서드 */
    public void onDetach(){
        super.onDetach();

        if(context != null){
            context = null;
            listener = null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup  container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment1, container, false);

        initUI(rootView);

        loadNoteListData();

        return rootView;
    }


    // XML 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하기 위한 메서드
    private void initUI(ViewGroup rootView){
        SwitchMultiButton switchButton = rootView.findViewById(R.id.switchButton);
        switchButton.setOnSwitchListener(new SwitchMultiButton.OnSwitchListener() {
            @Override
            public void onSwitch(int position, String tabText) {
                adapter.switchLayout(position);
                adapter.notifyDataSetChanged();
            }
        });

        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        adapter = new NoteAdapter();

        recyclerView.setAdapter(adapter);

        /*          작동 안함 ;;
        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position) {
                Note item = adapter.getItem(position);

                Toast.makeText(getContext(), "아이템 선택됨 : " + item.getContents(), Toast.LENGTH_LONG).show();
            }
        });

         */

    }

    /* 리스트 데이터 로딩 */
    @SuppressLint("Range")
    public int loadNoteListData(){
        AppConstants.println("loadNoteListData called.");

        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";
        int recordCount = -1;

        NoteDatabase database = NoteDatabase.getInstance(context);

        if(database != null){
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            AppConstants.println("record count : " + recordCount + "\n");

            ArrayList<Note> items = new ArrayList<>();

            for(int i=0; i < recordCount; i++){
                outCursor.moveToNext();

                int _id = outCursor.getInt(0);
                String weather = outCursor.getString(1);
                String address = outCursor.getString(2);
                String locationX = outCursor.getString(3);
                String locationY = outCursor.getString(4);
                String contents = outCursor.getString(5);
                String mood = outCursor.getString(6);
                String picture = outCursor.getString(7);
                String dateStr = outCursor.getString(8);
                String createDateStr = null;

                if(dateStr != null && dateStr.length() > 10){
                    try{
                        Date inDate = AppConstants.dateFormat4.parse(dateStr);
                        createDateStr = AppConstants.dateFormat3.format(inDate);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }else{
                    createDateStr = "";
                }

                AppConstants.println("#" + i + " -> " + _id + ", " + weather + ", " +
                        address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
                        mood +", " + picture + ", " + createDateStr);

                items.add(new Note(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
            }

            outCursor.close();

            adapter.setItems(items);
            adapter.notifyDataSetChanged();
        }


        return recordCount;
    }
}


