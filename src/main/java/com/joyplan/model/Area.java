package com.joyplan.model;

public enum Area {
    CORNICHE("Corniche / Al Khalidiyah", 24.4764, 54.3213),
    MUSHRIF("Al Mushrif / Al Wahda", 24.4560, 54.3860),
    AL_REEM("Al Reem Island", 24.4990, 54.4030),
    AL_MARYAH("Al Maryah Island", 24.5010, 54.3890),
    SAADIYAT("Saadiyat Island", 24.5350, 54.4330),
    AL_QANA("Al Maqta / Al Qana", 24.4160, 54.4990),
    YAS_ISLAND("Yas Island", 24.4890, 54.6060),
    AL_RAHA("Al Raha Beach", 24.4500, 54.5960),
    KHALIFA_CITY("Khalifa City", 24.4190, 54.5770);

    private final String label;
    private final double latitude;
    private final double longitude;

    Area(String label, double latitude, double longitude) {
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLabel() {
        return label;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
