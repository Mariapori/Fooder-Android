package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.models.Food;

public class FoodDetails extends AppCompatActivity {
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    ActionBar actionBar;
    Toolbar toolbar;

    Food food;

    TextView tvFoodDetails, tvFoodDescription;

    FirebaseAnalytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        food = (Food) getIntent().getSerializableExtra("food");

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        tvFoodDetails = findViewById(R.id.tv_food_details);
        tvFoodDescription = findViewById(R.id.tv_food_description);

        tvFoodDetails.setText(
                String.format(
                        "%s · %s0 €",
                        food.getName(),
                        food.getPrice()
                ).replace(".", ",")
        );

        if (food.getDescription().contains("null")) {
            tvFoodDescription.setText(
                    String.format(
                            "%s\n%s",
                            "Tällä ruualla ei ole kuvausta.",
                            "Kyseessä voi olla lisuke/annos, jolle ei ole annettu kuvausta."
                    )
            );
            tvFoodDescription.setTypeface(tvFoodDescription.getTypeface(), Typeface.ITALIC);
        } else {
            tvFoodDescription.setText(food.getDescription());
            tvFoodDescription.setTypeface(tvFoodDescription.getTypeface(), Typeface.NORMAL);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
