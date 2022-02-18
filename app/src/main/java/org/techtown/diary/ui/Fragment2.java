package org.techtown.diary.ui;

import android.app.AlertDialog;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.github.channguyen.rsv.RangeSliderView;

import org.techtown.diary.BuildConfig;
import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.adapter.Note;
import org.techtown.diary.data.AppConstants;
import org.techtown.diary.listener.OnRequestListener;
import org.techtown.diary.listener.OnTabSelectedListener;
import org.techtown.diary.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Date;

public class Fragment2 extends Fragment {

    Context context;
    OnTabSelectedListener listener;
    OnRequestListener requestListener;

    int mMode = AppConstants.MODE_INSERT;
    int _id = -1;
    int weatherIndex = 0;

    RangeSliderView moodSlider;
    int moodIndex = 2;

    Note item;

    ImageView weatherIcon;
    ImageView weatherImage1; // 내용 위주
    ImageView weatherImage2; // 사진 위주
    TextView dateTextView;
    TextView locationTextView;

    EditText contentsInput;
    ImageView pictureImageView;

    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    Uri uri;
    File file;
    Bitmap resultPhotoBitmap;

    String packName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        // 입력 화면이 보일 때 마다 현재 위치를 확인함 (onCreateView)
        if(requestListener != null)
            requestListener.onRequest("getCurrentLocation");

        applyItem();

        packName = weatherIcon.getContext().getPackageName();

