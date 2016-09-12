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
package org.medipi.devices;

import extfx.scene.chart.DateAxis;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.DateStringConverter;
import org.medipi.DashboardTile;
import org.medipi.MediPi;
import org.medipi.utilities.Utilities;
import org.medipi.model.DeviceDataDO;


/**
 * Class to display and handle the functionality for a generic Diagnostic Scale
 * Medical Device.
 *
 * Dependent on the view mode the UI displays either a graphical representation
 * of the results (a series of graphs: weight, water, muscle and fat) or a
 * visual guide to using the device. When data is taken from the device metadata
 * is added to identify it. A single most recent weight measurement is displayed
 * in large type
 *
 * This class expects each line of data coming in from the driver to start with
 * the following in order:
 *
 * START, LOOP: n (where n is the loop number), DATA:x (where x is a line of
 * char separated data), END. All individual data points within a line are
 * separated using the configurable separator
 *
 * JavaFX has no implementation for a Date axis in its graphs so the extFX
 * library has been used (Published under the MIT OSS licence. This may need to
 * be altered/changed (https://bitbucket.org/sco0ter/extfx)
 *
 * TODO: This class expect measurements for weight, %water, %muscle and %fat
 * which for any specific scale might not be appropriate
 *
 * @author rick@robinsonhq.com
 */
@SuppressWarnings("restriction")
public abstract class Scale extends Device {

	private final String DEVICE_TYPE = "Diagnostic Scale";
	private final static String PROFILEID = "urn:nhs-en:profile:DiagnosticScale";
	protected Button downloadButton;
	protected ArrayList<String[]> deviceData = new ArrayList<>();
	// property to indicate whether data has been recorded for this device
	protected BooleanProperty hasData = new SimpleBooleanProperty(false);
	private VBox scaleWindow;
	//defining a weightSeries
	LineChart<Date, Number> weightLineChart;
	XYChart.Series weightSeries;
	private NumberAxis weightYAxis;
	private DateAxis weightXAxis;
	private XYChart.Data<Date, Number> weightXYData;

	//defining a body fat Series
	LineChart<Date, Number> bodyFatLineChart;
	XYChart.Series bodyFatSeries;
	private NumberAxis bodyFatYAxis;
	private DateAxis bodyFatXAxis;
	private XYChart.Data<Date, Number> bodyFatXYData;

	//defining a %water Series
	LineChart<Date, Number> waterLineChart;
	XYChart.Series waterSeries;
	private DateAxis waterXAxis;
	private NumberAxis waterYAxis;
	private XYChart.Data<Date, Number> waterXYData;

	//defining a %muscle Series
	LineChart<Date, Number> muscleLineChart;
	XYChart.Series muscleSeries;
	private DateAxis muscleXAxis;
	private NumberAxis muscleYAxis;
	private XYChart.Data<Date, Number> muscleXYData;

	private VBox deviceWindow;
	private Label lastWeightDB;
	private Label lastWeight;
	private double minWeight = 0;
	private double maxWeight = 0;
	private double minBodyFat = 0;
	private double maxBodyFat = 0;
	private double minWater = 0;
	private double maxWater = 0;
	private double minMuscle = 0;
	private double maxMuscle = 0;
	private double previousWeight;
	private double previousBodyFat;
	private double previousWater;
	private double previousMuscle;
	private Instant previousDate = null;
	protected ProgressBar downProg = new ProgressBar(0.0F);

	private int weightDP = 0;
	private int bodyFatDP = 0;
	private int waterDP = 0;
	private int muscleDP = 0;
	private final StringProperty weightProperty = new SimpleStringProperty("");
	private Node dataBox;
    protected HBox weightHBox;
    private ArrayList<String> metadata = new ArrayList<>();

	/**
	 * This is the data separator from the MediPi.properties file
	 *
	 */
	protected String separator;

	/**
	 * This defines how many steps there are in the progress bar - 0 means that
	 * no progressbar is shown
	 */
	protected Double progressBarResolution = null;

	/**
	 * Constructor for Generic Diagnostic scale
	 *
	 */
	public Scale() {
	}

