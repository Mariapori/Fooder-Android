package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.models.Food;

public class FoodDetails extends AppCompatActivity {
    ActionBar actionBar;
    Toolbar toolbar;

    Food food;

    TextView tvFoodDetails, tvFoodDescription;

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

        tvFoodDetails = findViewById(R.id.tv_food_details);
        tvFoodDescription = findViewById(R.id.tv_food_description);

        tvFoodDetails.setText(
                String.format(
                        "%s · %s0 €",
                        food.getName(),
                        food.getPrice()
                )
        );
        tvFoodDescription.setText(food.getDescription());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
