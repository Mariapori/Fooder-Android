package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Food implements Serializable {
    public String name;
    public String description;
    public double price;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
