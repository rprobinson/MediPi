/*
 Copyright 2016  Richard Robinson @ HSCIC <rrobinson@hscic.gov.uk, rrobinson@nhs.net>

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.medipi.utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.TimeZone;
import javafx.scene.image.ImageView;

/**
 * Utilities class to allow universal access to useful methods or public Objects
 *
 * TODO: Need to extract out these methods from MediPi
 *
 * @author rick@robinsonhq.com
 */
public class Utilities {

    private final Properties properties;

    public static final DateFormat DISPLAY_FORMAT = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
    public static final DateFormat INTERNAL_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat DISPLAY_DOB_FORMAT = new SimpleDateFormat("dd-MMM-yyyy");
    public static final DateFormat INTERNAL_DOB_FORMAT = new SimpleDateFormat("yyyyMMdd");
    public static final DateFormat DISPLAY_TABLE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static final DateFormat INTERNAL_DEVICE_FORMAT = new SimpleDateFormat("yyyy-MM-dd':'HH:mm");
    public static final DateFormat DISPLAY_SCALE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat DISPLAY_SCHEDULE_FORMAT = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    public static final DateFormat INTERNAL_SPINE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss.SSS");
    public static final DateFormat DISPLAY_OXIMETER_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");
    public static final DateFormat ISO8601FORMATDATEMILLI = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    public static final DateFormat ISO8601FORMATDATESECONDS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    public static final DateFormat ISO8601FORMATDATEMINUTES = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");

    /**
     *
     * @param mp
     */
    public Utilities(Properties mp) {
        properties = mp;
        final String TIMEZONE = "medipi.timezone";
        String timezone = properties.getProperty(TIMEZONE);
        if (timezone != null && timezone.length() > 0) {
            ISO8601FORMATDATEMILLI.setTimeZone(TimeZone.getTimeZone(timezone));
            ISO8601FORMATDATESECONDS.setTimeZone(TimeZone.getTimeZone(timezone));
            ISO8601FORMATDATEMINUTES.setTimeZone(TimeZone.getTimeZone(timezone));
        } else {
            ISO8601FORMATDATEMILLI.setTimeZone(TimeZone.getTimeZone("GMT"));
            ISO8601FORMATDATESECONDS.setTimeZone(TimeZone.getTimeZone("GMT"));
            ISO8601FORMATDATEMINUTES.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
    }

    /**
     * Utility method to return an imageView from a MediPi property reference
     *
     * @param property MediPi property reference
     * @param w width of returned imageView
     * @param h height of returned imageView
     * @return ImageView of the property name
     */
    public ImageView getImageView(String property, Integer w, Integer h) {

        String img = properties.getProperty(property);
        ImageView image;
        if (img == null || img.trim().length() == 0) {
            image = new ImageView("/org/medipi/Default.jpg");
        } else {
            image = new ImageView("file:///" + img);
        }
        if (h != null) {
            image.setFitHeight(h);
        }
        if (w != null) {
            image.setFitWidth(w);
        }
        return image;
    }
}
