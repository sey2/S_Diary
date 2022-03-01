package org.techtown.diary.async;


    import android.annotation.SuppressLint;
    import android.content.Context;
    import android.database.Cursor;
    import android.os.AsyncTask;

    import org.techtown.diary.adapter.Note;
    import org.techtown.diary.adapter.NoteAdapter;
    import org.techtown.diary.data.AppConstants;
    import org.techtown.diary.db.NoteDatabase;

    import java.util.ArrayList;
    import java.util.Date;

public class LoadListData extends AsyncTask<Void, String, Integer> {
    Context context;
    NoteAdapter adapter;

    public LoadListData(Context context, NoteAdapter adapter) {
        this.adapter = adapter;
        this.context = context;
    }

    @SuppressLint("Range")
    @Override
    protected Integer doInBackground(Void... params) {
        AppConstants.println("loadNoteListData called.");

        String sql = "select _id, WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE, CREATE_DATE, MODIFY_DATE from " + NoteDatabase.TABLE_NOTE + " order by CREATE_DATE desc";
        int recordCount = -1;

        NoteDatabase database = NoteDatabase.getInstance(context);

        if (database != null) {
            Cursor outCursor = database.rawQuery(sql);

            recordCount = outCursor.getCount();
            AppConstants.println("record count : " + recordCount + "\n");

            ArrayList<Note> items = new ArrayList<>();

            for (int i = 0; i < recordCount; i++) {
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

                if (dateStr != null && dateStr.length() > 10) {
                    try {
                        Date inDate = AppConstants.dateFormat4.parse(dateStr);
                        createDateStr = AppConstants.dateFormat3.format(inDate);
                        int monthIndex;
                        if ((monthIndex = createDateStr.indexOf("월")) >= 0) {
                            createDateStr = createDateStr.substring(0, monthIndex + 1) + " " + createDateStr.substring(monthIndex + 1, createDateStr.length());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    createDateStr = "";
                }

                AppConstants.println("#" + i + " -> " + _id + ", " + weather + ", " +
                        address + ", " + locationX + ", " + locationY + ", " + contents + ", " +
                        mood + ", " + picture + ", " + createDateStr);

                items.add(new Note(_id, weather, address, locationX, locationY, contents, mood, picture, createDateStr));
            }

            outCursor.close();

            adapter.setItems(items);
        }
        return 1;
    }

}