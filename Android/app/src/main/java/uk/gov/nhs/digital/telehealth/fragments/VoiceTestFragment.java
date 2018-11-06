package uk.gov.nhs.digital.telehealth.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uk.gov.nhs.digital.telehealth.R;
import uk.gov.nhs.digital.telehealth.activities.HomeScreenActivity;
import uk.gov.nhs.digital.telehealth.connection.service.BluetoothConnection;
import uk.gov.nhs.digital.telehealth.service.others.BluetoothHeadset;

import java.util.Set;

public class VoiceTestFragment extends Fragment {
    public static Activity activity;
    private TextView usbDeviceInfo;
    private BluetoothConnection connection;
    //private BluetoothHelper bluetoothHelper;

    private TextToSpeech textToSpeech;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_capture_voice, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ((HomeScreenActivity) context).onSectionAttached(5);
    }

    @Override
    public void onResume() {
        super.onResume();
        //bluetoothHelper.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        //bluetoothHelper.stop();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = getView();
        activity = getActivity();
        usbDeviceInfo = (TextView) view.findViewById(R.id.usbDeviceInfo);
        usbDeviceInfo.setMovementMethod(new ScrollingMovementMethod());
        textToSpeech = new TextToSpeech(activity, null);
        //bluetoothHelper = new BluetoothHelper(activity);

        Button stopButton = (Button) view.findViewById(R.id.stopProcess);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //bluetoothHelper.stop();
                connection.close();
            }
        });

        connection = new BluetoothConnection("FC:58:FA:8A:F8:6F");


        Button startButton = (Button) view.findViewById(R.id.startProcess);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        usbDeviceInfo.setText("");
                    }
                });

                try {
                    connection.open(1, true);
                    connection.write("Hello World".getBytes());
                    BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                    if (bluetooth != null) {
                        if (bluetooth.isEnabled()) {
                            String deviceAddress = bluetooth.getAddress();
                            String deviceName = bluetooth.getName();
                            final String myDeviceInformation = deviceName + " : " + deviceAddress;
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usbDeviceInfo.setText("My device name:" + myDeviceInformation);
                                }
                            });

                            BluetoothDevice headSet = null;
                            Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
                            for (BluetoothDevice device : devices) {
                                final StringBuilder devicesInformation = getDeviceInformation(device);

                                activity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        usbDeviceInfo.append(devicesInformation.toString());
                                    }
                                });

                                if (device.getName().equals("LG730")) {
                                    headSet = device;
                                }
                            }
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usbDeviceInfo.setText("Bluetooth not enabled");
                                }
                            });
                        }
                    } else {
                        usbDeviceInfo.setText("Bluetooth not available.");
                    }
                } catch (final Exception e) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            usbDeviceInfo.setText("\n" + e.getMessage());
                        }
                    });
                }

                /*bluetoothHelper.start();
                //speak("Hello Mastek");
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                            if (bluetooth != null) {
                                if (bluetooth.isEnabled()) {
                                    String deviceAddress = bluetooth.getAddress();
                                    String deviceName = bluetooth.getName();
                                    final String myDeviceInformation = deviceName + " : " + deviceAddress;
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            usbDeviceInfo.setText("My device name:" + myDeviceInformation);
                                        }
                                    });

                                    BluetoothDevice headSet = null;
                                    Set<BluetoothDevice> devices = bluetooth.getBondedDevices();
                                    for (BluetoothDevice device : devices) {
                                        final StringBuilder devicesInformation = getDeviceInformation(device);

                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                usbDeviceInfo.append(devicesInformation.toString());
                                            }
                                        });

                                        if (device.getName().equals("LG730")) {
                                            headSet = device;
                                        }
                                    }
                                } else {
                                    activity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            usbDeviceInfo.setText("Bluetooth not enabled");
                                        }
                                    });
                                }
                            } else {
                                usbDeviceInfo.setText("Bluetooth not available.");
                            }
                        } catch (final Exception e) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    usbDeviceInfo.setText("\n" + e.getMessage());
                                }
                            });
                        }
                    }
                }).start();*/
            }
        });
    }

    @NonNull
    private StringBuilder getDeviceInformation(BluetoothDevice device) {
        final StringBuilder devicesInformation = new StringBuilder();
        ParcelUuid[] uuids = device.getUuids();
        devicesInformation.append("\nDevice:[");
        devicesInformation.append("Address:" + device.getAddress());
        devicesInformation.append(", Name:" + device.getName());
        devicesInformation.append(", BluetoothClass:" + device.getBluetoothClass());
        devicesInformation.append(", BondState:" + device.getBondState());
        devicesInformation.append(", Type:" + device.getType());
        if (uuids != null) {
            devicesInformation.append(", UUIDs:" + uuids.length);
            for (ParcelUuid uuid : uuids) {
                devicesInformation.append(", UUID:" + uuid.getUuid());
            }
        }
        devicesInformation.append("]\n");
        return devicesInformation;
    }

    /*protected void speak(String text) {
        HashMap<String, String> myHashRender = new HashMap<String, String>();

        if (bluetoothHelper.isOnHeadsetSco()) {
            myHashRender.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_VOICE_CALL));
        }
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, myHashRender);
    }*/

}

class BluetoothHelper extends BluetoothHeadset {
    public BluetoothHelper(Context context) {
        super(context);
    }

    @Override
    public void onScoAudioDisconnected() {
        // Cancel speech recognizer if desired
    }

    @Override
    public void onScoAudioConnected() {
        // Should start speech recognition here if not already started
    }

    @Override
    public void onHeadsetDisconnected() {

    }

    @Override
    public void onHeadsetConnected() {

    }
}