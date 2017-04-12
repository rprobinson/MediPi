/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.medipi;

import java.util.HashMap;
import java.util.Map;
import javafx.animation.Interpolator;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author riro
 */
public class AlertBanner {
    private ObservableMap<String, String> alertBannerMap;
    private final Label alertBannerMessage = new Label("");
    private ScrollPane alertSP = new ScrollPane();
    
    private AlertBanner() {
            //Create a map object to contain all the alert messages to be displayed on the lower banner
            Map<String, String> map = new HashMap<>();
            alertBannerMap = FXCollections.observableMap(map);
            alertBannerMessage.setAlignment(Pos.TOP_LEFT);
            alertBannerMessage.setWrapText(true);
            alertBannerMessage.setPrefWidth(400);
            alertBannerMessage.setId("lowerbanner-scroll");
            alertBannerMessage.setMinHeight(40);

            alertSP.setMaxWidth(400);
            alertSP.setMinWidth(400);
            alertSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            alertSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            alertSP.setPannable(false);
            alertSP.setContent(alertBannerMessage);
            alertSP.setStyle("-fx-background-color: transparent;");
            // Provides the animated scrolling behavior for the text
            TranslateTransition transTransition = new TranslateTransition();
            transTransition.setDuration(new Duration(7500));
            transTransition.setNode(alertBannerMessage);
            transTransition.setToY(-40);
            transTransition.setFromY(0);
            transTransition.setInterpolator(Interpolator.LINEAR);
            transTransition.setCycleCount(Timeline.INDEFINITE);
            alertBannerMap.addListener(new MapChangeListener() {
                @Override
                public void onChanged(MapChangeListener.Change change) {
                    ObservableMap om = change.getMap();
                    StringBuilder sb = new StringBuilder();
                    om.forEach((k, v) -> {
                        sb.append(v.toString())
                                .append("\n");
                    });
                    Platform.runLater(() -> {
                        alertBannerMessage.setText(sb.toString());
                        alertBannerMessage.setTextFill(Color.RED);
                        double alertMessageHeight = 0;
                        switch (om.size()) {
                            case 0:
                            case 1:
                            case 2:
                                transTransition.jumpTo(Duration.ZERO);
                                transTransition.stop();
                                transTransition.setToY(-40);
                                alertMessageHeight = 40;
                                break;
                            default:
                                alertMessageHeight = (om.size() * 20);
                                alertBannerMessage.setMinHeight(alertMessageHeight + 40);
                                System.out.println("height" + alertBannerMessage.getHeight() + "," + alertBannerMessage.getMinHeight() + "," + alertBannerMessage.getMaxHeight() + "," + alertBannerMessage.getPrefHeight());
                                System.out.println(alertMessageHeight);
                                transTransition.setToY(-alertMessageHeight);
                                transTransition.play();
                                break;
                        }
                    });
                }
            });
    }
    
    public static AlertBanner getInstance() {
        return AlertBannerHolder.INSTANCE;
    }
    
    public ScrollPane getAlertBanner(){
        return alertSP;
    }
    public void addAlert(String classKey, String message){
        alertBannerMap.put(classKey, message);
    }
    public void removeAlert(String classKey){
        alertBannerMap.remove(classKey);
    }
    public boolean hasAlert(String classKey){
        if(alertBannerMap.get(classKey)==null){
            return false;
        } else {
            return true;
        }
    }
    private static class AlertBannerHolder {

        private static final AlertBanner INSTANCE = new AlertBanner();
    }
    
}
