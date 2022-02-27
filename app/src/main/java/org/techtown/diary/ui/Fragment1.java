package org.techtown.diary.ui;

import org.techtown.diary.custom.ContentDeleteDialog;
import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.adapter.NoteAdapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.adapter.Note;
import org.techtown.diary.data.AppConstants;
import org.techtown.diary.listener.OnNoteItemClickListener;
import org.techtown.diary.listener.OnTabSelectedListener;
import org.techtown.diary.R;

import java.util.ArrayList;
import java.util.Date;


public class Fragment1 extends Fragment {

    RecyclerView recyclerView;
    NoteAdapter adapter;

    public Context context;
    OnTabSelectedListener listener;

    NoteDatabase database;

    @Override   /* Acticity 에서 프래그먼트를 호출하면 호출되는 메서드 */
    public void onAttach(Context context){
        super.onAttach(context);

        this.context = context;

        if(context instanceof OnTabSelectedListener)
            listener = (OnTabSelectedListener) context;

        database = NoteDatabase.getInstance(context);

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
    private void initUI(ViewGroup rootView) {

        /* 사진 위주 */
        ImageButton imageButton1 = rootView.findViewById(R.id.imageButton);
        imageButton1.setOnClickListener((v) -> {
            adapter.switchLayout(1);
            adapter.notifyDataSetChanged();
        });

        /* 내용 위주 */
        ImageButton imageButton2 = rootView.findViewById(R.id.imageButton2);
        imageButton2.setOnClickListener((v) -> {
            adapter.switchLayout(0);
            adapter.notifyDataSetChanged();
        });


        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);


        adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);


        // SwipeLayout Click 시
        adapter.setOnItemClickListener(new OnNoteItemClickListener() {
            @Override
            public void onEditClick(NoteAdapter.ViewHolder holder, View view, int position, int adapterPosition, ArrayList<Note> items) {
                listener.onTabSelected(3,items.get(adapterPosition));
            }

            @Override
            public void onDeleteClick(NoteAdapter.ViewHolder holder, View view, int position, int adapterPosition, ArrayList<Note> items) {
                ContentDeleteDialog contentDeleteDialog = new ContentDeleteDialog(context);
                contentDeleteDialog.show();
                contentDeleteDialog.deleteButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 리싸이클러 뷰 아이템 목록에서 안보이게 하기
                        items.remove(adapterPosition);
                        adapter.notifyItemRemoved(adapterPosition);

                        // 일기 삭제
                        deleteNote(position);
                        contentDeleteDialog.dismiss();
                    }
                });
            }
        });
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
                        int monthIndex;
                        if((monthIndex = createDateStr.indexOf("월"))>=0){
                            createDateStr = createDateStr.substring(0,monthIndex+1) + " " +createDateStr.substring(monthIndex+1,createDateStr.length());
                        }
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

    /* swipe 삭제 버튼 누르면 DB에서 해당 일기를 찾아 삭제 */
    public void deleteNote(int position){

        String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                " where " +
                "   _id = " + position;

        database.execSQL(sql);
    }

}


