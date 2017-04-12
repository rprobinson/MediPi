/*
 * Copyright 2016  Richard Robinson @ NHS Digital <rrobinson@nhs.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.medipi;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Creates a digital clock display as a simple label. Format of the clock
 * display is day, date month hh:mm a, where: hh Hour in am/pm (1-12) mm Minute
 * in hour ss Second in minute a Am/pm marker Time is the system time for the
 * local timezone.
 */
class DigitalClock extends Label {

    public DigitalClock() {
        bindToTime();
    }

    private void bindToTime() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Calendar time = Calendar.getInstance();
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE d MMM, h:mm a");
                        if (AlertBanner.getInstance().hasAlert("timeserver")) {
                            setText("Xxx XX Xxx, XX:XX XX");
                        } else {
                            setText(simpleDateFormat.format(time.getTime()));
                        }
                    }
                }
                ),
                new KeyFrame(Duration.seconds(1))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }
}
