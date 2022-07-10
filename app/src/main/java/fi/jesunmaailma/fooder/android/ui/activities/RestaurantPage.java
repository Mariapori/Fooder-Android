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
import android.util.Log;
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
import fi.jesunmaailma.fooder.android.adapters.CategoryAdapter;
import fi.jesunmaailma.fooder.android.models.Category;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class RestaurantPage extends AppCompatActivity {
    public static final String CATEGORY_TAG = "TAG";

    ActionBar actionBar;
    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvRestaurantName, tvRestaurantAddress;
    RecyclerView recyclerView;

    ProgressBar progressBar;

    CoordinatorLayout snackBar;

    List<Category> categoryList;
    CategoryAdapter categoryAdapter;

    Restaurant restaurant;

    FooderDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_page);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        restaurant = (Restaurant) getIntent().getSerializableExtra("restaurant");

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvRestaurantAddress = findViewById(R.id.tv_restaurant_address);

        recyclerView = findViewById(R.id.categories_list);

        progressBar = findViewById(R.id.progressBar);

        snackBar = findViewById(R.id.snackbar);

        categoryList = new ArrayList<>();

        service = new FooderDataService(this);

        categoryAdapter = new CategoryAdapter(categoryList);
        recyclerView.setAdapter(categoryAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

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

                getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + restaurant.getId());
            }
        });

        getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + restaurant.getId());
    }

    public void getRestaurantById(String url) {
        service.getRestaurantById(url, new FooderDataService.OnRestaurantByIdDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                // Log.d(CATEGORY_TAG, "Data: " + response);

                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                categoryList.clear();

                try {
                    tvRestaurantName.setText(response.getString("nimi"));
                    tvRestaurantAddress.setText(
                            String.format(
                                    "%s, %s %s",
                                    response.getString("osoite"),
                                    response.getString("postinumero"),
                                    response.getString("kaupunki")
                            )
                    );

                    JSONObject foodMenuData = response.getJSONObject("ruokalista");
                    JSONArray categoriesData = foodMenuData.getJSONArray("kategoriat");

                    for (int i = 0; i < categoriesData.length(); i++) {
                        JSONObject categoryData = categoriesData.getJSONObject(i);

                        Category category = new Category();

                        category.setRestaurantId(response.getInt("id"));
                        category.setName(categoryData.getString("nimi"));

                        categoryList.add(category);
                        categoryAdapter.notifyDataSetChanged();
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
                                        + "HaeYritys?id=" + restaurant.getId()
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