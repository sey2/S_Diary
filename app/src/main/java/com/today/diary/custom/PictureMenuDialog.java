package com.today.diary.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;

import com.today.diary.R;

public class PictureMenuDialog extends Dialog {

    RadioGroup radioGroup;
    RadioGroup radioGroupEx;
    ImageButton cancelButton;
    Button backButton;
    Button selectedButton;

    RadioButton deleteRadio;

    public PictureMenuDialog(@NonNull Context context){
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.picture_menu_dialog);

        radioGroup = findViewById(R.id.radioGroup);
        backButton = findViewById(R.id.backButton);
        selectedButton = findViewById(R.id.selectedButton);
        cancelButton = findViewById(R.id.cancelButton);     // 화면 닫기 x 버튼

        deleteRadio = findViewById(R.id.deleteRadio);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


    }

    public void deleteRadioVisible(){
        deleteRadio.setVisibility(View.VISIBLE);
    }

    public void deleteRadioGone(){
        deleteRadio.setVisibility(View.GONE);
    }


    public void rgGroupChangeListener(RadioGroup.OnCheckedChangeListener listener){
        radioGroup.setOnCheckedChangeListener(listener);
    }

    public void setBackButtonListener(View.OnClickListener listener){
        backButton.setOnClickListener(listener);
    }

    public void setSelectedButton(View.OnClickListener listener){
        selectedButton.setOnClickListener(listener);
    }



}
