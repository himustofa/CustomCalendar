package com.mk.customcalendar;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomCalendarView extends LinearLayout {

    private Date minDate;
    private Date maxDate;
    private Date highlightedDate;
    private RecyclerView mRecyclerView;
    private CustomCalendarViewAdapter mCustomCalendarViewAdapter;
    private TextView yearMonthTextview;
    private int index = 0;
    private Context mContext;
    private OnDateClickListener dateClickListener;
    private ArrayList<Selector> dateList = new ArrayList<>();
    private static int lastSelectedPosition = -1;
    private final int TYPE_ALL = 1;
    private final int TYPE_SINGLE = 2;
    private DateFormatSymbols mDateFormatSymbols;
    private String TAG = "CustomCalendarView";


    public void setDateClickListener(OnDateClickListener dateClickListener) {
        this.dateClickListener = dateClickListener;
    }

    private RecyclerView.LayoutManager layoutManager;
    MyHandlerThread mHandlerThread;
    private Handler handler = new Handler();


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public CustomCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public CustomCalendarView(Context context) {
        super(context);
        init(context, null, 0, 0);

    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;
        mDateFormatSymbols = DateFormatSymbols.getInstance(Locale.US);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_calendar_view, this);
        MyHandlerThread handlerThread = new MyHandlerThread("CustomCalendar");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mRecyclerView = (RecyclerView) this.findViewById(R.id.custom_recyclerview);
        this.yearMonthTextview = (TextView) this.findViewById(R.id.year_month_textview);
    }

    private void onDateChanged(int position) {
        Log.d(TAG, "onDateChange:" + position);
        if (dateClickListener != null) {
            if (position == -1) {
                dateClickListener.onDateClicked(null);
                return;
            }

            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.setTime(dateList.get(position).getDate());
            selectedCalendar.set(Calendar.HOUR_OF_DAY, 0);
            selectedCalendar.set(Calendar.MINUTE, 0);
            selectedCalendar.set(Calendar.SECOND, 0);
            selectedCalendar.set(Calendar.MILLISECOND, 0);

            String month = getMonth(selectedCalendar.get(Calendar.MONTH));
            long year = selectedCalendar.get(Calendar.YEAR);
            Log.d(TAG, "onDateChange:" + month + "/" + year);
            yearMonthTextview.setText(String.valueOf(month + " / " + year));
            dateClickListener.onDateClicked(selectedCalendar.getTime());
        }
    }


    public Date getHighlightedDate() {
        return highlightedDate;
    }

    public void setHighlightedDate(Date highlightedDate) {
        this.highlightedDate = highlightedDate;
    }

    public Date getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(Date maxDate) {
        this.maxDate = maxDate;
    }

    public Date getMinDate() {
        return minDate;
    }

    public void setMinDate(Date minDate) {
        this.minDate = minDate;
    }

    public String getMonth(int month) {
        if (mDateFormatSymbols == null)
            mDateFormatSymbols = DateFormatSymbols.getInstance(Locale.US);
        String monthStr = mDateFormatSymbols.getMonths()[month];
        return monthStr.substring(0, monthStr.length() >= 3 ? 3 : monthStr.length());
    }

    public String getDay(int day) {
        if (mDateFormatSymbols == null)
            mDateFormatSymbols = DateFormatSymbols.getInstance(Locale.US);
        String dayStr = mDateFormatSymbols.getWeekdays()[day];
        return dayStr.substring(0, dayStr.length() >= 3 ? 3 : dayStr.length());
    }

    public void generateCalendarView() {
        generateDateView();

    }


    private void generateDateView() {
        final Calendar currentCalendar = Calendar.getInstance();
        final int currentYear = currentCalendar.get(Calendar.YEAR);
        final int currentMonth = currentCalendar.get(Calendar.MONTH);
        final int currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);

        final Calendar cal = Calendar.getInstance();
        cal.setTime(getMinDate());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        int highLightYear = 0;
        int highLightMonth = 0;
        int highLightDay = 0;
        if (getHighlightedDate() != null) {
            Calendar highLightCal = Calendar.getInstance();
            highLightCal.setTime(getHighlightedDate());
            highLightYear = highLightCal.get(Calendar.YEAR);
            highLightMonth = highLightCal.get(Calendar.MONTH);
            highLightDay = highLightCal.get(Calendar.DAY_OF_MONTH);
        }

        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(getMaxDate());

        dateList.clear();
        lastSelectedPosition = -1;
        index = 0;

        final int finalHighLightDay = highLightDay;
        final int finalHighLightMonth = highLightMonth;
        final int finalHighLightYear = highLightYear;
        mHandlerThread = new MyHandlerThread("CustomCalendarView");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (cal.getTime().before(endCalendar.getTime()) || cal.getTime().equals(endCalendar.getTime())) {
                    int thisYear = cal.get(Calendar.YEAR);
                    int thisMonth = cal.get(Calendar.MONTH);
                    int thisDay = cal.get(Calendar.DAY_OF_MONTH);

                    if ((thisYear == currentYear && thisMonth == currentMonth && thisDay == currentDay) || (finalHighLightYear > 0 && thisYear == finalHighLightYear && thisMonth == finalHighLightMonth && thisDay == finalHighLightDay)) {
                        lastSelectedPosition = index;
                        dateList.add(new Selector(index, true, cal.getTime()));
                    } else {
                        dateList.add(new Selector(index, false, cal.getTime()));
                    }
                    index++;
                    cal.add(Calendar.DATE, 1);
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCustomCalendarViewAdapter = new CustomCalendarViewAdapter(dateList);
                        mRecyclerView.setLayoutManager(layoutManager);
                        mRecyclerView.setAdapter(mCustomCalendarViewAdapter);
                        mRecyclerView.scrollToPosition(lastSelectedPosition);
                    }
                });
            }
        };

        mHandlerThread.start();
        mHandlerThread.prepareHandler();
        mHandlerThread.postTask(runnable);

    }

    private class CustomCalendarViewAdapter extends RecyclerView.Adapter {
        ArrayList<Selector> calList;

        CustomCalendarViewAdapter(ArrayList<Selector> dateList) {
            this.calList = dateList;
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            private View base;
            private TextView dayTxt, dateTxt;
            private LinearLayout textItemLayout;

            private ViewHolder(View base) {
                super(base);
                this.base = base;
                dayTxt = (TextView) base.findViewById(R.id.day_txt);
                dateTxt = (TextView) base.findViewById(R.id.date_txt);
                textItemLayout = (LinearLayout) base.findViewById(R.id.textItemLayout);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_textview, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            if (dateList != null && position < calList.size()) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(calList.get(position).getDate());
                int calDate = cal.get(Calendar.DATE);
                String day = getDay(cal.get(Calendar.DAY_OF_WEEK));
                String month = getMonth(cal.get(Calendar.MONTH));
                long year = cal.get(Calendar.YEAR);

                ((ViewHolder) holder).dayTxt.setText(day);
                ((ViewHolder) holder).dateTxt.setText(String.valueOf(calDate));

                cal.clear();

                if (lastSelectedPosition == position) {
                    yearMonthTextview.setText(String.valueOf(month + " / " + year));
                    ((ViewHolder) holder).textItemLayout.setBackgroundColor(getResources().getColor(R.color.colorOrange));
                } else {
                    ((ViewHolder) holder).textItemLayout.setBackgroundColor(getResources().getColor(R.color.colorGrayLight));
                }
                ((ViewHolder) holder).textItemLayout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        highLightDate(TYPE_SINGLE, position);
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return dateList != null ? dateList.size() : 0;
        }
    }

    private void highLightDate(int type, int position) {
        if (type == TYPE_ALL) {
            if (lastSelectedPosition >= 0 && lastSelectedPosition < dateList.size()) {
                dateList.get(lastSelectedPosition).setIsSelected(false);
                mRecyclerView.getAdapter().notifyDataSetChanged();
            }
            lastSelectedPosition = -1;
            onDateChanged(-1);
        } else {
            if (lastSelectedPosition >= 0 && lastSelectedPosition < dateList.size()) {
                dateList.get(lastSelectedPosition).setIsSelected(false);
            }
            lastSelectedPosition = position;
            if (position < dateList.size()) {
                dateList.get(position).setIsSelected(true);
            }
            mRecyclerView.getAdapter().notifyDataSetChanged();
            onDateChanged(position);
        }
    }


    interface OnDateClickListener {
        void onDateClicked(Date selectedDate);
    }


    private class Selector {
        private int position;
        private boolean isSelected;
        private Date date;

        Selector(int position, boolean isSelected, Date date) {
            this.position = position;
            this.isSelected = isSelected;
            this.date = date;
        }

        private int getPosition() {
            return position;
        }

        private void setPosition(int position) {
            this.position = position;
        }

        private boolean isSelected() {
            return isSelected;
        }

        private void setIsSelected(boolean isSelected) {
            this.isSelected = isSelected;
        }

        private Date getDate() {
            return date;
        }

        private void setDate(Date date) {
            this.date = date;
        }
    }

    private class MyHandlerThread extends HandlerThread {

        private Handler handler;

        private MyHandlerThread(String name) {
            super(name);
        }

        private void postTask(Runnable task) {
            handler.post(task);
        }

        private void prepareHandler() {
            handler = new Handler(getLooper());
        }
    }

    public void onResume() {}

    public void onPause() {}

    public void onDestroy() {
        mHandlerThread.quit();
    }

    public void setLayoutManger(RecyclerView.LayoutManager layoutManger) {
        this.layoutManager = layoutManger;
    }

    public void notifyAdapter() {
        mCustomCalendarViewAdapter.notifyDataSetChanged();
    }
}

