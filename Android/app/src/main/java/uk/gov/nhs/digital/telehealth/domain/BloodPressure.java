package uk.gov.nhs.digital.telehealth.domain;

import uk.gov.nhs.digital.telehealth.util.TimestampUtil;

import java.sql.Timestamp;
import java.text.ParseException;

public class BloodPressure {
    private String androidDeviceId;
    private int systolicPressure;
    private int diastolicPressure;
    private BM55User user;
    private int pulseRate;
    private boolean restingIndicator;
    private boolean arrhythmia;
    private GeographicalLocation location;
    private Timestamp measuredTime;

    public BloodPressure(String androidDeviceId, int systolicPressure, int diastolicPressure, BM55User user, int pulseRate, boolean restingIndicator, boolean arrhythmia, GeographicalLocation location, Timestamp measuredTime) {
        this.androidDeviceId = androidDeviceId;
        this.systolicPressure = systolicPressure;
        this.diastolicPressure = diastolicPressure;
        this.user = user;
        this.pulseRate = pulseRate;
        this.restingIndicator = restingIndicator;
        this.arrhythmia = arrhythmia;
        this.location = location;
        this.measuredTime = measuredTime;
    }

    public BloodPressure(byte[] data) {
        this.systolicPressure = data[0] + 25;
        this.diastolicPressure = data[1] + 25;
        this.pulseRate = data[2];
        String day = null;
        String month = null;
        String year = null;
        String hour = null;
        String minute = null;
        BM55User user = null;

        if(data[3]<0) {
            this.restingIndicator = true;
            month = String.valueOf(data[3] + 128);
        } else {
            month = String.valueOf(data[3]);
        }

        if(data[4]<0) {
            this.user = BM55User.B;
            day = String.valueOf(data[4] + 128);
        } else {
            this.user = BM55User.A;
            day = String.valueOf(data[4]);
        }

        hour = String.valueOf(data[5]);
        minute = String.valueOf(data[6]);

        if(data[7]<0) {
            this.arrhythmia = true;
            year = String.valueOf(data[7] + 128 + 2000);
        } else {
            year = String.valueOf(data[7]+ 2000);
        }

        String stringMeasurementTime = year + "-" + month + "-" + day + " " + hour + ":" + minute;
        try {
            this.measuredTime = TimestampUtil.getTimestamp("yyyy-MM-dd hh:mm", stringMeasurementTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public BloodPressure(String androidDeviceId, GeographicalLocation location, byte[] data) {
        this(data);
        this.androidDeviceId = androidDeviceId;
        this.location = location;
    }

    public String getAndroidDeviceId() {
        return androidDeviceId;
    }

    public void setAndroidDeviceId(String androidDeviceId) {
        this.androidDeviceId = androidDeviceId;
    }

    public int getSystolicPressure() {
        return systolicPressure;
    }

    public void setSystolicPressure(int systolicPressure) {
        this.systolicPressure = systolicPressure;
    }

    public int getDiastolicPressure() {
        return diastolicPressure;
    }

    public void setDiastolicPressure(int diastolicPressure) {
        this.diastolicPressure = diastolicPressure;
    }

    public BM55User getUser() {
        return user;
    }

    public void setUser(BM55User user) {
        this.user = user;
    }

    public int getPulseRate() {
        return pulseRate;
    }

    public void setPulseRate(int pulseRate) {
        this.pulseRate = pulseRate;
    }

    public boolean isRestingIndicator() {
        return restingIndicator;
    }

    public void setRestingIndicator(boolean restingIndicator) {
        this.restingIndicator = restingIndicator;
    }

    public boolean isArrhythmia() {
        return arrhythmia;
    }

    public void setArrhythmia(boolean arrhythmia) {
        this.arrhythmia = arrhythmia;
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
        return "BloodPressure:\n" +
                "androidDeviceId='" + androidDeviceId + "\'" +
                "\nsystolicPressure=" + systolicPressure +
                "\ndiastolicPressure=" + diastolicPressure +
                "\nuser=" + user +
                "\npulseRate=" + pulseRate +
                "\nrestingIndicator=" + restingIndicator +
                "\narrhythmia=" + arrhythmia +
                "\nlocation=" + location +
                "\nmeasuredTime=" + measuredTime;
    }
}