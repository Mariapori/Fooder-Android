package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import fi.jesunmaailma.fooder.android.R;

public class OnboardingActivity extends AppCompatActivity {

    MaterialButton mbSignIn, mbSkip;

    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        mbSignIn = findViewById(R.id.btn_sign_in);
        mbSkip = findViewById(R.id.btn_skip);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Kirjaudu sisään -nappi vie kirjautumissivulle
        mbSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        mbSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (user != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }
}