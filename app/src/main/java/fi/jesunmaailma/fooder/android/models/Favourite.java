package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Favourite implements Serializable {
    public int restaurantId;
    public String userEmail;
    public String name;
    public String city;

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    // VAIN virheenjäljitystä varten.
    @Override
    public String toString() {
        return "Favourite{" +
                "restaurantId=" + restaurantId +
                ", userEmail='" + userEmail + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
