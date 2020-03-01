package com.mk.customcalendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements CustomCalendarView.OnDateClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //https://stackoverflow.com/questions/17889878/custom-calendar-and-calendar-adapter
        //https://stackoverflow.com/questions/48074766/want-to-make-custom-calendar-like-below/48074946
        CustomCalendarView customCalendarView = (CustomCalendarView )findViewById(R.id.custom_calendar_view);
        customCalendarView.setDateClickListener(this);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        customCalendarView.setLayoutManger(mLinearLayoutManager);

        Date date = new Date();
        date.setTime(System.currentTimeMillis());

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(date);

        Calendar highlightCal = Calendar.getInstance();
        highlightCal.setTime(date);
        highlightCal.add(Calendar.DATE, 1); //selected date

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(date);
        endCal.add(Calendar.DATE, 25); //total days

        customCalendarView.setMinDate(startCal.getTime());
        customCalendarView.setMaxDate(endCal.getTime());
        customCalendarView.setHighlightedDate(highlightCal.getTime());
        customCalendarView.generateCalendarView();
    }

    @Override
    public void onDateClicked(Date selectedDate) {
        Toast.makeText(this, ""+selectedDate, Toast.LENGTH_SHORT).show();
    }
}
