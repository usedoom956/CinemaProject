package com.example.cinema.SecondPages.CinemaHelpClasses;

import java.util.List;

public class CinemaDataClass {
    private String name;
    private String venue;
    private int freeSeats;
    private String date;
    private String imagePath;

    private List<Boolean> seats;

    private List<Boolean> isReserved;

    public CinemaDataClass() {}

    public CinemaDataClass(String name, String venue, int freeSeats, String date, String imagePath) {
        this.name = name;
        this.venue = venue;
        this.freeSeats = freeSeats;
        this.date = date;
        this.imagePath = imagePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getFreeSeats() {
        return freeSeats;
    }

    public void setFreeSeats(int freeSeats) {
        this.freeSeats = freeSeats;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public void setIsReserved(List<Boolean> isReserved) {
        this.isReserved = isReserved;
    }

    public void setSeats(List<Boolean> seats) {
        this.seats = seats;
    }

    public List<Boolean> getIsReserved() {
        return isReserved;
    }

    public List<Boolean> getSeats() {
        return seats;
    }
}


