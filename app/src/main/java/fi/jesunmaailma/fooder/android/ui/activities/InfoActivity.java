package fi.jesunmaailma.fooder.android.ui.activities;

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

import fi.jesunmaailma.fooder.android.BuildConfig;
import fi.jesunmaailma.fooder.android.R;

public class InfoActivity extends AppCompatActivity {
    TextView tvCopyright, tvVersion;
    MaterialButton mbGithubRepo;

    ActionBar actionBar;
    Toolbar toolbar;

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

        tvCopyright = findViewById(R.id.tv_copyright);
        mbGithubRepo = findViewById(R.id.mb_github_repo);
        tvVersion = findViewById(R.id.tv_version);

        tvCopyright.setText(
                String.format(
                        "© %s Jesun Maailma",
                        "2022"
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