package org.techtown.diary.ui;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
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
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.techtown.diary.BuildConfig;
import org.techtown.diary.custom.ContentDeleteDialog;
import org.techtown.diary.custom.PictureMenuDialog;
import org.techtown.diary.db.NoteDatabase;
import org.techtown.diary.adapter.Note;
import org.techtown.diary.data.AppConstants;
import org.techtown.diary.listener.OnRequestListener;
import org.techtown.diary.listener.OnTabSelectedListener;
import org.techtown.diary.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    String filePath;

    Animation moodAnim;
    MoodClickListener moodClickListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment2, container, false);

        packName = container.getContext().getPackageName();

        initUI(rootView);

        // 입력 화면이 보일 때 마다 현재 위치를 확인함 (onCreateView)
        if(requestListener != null)
            requestListener.onRequest("getCurrentLocation");


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

    @Override
    public void onStop(){
        super.onStop();
        item = null;
        contentsInput.setText("");

    }

    @Override
    public void onStart(){
        super.onStart();
        if(mMode == AppConstants.MODE_MODIFY)
            applyItem();
    }

    /* 일기 수정 -> 전에 작성한 내용 불러오기 */
    public void applyItem() {
        AppConstants.println("applyItem called.");

        if (item!=null) {
            setWeather(null);
            setAddress(item.getAddress());
            setDateString(item.getCreateDateStr());
            setContents(item.getContents());

            String picturePath = item.getPicture();
            if (picturePath == null || picturePath.equals("")) {
                pictureImageView.setImageResource(R.drawable.noimagefound);
            } else {
                setPicture(item.getPicture(), pictureImageView.getWidth());
                pictureImageView.setImageBitmap(resultPhotoBitmap);
            }

        } else {
            weatherIcon.setImageResource(R.drawable.weather_1);
            weatherIndex = 0;
            setAddress("");

            Date currentDate = new Date();
            String currentDateString = AppConstants.dateFormat3.format(currentDate);
            setDateString(currentDateString);

            contentsInput.setText("");
            pictureImageView.setImageResource(R.drawable.noimagefound);
        }

    }


    // XML 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하기 위한 메서드
    private void initUI(ViewGroup rootView){
        weatherIcon = rootView.findViewById(R.id.weatherIcon);
//        weatherImage1 = rootView.findViewById(R.id.weatherImageView);
        weatherImage2 = rootView.findViewById(R.id.weatherImageView2);
        dateTextView = rootView.findViewById(R.id.dateTextView);
        locationTextView = rootView.findViewById(R.id.locationTextView);

        contentsInput = rootView.findViewById(R.id.contentsInput);
        pictureImageView = rootView.findViewById(R.id.pictureImageView);

        moodAnim = AnimationUtils.loadAnimation(context,R.anim.mood_icon_ani);
        curMood = rootView.findViewById(R.id.mood1);
        moodClickListener = new MoodClickListener();


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
                    listener.onTabSelected(0,null);
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
                    listener.onTabSelected(0,null);
            }
        });

        moodImageView1 = rootView.findViewById(R.id.mood1);
        moodImageView1.setOnClickListener(moodClickListener);

        moodImageView2 = rootView.findViewById(R.id.mood2);
        moodImageView2.setOnClickListener(moodClickListener);

        moodImageView3 = rootView.findViewById(R.id.mood3);
        moodImageView3.setOnClickListener(moodClickListener);

        moodImageView4 = rootView.findViewById(R.id.mood4);
        moodImageView4.setOnClickListener(moodClickListener);

        moodImageView5 = rootView.findViewById(R.id.mood5);
        moodImageView5.setOnClickListener(moodClickListener);

    }

    public void setWeather(String data){
        String [] array = {"맑음", "구름 조금", "구름 많음", "흐림", "비", "눈/비", "눈"};

        if(mMode == AppConstants.MODE_MODIFY){
            String str = "@drawable/weather_" + Integer.toString(Integer.parseInt(item.getWeather()) + 1);
            int path = weatherIcon.getResources().getIdentifier(str,"drawable",packName);
            weatherIcon.setImageResource(path);
            weatherIndex = Integer.parseInt(item.getWeather());
        }
        else if(data != null && mMode == AppConstants.MODE_INSERT){
            for(int i=0; i<array.length; i++){

                if(array[i].equals(data)) {
                    String str = "@drawable/weather_" + Integer.toString(i + 1);
                    int path = weatherIcon.getResources().getIdentifier(str, "drawable", packName);
                    weatherIcon.setImageResource(path);
                    weatherIndex = i;
                    break;
                }

            }
        }
    }

    public void setContents(String data) {
        contentsInput.setText(data);
    }

    public void setAddress(String data){
        locationTextView.setText(data);
    }

    public void setDateString(@NonNull  String dataString){
        dateTextView.setText(dataString);
    }

    public void contentDialogSet(boolean is_Ex){
        PictureMenuDialog pictureMenuDialog = new PictureMenuDialog(context);
        pictureMenuDialog.show();

        if(is_Ex)
            pictureMenuDialog.deleteRadioVisible();
        else
            pictureMenuDialog.deleteRadioGone();


        pictureMenuDialog.rgGroupChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int whichButton) {
                if(whichButton == R.id.takePictureRadio)
                    selectedPhotoMenu = R.id.takePictureRadio;

                else if(whichButton == R.id.selectPictureRadio)
                    selectedPhotoMenu = R.id.selectPictureRadio;

                else if (whichButton == R.id.deleteRadio)
                    selectedPhotoMenu = R.id.deleteRadio;

            }
        });

        pictureMenuDialog.setSelectedButton(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedPhotoMenu == R.id.takePictureRadio) {
                    showPhotoCaptureActivity();
                    pictureMenuDialog.dismiss();
                }
                else if(selectedPhotoMenu == R.id.selectPictureRadio) {
                    showPhotoSelectionActivity();
                    pictureMenuDialog.dismiss();
                }else if(selectedPhotoMenu == R.id.deleteRadio){
                    isPhotoCanceled = true;
                    isPhotoCaptured = false;
                    isPhotoFileSaved = false;
                    pictureImageView.setImageResource(R.drawable.imagetab);
                    pictureMenuDialog.dismiss();
                }
            }
        });

        pictureMenuDialog.setBackButtonListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                pictureMenuDialog.dismiss();
            }
        });
    }

    public void showDialog(int id){

        switch (id){

            case AppConstants.CONTENT_PHOTO:
                contentDialogSet(false);
                break;

            case AppConstants.CONTENT_PHOTO_EX:
                contentDialogSet(true);
                break;

            case AppConstants.CONTENT_DELETE:
                ContentDeleteDialog contentDeleteDialog = new ContentDeleteDialog(context);
                contentDeleteDialog.show();
                contentDeleteDialog.deleteButtonListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        contentsInput.setText("");
                        pictureImageView.setImageResource(R.drawable.imagetab);
                        isPhotoFileSaved = false;
                        isPhotoCaptured = false;
                        contentDeleteDialog.dismiss();
                    }
                });

                break;

            default:
                break;
        }


    }


    public void setItem(Note item) {
        this.item = item;
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
        intent.putExtra("crop",true);
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, AppConstants.REQ_PHOTO_SELECTION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNull  Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case AppConstants.REQ_PHOTO_CAPTURE:        // 사진을 찍는 경우
                if(resultCode == getActivity().RESULT_OK) {
                    CropImage.activity(getImageUri()).setGuidelines(CropImageView.Guidelines.ON).start(context, this);
                }
                else
                    Log.d("test", "Not RESULT_OK");
                break;

            case AppConstants.REQ_PHOTO_SELECTION:  // 사진을 앨범에서 선택하는 경우
                if(resultCode == getActivity().RESULT_OK) {
                    Uri fileUri = intent.getData();
                    CropImage.activity(fileUri).setGuidelines(CropImageView.Guidelines.ON).start(context, this);
                }

                break;

            /* 자른 사진을 pictureImageView에 적용 */
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(intent);
                if (result != null) {
                    Uri resultUri = result.getUri();

                    ContentResolver resolver = context.getContentResolver();

                    try {
                        InputStream instream = resolver.openInputStream(resultUri);
                        resultPhotoBitmap = BitmapFactory.decodeStream(instream);

                        //resultPhotoBitmap = getRoundedCornerBitmap(resultPhotoBitmap, 20);
                        pictureImageView.setImageBitmap(resultPhotoBitmap);

                        instream.close();
                        AppConstants.println("picture set");

                        isPhotoCaptured = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                }

        }
    }

    /* 카메라로 찍은 사진을 Uri로 변환  (크롭 Result에 보내주기 위헤 ) */
    private Uri getImageUri()  {
        filePath = file.getAbsolutePath();
        setPicture(file.getAbsolutePath(), 8);
        resultPhotoBitmap = decodeSampledBitmapFromResource(file,
                pictureImageView.getWidth(), pictureImageView.getHeight());

        resultPhotoBitmap = rotateImage();


        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        resultPhotoBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), resultPhotoBitmap, "Title", null);
        return Uri.parse(path);
    }

    public Bitmap rotateImage() {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_UNDEFINED);

        float angle = 0;

        switch (orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90; break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270; break;
        }
        
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(resultPhotoBitmap,0,0,resultPhotoBitmap.getWidth(),resultPhotoBitmap.getHeight(),matrix,true);
    }

    public void setPicture(String picturePath, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        resultPhotoBitmap = BitmapFactory.decodeFile(picturePath, options);
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
        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        int inSampleSize = 1;

        if(height > reqHeight || width > reqWidth){
            while((halfHeight / inSampleSize) >= reqHeight && (halfWidth/inSampleSize) >= reqWidth){
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

    class MoodClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            curMood.clearAnimation();
            curMood = (ImageView) getSelectMood(v);
            curMood.startAnimation(moodAnim);
            setMoodIndex();
        }
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

    private void setMoodIndex(){
        if(curMood == moodImageView1)
            moodIndex = 0;
        else if(curMood == moodImageView2)
            moodIndex = 1;
        else if(curMood == moodImageView3)
            moodIndex = 2;
        else if(curMood == moodImageView4)
            moodIndex = 3;
        else if(curMood == moodImageView5)
            moodIndex = 4;
    }

    public void setmMode(int mode){
        this.mMode = mode;
    }

    public int getmMode() { return this.mMode; }

}