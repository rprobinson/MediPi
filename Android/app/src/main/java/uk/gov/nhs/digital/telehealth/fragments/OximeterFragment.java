package uk.gov.nhs.digital.telehealth.fragments;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.dev.ops.common.domain.ContextInfo;
import com.dev.ops.common.utils.HttpUtil;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;

import uk.gov.nhs.digital.telehealth.activities.HomeScreenActivity;
import uk.gov.nhs.digital.telehealth.R;
import uk.gov.nhs.digital.telehealth.domain.GeographicalLocation;
import uk.gov.nhs.digital.telehealth.domain.HeartRate;
import uk.gov.nhs.digital.telehealth.connection.service.USBConnectionService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;

public class OximeterFragment extends Fragment {
    private HomeScreenActivity activity;
    private TextView usbDeviceInfo;
    private TextView valueSpO2;
    private TextView valuePRbpm;
    private TextView valueCurrentLocation;

    private String androidDeviceId;
    private boolean continueProcess;
    private ContextInfo contextInfo;
    private RestTemplate restTemplate;
    private String url = "http://104.155.90.134:10060/heartRate/";// Cloud
    //private String url = "http://192.168.0.19:10060/heartRate/";//Home
    //private String url = "http://192.168.6.140:10060/heartRate/";//MMobile

    private GeographicalLocation location;
    private USBConnectionService connectionService;

    private static final int VENDOR_ID = 0x10c4;
    private static final int PRODUCT_ID = 0xea60;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View InputFragmentView = inflater.inflate(R.layout.activity_capture_heart_rate, container, false);
        return inflater.inflate(R.layout.activity_capture_heart_rate, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeScreenActivity) context).onSectionAttached(1);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getView();
        activity = (HomeScreenActivity) getActivity();
        usbDeviceInfo = (TextView) view.findViewById(R.id.usbDeviceInfo);
        valueSpO2 = (TextView) view.findViewById(R.id.valueSpO2);
        valuePRbpm = (TextView) view.findViewById(R.id.valuePRbpm);
        valueCurrentLocation = (TextView) view.findViewById(R.id.valueCurrentLocation);

        usbDeviceInfo.setMovementMethod(new ScrollingMovementMethod());
        androidDeviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
        connectionService = new USBConnectionService();

        Button stopButton = (Button) view.findViewById(R.id.stopProcess);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                continueProcess = false;
            }
        });

        Button startButton = (Button) view.findViewById(R.id.startProcess);
        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                usbDeviceInfo.setText("");
                continueProcess = true;

                location = activity.getGeoLocation();
                displayGeographicalAddress(location);

                new Thread(new Runnable() {
                    UsbSerialPort port = null;
                    public void run() {
                        try {
                            contextInfo = new ContextInfo("MediPi", "EasyHeartRate");
                            UsbDevice device = connectionService.getDevice(activity, VENDOR_ID, PRODUCT_ID);
                            final UsbSerialDriver driver = connectionService.getUsbDriverByDevice(activity, device);
                            UsbDeviceConnection connection = (UsbDeviceConnection) connectionService.getConnection(activity, device);

                            // Read some data! Most have just one port (port 0).
                            port = driver.getPorts().get(0);

                            port.open(connection);
                            port.setParameters(19200, 8, 1, 0);
                            byte[] startBytes = new byte[]{(byte) 0xf5, (byte) 0xf5};
                            port.write(startBytes, 1000);
                            while (continueProcess) {
                                boolean objectInitialized = false;
                                byte inputBuffer[] = new byte[16];
                                port.read(inputBuffer, 1000);
                                HeartRate heartRate = null;
                                for (final byte inputByte : inputBuffer) {
                                    if (inputByte > 0) {
                                        if (!objectInitialized) {
                                            heartRate = new HeartRate(androidDeviceId, Integer.valueOf(inputByte), location);
                                            objectInitialized = true;
                                        } else {
                                            heartRate.setPercentageSpO2(Integer.valueOf(inputByte));
                                            heartRate.setMeasuredTime(new Timestamp(System.currentTimeMillis()));
                                            final int heartBeats = heartRate.getPulseRate();
                                            final int percentageSpO2 = heartRate.getPercentageSpO2();
                                            objectInitialized = false;

                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    valuePRbpm.setText(String.valueOf(heartBeats));
                                                    valueSpO2.setText(String.valueOf(percentageSpO2));
                                                }
                                            });
                                            final HttpEntity<?> entity = HttpUtil.getEntityWithHeaders(contextInfo, heartRate);
                                            final HeartRate responseHeartRate = restTemplate.exchange(url, HttpMethod.POST, entity, HeartRate.class).getBody();
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    valuePRbpm.setText(R.string.default_empty_string);
                                    valueSpO2.setText(R.string.default_empty_string);
                                    valueCurrentLocation.setText(R.string.default_empty_string);
                                }
                            });
                        } catch (final Exception e) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usbDeviceInfo.setText("\n" + e.getMessage());
                                }
                            });
                        } finally {
                            try {
                                if(port != null) {
                                    port.close();
                                }
                            } catch (final Exception e) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usbDeviceInfo.setText("Unable to communicate with the device.\n\n" + e.getMessage());
                                    }
                                });
                            }
                        }
                    }
                }).start();
            }
        });
    }

    private void displayGeographicalAddress(final String address) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (continueProcess && StringUtils.isNotEmpty(address)) {
                    valueCurrentLocation.setText(address);
                } else {
                    valueCurrentLocation.setText(R.string.default_empty_string);
                }
            }
        });
    }

    private void displayGeographicalAddress(final GeographicalLocation location) {
        if(location != null) {
            displayGeographicalAddress(location.getAddress());
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    valueCurrentLocation.setText(R.string.message_location_untraceable);
                }
            });
        }
    }
}