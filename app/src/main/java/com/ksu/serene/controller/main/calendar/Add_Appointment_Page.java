package com.ksu.serene.controller.main.calendar;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.ksu.serene.controller.Constants;
import com.ksu.serene.controller.Reminder.AlarmScheduler;
import com.ksu.serene.controller.liveChart.utils.Utils;

import com.ksu.serene.model.Reminder;
import com.ksu.serene.model.TherapySession;
import com.ksu.serene.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import www.sanju.motiontoast.MotionToast;

public class Add_Appointment_Page extends AppCompatActivity {

    private EditText AppName;
    private Button Date;
    private Button Time;
    private Button Confirm;
    private ImageView back;
    private Calendar calendar ;
    private Calendar current = Calendar.getInstance();
    private SimpleDateFormat DateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String appDocumentID;
    private Timestamp dateTS;
    private String date;
    private Calendar calendarTimeStamp = Calendar.getInstance();

    //for  day
    private final DatePickerDialog.OnDateSetListener AppDate = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            calendar.set(Calendar.YEAR,year);
            calendar.set(Calendar.MONTH, monthOfYear);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Date.setText(DateFormat.format(calendar.getTime()));
           // Date.setText(calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH))+"/"+calendar.get(Calendar.YEAR) );
        }
    };

    //for time
    private final TimePickerDialog.OnTimeSetListener AppTime = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);

            Time.setText(String.format("%02d : %02d", hours ,minutes) );
        }
    };

    // var's used for check day and time
    private java.util.Date AD;
    private Date CuttentTime;
    private Date AT;
    private boolean added = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_appointment_page);

        SharedPreferences sp = getSharedPreferences(Constants.Keys.USER_DETAILS, Context.MODE_PRIVATE);
        String preferred_lng = sp.getString("PREFERRED_LANGUAGE", "en");
        Utils.setLocale(preferred_lng, this);
        getSupportActionBar().hide();

        back = findViewById(R.id.backButton);
        calendar = Calendar.getInstance();
        AppName = findViewById(R.id.nameET);
        Date = findViewById(R.id.MTillDays);
        Time = findViewById(R.id.MTime);
        Confirm = findViewById(R.id.button);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        date = getIntent().getStringExtra("date");
        if (date != null){
            Date.setText(date);
        }

        //when day edit text click show calender view to choose the start and end days
        //the start day when click
        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialogStartDate = new DatePickerDialog(Add_Appointment_Page.this, AppDate, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialogStartDate.getDatePicker().setMinDate(System.currentTimeMillis());
                datePickerDialogStartDate.show();
            }
        });
        //when time text click show the watch view to choose the time of taken
        Time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(Add_Appointment_Page.this, AppTime, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false);
                timePickerDialog.show();
            }
        });
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkFields(AppName.getText().toString(), Time.getText().toString(), Date.getText().toString())){
                    Toast.makeText(Add_Appointment_Page.this,R.string.EmptyFields, Toast.LENGTH_LONG).show();
                    return;
                }
                //check all filed filled
                if (checkFields(AppName.getText().toString(), Time.getText().toString(), Date.getText().toString())) {
                    if (!checkDayandTime(Date.getText().toString(), Time.getText().toString())){
                        Toast.makeText(Add_Appointment_Page.this, R.string.CurrentTime, Toast.LENGTH_LONG).show();
                        return;
                    }
                    //check the day and time in future or the day now but time in future
                    if (checkDayandTime(Date.getText().toString(), Time.getText().toString())) {
                        // if all checked successfully save medicine in firestore with user id
                        //if (SaveNewMed (AppName.getText().toString(),Date.getText().toString() ,  Time.getText().toString() , Integer.parseInt(Dose.getText().toString()))) {
                        //update calender view color From Start to finish days
                        //search about it
                        SaveNewApp (AppName.getText().toString() , Date.getText().toString(), Time.getText().toString());

                    }
                }
            }
        });
    }
    //The method for check if all field empty or not
    public boolean checkFields (String AName, String time, String date ) {
        if ( !(AName.equals("")) && !(time.equals("Set Time")) && !(date.equals("Set Day"))  ){
            return true;
        }
        return false;
    }

    //check if time after current time when the Date is current date
    public boolean checkDayandTime (String date, String time) {
        SimpleDateFormat TimeFormat = new SimpleDateFormat ("HH : mm", Locale.UK);
        Date CurrentDate = new Date();
        //convert string to date to used in compare
        try {
            AD = DateFormat.parse(date);
            dateTS = new Timestamp(AD);
            calendarTimeStamp.setTimeInMillis(dateTS.getSeconds()*1000);
            AT = TimeFormat.parse(time);
            CuttentTime = TimeFormat.parse(new SimpleDateFormat("HH : mm",Locale.UK).format(new Date()));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        // if the  day after current date no need for check for time
        if ( calendarTimeStamp.get(Calendar.YEAR) > current.get(Calendar.YEAR)
                || ( calendarTimeStamp.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                        && (calendarTimeStamp.get(Calendar.MONTH)+1)> (current.get(Calendar.MONTH)+1) )
                || ( calendarTimeStamp.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                        && (calendarTimeStamp.get(Calendar.MONTH)+1)  == (current.get(Calendar.MONTH)+1)
                        && calendarTimeStamp.get(Calendar.DAY_OF_MONTH) > current.get(Calendar.DAY_OF_MONTH) ) )  {
            return true;
        }
        //check if the  date is the current date the time is after or same current time, if not return false after display meaningful message
         else if (calendarTimeStamp.get(Calendar.YEAR) == current.get(Calendar.YEAR)
                && (calendarTimeStamp.get(Calendar.MONTH)+1)== (current.get(Calendar.MONTH)+1)
                && calendarTimeStamp.get(Calendar.DAY_OF_MONTH) == current.get(Calendar.DAY_OF_MONTH)) {
            if ( AT.getHours() > CuttentTime.getHours()
                    || ( AT.getHours() == CuttentTime.getHours() && AT.getMinutes() > CuttentTime.getMinutes() ) ){
                return true;
            }
        }
         return false;
    }

    //save new appointment in firestore
    private void SaveNewApp (String AppName , String date , String time) {
        SimpleDateFormat TimeFormat = new SimpleDateFormat ("HH : mm",Locale.UK);
        Date CurrentDate = new Date();
        //convert string to date to used in make TherapySession obj
        try {
            AD = DateFormat.parse(date);
            dateTS = new Timestamp(AD);
            AT = TimeFormat.parse(time);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        String patientID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        //store the newApp obj in firestore
        String AppID = db.collection("PatientSessions").document().getId();
        appDocumentID = getRandomID();
        final TherapySession newApp = new TherapySession(AppID, AppName, date , time);
        Map<String, Object> App = new HashMap<>();
        App.put("date", newApp.getDay().toString());
        App.put("name", newApp.getName());
        App.put("patinetID", patientID);
        App.put("documentID", appDocumentID);
        App.put("time", newApp.getTime().toString());
        App.put("dateTimestamp", dateTS);

        // Add a new document with a generated ID
        db.collection("PatientSessions").document(appDocumentID)
                .set(App)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setReminder(newApp);
                            //Toast.makeText(Add_Appointment_Page.this, R.string.AppSavedSuccess, Toast.LENGTH_LONG).show();
                            Resources res = getResources();
                            String text = String.format(res.getString(R.string.AppSavedSuccess));
                            MotionToast.Companion.darkToast(
                                    Add_Appointment_Page.this,
                                    text,
                                    MotionToast.Companion.getTOAST_SUCCESS(),
                                    MotionToast.Companion.getGRAVITY_BOTTOM(),
                                    MotionToast.Companion.getLONG_DURATION(),
                                    ResourcesCompat.getFont( Add_Appointment_Page.this, R.font.montserrat));

                            finish();
                            added = true;
                        } else {
                            Toast.makeText(Add_Appointment_Page.this, R.string.AppSavedFialed, Toast.LENGTH_LONG).show();
                            added = false;
                        }
                    }
                });
    }

    public void setReminder(TherapySession newApp){
        //return content URI for new reminder
        ContentValues values = new ContentValues();
        values.put(Reminder.ReminderEntry.KEY_NAME, newApp.getName());//name
        values.put(Reminder.ReminderEntry.KEY_DOCUMENT_ID, appDocumentID);
        values.put(Reminder.ReminderEntry.KEY_DATE, newApp.getDay());//date
        values.put(Reminder.ReminderEntry.KEY_TIME, newApp.getTime().toString());//time
        values.put(Reminder.ReminderEntry.KEY_DOSE, "");//dose
        values.put(Reminder.ReminderEntry.KEY_REPEAT_TIME, "");
        values.put(Reminder.ReminderEntry.KEY_REPEAT_TYPE, "");
        values.put(Reminder.ReminderEntry.KEY_REPEAT_NO, "");


        //time for first notification
        Calendar reminder = Calendar.getInstance();

        int startYear = Integer.parseInt(newApp.getDay().substring(6));
        int startMonth = Integer.parseInt(newApp.getDay().substring(3,5))-1; //01/34/56
        int startDay = Integer.parseInt(newApp.getDay().substring(0,2));

        int hours = Integer.parseInt(newApp.getTime().substring(0,2)); //hh : mm
        int minutes = Integer.parseInt(newApp.getTime().substring(5));


        //reminder.set(StartD.getYear(), StartD.getMonth(), StartD.getDate(), MTime.getHours(), MTime.getMinutes());
        reminder.set(startYear, startMonth, startDay, hours, minutes,0);


        long selectedTimestamp =  reminder.getTimeInMillis();

        // This is a NEW reminder, so insert a new reminder into the provider,
        // returning the content URI for the new reminder.
        Uri newUri = getContentResolver().insert(Reminder.ReminderEntry.CONTENT_URI, values);
        String newUriPath = newUri.toString();

        // Show a toast message depending on whether or not the insertion was successful.
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            //Toast.makeText(this, "insert_reminder_failed",Toast.LENGTH_SHORT).show();
            Log.d("Reminder", "insert_reminder_failed");
        } else {
            // Otherwise, the insertion was successful and we can display a toast.

            //add URI field to firebase
            Map<String, Object> URIpath = new HashMap<>();
            URIpath.put("URI_path", newUriPath);

            db.collection("PatientSessions").document(appDocumentID)
                    .set(URIpath, SetOptions.merge());


            //create new notification
            new AlarmScheduler().setAlarm(getApplicationContext(), selectedTimestamp, newUri);//no repeating
            //Toast.makeText(this, "Alarm time is at " + new java.text.SimpleDateFormat("dd/MM/yyyy hh:mm").format(new Date(selectedTimestamp)),Toast.LENGTH_LONG).show();
            Log.d("Alarm time is at ", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date(selectedTimestamp)));

            //Toast.makeText(this, "Reminder was successfully scheduled",Toast.LENGTH_SHORT).show();
            Log.d("Reminder: ", "Reminder was successfully scheduled");
        }
    }

    public String getRandomID(){
        return UUID.randomUUID().toString();
    }

}

