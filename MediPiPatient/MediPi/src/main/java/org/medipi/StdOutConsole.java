/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi;

import java.io.IOException;
import java.io.OutputStream;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 * @author riro
 */
public class StdOutConsole extends OutputStream {

    private final TextArea output;
    private StringBuilder sb = new StringBuilder();

    public StdOutConsole(TextArea ta) {
        this.output = ta;
    }

    @Override
    public void write(int i) throws IOException {
        sb.append(String.valueOf((char) i));
        if (i == 10) {
            Platform.runLater(new Runnable() {
                public void run() {
                    output.appendText(sb.toString());
                    sb = new StringBuilder();
                }
            });
        }
    }
}
