package org.techtown.diary;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.channguyen.rsv.RangeSliderView;

public class Fragment2 extends Fragment {

    Context context;
    OnTabSelectedListener listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        return rootView;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        this.context = context;

        if(context instanceof OnTabSelectedListener)
            listener = (OnTabSelectedListener) context;
     }

     @Override
     public void onDetach(){
        super.onDetach();

        if(context!=null){
            context = null;
            listener = null;
        }
     }

    // XML 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하기 위한 메서드
    private void initUI(ViewGroup rootView){
        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onTabSelected(0);
            }
        });

        Button deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener != null)
                    listener.onTabSelected(0);
            }
        });

        Button closeButton = rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!= null)
                    listener.onTabSelected(0);
            }
        });

        RangeSliderView sliderView = rootView.findViewById(R.id.sliderView);
        sliderView.setOnSlideListener(new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Toast.makeText(context, "moodIndex changed to " + index, Toast.LENGTH_LONG).show();
            }
        });

        sliderView.setInitialIndex(2);  // 슬라이더 인덱스 설정
    }
}