	/**
	 * Initiation method called for this Element.
	 *
	 * Successful initiation of the this class results in a null return. Any
	 * other response indicates a failure with the returned content being a
	 * reason for the failure
	 *
	 * @return populated or null for whether the initiation was successful
	 * @throws java.lang.Exception
	 */
	@Override
	public String init() throws Exception {

		String uniqueDeviceName = getClassTokenName();
		separator = medipi.getDataSeparator();
		ImageView iw = medipi.utils.getImageView("medipi.images.arrow", 20, 20);
		iw.setRotate(90);
		downloadButton = new Button("Download", iw);
		downloadButton.setId("button-download");
		scaleWindow = new VBox();
		scaleWindow.setPadding(new Insets(0, 5, 0, 5));
		scaleWindow.setSpacing(5);
		scaleWindow.setMinSize(800, 350);
		scaleWindow.setMaxSize(800, 350);
		downProg.setVisible(false);
		HBox buttonHbox = new HBox();
		buttonHbox.setSpacing(10);
		buttonHbox.getChildren().add(downloadButton);
		if(progressBarResolution > 0D) {
			buttonHbox.getChildren().add(downProg);
		}
		//Decide whether to show basic or advanced view
		if(medipi.isBasicDataView()) {
			Guide guide = new Guide(MediPi.ELEMENTNAMESPACESTEM + uniqueDeviceName);
			dataBox = guide.getGuide();
		} else {
			//Setup the results graph
			Instant i  = Instant.now();
			weightXAxis = new DateAxis(Date.from(i.minus(1,ChronoUnit.DAYS)), Date.from(i.plus(1,ChronoUnit.DAYS)));
			StringConverter sc = new DateStringConverter(Utilities.DISPLAY_SCALE_FORMAT_DATE);
			weightXAxis.setTickLabelFormatter(sc);
			weightXAxis.setLabel("Time");
			bodyFatXAxis = new DateAxis(Date.from(i.minus(1,ChronoUnit.DAYS)), Date.from(i.plus(1,ChronoUnit.DAYS)));
			bodyFatXAxis.setTickLabelFormatter(sc);
			bodyFatXAxis.setLabel("Time");
			waterXAxis = new DateAxis(Date.from(i.minus(1,ChronoUnit.DAYS)), Date.from(i.plus(1,ChronoUnit.DAYS)));
			waterXAxis.setTickLabelFormatter(sc);
			waterXAxis.setLabel("Time");
			muscleXAxis = new DateAxis(Date.from(i.minus(1,ChronoUnit.DAYS)), Date.from(i.plus(1,ChronoUnit.DAYS)));
			muscleXAxis.setTickLabelFormatter(sc);
			muscleXAxis.setLabel("Time");
			weightYAxis = new NumberAxis("Kg", 0, 10, 0.5);
			bodyFatYAxis = new NumberAxis("%", 0, 10, 0.5);
			waterYAxis = new NumberAxis("%", 0, 10, 0.5);
			muscleYAxis = new NumberAxis("%", 0, 10, 0.5);
			//creating the chart
			weightLineChart = new LineChart<>(weightXAxis, weightYAxis);
			weightLineChart.setLegendVisible(false);
			bodyFatLineChart = new LineChart<>(bodyFatXAxis, bodyFatYAxis);
			bodyFatLineChart.setLegendVisible(false);
			waterLineChart = new LineChart<>(waterXAxis, waterYAxis);
			waterLineChart.setLegendVisible(false);
			muscleLineChart = new LineChart<>(muscleXAxis, muscleYAxis);
			muscleLineChart.setLegendVisible(false);

			weightLineChart.setTitle("Weight");
			bodyFatLineChart.setTitle("%Body Fat");
			waterLineChart.setTitle("%Water");
			muscleLineChart.setTitle("%Muscle");
			weightLineChart.setCreateSymbols(false);
			bodyFatLineChart.setCreateSymbols(false);
			waterLineChart.setCreateSymbols(false);
			muscleLineChart.setCreateSymbols(false);
			// The 4 graphs are set within a tabbed pane
			TabPane tp = new TabPane();
			Tab weightTab = new Tab();
			weightTab.setClosable(false);
			weightTab.setContent(weightLineChart);
			weightTab.setText("Weight");
			tp.getTabs().add(weightTab);
			Tab bodyFatTab = new Tab();
			bodyFatTab.setClosable(false);
			bodyFatTab.setContent(bodyFatLineChart);
			bodyFatTab.setText("%Body Fat");
			tp.getTabs().add(bodyFatTab);
			Tab waterTab = new Tab();
			waterTab.setContent(waterLineChart);
			waterTab.setText("%Water");
			waterTab.setClosable(false);
			tp.getTabs().add(waterTab);
			Tab muscleTab = new Tab();
			muscleTab.setContent(muscleLineChart);
			muscleTab.setText("%Muscle");
			muscleTab.setClosable(false);
			tp.getTabs().add(muscleTab);
			//defining a weightSeries
			deviceWindow = new VBox();
			deviceWindow.setPadding(new Insets(0, 5, 0, 5));
			deviceWindow.setSpacing(5);
			deviceWindow.setAlignment(Pos.CENTER);
			deviceWindow.setMinWidth(600);
            deviceWindow.getChildren().addAll(
                    tp
            );
			dataBox = deviceWindow;
		}
		// create the large result box for the last measurement
		// downloaded from the device
		// Mass reading
		lastWeight = new Label("--");
		lastWeightDB = new Label("");

		weightHBox = new HBox();
		weightHBox.setAlignment(Pos.CENTER);
		weightHBox.setId("resultsbox");
		weightHBox.setPrefWidth(200);
		Label kg = new Label("Kg");
		kg.setId("resultstext");
		kg.setStyle("-fx-font-size:10px;");
        weightHBox.getChildren().addAll(
                lastWeight,
                kg
        );
		//create the main window HBox
		HBox dataHBox = new HBox();
        dataHBox.getChildren().addAll(
                dataBox,
                weightHBox
        );
        scaleWindow.getChildren().addAll(
                dataHBox,
                new Separator(Orientation.HORIZONTAL)
        );
		// set main Element window
		window.setCenter(scaleWindow);
		setButton2(buttonHbox);

		downloadButton();

		// successful initiation of the this class results in a null return
		return null;
	}