        return rootView;
    }

    @Override
    public void onAttach(@NonNull  Context context){
        super.onAttach(context);

        this.context = context;

        if(context instanceof OnTabSelectedListener)
            listener = (OnTabSelectedListener) context;

        if(context instanceof OnRequestListener)
            requestListener= (OnRequestListener) context;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        if(context!=null){
            context = null;
            listener = null;
            requestListener = null;
        }
    }

    // XML 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하기 위한 메서드
    private void initUI(ViewGroup rootView){
        weatherIcon = rootView.findViewById(R.id.weatherIcon);
        weatherImage1 = rootView.findViewById(R.id.weatherImageView);
        weatherImage2 = rootView.findViewById(R.id.weatherImageView2);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);

        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);

        pictureImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPhotoCaptured || isPhotoFileSaved) {
                    showDialog(AppConstants.CONTENT_PHOTO_EX);
                } else {
                    showDialog(AppConstants.CONTENT_PHOTO);
                }
            }
        });


        Button saveButton = rootView.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mMode == AppConstants.MODE_INSERT)
                    saveNote();
                else if(mMode == AppConstants.MODE_MODIFY)
                    modifyNote();

                if(listener != null)
                    listener.onTabSelected(0);
            }
        });

        Button deleteButton = rootView.findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote();

                if(listener != null)
                    listener.onTabSelected(0);
            }
        });

        Button closeButton = rootView.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(listener!= null)
                    listener.onTabSelected(0);
            }
        });

        moodSlider = rootView.findViewById(R.id.sliderView);
        final RangeSliderView.OnSlideListener listener = new RangeSliderView.OnSlideListener() {
            @Override
            public void onSlide(int index) {
                Toast.makeText(context, "moodIndex changed to " + index, Toast.LENGTH_LONG).show();
                moodIndex = index;
            }
        };

        moodSlider.setOnSlideListener(listener);
        moodSlider.setInitialIndex(2);  // 슬라이더 인덱스 설정
    }

    public void setWeather(String data){
        String [] array = {"맑음", "구름 조금", "구름 많음", "흐림", "비", "눈/비", "눈"};

        if(data != null){
            for(int i=0; i<array.length; i++){
                if(array[i].equals(data)) {
                    String str = "@drawable/weather_" + Integer.toString(i+1);
                    int path = weatherIcon.getResources().getIdentifier(str,"drawable",packName);
                    weatherIcon.setImageResource(path);
                    weatherIndex = i;
                    break;
                }
            }

            Log.d("Fragment2", "Unknown weather string: " + data);
        }
    }

    public void setAddress(String data){
        locationTextView.setText(data);
    }

    public void setDateString(@NonNull  String dataString){
        dateTextView.setText(dataString);
    }

    public void showDialog(int id){
        AlertDialog.Builder builder = null;

        switch (id){

            case AppConstants.CONTENT_PHOTO:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });

                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedPhotoMenu == 0)
                            showPhotoCaptureActivity();
                        else if(selectedPhotoMenu == 1)
                            showPhotoSelectionActivity();
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });
                break;

            case AppConstants.CONTENT_PHOTO_EX:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("사진 메뉴 선택");
                builder.setSingleChoiceItems(R.array.array_photo_ex, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int whichButton) {
                        selectedPhotoMenu = whichButton;
                    }
                });

                builder.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(selectedPhotoMenu == 0)
                            showPhotoCaptureActivity();
                        else if (selectedPhotoMenu == 1)
                            showPhotoCaptureActivity();
                        else if (selectedPhotoMenu == 2){
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.noimagefound);
                        }
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });

                break;

            default:
                break;
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setContents(String data) {
        contentsInput.setText(data);
    }

    public void setMood(String mood) {
        try {
            moodIndex = Integer.parseInt(mood);
            moodSlider.setInitialIndex(moodIndex);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void applyItem() {
        AppConstants.println("applyItem called.");

        if (item != null) {
            mMode = AppConstants.MODE_MODIFY;

            setWeatherIndex(Integer.parseInt(item.getWeather()));
            setAddress(item.getAddress());
            setDateString(item.getCreateDateStr());
            setContents(item.getContents());

            String picturePath = item.getPicture();
            AppConstants.println("picturePath : " + picturePath);

            if (picturePath == null || picturePath.equals("")) {
                pictureImageView.setImageResource(R.drawable.noimagefound);
            } else {
                setPicture(item.getPicture(), 1);
            }

            setMood(item.getMood());
        } else {
            mMode = AppConstants.MODE_INSERT;

            setWeatherIndex(0);
            setAddress("");

            Date currentDate = new Date();
            String currentDateString = AppConstants.dateFormat3.format(currentDate);
            setDateString(currentDateString);

            contentsInput.setText("");
            pictureImageView.setImageResource(R.drawable.noimagefound);
            setMood("2");
        }

    }

    public void setItem(Note item) {
        this.item = item;
    }

    public void setWeatherIndex(int index) {

        for(int i=0; i<7; i++){
            if(index == i) {
                String str = "@drawable/weather_" + Integer.toString(i+1);
                int path = weatherIcon.getResources().getIdentifier(str,"drawable",packName);
                weatherIcon.setImageResource(path);
                weatherIndex = index;
                return;
            }
        }

    }

    public void showPhotoCaptureActivity(){
        try{
            file = createFile();

            if(file.exists())
                file.delete();

            file.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(Build.VERSION.SDK_INT >= 24)
            uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
        else
            uri = Uri.fromFile(file);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_CAPTURE);

    }

    private File createFile(){
        String filename = createFilename();
        File outFile = new File(context.getFilesDir(), filename);

        return outFile;
    }

    public void showPhotoSelectionActivity(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull  Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode){
            case AppConstants.REQ_PHOTO_CAPTURE:        // 사진을 찍는 경우

                setPicture(file.getAbsolutePath(), 8);
                resultPhotoBitmap = decodeSampledBitmapFromResource(file,
                        pictureImageView.getWidth(), pictureImageView.getHeight());

                pictureImageView.setImageBitmap(resultPhotoBitmap);
                break;

            case AppConstants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우
                Uri fileUri = intent.getData();

                ContentResolver resolver = context.getContentResolver();

                try{
                    InputStream instream = resolver.openInputStream(fileUri);
                    resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                    pictureImageView.setImageBitmap(resultPhotoBitmap);

                    instream.close();

                    isPhotoCaptured = true;
                }catch (Exception e){
                    e.printStackTrace();
                }

                break;
        }
    }

    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);

        pictureImageView.setImageBitmap(resultPhotoBitmap);
    }


    public static Bitmap decodeSampledBitmapFromResource(File res, int reqWidth, int reqHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res.getAbsolutePath(),options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(res.getAbsolutePath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight){
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            final int halfHeight = height;
            final int halfWidth = width;

            while((halfHeight / inSampleSize) >= reqHeight && (halfWidth/inSampleSize) >= reqWidth){
                inSampleSize*=2;
            }

        }
        return inSampleSize;
    }

    private String createFilename(){
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }

    private void saveNote(){
        String address = locationTextView.getText().toString();
        String contents = contentsInput.getText().toString();
        String picturePath = savePicture();

        String sql = "insert into " + NoteDatabase.TABLE_NOTE +
                "(WEATHER, ADDRESS, LOCATION_X, LOCATION_Y, CONTENTS, MOOD, PICTURE) values(" +
                "'"+ weatherIndex + "', " +
                "'"+ address + "', " +
                "'"+ "" + "', " +
                "'"+ "" + "', " +
                "'"+ contents + "', " +
                "'"+ moodIndex + "', " +
                "'"+ picturePath + "')";

        Log.d("Fragment2", "sql : " + sql);
        NoteDatabase database = NoteDatabase.getInstance(context);
        database.execSQL(sql);
    }

    private void modifyNote(){
        if(item != null){
            String address = locationTextView.getText().toString();
            String contents = contentsInput.getText().toString();

            String picturePath = savePicture();

            // update note
            String sql = "update " + NoteDatabase.TABLE_NOTE +
                    " set " +
                    "   WEATHER = '" + weatherIndex + "'" +
                    "   ,ADDRESS = '" + address + "'" +
                    "   ,LOCATION_X = '" + "" + "'" +
                    "   ,LOCATION_Y = '" + "" + "'" +
                    "   ,CONTENTS = '" + contents + "'" +
                    "   ,MOOD = '" + moodIndex + "'" +
                    "   ,PICTURE = '" + picturePath + "'" +
                    " where " +
                    "   _id = " + item.get_id();


            Log.d("Fragment2","sql" + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private void deleteNote(){
        AppConstants.println("deleteNote called");

        if(item != null){
            String sql = "delete from " + NoteDatabase.TABLE_NOTE +
                    " where " +
                    "   _id = " + item.get_id();

            Log.d("Fragment2", "sql : " + sql);
            NoteDatabase database = NoteDatabase.getInstance(context);
            database.execSQL(sql);
        }
    }

    private String savePicture() {
        if(resultPhotoBitmap == null){
            AppConstants.println("No picture to be saved.");
            return "";
        }

        File photoFolder = new File(AppConstants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()){
            Log.d("Fragment2", "creating photo folder : " + photoFolder );
            photoFolder.mkdir();
        }

        String photoFilename = createFilename();
        String picturePath = photoFolder + File.separator + photoFilename;

        try {
            FileOutputStream outstream = new FileOutputStream(picturePath);
            resultPhotoBitmap.compress(Bitmap.CompressFormat.PNG, 100, outstream);
            outstream.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return picturePath;
    }

}