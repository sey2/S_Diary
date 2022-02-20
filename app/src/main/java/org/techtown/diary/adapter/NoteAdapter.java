package org.techtown.diary.adapter;

import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import org.techtown.diary.R;
import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.listener.OnNoteItemClickListener;
import org.techtown.diary.ui.Fragment1;
import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>
        implements OnNoteItemClickListener {

    static ArrayList<Note> items = new ArrayList<Note>();
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    OnNoteItemClickListener listener;
    int layoutType = 0;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.note_item, viewGroup, false);


        return new ViewHolder(itemView, this, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            Note item = items.get(position);

            binderHelper.setOpenOnlyOne(true);
            binderHelper.bind(viewHolder.swipelayout,Integer.toString(item.get_id()));
            viewHolder.bind(item);
            viewHolder.setItem(item);
            viewHolder.setLayoutType(layoutType);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(Note item) {
        items.add(item);
    }

    public void setItems(ArrayList<Note> items) {
        this.items = items;
    }

    public Note getItem(int position) {
        return items.get(position);
    }

    public void setOnItemClickListener(OnNoteItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public void onItemClick(ViewHolder holder, View view, int position) {
        if (listener != null) {
            listener.onItemClick(holder, view, position);
        }
    }


    public void switchLayout(int position) {
        layoutType = position;
    }

   public class ViewHolder extends RecyclerView.ViewHolder {
        SwipeRevealLayout swipelayout;
        private View deleteLayout;
        private View editLayout;

        LinearLayout layout1;
        LinearLayout layout2;

        ImageView moodImageView;
        ImageView moodImageView2;

        ImageView pictureExistsImageView;
        ImageView pictureImageView;

        ImageView weatherImageView;
        ImageView weatherImageView2;

        TextView contentsTextView;
        TextView contentsTextView2;

        TextView locationTextView;
        TextView locationTextView2;

        TextView dateTextView;
        TextView dateTextView2;

        NoteDatabase database;

       public ViewHolder(View itemView, final OnNoteItemClickListener listener, int layoutType) {
            super(itemView);

            swipelayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            editLayout = itemView.findViewById(R.id.txtEdit);
            deleteLayout = itemView.findViewById(R.id.txtDelete);

            layout1 = itemView.findViewById(R.id.layout1);
            layout2 = itemView.findViewById(R.id.layout2);

            moodImageView = itemView.findViewById(R.id.moodImageView);
            moodImageView2 = itemView.findViewById(R.id.moodImageView2);

            pictureExistsImageView = itemView.findViewById(R.id.pictureExistsImageView);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);

            weatherImageView = itemView.findViewById(R.id.weatherImageView);
            weatherImageView2 = itemView.findViewById(R.id.weatherImageView2);

            contentsTextView = itemView.findViewById(R.id.contentsTextView);
            contentsTextView2 = itemView.findViewById(R.id.contentsTextView2);

            locationTextView = itemView.findViewById(R.id.locationTextView);
            locationTextView2 = itemView.findViewById(R.id.locationTextView2);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateTextView2 = itemView.findViewById(R.id.dateTextView2);

           // 데이터 베이스 객체 얻어오기
           Fragment1 fragment1 = new Fragment1();
           database = NoteDatabase.getInstance(fragment1.context);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();

                    if (listener != null) {
                        listener.onItemClick(ViewHolder.this, view, position);
                    }
                }
            });

            setLayoutType(layoutType);
        }

        // Swipe Layout (삭제, 수정) 리스너 설정
        public void bind(final Note item){
            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("test", "position = " + Integer.toString(getAdapterPosition()));

                    int position = item._id;

                    // 리싸이클러 뷰 아이템 목록에서 안보이게 하기
                    items.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());

                    // 일기 삭제
                    deleteNote(position);
                }
            });


            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String displayText = ""  + " clicked";
                }
            });
        }



        public void setItem(Note item) {
            // set mood
            String mood = item.getMood();
            int moodIndex = Integer.parseInt(mood);
            setMoodImage(moodIndex);

            // set picture exists
            String picturePath = item.getPicture();
            Log.d("NoteAdapter", "picturePath -> " + picturePath);

            if (picturePath != null && !picturePath.equals("")) {
                pictureExistsImageView.setVisibility(View.VISIBLE);
                pictureImageView.setVisibility(View.VISIBLE);
                pictureImageView.setImageURI(Uri.parse("file://" + picturePath));

            } else {
                pictureExistsImageView.setVisibility(View.GONE);
                pictureImageView.setImageResource(R.drawable.noimagefound);

            }

            // set weather
            String weather = item.getWeather();
            int weatherIndex = Integer.parseInt(weather);
            setWeatherImage(weatherIndex);

            contentsTextView.setText(item.getContents());
            contentsTextView2.setText(item.getContents());

            locationTextView.setText(item.getAddress());
            locationTextView2.setText(item.getAddress());

            dateTextView.setText(item.getCreateDateStr());
            dateTextView2.setText(item.getCreateDateStr());
        }

        public void setMoodImage(int moodIndex) {
            String packName = moodImageView.getContext().getPackageName();

            for(int i=0; i<=4; i++){
                if(i == moodIndex){
                    String str = "@drawable/smile" + Integer.toString(moodIndex+1) + "_48";
                    int path = moodImageView.getResources().getIdentifier(str,"drawable",packName);
                    moodImageView.setImageResource(path);
                    moodImageView2.setImageResource(path);
                    return;
                }
            }

            moodImageView.setImageResource(R.drawable.smile3_48);
            moodImageView2.setImageResource(R.drawable.smile3_48);

       }

        public void setWeatherImage(int weatherIndex) {
            String packName = weatherImageView.getContext().getPackageName();

            for(int i=0; i<=6; i++){
                if(i == weatherIndex){
                    String str ="@drawable/weather_" + Integer.toString(weatherIndex+1);
                    int path = weatherImageView.getResources().getIdentifier(str,"drawable",packName);
                    weatherImageView.setImageResource(path);
                    weatherImageView2.setImageResource(path);
                return;
                }
            }

            weatherImageView.setImageResource(R.drawable.weather_1);
            weatherImageView2.setImageResource(R.drawable.weather_1);

        }

        public void setLayoutType(int layoutType) {
            if (layoutType == 0) {
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            } else if (layoutType == 1) {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }

        /* swipe 삭제 버튼 누르면 DB에서 해당 일기를 찾아 삭제 */
        public void deleteNote(int position){

           String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                    " where " +
                    "   _id = " + position;

            database.execSQL(sql);
        }

    }


}