	private void downloadButton() {
		// Setup download button action to run in its own thread
		downloadButton.setOnAction((ActionEvent t) -> {
			resetDevice();
			downloadData();
		});
	}

	/**
	 * method to get the generic Type of the device
	 *
	 * @return generic type of device e.g. Blood Pressure
	 */
	@Override
	public String getType() {
		return DEVICE_TYPE;
	}

	@Override
	public String getProfileId() {
		return PROFILEID;
	}

	// initialises the device window and the data behind it
	@Override
	public void resetDevice() {
		deviceData = new ArrayList<>();
		hasData.set(false);
		metadata.clear();
		if(!medipi.isBasicDataView()) {
			if(weightSeries != null) {
				weightSeries.getData().remove(0, weightDP);
			}
			weightLineChart.getData().removeAll(weightSeries);
			weightSeries = new XYChart.Series();
			weightLineChart.getData().add(weightSeries);

			if(bodyFatSeries != null) {
				bodyFatSeries.getData().remove(0, bodyFatDP);
			}
			bodyFatLineChart.getData().removeAll(bodyFatSeries);
			bodyFatSeries = new XYChart.Series();
			bodyFatLineChart.getData().add(bodyFatSeries);
			if(waterSeries != null) {
				waterSeries.getData().remove(0, waterDP);
			}
			waterLineChart.getData().removeAll(waterSeries);
			waterSeries = new XYChart.Series();
			waterLineChart.getData().add(waterSeries);
			if(muscleSeries != null) {
				muscleSeries.getData().remove(0, muscleDP);
			}
			muscleLineChart.getData().removeAll(muscleSeries);
			muscleSeries = new XYChart.Series();
			muscleLineChart.getData().add(muscleSeries);
			weightDP = 0;
			bodyFatDP = 0;
			waterDP = 0;
			muscleDP = 0;
		}
		lastWeight.setText("--");
		lastWeightDB.setText("");
		weightProperty.setValue("");
	}

