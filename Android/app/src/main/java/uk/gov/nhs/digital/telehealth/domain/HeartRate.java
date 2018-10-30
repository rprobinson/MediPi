package uk.gov.nhs.digital.telehealth.domain;

import android.provider.Settings;

import java.sql.Timestamp;

public class HeartRate {
    private String androidDeviceId;
    private int pulseRate;
    private int percentageSpO2;
    private GeographicalLocation location;
    private Timestamp measuredTime;

    public HeartRate() {

    }

    public HeartRate(String androidDeviceId, int pulseRate, int percentageSpO2, GeographicalLocation location, Timestamp measuredTime) {
        this();
        this.androidDeviceId = androidDeviceId;
        this.pulseRate = pulseRate;
        this.percentageSpO2 = percentageSpO2;
        this.location = location;
        this.measuredTime = measuredTime;
    }

    public HeartRate(String androidDeviceId, int pulseRate, GeographicalLocation location) {
        this(androidDeviceId, pulseRate, 0, location, null);
    }

    public HeartRate(String androidDeviceId, int pulseRate) {
        this(androidDeviceId, pulseRate, null);
    }

    public String getAndroidDeviceId() {
        return androidDeviceId;
    }

    public void setAndroidDeviceId(String androidDeviceId) {
        this.androidDeviceId = androidDeviceId;
    }

    public int getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(int pulseRate) {
        this.pulseRate = pulseRate;
    }

    public int getPercentageSpO2() {
        return percentageSpO2;
    }

    public void setPercentageSpO2(int percentageSpO2) {
        this.percentageSpO2 = percentageSpO2;
    }

    public GeographicalLocation getLocation() {
        return location;
    }

    public void setLocation(GeographicalLocation location) {
        this.location = location;
    }

    public Timestamp getMeasuredTime() {
        return measuredTime;
    }

    public void setMeasuredTime(Timestamp measuredTime) {
        this.measuredTime = measuredTime;
    }

    @Override
    public String toString() {
        return "HeartRate{" +
                "androidDeviceId='" + androidDeviceId + '\'' +
                ", pulseRate=" + pulseRate +
                ", percentageSpO2=" + percentageSpO2 +
                ", location=" + location +
                ", measuredTime=" + measuredTime +
                '}';
    }
}
