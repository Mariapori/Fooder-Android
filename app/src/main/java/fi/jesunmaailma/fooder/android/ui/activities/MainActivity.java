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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
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

import fi.jesunmaailma.fooder.android.R;
import fi.jesunmaailma.fooder.android.adapters.RestaurantAdapter;
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

    LinearLayout authRequiredContainer;

    TextView tvHeadline;
    MaterialButton mbLogin;

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;

    CoordinatorLayout snackBar;

    RestaurantAdapter restaurantAdapter;
    List<Restaurant> restaurantList;

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
        authRequiredContainer = findViewById(R.id.auth_required_container);
        mbLogin = findViewById(R.id.loginBtn);

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

        if (user == null) {
            authRequiredContainer.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
            rvRestaurantList.setVisibility(View.GONE);

            mbLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            });
        } else {
            authRequiredContainer.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);

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
                                        user.getDisplayName()
                                )
                        );
                    }
                }
            });
            getRestaurants(getResources().getString(R.string.digiruokalista_api_base_url) + "HaeYritykset");
        }
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
            case R.id.mi_info:
                closeDrawer(drawer);
                startActivity(new Intent(getApplicationContext(), InfoActivity.class));
                break;
        }
        return false;
    }
}