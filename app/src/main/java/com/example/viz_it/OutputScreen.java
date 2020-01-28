package com.example.viz_it;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class OutputScreen extends AppCompatActivity {

    FirebaseAuth fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output_screen);

        fAuth = FirebaseAuth.getInstance();

        Button logout = findViewById(R.id.logout);
        Button saveButton = findViewById(R.id.status);



        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getApplicationContext());
                builder.setTitle("Status");
                builder.setMessage("Welcome Users");
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                Snackbar.make(findViewById(android.R.id.content),"Welcome" ,Snackbar.LENGTH_LONG).show();

            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), Phone.class));
                finish();
            }
        });

    }
}


