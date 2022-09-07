package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.RestaurantAdapter;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class MainActivity extends AppCompatActivity {
    public static final String RESTAURANT_DATA_TAG = "TAG";

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView rvRestaurantList;
    ProgressBar progressBar;

    CoordinatorLayout snackBar;

    RestaurantAdapter restaurantAdapter;
    List<Restaurant> restaurantList;

    FooderDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        rvRestaurantList = findViewById(R.id.restaurant_list);
        progressBar = findViewById(R.id.progressBar);

        snackBar = findViewById(R.id.snackbar);

        progressBar.setVisibility(View.VISIBLE);

        restaurantList = new ArrayList<>();

        restaurantAdapter = new RestaurantAdapter(restaurantList);
        rvRestaurantList.setAdapter(restaurantAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvRestaurantList.setLayoutManager(manager);

        service = new FooderDataService(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                rvRestaurantList.setVisibility(View.GONE);

                getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
            }
        });

        getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
    }

    public void getRestaurants(String url) {
        service.getRestaurants(url, new FooderDataService.OnRestaurantDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
                rvRestaurantList.setVisibility(View.VISIBLE);

                restaurantList.clear();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject restaurantData = response.getJSONObject(i);

                        Restaurant restaurant = new Restaurant();

                        restaurant.setId(restaurantData.getInt("id"));
                        restaurant.setName(restaurantData.getString("nimi"));
                        restaurant.setAddress(restaurantData.getString("osoite"));
                        restaurant.setPostalCode(restaurantData.getString("postinumero"));
                        restaurant.setCity(restaurantData.getString("kaupunki"));

                        restaurantList.add(restaurant);
                        restaurantAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                        rvRestaurantList.setVisibility(View.GONE);
                        getRestaurants(
                                getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset"
                        );
                    }
                });
                snackbar.show();
            }
        });
    }
}