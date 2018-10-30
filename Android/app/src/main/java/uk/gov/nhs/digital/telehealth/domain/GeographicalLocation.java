package uk.gov.nhs.digital.telehealth.domain;

public class GeographicalLocation {
    private double latitude;
    private double longitude;
    private String address;

    public GeographicalLocation() {
    }

    public GeographicalLocation(double latitude, double longitude, String address) {
        this();
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
    }

    public GeographicalLocation(double latitude, double longitude) {
        this(latitude, longitude, null);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "GeographicalLocation{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", address='" + address + '\'' +
                '}';
    }
}
