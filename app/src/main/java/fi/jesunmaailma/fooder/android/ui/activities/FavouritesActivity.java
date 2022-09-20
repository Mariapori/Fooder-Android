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

    Favourite favourite;

    FooderDataService service;

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

                if (user != null) {
                    getFavourites(
                            String.format(
                                    "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                                    getResources().getString(R.string.digiruokalista_api_base_url),
                                    user.getEmail()
                            )
                    );
                }
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

            getFavourites(
                    String.format(
                            "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail()
                    )
            );
        }
    }

    public void getFavourites(String url) {
        service.getUserFavourites(url, new FooderDataService.OnFavouriteDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                swipeRefreshLayout.setRefreshing(false);
                swipeRefreshLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rvFavouritesList.setVisibility(View.VISIBLE);

                favouriteList.clear();

                Log.d(RESTAURANT_DATA_TAG, "onResponse: " + response);

                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject favouriteData = response.getJSONObject(i);

                        favourite = new Favourite();

                        favourite.setFavoriteId(favouriteData.getInt("id"));
                        favourite.setRestaurantId(favouriteData.getInt("yritysID"));
                        for (int j = 0; j < MainActivity.restaurantList.size(); j++) {
                            Restaurant rafla = MainActivity.restaurantList.get(j);
                            if(favourite.getRestaurantId() == rafla.getId()){
                                favourite.setRestaurantName(rafla.getName());
                                favourite.setRestaurantCity(rafla.getCity());
                                break;
                            }
                        }
                        favouriteList.add(favourite);
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

    @Override
    protected void onResume() {
        super.onResume();
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        rvFavouritesList.setVisibility(View.GONE);
        authRequiredContainer.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        getFavourites(
                String.format(
                        "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                        getResources().getString(R.string.digiruokalista_api_base_url),
                        user.getEmail()
                )
        );
    }
}