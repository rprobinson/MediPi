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

import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

/**
 * Creates an LED style element which allows actions such as blinking and setting colour
  */
public class LED extends Circle implements Runnable {

    private RadialGradient onGradient;
    private RadialGradient offGradient;
    private int BlinkRateMillis;
    private boolean stopBlinking = false;

    /**
     * Constructor method to create LED
     * @throws Exception
     */
    public LED() throws Exception {
        this.setCenterX(100);
        this.setCenterY(100);
        this.setRadius(7);
        final Effect glow = new Glow(0.2);
        this.setStroke(Color.BLACK);
        this.setStrokeWidth(1);
        this.setEffect(glow);
        onGradient = new RadialGradient(60, .1, 97, 97, 7, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.GREY));
        this.setFill(onGradient);
    }

    /**
     * Method to call and describe the blinking action of the LED
     * @param onColour Colour to show when LED is blinking ON
     * @param offColour Colour to show when LED is blinking OFF
     * @param blinkRateMillis rate at which to blink
     */
    public void blink(Color onColour, Color offColour, int blinkRateMillis) {
        onGradient = new RadialGradient(60, .1, 97, 97, 7, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, onColour));
        offGradient = new RadialGradient(60, .1, 97, 97, 7, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, offColour));
        BlinkRateMillis = blinkRateMillis;
        stopBlinking = false;
        new Thread(this).start();
    }

    /**
     * Method to call the LED to constant colour
     * @param onColour the colour of the LED
     */
    public void ledOn(Color onColour) {
        stopBlinking = true;
        onGradient = new RadialGradient(60, .1, 97, 97, 7, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, onColour));
        this.setFill(onGradient);
    }

    @Override
    public void run() {
        while (!stopBlinking) {

            this.setFill(onGradient);
            try {
                Thread.sleep(BlinkRateMillis);
            } catch (InterruptedException ex) {
            }
            if(stopBlinking){
                break;
            }
            this.setFill(offGradient);
            try {
                Thread.sleep(BlinkRateMillis);
            } catch (InterruptedException ex) {
            }
        }

    }

}
