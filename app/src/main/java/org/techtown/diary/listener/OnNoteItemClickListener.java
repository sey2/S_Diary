package org.techtown.diary.listener;

import android.view.View;

import org.techtown.diary.adapter.NoteAdapter;

public interface OnNoteItemClickListener {
    public void onItemClick(NoteAdapter.ViewHolder holder, View view, int position);
}