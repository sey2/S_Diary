package com.today.diary.adapter;

import android.content.Context;
import android.net.Uri;
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
import com.today.diary.listener.OnNoteItemClickListener;

import com.today.diary.R;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder>
        implements OnNoteItemClickListener {

    static ArrayList<Note> items = new ArrayList<Note>();
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    OnNoteItemClickListener listener;
    int layoutType = 0;

    Context context;


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
            viewHolder.bind(item, items);
            viewHolder.setItem(item);
            //viewHolder.setLayoutType(layoutType);
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
    public void onDeleteClick(ViewHolder holder, View view, int position,int adapterPosition, ArrayList<Note> items) {
        if (listener != null) {
            listener.onDeleteClick(holder, view, position, adapterPosition,items);
        }
    }

    @Override
    public void onEditClick(ViewHolder holder, View view, int position, int adapterPosition,ArrayList<Note> items){
        if(listener != null)
            listener.onEditClick(holder, view, position, adapterPosition, items);
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

       public ViewHolder(View itemView, final OnNoteItemClickListener listener, int layoutType) {
            super(itemView);

            swipelayout = (SwipeRevealLayout) itemView.findViewById(R.id.swipe_layout);
            editLayout = itemView.findViewById(R.id.txtEdit);
            deleteLayout = itemView.findViewById(R.id.txtDelete);

          //  layout1 = itemView.findViewById(R.id.layout1);
            layout2 = itemView.findViewById(R.id.layout2);

           // moodImageView = itemView.findViewById(R.id.moodImageView);
            moodImageView2 = itemView.findViewById(R.id.moodImageView2);

          //  pictureExistsImageView = itemView.findViewById(R.id.pictureExistsImageView);
            pictureImageView = itemView.findViewById(R.id.pictureImageView);

            //weatherImageView = itemView.findViewById(R.id.weatherImageView);
            weatherImageView2 = itemView.findViewById(R.id.weatherImageView2);

          //  contentsTextView = itemView.findViewById(R.id.contentsTextView);
            contentsTextView2 = itemView.findViewById(R.id.contentsTextView2);

            locationTextView = itemView.findViewById(R.id.locationTextView);
            locationTextView2 = itemView.findViewById(R.id.locationTextView2);

            dateTextView = itemView.findViewById(R.id.dateTextView);
            dateTextView2 = itemView.findViewById(R.id.dateTextView2);

           // setLayoutType(layoutType);
        }

        // Swipe Layout (삭제, 수정) 리스너 설정
        public void bind(final Note item, final ArrayList<Note> items){
            deleteLayout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int position = item._id;

                    if(listener != null){
                        listener.onDeleteClick(ViewHolder.this, view, position,	getAdapterPosition(), items);
                    }

                }
            });


            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = item._id;

                    if(listener != null)
                        listener.onEditClick(ViewHolder.this, view, position,getAdapterPosition(),items);
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

            if (picturePath != null && !picturePath.equals("")) {
//                pictureExistsImageView.setVisibility(View.VISIBLE);
                pictureImageView.setVisibility(View.VISIBLE);
                pictureImageView.setImageURI(Uri.parse("file://" + picturePath));

            } else {
              //  pictureExistsImageView.setVisibility(View.GONE);
                pictureImageView.setImageResource(R.drawable.noimagefound);

            }

            // set weather
            String weather = item.getWeather();
            int weatherIndex = Integer.parseInt(weather);
            setWeatherImage(weatherIndex);

          //  contentsTextView.setText(item.getContents());
            contentsTextView2.setText(item.getContents());

          //  locationTextView.setText(item.getAddress());
            locationTextView2.setText(item.getAddress());

          //  dateTextView.setText(item.getCreateDateStr() +" 날씨 " + getWeatherString(item)); // 내용 위주    00월 00일 날씨 맑음
            dateTextView2.setText(item.getCreateDateStr() +" 날씨 " + getWeatherString(item));    // 사진 위주
        }

       public String getWeatherString(Note item){
           String [] weathers = {"맑음", "구름 조금", "구름 많음", "흐림", "비", "눈/비", "눈"};

           return weathers[Integer.parseInt(item.getWeather())];
       }

        public void setMoodImage(int moodIndex) {
            String packName = moodImageView2.getContext().getPackageName();

            for(int i=0; i<=4; i++){
                if(i == moodIndex){
                    String str = "@drawable/smile" + Integer.toString(moodIndex+1) + "_48";
                    int path = moodImageView2.getResources().getIdentifier(str,"drawable",packName);
                    //moodImageView.setImageResource(path);
                    moodImageView2.setImageResource(path);
                    return;
                }
            }

           // moodImageView.setImageResource(R.drawable.smile3_48);
            moodImageView2.setImageResource(R.drawable.smile3_48);

       }

        public void setWeatherImage(int weatherIndex) {
            String packName = weatherImageView2.getContext().getPackageName();

            for(int i=0; i<=6; i++){
                if(i == weatherIndex){
                    String str ="@drawable/weather_" + Integer.toString(weatherIndex+1);
                    int path = weatherImageView2.getResources().getIdentifier(str,"drawable",packName);
                  //  weatherImageView.setImageResource(path);
                    weatherImageView2.setImageResource(path);
                return;
                }
            }

            //weatherImageView.setImageResource(R.drawable.weather_1);
            weatherImageView2.setImageResource(R.drawable.weather_1);

        }

        /*
        public void setLayoutType(int layoutType) {
            if (layoutType == 0) {
                layout1.setVisibility(View.VISIBLE);
                layout2.setVisibility(View.GONE);
            } else if (layoutType == 1) {
                layout1.setVisibility(View.GONE);
                layout2.setVisibility(View.VISIBLE);
            }
        }
         */


    }


}