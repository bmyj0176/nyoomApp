package com.example.madproject.pages.bus_times;

public class BusStopPanel {
    private String busStopName;
    private String busStopCode;
    private String streetName;
    private String[] AT;
    private boolean isBookmarked;
    private boolean BMAnimDone;

    public BusStopPanel(String busStopName, String busStopCode, String streetName, String[] AT, boolean isBookmarked, boolean BMAnimDone) {
        this.busStopName = busStopName;
        this.busStopCode = busStopCode;
        this.streetName = streetName;
        this.AT = AT;
        this.isBookmarked = isBookmarked;
        this.BMAnimDone = BMAnimDone;
    }
    public void setAT(String[] AT) {
        this.AT = AT;
    }
    public void toggleIsBookmarked() { isBookmarked = !isBookmarked; }
    public void setBMAnimDone(boolean BMAnimDone) { this.BMAnimDone = BMAnimDone; }
    public String getBusStopName() { return busStopName; }
    public String getBusStopCode() { return busStopCode; }
    public String getStreetName() { return streetName; }
    public String[] getAT() { return AT; }
    public boolean getIsBookmarked() { return isBookmarked; }
    public boolean bookmarkAnimDone() { return BMAnimDone; }
}
