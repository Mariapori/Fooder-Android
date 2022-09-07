package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.models.Food;

public class FoodDetails extends AppCompatActivity {
    Food food;

    TextView tvFoodDetails, tvFoodDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        food = (Food) getIntent().getSerializableExtra("food");

        tvFoodDetails = findViewById(R.id.tv_food_details);
        tvFoodDescription = findViewById(R.id.tv_food_description);

        tvFoodDetails.setText(
                String.format(
                        "%s · %s€",
                        food.getName(),
                        food.getPrice()
                )
        );
        tvFoodDescription.setText(food.getDescription());
    }
}