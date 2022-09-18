package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.FavouriteAdapter;
import fi.jesunmaailma.fooder.android.models.Favourite;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class FavouritesActivity extends AppCompatActivity {
    public static final String RESTAURANT_DATA_TAG = "TAG";
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    ActionBar actionBar;
    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView rvFavouritesList;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAnalytics analytics;

    CoordinatorLayout snackBar;

    LinearLayout authRequiredContainer;
    MaterialButton mbLogin;

    FavouriteAdapter adapter;
    List<Favourite> favouriteList;
    List<Restaurant> restaurantList;

    Favourite favourite;
    Restaurant restaurant;

    FooderDataService service;

    JSONObject restaurantData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        rvFavouritesList = findViewById(R.id.rv_favourites_list);

        authRequiredContainer = findViewById(R.id.auth_required_container);

        mbLogin = findViewById(R.id.btn_sign_in);

        progressBar = findViewById(R.id.progressBar);

        snackBar = findViewById(R.id.snackbar);

        favouriteList = new ArrayList<>();
        restaurantList = new ArrayList<>();

        adapter = new FavouriteAdapter(favouriteList);
        rvFavouritesList.setAdapter(adapter);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        rvFavouritesList.setLayoutManager(manager);

        service = new FooderDataService(this);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.VISIBLE);
                rvFavouritesList.setVisibility(View.GONE);

                getRestaurants(
                        String.format(
                                "%sHaeYritykset",
                                getResources().getString(R.string.digiruokalista_api_base_url)
                        )
                );
                getFavourites(
                        String.format(
                                "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                                getResources().getString(R.string.digiruokalista_api_base_url),
                                user.getEmail()
                        )
                );
            }
        });

        if (user == null) {
            swipeRefreshLayout.setVisibility(View.GONE);
            rvFavouritesList.setVisibility(View.GONE);
            authRequiredContainer.setVisibility(View.VISIBLE);

            mbLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            });
        } else {
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            rvFavouritesList.setVisibility(View.GONE);
            authRequiredContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

            getRestaurants(
                    String.format(
                            "%sHaeYritykset",
                            getResources().getString(R.string.digiruokalista_api_base_url)
                    )
            );
            getFavourites(
                    String.format(
                            "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail()
                    )
            );
        }
    }

    public void getRestaurants(String url) {
        service.getRestaurants(url, new FooderDataService.OnRestaurantDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rvFavouritesList.setVisibility(View.VISIBLE);

                Log.d(RESTAURANT_DATA_TAG, "onResponse: " + response);

                favouriteList.clear();
                restaurantList.clear();

                for (int i = 0; i < response.length(); i++) {
                    try {
                        restaurantData = response.getJSONObject(i);

                        restaurant = new Restaurant();

                        restaurant.setId(restaurantData.getInt("id"));
                        restaurant.setName(restaurantData.getString("nimi"));
                        restaurant.setAddress(restaurantData.getString("osoite"));
                        restaurant.setCity(restaurantData.getString("kaupunki"));
                        restaurant.setPostalCode(restaurantData.getString("postinumero"));

                        restaurantList.add(restaurant);
                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(RESTAURANT_DATA_TAG, "onError: " + error);

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
                        rvFavouritesList.setVisibility(View.GONE);
                        getRestaurants(
                                String.format(
                                        "%sHaeYritykset",
                                        getResources().getString(R.string.digiruokalista_api_base_url)
                                )
                        );
                        getFavourites(
                                String.format(
                                        "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                                        getResources().getString(R.string.digiruokalista_api_base_url),
                                        user.getEmail()
                                )
                        );
                    }
                });
                snackbar.show();
            }
        });
    }

    public void getFavourites(String url) {
        service.getUserFavourites(url, new FooderDataService.OnFavouriteDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d(RESTAURANT_DATA_TAG, "onResponse: " + response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject favouriteData = response.getJSONObject(i);

                        favourite = new Favourite();

                        favourite.setRestaurantId(favouriteData.getInt("yritysID"));
                        favourite.setUserEmail(favouriteData.getString("kayttaja"));
                        favourite.setName(restaurantData.getString("nimi"));
                        favourite.setCity(restaurantData.getString("kaupunki"));

                        if (favourite.getRestaurantId() == restaurant.getId()) {
                            restaurantList.add(restaurant);
                            favouriteList.add(favourite);
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onError(String error) {
                Log.e(RESTAURANT_DATA_TAG, "onError: " + error);

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
                        rvFavouritesList.setVisibility(View.GONE);
                        getRestaurants(
                                String.format(
                                        "%sHaeYritykset",
                                        getResources().getString(R.string.digiruokalista_api_base_url)
                                )
                        );
                        getFavourites(
                                String.format(
                                        "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                                        getResources().getString(R.string.digiruokalista_api_base_url),
                                        user.getEmail()
                                )
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