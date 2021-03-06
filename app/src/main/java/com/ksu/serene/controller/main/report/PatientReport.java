package com.ksu.serene.controller.main.report;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.android.gms.maps.model.LatLng;
import com.ksu.serene.controller.Constants;
import com.ksu.serene.controller.liveChart.utils.Utils;
import com.ksu.serene.controller.main.report.print.PdfDocumentAdapter;
import com.ksu.serene.controller.main.report.print.PrintJobMonitorService;
import com.ksu.serene.model.Event;
import com.ksu.serene.model.Location;
import com.ksu.serene.R;
import com.ksu.serene.model.anxietyGraph;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONObject;

import static timber.log.Timber.tag;

public class PatientReport extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String userId;
    public FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    private TextView noResult;
    private String duration;
    private String startDate;
    private String endDate;
    private GraphsAdapter adapter;

    // Upper
    private ImageView ALGraph;
    private TextView reportDuration;
    private ImageView backBtn;


    // Locations
    private RecyclerView locationRecyclerView;
    private locationsAdapter locationAdapter;
    private ArrayList<Location> highLocations;
    public static ArrayList<WeightedLatLng> locations;
    private Button showHeatmap;

    // Events
    private RecyclerView eventRecyclerView;
    private eventsAdapter eventAdapter;
    private ArrayList<Event> highEvents;
    private TextView noEvents;


    // Recommendation
    private TextView sleepRecommendTV, sleepAvg, sleepRcmnd, noSleepRcmnd, downSleepAvg1, downSleepAvg2;
    private TextView stepsRecommendTV, stepsAvg, stepsRcmnd, noStepsRcmnd, downStepsAvg1, downStepsAvg2;
    private View v1, v2;


    // Print & Share
    private ImageView threeDots;
    private ReportOptions reportOptions;
    private RecyclerView optionsRV;
    private LinearLayout outsideOptions;
    private String[] options = {"Print", "Share"};



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_report);

        SharedPreferences sp = getSharedPreferences(Constants.Keys.USER_DETAILS, Context.MODE_PRIVATE);
        String preferred_lng = sp.getString("PREFERRED_LANGUAGE", "en");
        Utils.setLocale(preferred_lng, this);

        getSupportActionBar().hide();

        // Change status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.darkAccent));
        mAuth = FirebaseAuth.getInstance();
        userId = mAuth.getCurrentUser().getUid();


        // get Duration
        getExtras();

        init();

        setGraphs();

        /* bring the info of the report which generated by the server side
           this includes AL graph & recommendation */
        lastGeneratedPatientReport();

        // location analysis & heat map
        location();

        // retrieve events with high anxiety level
        events();

    }//onCreate

    // TODO : Replace this part with live chart part

    private void setGraphs() {
        SliderView sliderView = findViewById(R.id.imageSlider);

        adapter = new GraphsAdapter(this);

        sliderView.setSliderAdapter(adapter);

        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM); //set indicator animation by using SliderLayout.IndicatorAnimations. :WORM or THIN_WORM or COLOR or DROP or FILL or NONE or SCALE or SCALE_DOWN or SLIDE and SWAP!!
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
        sliderView.setIndicatorSelectedColor(this.getResources().getColor(R.color.darkAccent));
        sliderView.setIndicatorUnselectedColor(this.getResources().getColor(R.color.gray_400));
    }


    private void init() {
        mAuth = FirebaseAuth.getInstance();
        ALGraph = findViewById(R.id.AL_graph);

        noResult = findViewById(R.id.noResult);

        reportDuration = findViewById(R.id.reportDuration);
        reportDuration.setText(reportDate());
        locationRecyclerView = findViewById(R.id.recycleView);

        eventRecyclerView = findViewById(R.id.eventsRecycleView);
        eventAdapter = new eventsAdapter(PatientReport.this, highEvents);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noEvents = findViewById(R.id.noEventResult);

        backBtn = findViewById(R.id.backButton);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        showHeatmap = findViewById(R.id.heatmap);
        showHeatmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                heatmapActivity();
            }
        });


        sleepRecommendTV = findViewById(R.id.recommendSleep);
        sleepAvg = findViewById(R.id.averageSleepN);
        sleepRcmnd = findViewById(R.id.recommendSleepN);
        noSleepRcmnd = findViewById(R.id.noResultSleep);

        stepsRecommendTV = findViewById(R.id.recommendSteps);
        stepsAvg = findViewById(R.id.averageStepsN);
        stepsRcmnd = findViewById(R.id.recommendStepsN);
        noStepsRcmnd = findViewById(R.id.noResultSteps);

        v1 = findViewById(R.id.viewV1);
        v2 = findViewById(R.id.viewV2);

        downSleepAvg1 = findViewById(R.id.averageSleepD);
        downSleepAvg2 = findViewById(R.id.recommendSleepD);

        downStepsAvg1 = findViewById(R.id.averageStepsD);
        downStepsAvg2 = findViewById(R.id.recommendStepsD);

        // Three dots options SHARE & PRINT
        threeDots = findViewById(R.id.more);


        threeDots.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //init the wrapper with style
                Context wrapper = new ContextThemeWrapper(getApplicationContext(), R.style.PopupMenu);

                //creating a popup menu
                PopupMenu popup = new PopupMenu(wrapper, threeDots);

                //inflating menu from xml resource
                popup.inflate(R.menu.report_menu);

                //adding click listener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_print) {//handle menu1 click
                            print();
                        }

                        if (item.getItemId() == R.id.menu_share) {//handle menu1 click
                            share();
                        }

                        return false;
                    }
                });

                //displaying the popup
                popup.show();

            }
        });


    }

    private String reportDate() {

        String interval;

        Date startD, endD;
        String start = "", end;


        DateFormat dateFormat = new SimpleDateFormat("d MMM");

        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.DATE, -1);
