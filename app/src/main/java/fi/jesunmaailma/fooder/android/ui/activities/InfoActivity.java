package fi.jesunmaailma.fooder.android.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Calendar;
import java.util.Date;

import fi.jesunmaailma.fooder.android.BuildConfig;
import fi.jesunmaailma.fooder.android.R;

public class InfoActivity extends AppCompatActivity {
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    TextView tvCopyright, tvVersion, tvOpenUnsplash;
    MaterialButton mbGithubRepo;

    ActionBar actionBar;
    Toolbar toolbar;

    FirebaseAnalytics analytics;

    Calendar calendar;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        analytics = FirebaseAnalytics.getInstance(this);
        calendar = Calendar.getInstance();

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        tvCopyright = findViewById(R.id.tv_copyright);
        mbGithubRepo = findViewById(R.id.mb_github_repo);
        tvVersion = findViewById(R.id.tv_version);
        tvOpenUnsplash = findViewById(R.id.tvOpenUnsplash);

        tvCopyright.setText(
                String.format(
                        "© %s Jesun Maailma",
                        calendar.get(Calendar.YEAR)
                )
        );

        mbGithubRepo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int toolbarColor = getResources().getColor(R.color.green);
                String githubRepoUrl = "https://github.com/JTG69YT/Fooder-Android";

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setShowTitle(true);
                builder.setToolbarColor(toolbarColor);

                CustomTabsIntent intent = builder.build();
                intent.launchUrl(InfoActivity.this, Uri.parse(githubRepoUrl));
            }
        });

        tvOpenUnsplash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int toolbarColor = getResources().getColor(R.color.green);
                String unsplashUrl = "https://unsplash.com";

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                builder.setShowTitle(true);
                builder.setToolbarColor(toolbarColor);

                CustomTabsIntent intent = builder.build();
                intent.launchUrl(InfoActivity.this, Uri.parse(unsplashUrl));
            }
        });

        tvVersion.setText(
                String.format(
                        "Versio: %s (%s)",
                        BuildConfig.VERSION_NAME,
                        BuildConfig.VERSION_CODE
                )
        );
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}