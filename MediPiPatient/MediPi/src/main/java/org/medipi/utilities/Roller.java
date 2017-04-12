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

import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * UI class which extends Label and gives functionality to incrementally add or
 * subtract the single digit label display. These individual digits can be
 * grouped together to form a manual input interface.
 *
 * @author rick@robinsonhq.com
 */
public class Roller extends Label {

    private static final String UNFILLED = "-";
    VBox rollerVB = new VBox();
    Button plus = new Button("+");
    Button minus = new Button("-");
    int currentValue = -1;
    private boolean hasValue = false;

    public Roller(int width, int height, int min, int max) {
        this.setText(UNFILLED);
        plus.setMaxSize(width, height / 2);
        plus.setMinSize(width, height / 2);
        plus.setId("button-plusminus");
        minus.setMaxSize(width, height / 2);
        minus.setMinSize(width, height / 2);
        minus.setId("button-plusminus");
        this.setId("resultstextroller");
        this.setAlignment(Pos.CENTER);
        this.setMaxSize(width, height);
        this.setMinSize(width, height);
        rollerVB.setAlignment(Pos.CENTER);
        rollerVB.getChildren().addAll(
                plus,
                this,
                minus
        );
        plus.disableProperty().bind(this.disableProperty());
        minus.disableProperty().bind(this.disableProperty());

        minus.setOnAction((ActionEvent t) -> {

            if (currentValue != -1) {
                currentValue--;
            }
            if (currentValue < min) {
                hasValue = false;
                currentValue = -1;
                this.setText(UNFILLED);
            } else {
                hasValue = true;
                this.setText(Integer.toString(currentValue));
            }
        });
        plus.setOnAction((ActionEvent t) -> {

            if (currentValue == -1) {
                currentValue = min;
            } else if (currentValue == max) {
            } else {
                currentValue++;

            }
            hasValue = true;
            if (currentValue > max) {
                this.setText(Integer.toString(currentValue--));
            } else {
                this.setText(Integer.toString(currentValue));
            }
        });
    }

    public boolean hasValue() {

        return hasValue;
    }

    public int getValue() {

        return Integer.valueOf(this.getText());
    }

    public VBox getRoller() {

        return rollerVB;
    }

    public void reset() {
        currentValue = -1;
        hasValue = false;
        this.setText("-");
    }

}
