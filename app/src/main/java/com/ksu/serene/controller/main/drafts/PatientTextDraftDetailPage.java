package com.ksu.serene.controller.main.drafts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import com.ksu.serene.R;

public class PatientTextDraftDetailPage extends AppCompatActivity {

    private TextView title;
    private TextView subj;
    private Button delete;
    private Button edit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String TDID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_text_draft_detail_page);
        TDID = getIntent().getStringExtra("TextDraftID");
        title = findViewById(R.id.TitleTextD);
        subj = findViewById(R.id.SubjtextD);
        delete = findViewById(R.id.DeleteTextD);
        edit = findViewById(R.id.EditTextD);

        db.collection("TextDraft")
                .document(TDID)
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                title.setText(documentSnapshot.get("title").toString());
                subj.setText(documentSnapshot.get("text").toString());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PatientTextDraftDetailPage.this, "Fails to get data", Toast.LENGTH_LONG);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //show window dialog with 2 button yes and no
                new AlertDialog.Builder(PatientTextDraftDetailPage.this)
                        .setTitle("Delete Text Draft")
                        .setMessage("Are you sure that you want delete the " + title.getText().toString())
                        .setPositiveButton("Yes, I'm sure", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, final int i) {
                                db.collection("TextDraft")
                                        .document(TDID)
                                        .delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(PatientTextDraftDetailPage.this, "The Text Draft deleted successfully", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent (PatientTextDraftDetailPage.this, MainActivity.class);
                                                startActivity(intent);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(PatientTextDraftDetailPage.this, "The Text Draft did not delete", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No, cancel", null)
                        .show();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientTextDraftDetailPage.this, EditPatientTextDraftPage.class);
                intent.putExtra("TextID", TDID);
                intent.putExtra("TextTitle", title.getText().toString());
                intent.putExtra("TextSubj", subj.getText().toString());
                startActivity(intent);
            }
        });

    }
}
