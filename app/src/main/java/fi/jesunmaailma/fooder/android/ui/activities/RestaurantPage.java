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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.FoodAdapter;
import fi.jesunmaailma.fooder.android.models.Food;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class RestaurantPage extends AppCompatActivity {
    // VAIN virheenjäljitystä varten.
    public static final String RESTAURANT_DATA_TAG = "TAG";

    ActionBar actionBar;
    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvRestaurantName, tvRestaurantAddress;
    RecyclerView recyclerView;

    // Firebase Auth
    FirebaseAuth auth;
    FirebaseUser user;

    ProgressBar progressBar;

    MaterialButton mbAddToFavourites, mbRemoveFromFavourites;

    CoordinatorLayout snackBar;

    List<Food> foodList;
    FoodAdapter foodAdapter;

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

        mbAddToFavourites = findViewById(R.id.mb_add_to_favourites);
        // TODO: Digiruokalista.com:n ylläpitäjälle tiedoksi: Lisää suosikin poistamisendpoint, jolloin käyttäjä pystyy poistamaan ravintolan suosikeista omalla napilla.
        mbRemoveFromFavourites = findViewById(R.id.mb_remove_from_favourites);

        snackBar = findViewById(R.id.snackbar);

        foodList = new ArrayList<>();

        service = new FooderDataService(this);

        foodAdapter = new FoodAdapter(foodList);
        recyclerView.setAdapter(foodAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        progressBar.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);

                getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + restaurant.getId());
            }
        });

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        getRestaurantById(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritys?id=" + restaurant.getId());

        if (user != null) {
            mbAddToFavourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String addRestaurantURL = String.format(
                            "%sLisaaSuosikki?Kayttaja=%s&YritysID=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail(),
                            restaurant.getId()
                            );

                    addToFavourites(addRestaurantURL);
                }
            });
        }
    }

    private void addToFavourites(String addRestaurantURL) {
        service.addRestaurantToFavourites(addRestaurantURL, new FooderDataService.OnFavouriteAddedRestaurantDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(RESTAURANT_DATA_TAG,"onResponse: " + response.toString());

                mbAddToFavourites.setVisibility(View.GONE);
                mbRemoveFromFavourites.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                Log.e(RESTAURANT_DATA_TAG, "onError: " + error);
            }
        });
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
                        JSONArray foodsData = categoryData.getJSONArray("ruuat");
                        try {
                            for (int ii = 0; ii < foodsData.length(); ii++) {
                                JSONObject foodData = foodsData.getJSONObject(ii);

                                Food food = new Food();

                                food.setListPos(ii);
                                food.setName(foodData.getString("nimi"));
                                food.setCategory(categoryData.getString("nimi"));
                                food.setDescription(foodData.getString("kuvaus"));
                                food.setPrice(foodData.getDouble("hinta"));
                                food.setIsFood(foodData.getBoolean("annos"));

                                //Jos ei ole annos, ei lisätä listaan.
                                //TODO: Luodaan "lisukkeille" oma lista.
                                if (food.getIsFood()) {
                                    foodList.add(food);
                                }

                            }
                        } catch (JSONException exx) {
                            exx.printStackTrace();
                        }
                        foodAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Hupsista!\nTarkista Internet-yhteys.",
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
                                        + restaurant.getId()
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