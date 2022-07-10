package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Category implements Serializable {
    public int restaurantId;
    public String name;

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