	/**
	 * Method which returns a booleanProperty which UI elements can be bound to,
	 * to discover whether there is data to be downloaded
	 *
	 * @return BooleanProperty signalling the presence of downloaded data
	 */
	@Override
	public BooleanProperty hasDataProperty() {
		return hasData;
	}

	/**
     * Gets a DeviceDataDO representation of the data
	 *
     * @return DevicedataDO containing the payload
	 */
	@Override
    public DeviceDataDO getData() {
        DeviceDataDO payload = new DeviceDataDO(UUID.randomUUID().toString());
		StringBuilder sb = new StringBuilder();
		//Add MetaData
		sb.append("metadata->persist->medipiversion->").append(medipi.getVersion()).append("\n");
        for (String s : metadata) {
			sb.append("metadata->persist->").append(s).append("\n");
		}
		sb.append("metadata->subtype->").append(getName()).append("\n");
		sb.append("metadata->datadelimiter->").append(medipi.getDataSeparator()).append("\n");
        if (scheduler!=null) {
            sb.append("metadata->scheduleeffectivedate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getCurrentScheduledEventTime())).append("\n");
            sb.append("metadata->scheduleexpirydate->").append(Utilities.ISO8601FORMATDATEMILLI_UTC.format(scheduler.getNextScheduledEventTime())).append("\n");
        }
        sb.append("metadata->columns->")
        	.append("iso8601time").append(medipi.getDataSeparator())
        	.append("weight").append(medipi.getDataSeparator())
        	.append("bodyfat").append(medipi.getDataSeparator())
        	.append("water").append(medipi.getDataSeparator())
        	.append("muscle").append("\n");
        sb.append("metadata->format->")
        	.append("DATE").append(medipi.getDataSeparator())
        	.append("DOUBLE").append(medipi.getDataSeparator())
        	.append("DOUBLE").append(medipi.getDataSeparator())
        	.append("DOUBLE").append(medipi.getDataSeparator())
        	.append("DOUBLE").append("\n");
        sb.append("metadata->units->")
                .append("NONE").append(medipi.getDataSeparator())
                .append("Kg").append(medipi.getDataSeparator())
                .append("%").append(medipi.getDataSeparator())
                .append("%").append(medipi.getDataSeparator())
                .append("%").append("\n");
		// Add Downloaded data
        for (String[] s : deviceData) {
			sb.append(s[0]);
			sb.append(separator);
			sb.append(s[1]);
			sb.append(separator);
			sb.append(s[2]);
			sb.append(separator);
			sb.append(s[3]);
			sb.append(separator);
			sb.append(s[4]);
			sb.append("\n");
		}
        payload.setProfileId(PROFILEID);
        payload.setPayload(sb.toString());
        return payload;
	}

	/**
	 * method to return the component to the dashboard
	 *
	 * @return @throws Exception
	 */
	@Override
	public BorderPane getDashboardTile() throws Exception {
		DashboardTile dashComponent = new DashboardTile(this);
		dashComponent.addTitle(getType());
		dashComponent.addOverlay(lastWeightDB, "Kg");
		return dashComponent.getTile();
	}

	/**
	 * Add data to the graph
	 *
	 * Private method to add data to the internal structure and propogate it to
	 * the UI
	 *
	 * @param i in UNIX epoch time format
	 * @param weight in Kg
	 * @param bodyFat %
	 * @param water %
	 * @param muscle %
	 */
	public void addDataPoint(Instant i, double weight, double bodyFat, double water, double muscle) {

		//weight graph - this is expected for all data points
		if(!medipi.isBasicDataView()) {
			weightXYData = new XYChart.Data<>(Date.from(i), weight);
			weightXYData.setNode(new AnnotateNode(previousWeight, weight));
			weightSeries.getData().add(weightXYData);
			weightDP++;
			if(weight > maxWeight) {
				maxWeight = weight;
			}
			if(weight < minWeight || minWeight == 0) {
				minWeight = weight;
			}
			if(previousDate == null) {
				weightXAxis.setLowerBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				bodyFatXAxis.setLowerBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				waterXAxis.setLowerBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				muscleXAxis.setLowerBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
			} else if(i.isAfter(previousDate)) {
				weightXAxis.setUpperBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				bodyFatXAxis.setUpperBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				waterXAxis.setUpperBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
				muscleXAxis.setUpperBound(Date.from(i.minus(1,ChronoUnit.DAYS)));
			}
			weightYAxis.setLowerBound(Math.round(minWeight) - 0.5);
			weightYAxis.setUpperBound(Math.round(maxWeight) + 0.5);
		}
		lastWeight.setText(String.valueOf(weight));
		lastWeightDB.setText(String.valueOf(weight));
		weightProperty.setValue(String.valueOf(weight));

		// All other data points may or may not be recorded - this is dependent
		// on whether the patient has taken the measurement in bare feet as these
		// measurements are taken using body resistivity.
		// Thus there will be all or nothing of these results
		// %body fat graph
		if(!medipi.isBasicDataView()) {
			if(bodyFat != 0) {
				bodyFatXYData = new XYChart.Data<>(Date.from(i), bodyFat);
				bodyFatXYData.setNode(new AnnotateNode(previousBodyFat, bodyFat));
				bodyFatSeries.getData().add(bodyFatXYData);
				bodyFatDP++;
				if(bodyFat > maxBodyFat) {
					maxBodyFat = bodyFat;
				}
				if(bodyFat < minBodyFat || minBodyFat == 0) {
					minBodyFat = bodyFat;
				}
				bodyFatYAxis.setLowerBound(Math.round(minBodyFat) - 0.5);
				bodyFatYAxis.setUpperBound(Math.round(maxBodyFat) + 0.5);
				previousBodyFat = bodyFat;
			}
			//%water graph
			if(water != 0) {
				waterXYData = new XYChart.Data<>(Date.from(i), water);
				waterXYData.setNode(new AnnotateNode(previousWater, water));
				waterSeries.getData().add(waterXYData);
				waterDP++;
				if(water > maxWater) {
					maxWater = water;
				}
				if(water < minWater || minWater == 0) {
					minWater = water;
				}
				waterYAxis.setLowerBound(Math.round(minWater) - 0.5);
				waterYAxis.setUpperBound(Math.round(maxWater) + 0.5);
				previousWater = water;
			}
			if(muscle != 0) {
				muscleXYData = new XYChart.Data<>(Date.from(i), muscle);
				muscleXYData.setNode(new AnnotateNode(previousMuscle, muscle));
				muscleSeries.getData().add(muscleXYData);
				muscleDP++;
				if(muscle > maxMuscle) {
					maxMuscle = muscle;
				}
				if(muscle < minMuscle || minMuscle == 0) {
					minMuscle = muscle;
				}
				muscleYAxis.setLowerBound(Math.round(minMuscle) - 0.5);
				muscleYAxis.setUpperBound(Math.round(maxMuscle) + 0.5);
				previousMuscle = muscle;
			}
			previousWeight = weight;
			previousDate = i;
		}
	}

	private Date getDate(Date d, int i) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(d);
		cal.add(Calendar.DAY_OF_YEAR, i);
		return cal.getTime();
	}

	/**
	 * Abstract method to download data from the device driver
	 *
	 * @return bufferedReader
	 */
	public abstract void downloadData();

	/**
	 * ----------------------INNER CLASS----------------------------------------
	 * Class which displays the point value of the data point within a square
	 * box on the line
	 */
	class AnnotateNode extends StackPane {

		AnnotateNode(double priorValue, double value) {
			setPrefSize(15, 15);

			final Label label = createDataThresholdLabel(priorValue, value);

			getChildren().setAll(label);
			toFront();
		}

		private Label createDataThresholdLabel(double priorValue, double value) {
			final Label label = new Label(value + "");
			label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
			label.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

			if(priorValue == 0) {
				label.setTextFill(Color.DARKGRAY);
			} else if(value > priorValue) {
				label.setTextFill(Color.FORESTGREEN);
			} else {
				label.setTextFill(Color.FIREBRICK);
			}

			label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
			return label;
		}
	}

}
