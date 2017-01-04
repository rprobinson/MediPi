/*
 *
 * Copyright (C) 2016 Krishna Kuntala @ Mastek <krishna.kuntala@mastek.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.medipi.devices.drivers.domain;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/**
 * The Class BF480Measurement converts the integer data array received from
 * serial interface to meaningful values related to weight measurement.
 */
public class BF480Measurement implements Comparable<BF480Measurement> {

    private double weight;
    private double bodyFat;
    private double water;
    private double muscles;
    private Instant measuredTime;

    /**
     * Instantiates a new BF480 measurement.
     *
     * @param weight the weight
     * @param bodyFat the body fat
     * @param water the water
     * @param muscles the muscles
     * @param measuredTime the measured time
     */
    public BF480Measurement(final double weight, final double bodyFat, final double water, final double muscles, final Instant measuredTime) {
        this.weight = weight;
        this.bodyFat = bodyFat;
        this.water = water;
        this.muscles = muscles;
        this.measuredTime = measuredTime;
    }

    /**
     * Instantiates a new BF480 measurement object.
     *
     * @param reading the reading integers which will be converted to meaningful
     * values after decoding them
     * @param readingStartByteNumber the byte number from where the users
     * reading will start. First 0 to 5 integers belongs to user 1, 6 to 11
     * belongs to user 2 and so on
     */
    public BF480Measurement(final int[] reading, final int readingStartByteNumber) {
        final int date = reading[readingStartByteNumber + 4];
        final int time = reading[readingStartByteNumber + 5];
        final int year = 1920 + (date >> 9);
        final int month = date >> 5 & 0xf;
        final int day = date & 0x1f;

        final int hour = time >> 8;
        final int minute = time & 0xff;

        final String stringMeasurementTime = year + "-" + month + "-" + day + " " + hour + ":" + minute;
        try {
            ZonedDateTime zdt = LocalDateTime.of(year, month, day, hour, minute).atZone(ZoneId.of("Z"));
            this.measuredTime = zdt.toInstant();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        this.weight = Double.valueOf(reading[readingStartByteNumber + 0]) / 10;
        this.bodyFat = Double.valueOf(reading[readingStartByteNumber + 1]) / 10;
        this.water = Double.valueOf(reading[readingStartByteNumber + 2]) / 10;
        this.muscles = Double.valueOf(reading[readingStartByteNumber + 3]) / 10;
    }

    /**
     * Method to get the weight.
     *
     * @return the weight
     */
    public double getWeight() {
        return this.weight;
    }

    /**
     * Method to set the weight.
     *
     * @param weight the new weight
     */
    public void setWeight(final double weight) {
        this.weight = weight;
    }

    /**
     * Method to get the body fat.
     *
     * @return the body fat
     */
    public double getBodyFat() {
        return this.bodyFat;
    }

    /**
     * Method to set the body fat.
     *
     * @param bodyFat the new body fat
     */
    public void setBodyFat(final double bodyFat) {
        this.bodyFat = bodyFat;
    }

    /**
     * Method to get the water.
     *
     * @return the water
     */
    public double getWater() {
        return this.water;
    }

    /**
     * Method to set the water.
     *
     * @param water the new water
     */
    public void setWater(final double water) {
        this.water = water;
    }

    /**
     * Method to get the muscles.
     *
     * @return the muscles
     */
    public double getMuscles() {
        return this.muscles;
    }

    /**
     * Method to set the muscles.
     *
     * @param muscles the new muscles
     */
    public void setMuscles(final double muscles) {
        this.muscles = muscles;
    }

    /**
     * Method to get the measured time.
     *
     * @return the measured time
     */
    public Instant getMeasuredTime() {
        return this.measuredTime;
    }

    /**
     * Method to set the measured time.
     *
     * @param measuredTime the new measured time
     */
    public void setMeasuredTime(final Instant measuredTime) {
        this.measuredTime = measuredTime;
    }

    @Override
    public int compareTo(final BF480Measurement measurement) {
        return this.getMeasuredTime().compareTo(measurement.getMeasuredTime());
    }

    /**
     * Method to get all the values in a string array.
     *
     * @return all the values in string array format
     * @throws ParseException the parse exception
     */
    public ArrayList<String> getAllValues() throws ParseException {
        final ArrayList<String> values = new ArrayList<>();
        values.add(this.getMeasuredTime().toString());
        values.add(String.valueOf(this.getWeight()));
        values.add(String.valueOf(this.getBodyFat()));
        values.add(String.valueOf(this.getWater()));
        values.add(String.valueOf(this.getMuscles()));
        return values;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "WeightMeasurement [weight=" + this.weight + ", bodyFat=" + this.bodyFat + "%, water=" + this.water + "%, muscles=" + this.muscles + "%, measuredTime=" + this.measuredTime + "]";
    }
}
