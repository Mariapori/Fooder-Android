package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.FavouriteAdapter;
import fi.jesunmaailma.fooder.android.adapters.RestaurantAdapter;
import fi.jesunmaailma.fooder.android.models.Favourite;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    // VAIN virheenjäljitystä varten.
    // public static final String RESTAURANT_DATA_TAG = "TAG";
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView rvRestaurantList;
    ProgressBar progressBar;

    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAnalytics analytics;
    FirebaseFirestore database;
    DocumentReference documentReference;

    TextView tvHeadline;

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    CoordinatorLayout snackBar;

    RestaurantAdapter restaurantAdapter;
    public static List<Restaurant> restaurantList;

    List<Favourite> favouriteList;
    Favourite favourite;

    FooderDataService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        database = FirebaseFirestore.getInstance();

        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        rvRestaurantList = findViewById(R.id.restaurant_list);
        progressBar = findViewById(R.id.progressBar);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tvHeadline = findViewById(R.id.headline);

        drawer = findViewById(R.id.drawer);
        toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                toolbar,
                R.string.openNavDrawer,
                R.string.closeNavDrawer
        );
        navigationView = findViewById(R.id.nav_view);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        snackBar = findViewById(R.id.snackbar);

        restaurantList = new ArrayList<>();
        favouriteList = new ArrayList<>();

        restaurantAdapter = new RestaurantAdapter(restaurantList, favouriteList);
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

                if (user != null) {
                    getFavourites(
                            String.format(
                                    "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                                    getResources().getString(R.string.digiruokalista_api_base_url),
                                    user.getEmail()
                            )
                    );
                }

                getRestaurants(
                        String.format(
                                "%sHaeYritykset",
                                getResources().getString(R.string.digiruokalista_api_base_url)
                        )
                );

            }
        });

        if (user == null) {
            progressBar.setVisibility(View.VISIBLE);
            rvRestaurantList.setVisibility(View.GONE);

            getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
        } else {
            progressBar.setVisibility(View.VISIBLE);
            rvRestaurantList.setVisibility(View.GONE);

            documentReference = database.collection("Users").document(user.getUid());
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot snapshot = task.getResult();
                    if (snapshot.exists()) {
                        tvHeadline.setText(
                                String.format(
                                        "Morjensta pöytään,\n%s!",
                                        snapshot.getString("firstName")
                                )
                        );
                    } else {
                        tvHeadline.setText(
                                String.format(
                                        "Morjensta pöytään,\n%s!",
                                        getFirstName(Objects.requireNonNull(user.getDisplayName()))
                                )
                        );
                    }
                }
            });
            getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
        if(user != null){
            getFavourites(
                    String.format(
                            "%sHaeKayttajanSuosikit?Kayttaja=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail()
                    )
            );
        }
    }

    public String getFirstName(String fullName) {
        int index = fullName.lastIndexOf(" ");
        if (index > -1) {
            return fullName.substring(0, index);
        }
        return fullName;
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
                progressBar.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Tapahtui virhe.",
                        Snackbar.LENGTH_INDEFINITE
                );
                snackbar.setAction("Päivitä", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        rvRestaurantList.setVisibility(View.GONE);
                        getRestaurants(
                                getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset"
                        );
                        if(user != null){
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
                snackbar.show();
            }
        });
    }
    public void getFavourites(String url) {
        service.getUserFavourites(url, new FooderDataService.OnFavouriteDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                favouriteList.clear();
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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
                        getRestaurants(
                                getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset"
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
    public static void closeDrawer(DrawerLayout drawer) {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_profile:
                closeDrawer(drawer);
                startActivity(new Intent(getApplicationContext(), Profile.class));
                break;
            case R.id.mi_favourites:
                closeDrawer(drawer);
                if(user != null) {
                    startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                }else{
                    Snackbar snackbar = Snackbar.make(
                            snackBar,
                            "Et ole kirjautunut sisään.",
                            Snackbar.LENGTH_LONG
                    );
                    snackbar.setDuration(5000);
                    snackbar.show();
                }
                break;
            case R.id.mi_info:
                closeDrawer(drawer);
                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                break;
        }
        return false;
    }
}