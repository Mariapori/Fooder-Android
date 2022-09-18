package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Favourite implements Serializable {
    public int restaurantId;
    public int favoriteId;
    public String restaurantName;
    private String restaurantCity;

    public int getRestaurantId() {
        return restaurantId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public String getRestaurantName(){
        return restaurantName;
    }

    public void setRestaurantName(String name){
        this.restaurantName = name;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    // VAIN virheenjäljitystä varten.
    @Override
    public String toString() {
        return "Favourite{" +
                "restaurantId=" + restaurantId +
                '}';
    }

    public void setRestaurantCity(String restaurantCity) {
        this.restaurantCity = restaurantCity;
    }

    public String getRestaurantCity() {
        return restaurantCity;
    }
}
