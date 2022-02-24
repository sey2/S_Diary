package org.techtown.diary.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import org.techtown.diary.R;

public class PictureMenuDialog extends Dialog {

    RadioGroup radioGroup;
    RadioGroup radioGroupEx;
    ImageButton cancelButton;
    Button backButton;
    Button selectedButton;

    public PictureMenuDialog(@NonNull Context context){
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_menu_dialog);

        radioGroup = findViewById(R.id.radioGroup);
        radioGroupEx = findViewById(R.id.radioGroupEx);
        backButton = findViewById(R.id.backButton);
        selectedButton = findViewById(R.id.selectedButton);
        cancelButton = findViewById(R.id.cancelButton);     // 화면 닫기 x 버튼

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }

    public void RadioGroupVisible(){
        radioGroup.setVisibility(View.VISIBLE);
        radioGroupEx.setVisibility(View.GONE);
    }

    public void RadioGroupVisibleEx(){
        radioGroup.setVisibility(View.GONE);
        radioGroupEx.setVisibility(View.VISIBLE);
    }


    public void rgGroupChangeListener(RadioGroup.OnCheckedChangeListener listener){
        radioGroup.setOnCheckedChangeListener(listener);
    }

    public void rgGroupExChangeListener(RadioGroup.OnCheckedChangeListener listener){
        radioGroupEx.setOnCheckedChangeListener(listener);
    }


    public void setBackButtonListener(View.OnClickListener listener){
        backButton.setOnClickListener(listener);
    }

    public void setSelectedButton(View.OnClickListener listener){
        selectedButton.setOnClickListener(listener);
    }



}
