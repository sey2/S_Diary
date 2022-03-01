package org.techtown.diary.ui;

import org.techtown.diary.async.LoadListData;
import org.techtown.diary.custom.ContentDeleteDialog;
import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.adapter.NoteAdapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.techtown.diary.adapter.Note;
import org.techtown.diary.listener.OnNoteItemClickListener;
import org.techtown.diary.listener.OnTabSelectedListener;
import org.techtown.diary.R;

import java.util.ArrayList;


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

        LoadListData loadListData = new LoadListData(context,adapter);
        loadListData.execute();
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


    /* swipe 삭제 버튼 누르면 DB에서 해당 일기를 찾아 삭제 */
    public void deleteNote(int position){

        String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                " where " +
                "   _id = " + position;

        database.execSQL(sql);
    }

}


