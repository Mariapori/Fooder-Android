package fi.jesunmaailma.fooder.android.models;

import java.io.Serializable;

public class Food implements Serializable {
    public String name;
    public String description;
    public String category;
    public double price;
    public boolean isFood;
    public boolean isVegan;
    public int listPos;

    public String getName() {
        return name;
    }

    public boolean getIsFood() {
        return isFood;
    }

    public void setIsFood(boolean isFood) {
        this.isFood = isFood;
    }

    public boolean getIsVegan() {
        return isVegan;
    }

    public void setIsVegan(boolean isVegan) {
        this.isVegan = isVegan;
    }

    public void setCategory(String category){
        this.category = category;
    }

    public String getCategory(){
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getListPos() {
        return listPos;
    }

    public void setListPos(int listPos) {
        this.listPos = listPos;
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
