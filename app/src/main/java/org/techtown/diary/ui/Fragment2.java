package org.techtown.diary.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
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
    int weatherIndex = 0;

    int moodIndex = 2;

    Note item;

    ImageView weatherIcon;      // 일기 작성 화면 일기 왼쪽 상단 아이콘
    ImageView weatherImage1; // 내용 위주
    ImageView weatherImage2; // 사진 위주
    TextView dateTextView;
    TextView locationTextView;

    EditText contentsInput;
    ImageView pictureImageView;

    ImageView moodImageView1;
    ImageView moodImageView2;
    ImageView moodImageView3;
    ImageView moodImageView4;
    ImageView moodImageView5;
    ImageView curMood = null;


    boolean isPhotoCaptured;
    boolean isPhotoFileSaved;
    boolean isPhotoCanceled;

    int selectedPhotoMenu;

    Uri uri;
    File file;
    static Bitmap resultPhotoBitmap;

    String packName;

    Animation moodAnim;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        initUI(rootView);

        // 입력 화면이 보일 때 마다 현재 위치를 확인함 (onCreateView)
        if(requestListener != null)
            requestListener.onRequest("getCurrentLocation");

        packName = container.getContext().getPackageName();

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

        moodAnim = AnimationUtils.loadAnimation(context,R.anim.mood_icon_ani);
        curMood = rootView.findViewById(R.id.mood1);

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

        moodImageView1 = rootView.findViewById(R.id.mood1);
        moodImageView1.setOnClickListener((v)->{
            curMood.clearAnimation();
            moodIndex = 0;
            moodImageView1.startAnimation(moodAnim);
            curMood = moodImageView1;
        });

        moodImageView2 = rootView.findViewById(R.id.mood2);
        moodImageView2.setOnClickListener((v)->{
            curMood.clearAnimation();
            moodIndex = 1;
            moodImageView2.startAnimation(moodAnim);
            curMood = moodImageView2;
        });

        moodImageView3 = rootView.findViewById(R.id.mood3);
        moodImageView3.setOnClickListener((v)->{
            curMood.clearAnimation();
            moodIndex = 2;
            moodImageView3.startAnimation(moodAnim);
            curMood = moodImageView3;
        });

        moodImageView4 = rootView.findViewById(R.id.mood4);
        moodImageView4.setOnClickListener((v)->{
            curMood.clearAnimation();
            moodIndex = 3;
            moodImageView4.startAnimation(moodAnim);
            curMood = moodImageView4;
        });

        moodImageView5 = rootView.findViewById(R.id.mood5);
        moodImageView5.setOnClickListener((v)->{
            curMood.clearAnimation();
            moodIndex = 4;
            moodImageView5.startAnimation(moodAnim);
            curMood = moodImageView5;
        });


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
                            showPhotoSelectionActivity();
                        else if (selectedPhotoMenu == 2){
                            isPhotoCanceled = true;
                            isPhotoCaptured = false;

                            pictureImageView.setImageResource(R.drawable.imagetab);
                        }
                    }
                });

                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                });

                break;

            case AppConstants.CONTENT_DELETE:
                builder = new AlertDialog.Builder(context);

                builder.setTitle("알림 메시지");
                builder.setMessage("정말 삭제 하시겠습니까?");

                builder.setPositiveButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

                builder.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        contentsInput.setText("");
                        pictureImageView.setImageResource(R.drawable.imagetab);
                    }
                });

                break;

            default:
                break;
        }

        builder.show();

    }

    public void setContents(String data) {
        contentsInput.setText(data);
    }

    public void setMood(String mood) {
        try {
            moodIndex = Integer.parseInt(mood);
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

        String str = "@drawable/weather_" + Integer.toString(index-1);
        int path = weatherIcon.getResources().getIdentifier(str,"drawable",packName);
        weatherIcon.setImageResource(path);
        weatherIndex = index;

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

                resultPhotoBitmap = getRoundedCornerBitmap(resultPhotoBitmap,20);

                pictureImageView.setImageBitmap(getRoundedCornerBitmap(resultPhotoBitmap,10));
                break;

            case AppConstants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우
                Uri fileUri = intent.getData();

                ContentResolver resolver = context.getContentResolver();

                try{
                    InputStream instream = resolver.openInputStream(fileUri);
                    resultPhotoBitmap = BitmapFactory.decodeStream(instream);
                    resultPhotoBitmap = getRoundedCornerBitmap(resultPhotoBitmap,20);

                    pictureImageView.setImageBitmap(getRoundedCornerBitmap(resultPhotoBitmap,15));

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

    /* 비트맵 모서리 둥글게*/
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int px) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = px;
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
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
            while((height / inSampleSize) >= reqHeight && (width/inSampleSize) >= reqWidth){
                inSampleSize*=2;
            }

        }
        return inSampleSize;
    }

    public static String createFilename(){
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

        // 일기 작성 화면 내용 지움
        contentsInput.setText("");
        pictureImageView.setImageResource(R.drawable.imagetab);

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

        showDialog(AppConstants.CONTENT_DELETE);
        isPhotoFileSaved = false;

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

    private View getSelectMood(View v){
        int id = v.getId();

        switch (id){
            case R.id.mood1:
                return moodImageView1;
            case R.id.mood2:
                return moodImageView2;
            case R.id.mood3:
                return moodImageView3;
            case R.id.mood4:
                return moodImageView4;
            case R.id.mood5:
                return moodImageView5;
        }
        return null;
    }

}