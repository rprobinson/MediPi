package uk.gov.nhs.digital.telehealth.fragments;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dev.ops.common.domain.ContextInfo;
import uk.gov.nhs.digital.telehealth.R;
import uk.gov.nhs.digital.telehealth.activities.HomeScreenActivity;
import uk.gov.nhs.digital.telehealth.domain.BloodPressure;
import uk.gov.nhs.digital.telehealth.domain.GeographicalLocation;
import uk.gov.nhs.digital.telehealth.connection.service.USBConnectionService;
import uk.gov.nhs.digital.telehealth.service.BM55USBService;
import uk.gov.nhs.digital.telehealth.service.USBService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class BloodPressureFragment extends Fragment {
    private HomeScreenActivity activity;
    private TextView deviceInfo;
    private TextView valueSYS;
    private TextView valueDIA;
    private TextView valueHeartRate;
    private TextView valueCurrentLocation;

    private ContextInfo contextInfo;
    private RestTemplate restTemplate;
    private String url = "http://104.155.90.134:10060/heartRate/";// Cloud
    //private String url = "http://192.168.0.19:10060/heartRate/";//Home
    //private String url = "http://192.168.6.140:10060/heartRate/";//MMobile

    private GeographicalLocation location;
    private USBConnectionService connectionService;
    private USBService usbService;
    private String androidDeviceId;
    private static final int VENDOR_ID = 0x0c45;
    private static final int PRODUCT_ID = 0x7406;
    byte[] readBytes = new byte[128];
    BluetoothAdapter bluetoothAdapter;

    private static final String ACTION_USB_PERMISSION ="com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    final UsbDevice bloodPressureMeter = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(bloodPressureMeter != null){
                            Toast.makeText(context, "Granted permission", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Granted not permission", Toast.LENGTH_SHORT).show();
                        Log.d("", "permission denied for device " + bloodPressureMeter);
                    }
                }
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_blood_pressure, container, false);
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
        deviceInfo = (TextView) view.findViewById(R.id.deviceInfo);
        valueSYS = (TextView) view.findViewById(R.id.valueSYS);
        valueDIA = (TextView) view.findViewById(R.id.valueDIA);
        valueHeartRate = (TextView) view.findViewById(R.id.valueHeartRate);
        valueCurrentLocation = (TextView) view.findViewById(R.id.valueCurrentLocation);
        androidDeviceId = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);

        deviceInfo.setMovementMethod(new ScrollingMovementMethod());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        connectionService = new USBConnectionService();
        usbService = new BM55USBService();
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

        final PendingIntent mPermissionIntent = PendingIntent.getBroadcast(activity, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        activity.registerReceiver(mUsbReceiver, filter);

        try {
            UsbDevice bloodPressureMeter = (UsbDevice) connectionService.getDevice(activity, VENDOR_ID, PRODUCT_ID);
            UsbManager manger = connectionService.getUsbManager(activity);
            manger.requestPermission(bloodPressureMeter, mPermissionIntent);
        } catch (final Exception e) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceInfo.setText("\n" + e.getMessage());
                }
            });
        }

        final Button startButton = (Button) view.findViewById(R.id.startProcess);

        startButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                try {
                    location = activity.getGeoLocation();
                    displayGeographicalAddress(location);
                    Log.d("MediMobile", "Starting to transfer readings");
                    new Thread(new Runnable() {
                        public void run() {
                            try {
                                Log.d("MediMobile", "Before: usbService.getMeasurements()");
                                final List<BloodPressure> measurements = (List<BloodPressure>) usbService.getMeasurements("A", activity, connectionService);
                                Log.d("MediMobile", "After: usbService.getMeasurements()");
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BloodPressure latestMeasurement = null;
                                        int readingsCounter = 1;
                                        for (BloodPressure bloodPressure : measurements) {
                                            final String reading = "\n\nReading " + readingsCounter + ":\n";
                                            bloodPressure.setAndroidDeviceId(androidDeviceId);
                                            bloodPressure.setLocation(location);
                                            deviceInfo.append(reading + bloodPressure.toString());
                                            Log.d("MediMobile", reading + bloodPressure.toString());
                                            latestMeasurement = bloodPressure;
                                            readingsCounter++;
                                        }

                                        if (latestMeasurement != null) {
                                            Log.d("MediMobile", "latestMeasurement: " + latestMeasurement.toString());
                                            valueDIA.setText(String.valueOf(latestMeasurement.getDiastolicPressure()));
                                            valueSYS.setText(String.valueOf(latestMeasurement.getSystolicPressure()));
                                            valueHeartRate.setText(String.valueOf(latestMeasurement.getPulseRate()));
                                        }
                                    }
                                });
                            } catch (final Exception e) {
                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        deviceInfo.append("\n" + e.getMessage());
                                    }
                                });
                            }
                        }
                    }).start();
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceInfo.setText("\n" + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void displayGeographicalAddress(final String address) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (StringUtils.isNotEmpty(address)) {
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