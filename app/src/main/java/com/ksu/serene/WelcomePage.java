package com.ksu.serene;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.ksu.serene.controller.Constants;
import com.ksu.serene.controller.liveChart.utils.Utils;
import com.ksu.serene.controller.main.profile.Editprofile;
import com.ksu.serene.controller.signup.Questionnairs;
import com.ksu.serene.controller.signup.Signup;
import com.ksu.serene.model.MySharedPreference;
import com.ksu.serene.model.Token;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WelcomePage extends AppCompatActivity {

    private Button logIn;
    private Button register;
    private TextView Eng, Ar;


    private String TAG = WelcomePage.class.getSimpleName();

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        sp = getSharedPreferences(Constants.Keys.USER_DETAILS, Context.MODE_PRIVATE);
        String preferred_lng = sp.getString("PREFERRED_LANGUAGE", "en");
        Utils.setLocale(preferred_lng, this);


        getSupportActionBar().hide();


        // Change status bar color
        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(this.getResources().getColor(R.color.darkAccent));


        logIn = findViewById(R.id.login);
        register = findViewById(R.id.register);
        Eng = findViewById(R.id.English);
        Ar = findViewById(R.id.Arabic);

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomePage.this, LogInPage.class);
                startActivity(intent);
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(WelcomePage.this, Signup.class);
                startActivity(intent);
            }
        });

       if (checkUserLogin()) {
            //user is logged in
         Intent intent = new Intent(WelcomePage.this, MainActivity.class);
         startActivity(intent);
         finish();

        }

        Eng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Eng.setVisibility(View.INVISIBLE);
                Ar.setVisibility(View.VISIBLE);
                setLocale("en");
            }
        });


        Ar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Ar.setVisibility(View.INVISIBLE);
                Eng.setVisibility(View.VISIBLE);
                setLocale("ar");
            }
        });


    }
    @Override
    public void onResume(){
        super.onResume();
        if(sp.getString("PREFERRED_LANGUAGE","").equals("en"))
            Eng.setTypeface(null, Typeface.BOLD);
        else
            Ar.setTypeface(null,Typeface.BOLD);

    }

    private boolean checkUserLogin(){


        if (mAuth.getCurrentUser() != null) {
            return true;
        } else
            return false;

    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

        SharedPreferences sp = WelcomePage.this.getSharedPreferences(Constants.Keys.USER_DETAILS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("PREFERRED_LANGUAGE", lang );
        editor.apply();

        finish();

        Intent refresh = new Intent(this, WelcomePage.class);
        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(refresh);
    }

}