//        endD = cal.getTime();
        end = dateFormat.format(cale.getTime());

        Calendar cal = Calendar.getInstance();

        switch (duration) {
            case "14":

                cal.add(Calendar.DATE, -14);
                startD = cal.getTime();
                start = dateFormat.format(startD);

                break;

            case "30":

                cal.add(Calendar.MONTH, -1);
                startD = cal.getTime();
                start = dateFormat.format(startD);

                break;

            case "custom":
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date startDate1 = null, endDate1 = null;
                try {
                    startDate1 = formatter.parse(startDate);
                    startDate1.setMonth(startDate1.getMonth() + 1);

                    endDate1 = formatter.parse(endDate);
                    endDate1.setMonth(endDate1.getMonth() + 1);

                } catch (ParseException e) {
                    e.printStackTrace();
                }

                start = dateFormat.format(startDate1);
                end = dateFormat.format(endDate1);//

                break;

        }//end of switch

        interval = start + " - " + end;
        return interval;

    }

    private void heatmapActivity() {

        if (!locations.isEmpty())
            startActivity(new Intent(PatientReport.this, LocationHeatMap.class));

    }

    private void lastGeneratedPatientReport() {
        String doc_id = "report";
        doc_id += userId;

        firebaseFirestore.collection("LastGeneratePatientReport")
                .document(doc_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {

                            // The patient has only one Doc contains his report info
                            DocumentSnapshot doc = task.getResult();
                            String img = "";

                            // TODO : remove this part
                            // Get graphs
                            if (doc.get("number_of_AL_graphs") != null) {
                                int numberOfGraphs = Integer.parseInt(doc.get("number_of_AL_graphs").toString());

                                for (int i = 0; i < numberOfGraphs; i++) {
                                    if (doc.get("AL_graph_" + i) != null)
                                         img = doc.get("AL_graph_" + i).toString();

                                    addNewItem(img);


                                }
                            }

                            recommendation(doc);

                        }//if
                    }// onComplete
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });//addOnCompleteListener


    }

    public void addNewItem( String url) {
        anxietyGraph sliderItem = new anxietyGraph();
        sliderItem.setDescription("");
        sliderItem.setImageUrl(url);
        adapter.addItem(sliderItem);
    }

    private void location() {

        highLocations = new ArrayList<>();
        locations = new ArrayList<>();

        locationRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        firebaseFirestore.collection("PatientLocations")
                .whereEqualTo("patientID", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            boolean locationFound = false;

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                // Check for locations date if it's within selected duration

                                Date loc_date = ((Timestamp) document.get("time")).toDate();
                                Calendar cal = Calendar.getInstance();
                                cal.add(Calendar.DATE, -1);
                                Date today = cal.getTime();


                                long calcDuration = daysBetween(loc_date, today);

                                switch (duration) {
                                    case "14":
                                        if (0 < calcDuration && calcDuration < 15) {
                                            locationFound = true;
                                            break; //get out of switch and proceed to save other attributes
                                        } else {
                                            locationFound = false;
                                            break;
                                        }

                                    case "30":
                                        if (0 < calcDuration && calcDuration < 31) {
                                            locationFound = true;
                                            break;
                                        } else {
                                            locationFound = false;
                                            break;
                                        }

                                    case "custom":
                                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                                        Date startDate1 = null, endDate1 = null;
                                        try {
                                            startDate1 = formatter.parse(startDate);
                                            startDate1.setMonth(startDate1.getMonth() + 1);
                                            endDate1 = formatter.parse(endDate);
                                            endDate1.setMonth(endDate1.getMonth() + 1);

                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        if (!(loc_date.before(startDate1) || loc_date.after(endDate1))) {
                                            locationFound = true;
                                            break;
                                        } else if (loc_date.getDay() == endDate1.getDay() && loc_date.getMonth() == endDate1.getMonth()) {
                                            locationFound = true;
                                            break;
                                        } else {
                                            locationFound = false;
                                            break;
                                        }

                                }//end of switch

                                if (locationFound) {

                                    showHeatmap.setEnabled(true);

                                    String locationName = document.get("name").toString();
                                    double lat = (double) document.get("lat");
                                    double lng;
                                    try {
                                        lng = (double) document.get("lng");

                                    } catch (NullPointerException e) {
                                        lng = (double) document.get("lon");

                                    }
                                    String anxietyLevel = "";
                                    if (document.get("anxietyLevel") != null) {
                                        anxietyLevel = document.get("anxietyLevel").toString();
                                    }

                                    String nearestLocs = "";
                                    if (document.get("nearestLoc") != null) {
                                        nearestLocs = document.get("nearestLoc").toString();
                                    }

                                    Double loc_AL = Double.parseDouble(anxietyLevel);

                                    switch (anxietyLevel) {

                                        case "2":
                                            anxietyLevel = "Medium";
                                            break;

                                        case "3":
                                            anxietyLevel = "High Anxiety";
                                            break;

                                        default:
                                            anxietyLevel = "Low";
                                            break;
                                    }


                                    int freq = 1; // for every location we assume frequency = 1

                                    // for the highest anxiety locations recycler view
                                    if (anxietyLevel.equals("High Anxiety")) {

                                        noResult.setVisibility(View.GONE);
                                        boolean newLoc = true; // assume it is a new location

                                        for (Location hList : highLocations) { // loop on the highest location list

                                            if (hList.getNearestLoc().equals(nearestLocs)) { // if nearby locations are equals

                                                newLoc = false; // not new locations
                                                freq = hList.getFrequency() + 1; // increment the old frequency

                                                highLocations.remove(hList); // remove the old location

                                                // update it with the new one with new frequency
                                                if(!(locationName.equals("") || nearestLocs.equals("")))
                                                highLocations.add(new Location(locationName, anxietyLevel, daysBetween(loc_date, today), lat, lng, nearestLocs, freq));

                                                break;
                                            }

                                        }// end loop

                                        if (newLoc) { // add new loc with freq = 1
                                            if(!(locationName.equals("") || nearestLocs.equals("")))
                                                highLocations.add(new Location(locationName, anxietyLevel, daysBetween(loc_date, today), lat, lng, nearestLocs, freq));
                                        }
                                    }


                                    // Heat map locations ( all )

                                    LatLng current = new LatLng(lat, lng);
                                    boolean found = false;
                                    //locations.add(new WeightedLatLng( current, loc_AL));

                                    for (WeightedLatLng lis : locations) {

                                        if (lis.getPoint().equals(current)) {
                                            if (lis.getIntensity() <= loc_AL) {
                                                locations.remove(lis);
                                                locations.add(new WeightedLatLng(current, loc_AL));
                                                found = true;
                                                break;
                                            }
                                        }

                                    }

                                    if (!found) {
                                        locations.add(new WeightedLatLng(current, loc_AL));
                                    }

                                }

                            }// for every location belonging to this patient (for loop)


//                            locationRecyclerView.setHasFixedSize(true);
                            locationAdapter = new locationsAdapter(PatientReport.this, highLocations);
                            locationRecyclerView.setAdapter(locationAdapter);

                            if (highLocations.size() == 0) {
                                locationRecyclerView.setVisibility(View.GONE);
                                noResult.setVisibility(View.VISIBLE);
                                noResult.setText(R.string.no_loc_high);
                            } else {
                                locationRecyclerView.setVisibility(View.VISIBLE);
                            }

                        }// end if

                    }// onComplete
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });//addOnCompleteListener

    }

    private static long daysBetween(Date one, Date two) {

        long diffInMillies = Math.abs(one.getTime() - two.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

        return diff;

    }

    private void recommendation(DocumentSnapshot doc) {

        boolean foundSleep = false;
        boolean foundSteps = false;

        String avgSleep = "", avgSteps = "";
        String rcmndSleep = "", rcmndSteps = "";
        boolean sleepR = false;
        boolean stepsR = false;

        if (doc.get("sleepRecomendation") != null)
             sleepR = (boolean) doc.get("sleepRecomendation");

        if (doc.get("stepsRecomendation") != null)
        stepsR = (boolean) doc.get("stepsRecomendation");


        if (sleepR) {
            foundSleep = true;
            if (doc.get("average_sleep_hours") != null)
                avgSleep = doc.get("average_sleep_hours").toString();
            if (doc.get("recommended_sleep_hours") != null)
                rcmndSleep = doc.get("recommended_sleep_hours").toString();

        }

        if (stepsR) {
            foundSteps = true;
            if (doc.get("average_steps") != null)
                avgSteps = doc.get("average_steps").toString();
            if (doc.get("recommended_steps") != null)
                rcmndSteps = doc.get("recommended_steps").toString();
        }


        if (foundSleep) {
            noSleepRcmnd.setVisibility(View.GONE);

            sleepRecommendTV.setText(R.string.sleep_rcmnd);
            // get from strings

            sleepAvg.setText(avgSleep);
            sleepRcmnd.setText(rcmndSleep);

        } else {
            sleepRecommendTV.setVisibility(View.GONE);
            sleepAvg.setVisibility(View.GONE);
            sleepRcmnd.setVisibility(View.GONE);
            downSleepAvg1.setVisibility(View.GONE);
            downSleepAvg2.setVisibility(View.GONE);
            v1.setVisibility(View.GONE);

            noSleepRcmnd.setVisibility(View.VISIBLE);
            //noSleepRcmnd.setText(R.string);

        }

        if (foundSteps) {
            noStepsRcmnd.setVisibility(View.GONE);

            stepsRecommendTV.setText(R.string.steps_rcmnd);

            stepsAvg.setText(avgSteps);
            stepsRcmnd.setText(rcmndSteps);

        } else {
            stepsRecommendTV.setVisibility(View.GONE);
            stepsAvg.setVisibility(View.GONE);
            stepsRcmnd.setVisibility(View.GONE);
            downStepsAvg1.setVisibility(View.GONE);
            downStepsAvg2.setVisibility(View.GONE);
            v2.setVisibility(View.GONE);

            noStepsRcmnd.setVisibility(View.VISIBLE);
            //noStepsRcmnd.setText(R.string.good_steps);
        }


    }

    // Google Calendar Event
    private void events() {

        highEvents = new ArrayList<>();

        firebaseFirestore.collection("PatientEvents")
                .whereEqualTo("patientID", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {

                            //boolean eventFound = false;

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                String id = document.getId();
                                String name = document.get("name").toString();
                                String DateTime = document.get("date").toString();
                                String[] DateTimeL = DateTime.split(" ");
                                String date = DateTimeL[0];
                                String time = DateTimeL[1]+" "+DateTimeL[2];


                                highEvents.add(new Event(name,date,time));

                                deleteDoc(id);
                            }// for every event belonging to this patient (for loop)


//                            locationRecyclerView.setHasFixedSize(true);
                            eventAdapter = new eventsAdapter(PatientReport.this, highEvents);
                            eventRecyclerView.setAdapter(eventAdapter);

                            if (highEvents.size() == 0) {

                                eventRecyclerView.setVisibility(View.GONE);
                                noEvents.setVisibility(View.VISIBLE);
                                //noEvents.setText(R.string.no_loc_high);

                            } else {
                                noEvents.setVisibility(View.GONE);
                                eventRecyclerView.setVisibility(View.VISIBLE);
                            }

                        }// end if



                    }// onComplete

                });


    }//events

    private void deleteDoc(String id){

        firebaseFirestore.collection("PatientEvents")
                .document(id)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i("AppInfo", "Event Deleted");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("AppInfo", "Failed to delete Event");
                    }
                });

    }

    public void getExtras() {
        Intent intent = getIntent();
        duration = intent.getExtras().getString(Constants.Keys.DURATION);

        if (duration.equals("custom")) {
            startDate = intent.getExtras().getString(Constants.Keys.START_DATE);
            endDate = intent.getExtras().getString(Constants.Keys.END_DATE);
        }

    }//getExtras

    public static String getCurrentDateTime() {
        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }

    // Share
    public void sharePDF(String path) {
        File pdf = new File(path);

        if (pdf.exists()) {
            Uri pdfUri = FileProvider.getUriForFile(getApplicationContext(),
                    getApplicationContext().getPackageName() + ".provider", pdf);

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, pdfUri);
            intent.setType("application/pdf");
            startActivity(Intent.createChooser(intent, "Share File"));

        } else {
            Log.i("AppInfo", "File doesn't exist");
        }
    }
    private void share() {

        Locale current = getResources().getConfiguration().locale;

        String filename = "";
        if (current.getLanguage().equals("ar")) {
            filename = "patientReport-AR";
        } else {
            filename = "patientReport-EN";
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        storageRef = storageRef.child(userId + "/lastGeneratedPatientReport/" + filename);

        File rootPath = new File(Environment.getExternalStorageDirectory(), "files");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, filename + " - " + getCurrentDateTime() + ".pdf");

        final File finalLocalFile = localFile;

        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                sharePDF(finalLocalFile.getPath());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });


    }


    // Print
    private void print() {

        Locale current = getResources().getConfiguration().locale;
        tag("AppInfo").d("current Language: %s", current.getDisplayLanguage());
        String filename = "";
        if (current.getLanguage().equals("ar")) {
            filename = "patientReport-AR";
        } else {
            filename = "patientReport-EN";
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        storageRef = storageRef.child(userId + "/lastGeneratedPatientReport/" + filename);


        File rootPath = new File(Environment.getExternalStorageDirectory(), "files");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }
        final File localFile = new File(rootPath, filename + " - " + getCurrentDateTime() + ".pdf");


        final File finalLocalFile = localFile;
        final String finalFilename = filename;
        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                doPrint("Serene - Patient Report",
                        new PdfDocumentAdapter(PatientReport.this, finalLocalFile, finalFilename),
                        new PrintAttributes.Builder().build());

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });

    }
    private PrintManager mgr = null;

    private PrintJob doPrint(String name, PrintDocumentAdapter adapter,
                             PrintAttributes attrs) {

        mgr = (PrintManager) getSystemService(PRINT_SERVICE);

        startService(new Intent(this, PrintJobMonitorService.class));

        return (mgr.print(name, adapter, attrs));

    }


}//end of class