package org.techtown.diary.listener;

import android.view.View;

import org.techtown.diary.adapter.Note;
import org.techtown.diary.adapter.NoteAdapter;

import java.util.ArrayList;

public interface OnNoteItemClickListener {
    void onEditClick(NoteAdapter.ViewHolder holder, View view, int itemPosition, int adapterPosition, ArrayList<Note> items);
    void onDeleteClick(NoteAdapter.ViewHolder holder, View view, int itemPosition,int adapterPosition, ArrayList<Note> items);
}