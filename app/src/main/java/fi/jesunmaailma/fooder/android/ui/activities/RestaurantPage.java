package fi.jesunmaailma.fooder.android.ui.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import fi.jesunmaailma.fooder.android.adapters.FoodAdapter;
import fi.jesunmaailma.fooder.android.models.Food;
import fi.jesunmaailma.fooder.android.models.Restaurant;
import fi.jesunmaailma.fooder.android.services.FooderDataService;

public class RestaurantPage extends AppCompatActivity {
    public static final String id = "id";
    public static final String name = "name";
    public static final String image = "image";

    ActionBar actionBar;
    Toolbar toolbar;

    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvRestaurantName, tvRestaurantAddress, tvAltText;
    RecyclerView recyclerView;

    // Firebase-tavaraa
    FirebaseAuth auth;
    FirebaseUser user;
    FirebaseAnalytics analytics;

    ProgressBar progressBar;

    MaterialButton mbAddToFavourites, mbRemoveFromFavourites, mbAddLike, mbRemoveLike;

    CoordinatorLayout snackBar;
    CardView cvAltTextContainer;

    List<Food> foodList;
    FoodAdapter foodAdapter;

    Restaurant restaurant;

    FooderDataService service;

    boolean AlreadyFavorite;

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
        AlreadyFavorite = getIntent().getBooleanExtra("isFavorite", false);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        tvRestaurantName = findViewById(R.id.tv_restaurant_name);
        tvRestaurantAddress = findViewById(R.id.tv_restaurant_address);
        tvAltText = findViewById(R.id.tv_alt_text);

        recyclerView = findViewById(R.id.categories_list);

        progressBar = findViewById(R.id.progressBar);

        mbAddToFavourites = findViewById(R.id.mb_add_to_favourites);
        mbRemoveFromFavourites = findViewById(R.id.mb_remove_from_favourites);
        mbAddLike = findViewById(R.id.mb_add_like);
        mbRemoveLike = findViewById(R.id.mb_remove_like);

        cvAltTextContainer = findViewById(R.id.cvAltTextContainer);

        if (AlreadyFavorite) {
            mbAddToFavourites.setVisibility(View.GONE);
            mbRemoveFromFavourites.setVisibility(View.VISIBLE);
        } else {
            mbAddToFavourites.setVisibility(View.VISIBLE);
            mbRemoveFromFavourites.setVisibility(View.GONE);
        }

        snackBar = findViewById(R.id.snackbar);

        foodList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        analytics = FirebaseAnalytics.getInstance(this);

        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, id);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, name);
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, image);
        analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

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

                if (user != null) {
                    getRestaurantById(
                            String.format(
                                    "%sHaeYritys?id=%s",
                                    getResources().getString(R.string.digiruokalista_api_base_url),
                                    restaurant.getId()
                            )
                    );
                } else {
                    getRestaurantById(
                            String.format(
                                    "%sHaeYritys?id=%s",
                                    getResources().getString(R.string.digiruokalista_api_base_url),
                                    restaurant.getId()
                            )
                    );
                    mbAddToFavourites.setVisibility(View.GONE);
                    mbRemoveFromFavourites.setVisibility(View.GONE);
                }
            }
        });

        if (user != null) {
            getRestaurantById(
                    String.format(
                            "%sHaeYritys?id=%s",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            restaurant.getId()
                    )
            );

            mbAddToFavourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToFavourites(String.format(
                            "%sLisaaSuosikki?Kayttaja=%s&YritysID=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail(),
                            restaurant.getId()
                    ));
                }
            });

            mbRemoveFromFavourites.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeFromFavourites(String.format(
                            "%sPoistaSuosikki?Kayttaja=%s&YritysID=%s&secret=AccessToken",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            user.getEmail(),
                            restaurant.getId()
                    ));
                }
            });

            mbAddLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Lisää tykkäyslogiikka tähän.
                    mbAddLike.setVisibility(View.GONE);
                    mbRemoveLike.setVisibility(View.VISIBLE);

                    Snackbar snackbar = Snackbar.make(
                            snackBar,
                            "Tämä ei tee vielä mitään.",
                            Snackbar.LENGTH_LONG
                    );
                    snackbar.setDuration(5000);
                    snackbar.show();
                }
            });

            mbRemoveLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Lisää tykkäyksen poistologiikka tähän.
                    mbRemoveLike.setVisibility(View.GONE);
                    mbAddLike.setVisibility(View.VISIBLE);

                    Snackbar snackbar = Snackbar.make(
                            snackBar,
                            "Tämä ei tee vielä mitään.",
                            Snackbar.LENGTH_LONG
                    );
                    snackbar.setDuration(5000);
                    snackbar.show();
                }
            });
        } else {
            getRestaurantById(
                    String.format(
                            "%sHaeYritys?id=%s",
                            getResources().getString(R.string.digiruokalista_api_base_url),
                            restaurant.getId()
                    )
            );

            mbAddToFavourites.setVisibility(View.GONE);
            mbRemoveFromFavourites.setVisibility(View.GONE);
            mbAddLike.setVisibility(View.GONE);
            mbRemoveLike.setVisibility(View.GONE);
        }
    }

    private void removeFromFavourites(String removedRestarauntURL) {
        service.deleteRestaurantFromFavourites(removedRestarauntURL, new FooderDataService.OnFavouriteDeletedDataResponse() {
            @Override
            public void onResponse(JSONArray response) {
                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Ravintola poistettu suosikeista.",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setDuration(5000);
                snackbar.show();

                mbAddToFavourites.setVisibility(View.VISIBLE);
                mbRemoveFromFavourites.setVisibility(View.GONE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Hupsista!\nRavintolan poisto suosikeista epäonnistui.",
                        Snackbar.LENGTH_INDEFINITE
                );
                snackbar.setAction("Sulje", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
            }
        });
    }

    private void addToFavourites(String addRestaurantURL) {
        service.addRestaurantToFavourites(addRestaurantURL, new FooderDataService.OnFavouriteAddedRestaurantDataResponse() {
            @Override
            public void onResponse(JSONObject response) {
                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Ravintola lisätty suosikkeihin.",
                        Snackbar.LENGTH_LONG
                );
                snackbar.setDuration(5000);
                snackbar.show();

                mbAddToFavourites.setVisibility(View.GONE);
                mbRemoveFromFavourites.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);

                Snackbar snackbar = Snackbar.make(
                        snackBar,
                        "Hupsista!\nRavintolan lisäys suosikkeihin epäonnistui.",
                        Snackbar.LENGTH_INDEFINITE
                );
                snackbar.setAction("Sulje", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        snackbar.dismiss();
                    }
                });
                snackbar.show();
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

                    tvRestaurantName.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                Uri mapsUri = Uri.parse(
                                        String.format(
                                                "geo:0,0?q=%s",
                                                response.getString("nimi")
                                        )
                                );

                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapsUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    if (response.getString("vapaaTeksti").contains("null")) {
                        cvAltTextContainer.setVisibility(View.GONE);
                    } else {
                        cvAltTextContainer.setVisibility(View.VISIBLE);
                        tvAltText.setText(response.getString("vapaaTeksti"));
                    }

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
                                food.setCategoryName(categoryData.getString("nimi"));
                                food.setCategoryDescription(categoryData.getString("kuvaus"));
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
    
    // TODO: Hakutoiminto on vielä epäkunnossa. Korjataan kun ehditään.

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}