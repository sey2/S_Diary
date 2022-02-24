package org.techtown.diary.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import org.techtown.diary.R;

public class StopWriteDialog extends Dialog {

    ImageButton cancelButton;
    Button backButton;
    Button continueButton;

    public StopWriteDialog(@NonNull Context context){
        super(context);
    }

    public StopWriteDialog(@NonNull Context context, int themeResId){
        super(context, themeResId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stop_write_dialog);

        cancelButton = findViewById(R.id.cancelButton);
        backButton = findViewById(R.id.backButton);
        continueButton = findViewById(R.id.continueButton);
    }

    public void setCancelButtonOnClickListener(View.OnClickListener listener) {
        cancelButton.setOnClickListener(listener);
    }

    public void setBackButtonOnClickListener(View.OnClickListener listener) {
        backButton.setOnClickListener(listener);
    }

    public void setContinueButtonOnClickListener(View.OnClickListener listener) {
        continueButton.setOnClickListener(listener);
    }



}
