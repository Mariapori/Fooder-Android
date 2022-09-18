package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Favourite implements Serializable {
    public int restaurantId;

    public int getRestaurantId() {
        return restaurantId;
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
}
