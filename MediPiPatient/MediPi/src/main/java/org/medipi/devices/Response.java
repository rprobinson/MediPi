/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi.devices;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.medipi.MediPiMessageBox;
import org.medipi.model.AlertDO;

/**
 *
 * @author riro
 */
public class Response {

    public static final String NO_RESULT = "NO_RESULT";

    private final ObjectProperty<BorderPane> deviceImage;
    private SimpleStringProperty deviceName;
    private ObjectProperty<Instant> measurementTime;
    private SimpleStringProperty result;
    private final ObjectProperty<Button> detailsButton;
    private SimpleStringProperty detailsString;
    private final String initialButtonMessageText;
    private ArrayList<AlertDO> alertDO = new ArrayList<>();

    public Response(BorderPane deviceImage, String deviceName, Instant measurementTime, String result, String ibtm) {

        this.deviceImage = new SimpleObjectProperty<>(deviceImage);
        this.deviceName = new SimpleStringProperty(deviceName);
        this.measurementTime = new SimpleObjectProperty<>(measurementTime);
        this.result = new SimpleStringProperty(result);
        Button details = new Button("Details");
        details.setId("button-response");
        this.initialButtonMessageText = ibtm;

        this.detailsString = new SimpleStringProperty(initialButtonMessageText);
        details.setOnAction((ActionEvent t) -> {
            MediPiMessageBox.getInstance().makeMessage(detailsString.get());
        });
        details.disableProperty().bind(
                Bindings.when(detailsString.isEqualTo(initialButtonMessageText))
                .then(true)
                .otherwise(false));
        detailsButton = new SimpleObjectProperty<>(details);
        alertDO = new ArrayList<>();
    }

    public void reset() {
        measurementTime = new SimpleObjectProperty<>(null);;
        result = new SimpleStringProperty(NO_RESULT);
        this.detailsString.set(initialButtonMessageText);
        setDetailsButtonMessageText(initialButtonMessageText);
        alertDO = new ArrayList<>();
    }

    public boolean updateAlert(AlertDO adoNew) {

        // Is there an alert present already?
        if (!this.getAlertDO().isEmpty()) {
            // is it an update of the original or a different data type
            int loop  = 0;
            boolean isNotInList = true;
            for (AlertDO ado : alertDO) {
                if (adoNew.getAttributeName().equals(ado.getAttributeName())) {
                    isNotInList = false;
                    // replace with later data of the same kind
                    if (adoNew.getDataValueTime().after(ado.getDataValueTime())) {
                        alertDO.set(loop, adoNew);
                        this.setMeasurementTime(calculateLatestMeasurementTime());
                        this.setResult(calculateStatus());
                        this.setDetailsButtonMessageText(calculateDetailsButtonMessageText());
                        return true;
                    } else{ // otherwise get out of loop as we dont need this
                        return false;
                    }
                }
                loop++;
            }
            if(isNotInList){
                    this.addAlertDO(adoNew);
                    this.setMeasurementTime(calculateLatestMeasurementTime());
                    this.setResult(calculateStatus());
                    this.setDetailsButtonMessageText(calculateDetailsButtonMessageText());
                    return true;                
            }

        } else {
            this.addAlertDO(adoNew);
            this.setMeasurementTime(calculateLatestMeasurementTime());
            this.setResult(calculateStatus());
            this.setDetailsButtonMessageText(calculateDetailsButtonMessageText());
            return true;
        }
        return false;
    }

    private Instant calculateLatestMeasurementTime() {
        Date latestMeasurementTime = null;
        for (AlertDO ado : alertDO) {
            if (latestMeasurementTime == null || latestMeasurementTime.before(ado.getDataValueTime())) {
                latestMeasurementTime = ado.getDataValueTime();
            }
        }
        return latestMeasurementTime.toInstant();

    }

    private String calculateDetailsButtonMessageText() {
        StringBuilder details = new StringBuilder();
        for (AlertDO ado : alertDO) {
            if(details.length()!=0){
                details.append("\n");
            }
            details.append(ado.getAlertText());
        }
        return details.toString();

    }

    private String calculateStatus() {
        // If there's only one entry and therefore status just return that 
        if (alertDO.size() == 1) {
            return alertDO.get(0).getStatus();
        }
        // otherwise loop through statuses and find which is highest
        int highestStatus = 0;
        String status = "";
        for (AlertDO ado : alertDO) {
            switch (ado.getStatus()) {
                case Responses.OUT_OF_THRESHOLD_STATUS:
                    if (highestStatus <= Responses.OUT_OF_THRESHOLD) {
                        highestStatus = Responses.OUT_OF_THRESHOLD;
                        status = Responses.OUT_OF_THRESHOLD_STATUS;
                    }
                    break;
                case Responses.CANNOT_CALCULATE_STATUS:
                    if (highestStatus <= Responses.CANNOT_CALCULATE) {
                        highestStatus = Responses.CANNOT_CALCULATE;
                        status = Responses.CANNOT_CALCULATE_STATUS;
                    }
                    break;
                case Responses.NO_RESULT_STATUS:
                    if (highestStatus <= Responses.NO_RESULT) {
                        highestStatus = Responses.NO_RESULT;
                        status = Responses.NO_RESULT_STATUS;
                    }
                    break;
                case Responses.IN_THRESHOLD_STATUS:
                    if (highestStatus <= Responses.IN_THRESHOLD) {
                        highestStatus = Responses.IN_THRESHOLD;
                        status = Responses.IN_THRESHOLD_STATUS;
                    }
                    break;
                default:
                    break;
            }
        }

        return status;

    }

    public void setDeviceName(String deviceName) {
        this.deviceName = new SimpleStringProperty(deviceName);
    }

    public ArrayList<AlertDO> getAlertDO() {
        return alertDO;
    }

    public void addAlertDO(AlertDO alertDO) {
        this.alertDO.add(alertDO);
    }

    public void setMeasurementTime(Instant measurementTime) {
        this.measurementTime = new SimpleObjectProperty<>(measurementTime);
    }

    public void setResult(String result) {
        this.result = new SimpleStringProperty(result);
    }

    public void setDetailsButtonMessageText(String s) {
        this.detailsString.set(s);
    }

    public ObjectProperty<BorderPane> getDeviceImage() {
        return deviceImage;
    }

    public String getDeviceName() {
        return deviceName.get();
    }

    public ObjectProperty<Instant> getMeasurementTime() {
        return measurementTime;
    }

    public String getResult() {
        return result.get();
    }

    public ObjectProperty<Button> getDetails() {
        return detailsButton;
    }

    public ObjectProperty<Instant> measurementTimeProperty() {
        return measurementTime;
    }

    public ObjectProperty<BorderPane> deviceImageProperty() {
        return deviceImage;
    }

    public ObjectProperty<Button> detailsButtonProperty() {
        return detailsButton;
    }
}
