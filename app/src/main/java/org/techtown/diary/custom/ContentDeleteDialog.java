package org.techtown.diary.custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import org.techtown.diary.R;

public class ContentDeleteDialog extends Dialog {

    ImageButton cancelButton;
    Button backButton;
    Button deleteButton;

    public ContentDeleteDialog(@NonNull Context context){
        super(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_delete_dialog);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        deleteButton = findViewById(R.id.deleteButton);
    }

    public void deleteButtonListener(View.OnClickListener listener){
        deleteButton.setOnClickListener(listener);
    }
}
