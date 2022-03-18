package com.today.diary.listener;

import android.view.View;

import com.today.diary.adapter.Note;
import com.today.diary.adapter.NoteAdapter;

import java.util.ArrayList;

public interface OnNoteItemClickListener {
    void onEditClick(NoteAdapter.ViewHolder holder, View view, int itemPosition, int adapterPosition, ArrayList<Note> items);
    void onDeleteClick(NoteAdapter.ViewHolder holder, View view, int itemPosition,int adapterPosition, ArrayList<Note> items);
}