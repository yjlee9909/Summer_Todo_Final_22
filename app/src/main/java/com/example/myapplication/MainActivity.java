package com.example.myapplication;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.calendarviewlib.CalendarView;
import com.example.myapplication.AlarmUi.AlarmActivity;
import com.example.myapplication.Community_ui.SetUpActivity;
import com.example.myapplication.data.Event;
import com.example.myapplication.uihelpers.CalendarDialog;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)

public class MainActivity extends AppCompatActivity {



    private final static int CREATE_EVENT_REQUEST_CODE = 100;

    private String[] mShortMonths;
    private CalendarView mCalendarView;
    private CalendarDialog mCalendarDialog;

    private List<Event> mEventList = new ArrayList<>();


    public static Intent makeIntent(Context context) {
        return new Intent(context, MainActivity.class);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setUKLocale(this);
        mShortMonths = new DateFormatSymbols().getShortMonths();
        initializeUI();
    }

    private void initializeUI() {

        setContentView(R.layout.activity_calendar_view_with_notes_sdk);

        //Initialize and assign variable
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        bottomNavigationView.setItemIconTintList(null);
        //Set home selected
        bottomNavigationView.setSelectedItemId(R.id.calendar);

        //perform itemselectedlistener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.todolist:
                        startActivity(new Intent(getApplicationContext()
                                , ToDoList.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.community:
                        startActivity(new Intent(getApplicationContext()
                                , Community_button.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.calendar:

                        return true;
                    case R.id.alarm:
                        startActivity(new Intent(getApplicationContext()
                                , AlarmActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.info:
                        startActivity(new Intent(getApplicationContext()
                                , SetUpActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCalendarView = findViewById(R.id.calendarView);
        mCalendarView.setOnMonthChangedListener(new CalendarView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int month, int year) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mShortMonths[month]);
                    getSupportActionBar().setSubtitle(Integer.toString(year));
                }
            }
        });
        mCalendarView.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate) {
                if (calendarObjects.size() != 0) {
                    mCalendarDialog.setSelectedDate(selectedDate);
                    mCalendarDialog.show();
                } else {
                    if (diffYMD(previousDate, selectedDate) == 0)
                        createEvent(selectedDate);
                }
            }
        });

        for (Event e : mEventList) {
            mCalendarView.addCalendarObject(parseCalendarObject(e));
        }

        if (getSupportActionBar() != null) {
            int month = mCalendarView.getCurrentDate().get(Calendar.MONTH);
            int year = mCalendarView.getCurrentDate().get(Calendar.YEAR);
            getSupportActionBar().setTitle(mShortMonths[month]);
            getSupportActionBar().setSubtitle(Integer.toString(year));
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                createEvent(mCalendarView.getSelectedDate());
            }
        });

        mCalendarDialog = CalendarDialog.Builder.instance(this)
                .setEventList(mEventList)
                .setOnItemClickListener(new CalendarDialog.OnCalendarDialogListener() {
                    @Override
                    public void onEventClick(Event event) {
                        onEventSelected(event);
                    }

                    @Override
                    public void onCreateEvent(Calendar calendar) {
                        createEvent(calendar);
                    }
                })
                .create();
    }

    private void onEventSelected(Event event) {
        Activity context = MainActivity.this;
        Intent intent = CreateEventActivity.makeIntent(context, event);

        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    private void createEvent(Calendar selectedDate) {
        Activity context = MainActivity.this;
        Intent intent = CreateEventActivity.makeIntent(context, selectedDate);

        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        overridePendingTransition(R.anim.slide_in_up, R.anim.stay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_calendar_view, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today: {
                mCalendarView.setSelectedDate(Calendar.getInstance());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int action = CreateEventActivity.extractActionFromIntent(data);
                Event event = CreateEventActivity.extractEventFromIntent(data);

                switch (action) {
                    case CreateEventActivity.ACTION_CREATE: {
                        mEventList.add(event);
                        mCalendarView.addCalendarObject(parseCalendarObject(event));
                        mCalendarDialog.setEventList(mEventList);
                        break;
                    }
                    case CreateEventActivity.ACTION_EDIT: {
                        Event oldEvent = null;
                        for (Event e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mEventList.add(event);

                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));
                            mCalendarView.addCalendarObject(parseCalendarObject(event));
                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                    case CreateEventActivity.ACTION_DELETE: {
                        Event oldEvent = null;
                        for (Event e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));

                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static int diffYMD(Calendar date1, Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
            return 0;

        return date1.before(date2) ? -1 : 1;
    }

    private static CalendarView.CalendarObject parseCalendarObject(Event event) {
        return new CalendarView.CalendarObject(
                event.getID(),
                event.getDate(),
                event.getColor(),
                event.isCompleted() ? Color.TRANSPARENT : Color.RED);
    }
}

