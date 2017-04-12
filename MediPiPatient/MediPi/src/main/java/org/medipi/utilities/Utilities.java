/*
 Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>

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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import javafx.scene.image.ImageView;

/**
 * Utilities class to allow universal access to useful methods or public Objects
 *
 *
 * @author rick@robinsonhq.com
 */
public class Utilities {

    private final Properties properties;

    // pre Java 8 Date formatters used for graph axes
    public static final DateFormat INTERNAL_FORMAT_DATE = new SimpleDateFormat("yyyyMMddHHmmss");
    public static final DateFormat DISPLAY_SCALE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd");
    public static final DateFormat DISPLAY_OXIMETER_TIME_FORMAT_DATE = new SimpleDateFormat("HH:mm:ss");

    public static final DateTimeFormatter DISPLAY_DOB_FORMAT = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
//    public static final DateTimeFormatter DISPLAY_DEVICE_FORMAT_LOCALTIME = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DISPLAY_DEVICE_FORMAT_LOCALTIME = DateTimeFormatter.ofPattern("EEE d MMM, h:mma").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter DISPLAY_SCHEDULE_FORMAT_LOCALTIME = DateTimeFormatter.ofPattern("EEE d MMM, h:mma").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter INTERNAL_FORMAT_UTC = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withZone(ZoneId.of("Z"));
    public static final DateTimeFormatter INTERNAL_SPINE_FORMAT_UTC = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSS").withZone(ZoneId.of("Z"));
    public static final DateTimeFormatter DISPLAY_FORMAT_LOCALTIME = DateTimeFormatter.ofPattern("EEE d MMM yyyy HH:mm:ss z").withZone(ZoneId.systemDefault());
    public static final DateTimeFormatter ISO8601FORMATDATEMILLI_UTC = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("Z"));
    public static final DateTimeFormatter DISPLAY_TABLE_FORMAT_LOCALTIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /**
     *
     * @param mp
     */
    public Utilities(Properties mp) {
        properties = mp;
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
        return getImageView(property, w, h, true);
    }

    /**
     * Utility method to return an imageView from a MediPi property reference
     *
     * @param property MediPi property reference
     * @param w width of returned imageView
     * @param h height of returned imageView
     * @return ImageView of the property name
     */
    public ImageView getImageView(String property, Integer w, Integer h, boolean defaultImageWhenNotAvailable) {

        String img = properties.getProperty(property);
        ImageView image;
        if (img == null || img.trim().length() == 0) {
            if (!defaultImageWhenNotAvailable) {
                image = new ImageView();
            } else {
                image = new ImageView("/org/medipi/Default.jpg");
            }
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
