package org.techtown.diary.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import org.techtown.diary.R;
import org.techtown.diary.data.AppConstants;
import org.techtown.diary.db.NoteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class Fragment3 extends Fragment {

    PieChart chart;
    BarChart chart2;

    Context context;

    ArrayList<Integer> colors = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ViewGroup rootView  = (ViewGroup) inflater.inflate(R.layout.fragment3, container, false);

        initUI(rootView);

        loadStatData();

        return rootView;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void onDetach(){
        super.onDetach();

        if(context != null)
            context = null;
    }


    public void loadStatData(){
        NoteDatabase database = NoteDatabase.getInstance(context);

        // 기분별 비율
        String sql = "select mood " +
                "  , count(mood) " +
                "from " + NoteDatabase.TABLE_NOTE + " " +
                "where create_date > '" + getMonthBefore(1) + "' " +
                "  and create_date < '" + getTomorrow() + "' " +
                "group by mood";

        Cursor cursor = database.rawQuery(sql);
        int recordCount = cursor.getCount();
        AppConstants.println("recordCount : " + recordCount);

        HashMap<String, Integer> dataHash1 = new HashMap<>();

        for(int i=0; i< recordCount; i++){
            cursor.moveToNext();

            String moodName = cursor.getString(0);
            int moodCount = cursor.getInt(1);

            AppConstants.println("#" + i + " -> " + moodName + ", " + moodCount);
            dataHash1.put(moodName, moodCount);
        }
        setData1(dataHash1);

        // second graph
        sql = "select strftime('%w', create_date) " +
                "  , avg(mood) " +
                "from " + NoteDatabase.TABLE_NOTE + " " +
                "where create_date > '" + getMonthBefore(1) + "' " +
                "  and create_date < '" + getTomorrow() + "' " +
                "group by strftime('%w', create_date)";

        cursor = database.rawQuery(sql);
        recordCount = cursor.getCount();
        AppConstants.println("recordCount : " + recordCount);

        HashMap<String,Integer> dataHash2 = new HashMap<String,Integer>();
        for (int i = 0; i < recordCount; i++) {
            cursor.moveToNext();

            String weekDay = cursor.getString(0);
            int moodCount = cursor.getInt(1);

            AppConstants.println("#" + i + " -> " + weekDay + ", " + moodCount);
            dataHash2.put(weekDay, moodCount);
        }

        setData2(dataHash2);


    }



    // XML 레이아웃 안에 들어 있는 위젯이나 레이아웃을 찾아 변수에 할당하기 위한 메서드
    private void initUI(ViewGroup rootView){
        addColor();     // 그래프 색 추가

        chart = rootView.findViewById(R.id.chart1);
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setDrawHoleEnabled(false);
        chart.setHighlightPerTapEnabled(true);  // 특정 부분 선택시 확대 효과 여부

        // chart.setTransparentCircleColor(Color.WHITE);       // 중간원과 바깥원 사이의 얇은 투명원의 색상 결정
        // chart.setTransparentCircleAlpha(110);   // 중간 원과 바깥 원 사이의 얇은 투명원의 알파 값 결정

       // chart.setHoleRadius(58f);   // 중간원 반지름
       // chart.setTransparentCircleRadius(61f);
        // chart.setDrawCenterText(true);


        Legend legend1 = chart.getLegend();     // 그래프 구성 요소들을 추가로 명시하는지 여부
        legend1.setEnabled(false);              // 추가 구성 요소 false

        chart.setEntryLabelColor(Color.WHITE);
       // chart.setEntryLabelTextSize(12f);     // entry 구성요소 label 크기


        chart2 = rootView.findViewById(R.id.chart2);
        chart2.setDrawValueAboveBar(true);

        chart2.getDescription().setEnabled(false);
        chart2.setDrawGridBackground(false);

        XAxis xAxis = chart2.getXAxis();
        xAxis.setEnabled(false);

        YAxis leftAxis = chart2.getAxisLeft();
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMinimum(0.0f);
        leftAxis.setGranularity(1f);

        YAxis rightAxis = chart2.getAxisRight();
        rightAxis.setEnabled(false);

        Legend legend2 = chart2.getLegend();
        legend2.setEnabled(false);

        chart2.animateXY(1500,1500);

    }
    private void setData1(HashMap<String,Integer> dataHash1) {
        ArrayList<PieEntry> entries = new ArrayList<>();

        String[] keys = {"0", "1", "2", "3", "4"};
        int[] icons = {R.drawable.smile1_24, R.drawable.smile2_24,
                R.drawable.smile3_24, R.drawable.smile4_24,
                R.drawable.smile5_24};

        for (int i = 0; i < keys.length; i++) {
            int value = 0;
            Integer outValue = dataHash1.get(keys[i]);
            if (outValue != null) {
                value = outValue.intValue();
            }

            if (value > 0) {
                entries.add(new PieEntry(value, "",
                        getResources().getDrawable(icons[i])));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "기분별 비율");

        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value){
                return String.format("%.0f",value) + "%";
            }
        });

        dataSet.setDrawIcons(true);     // 아이콘 여부 표시
        dataSet.setSliceSpace(10f);     // 그래프 간격
        dataSet.setIconsOffset(new MPPointF(0, -40));       // 아이콘 offset
       // dataSet.setSelectionShift(5f);    //  특정 부분 선택시 확대 효과 크기


        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);

        data.setValueTextSize(22.0f);           // 그래프 내 text 크기
        data.setValueTextColor(Color.WHITE );    // 그래프 내 text 색상

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            data.setValueTypeface(getResources().getFont(R.font.main_font));    // 그래프 내 text 폰트


        chart.setData(data);
        chart.invalidate();
    }

    private void setData2(HashMap<String,Integer> dataHash2) {
        ArrayList<BarEntry> entries = new ArrayList<>();

        String[] keys = {"0", "1", "2", "3", "4", "5", "6"};
        int[] icons = {R.drawable.smile1_24, R.drawable.smile2_24,
                R.drawable.smile3_24, R.drawable.smile4_24,
                R.drawable.smile5_24};

        for (int i = 0; i < keys.length; i++) {
            float value = 0.0f;
            Integer outValue = dataHash2.get(keys[i]);
            AppConstants.println("#" + i + " -> " + outValue);
            if (outValue != null) {
                value = outValue.floatValue();
            }

            Drawable drawable = null;
            if (value <= 1.0f) {
                drawable = getResources().getDrawable(icons[0]);
            } else if (value <= 2.0f) {
                drawable = getResources().getDrawable(icons[1]);
            } else if (value <= 3.0f) {
                drawable = getResources().getDrawable(icons[2]);
            } else if (value <= 4.0f) {
                drawable = getResources().getDrawable(icons[3]);
            } else if (value <= 5.0f) {
                drawable = getResources().getDrawable(icons[4]);
            }

            entries.add(new BarEntry(Float.valueOf(String.valueOf(i+1)), value, drawable));
        }

        BarDataSet dataSet2 = new BarDataSet(entries, "요일별 기분");
        dataSet2.setColor(Color.rgb(240, 120, 124));

        dataSet2.setColors(colors);
        dataSet2.setIconsOffset(new MPPointF(0, -10));

        BarData data = new BarData(dataSet2);
        data.setValueTextSize(10f);
        data.setDrawValues(false);
        data.setBarWidth(0.8f);

        chart2.setData(data);
        chart2.invalidate();
    }


    public String getToday() {
        Date todayDate = new Date();

        return AppConstants.dateFormat5.format(todayDate);
    }

    public String getTomorrow() {
        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.DAY_OF_MONTH, 1);

        return AppConstants.dateFormat5.format(cal.getTime());
    }

    public String getDayBefore(int amount) {
        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.DAY_OF_MONTH, (amount * -1));

        return AppConstants.dateFormat5.format(cal.getTime());
    }

    public String getMonthBefore(int amount) {
        Date todayDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(todayDate);
        cal.add(Calendar.MONTH, (amount * -1));

        return AppConstants.dateFormat5.format(cal.getTime());
    }

    private void addColor(){
        colors.add(getResources().getColor(R.color.status_bar));
        colors.add(getResources().getColor(R.color.lite_pink));
        colors.add(getResources().getColor(R.color.pastel_green));
        colors.add(getResources().getColor(R.color.alice_blue));
        colors.add(getResources().getColor(R.color.bg_color));

    }

}
