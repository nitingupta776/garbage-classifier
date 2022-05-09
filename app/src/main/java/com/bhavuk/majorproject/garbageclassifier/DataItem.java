package com.bhavuk.majorproject.garbageclassifier;

public class DataItem {
    private String Id;
    private String Name;
    private String ItemType;
    private String OrganicProbability;
    private String RecycleProbability;
    private String Imageid;
    private boolean IsItemPicked;
    private String Latitude;
    private String Longitude;
    private String Altitude;

    public DataItem(String name, String itemType, String imageid, boolean isItemPicked) {
        Name = name;
        ItemType = itemType;
        Imageid = imageid;
        IsItemPicked = isItemPicked;
    }

    public DataItem() {

    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getItemType() {
        return ItemType;
    }

    public void setItemType(String itemType) {
        ItemType = itemType;
    }

    public String getImageid() {
        return Imageid;
    }

    public void setImageid(String imageid) {
        Imageid = imageid;
    }

    public boolean isItemPicked() {
        return IsItemPicked;
    }

    public void setItemPicked(boolean itemPicked) {
        IsItemPicked = itemPicked;
    }



    public String getRecycleProbability() {
        return RecycleProbability;
    }

    public void setRecycleProbability(String recycleProbability) {
        RecycleProbability = recycleProbability;
    }

    public String getOrganicProbability() {
        return OrganicProbability;
    }

    public void setOrganicProbability(String organicProbability) {
        OrganicProbability = organicProbability;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getAltitude() {
        return Altitude;
    }

    public void setAltitude(String altitude) {
        Altitude = altitude;
    }
}
