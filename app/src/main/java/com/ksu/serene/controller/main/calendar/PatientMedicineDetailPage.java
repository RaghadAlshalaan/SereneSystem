package com.ksu.serene.controller.main.calendar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ksu.serene.MainActivity;
import com.ksu.serene.model.Medicine;
import com.ksu.serene.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


public class PatientMedicineDetailPage extends AppCompatActivity {

    private TextView MedicineName;
    private TextView StartDay;
    private TextView EndDay;
    private TextView Time;
    private TextView Dose;
    private Button Delete;
    private Medicine medicine;
    private String MedID;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_medicine_detail_page);
        MedID = getIntent().getStringExtra("MedicineID");
        MedicineName = (TextView) findViewById(R.id.MName);
        StartDay = (TextView) findViewById(R.id.MFromDays);
        EndDay = (TextView) findViewById(R.id.MTillDays);
        Time = (TextView) findViewById(R.id.MTime);
        Dose = (TextView) findViewById(R.id.MDose);
        Delete = (Button) findViewById(R.id.DeleteMedicine);

        db.collection("PatientMedicin")
                .document(MedID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                MedicineName.setText(documentSnapshot.get("name").toString());
                StartDay.setText(documentSnapshot.get("Fday").toString());
                EndDay.setText(documentSnapshot.get("Lday").toString());
                Time.setText(documentSnapshot.get("time").toString());
                Dose.setText(documentSnapshot.get("doze").toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PatientMedicineDetailPage.this, "Fails to get data", Toast.LENGTH_LONG).show();
            }
        });

        Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show window dialog with 2 button yes and no
                new AlertDialog.Builder(PatientMedicineDetailPage.this)
                        .setTitle("Delete Medicine Reminder")
                        .setMessage("Are you sure that you want delete the" + MedicineName.getText().toString())
                        .setPositiveButton("Yes, I'm sur", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                db.collection("PatientMedicin")
                                        .document(MedID)
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(PatientMedicineDetailPage.this, "The Med reminder deleted successfully", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent (PatientMedicineDetailPage.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PatientMedicineDetailPage.this, "The Med reminder did not delete", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No, cancel", null)
                        .show();
            }
        });
    }
}
