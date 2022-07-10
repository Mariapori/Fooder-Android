package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.FoodAdapter;
import fi.jesunmaailma.fooder.android.models.Category;
import fi.jesunmaailma.fooder.android.models.Food;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class CategoryPage extends AppCompatActivity {
    ActionBar actionBar;
    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvCategoryName;
    RecyclerView recyclerView;

    CoordinatorLayout snackBar;

    ProgressBar progressBar;

    List<Food> foodList;
    FoodAdapter foodAdapter;

    Category category;

    FooderDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_page);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        category = (Category) getIntent().getSerializableExtra("category");

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        tvCategoryName = findViewById(R.id.tv_category_name);

        tvCategoryName.setText(category.getName());

        recyclerView = findViewById(R.id.food_list);
        progressBar = findViewById(R.id.progressBar);

        snackBar = findViewById(R.id.snackbar);

        foodList = new ArrayList<>();

        foodAdapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(foodAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        service = new FooderDataService(this);

        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);

                getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + category.getRestaurantId());
            }
        });

        getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + category.getRestaurantId());
    }

    public void getRestaurantById(String url) {
        service.getRestaurantById(url, new FooderDataService.OnRestaurantByIdDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                foodList.clear();

                try {
                    JSONObject foodMenuData = response.getJSONObject("ruokalista");
                    JSONArray categoriesData = foodMenuData.getJSONArray("kategoriat");

                    for (int i = 0; i < categoriesData.length(); i++) {
                        JSONObject categoryData = categoriesData.getJSONObject(i);
                        JSONArray foodsData = categoryData.getJSONArray("ruuat");

                        for (int j = 0; j < foodsData.length(); j++) {
                            JSONObject foodData = foodsData.getJSONObject(j);

                            Food food = new Food();

                            food.setName(foodData.getString("nimi"));
                            food.setDescription(foodData.getString("kuvaus"));
                            food.setPrice(foodData.getDouble("hinta"));

                            foodList.add(food);
                            foodAdapter.notifyDataSetChanged();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        String.format("Virhe:\n%s", error),
                        Snackbar.LENGTH_INDEFINITE
                );
                snackbar.setAction("Päivitä", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        swipeRefreshLayout.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.GONE);
                        getRestaurantById(
                                getResources().getString(R.string.digiruokalista_api_base_url)
                                + "HaeYritys?id="
                                + category.getRestaurantId()
                        );
                    }
                });
                snackbar.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}