package uk.gov.nhs.digital.telehealth.service.others;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.util.Log;

import java.util.List;

/**
 * This is a utility to detect bluetooth headset connection and establish audio connection
 * for android API >= 8. This includes a work around for  API < 11 to detect already connected headset
 * before the application starts. This work around would only fails if Sco audio
 * connection is accepted but the connected device is not a headset.
 *
 * @author Hoan Nguyen
 *
 */
public abstract class BluetoothHeadset {
    private  Context mContext;

    private BluetoothAdapter mBluetoothAdapter;
    private android.bluetooth.BluetoothHeadset mBluetoothHeadset;
    private BluetoothDevice mConnectedHeadset;

    private AudioManager mAudioManager;

    private boolean mIsCountDownOn;
    private boolean mIsStarting;
    private boolean mIsOnHeadsetSco;
    private boolean mIsStarted;

    private static final String TAG = "BluetoothHeadsetUtils"; //$NON-NLS-1$

    /**
     * Constructor
     * @param context
     */
    public BluetoothHeadset(Context context)
    {
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Call this to start BluetoothHeadsetUtils functionalities.
     * @return  The return value of startBluetooth() or startBluetooth11()
     */
    public boolean start()
    {
        if (!mIsStarted)
        {
            mIsStarted = true;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            {
                mIsStarted = startBluetooth();
            }
            else
            {
                mIsStarted = startBluetooth11();
            }
        }

        return mIsStarted;
    }

    /**
     * Should call this on onResume or onDestroy.
     * Unregister broadcast receivers and stop Sco audio connection
     * and cancel count down.
     */
    public void stop()
    {
        if (mIsStarted)
        {
            mIsStarted = false;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
            {
                stopBluetooth();
            }
            else
            {
                stopBluetooth11();
            }
        }
    }

    /**
     *
     * @return  true if audio is connected through headset.
     */
    public boolean isOnHeadsetSco()
    {
        return mIsOnHeadsetSco;
    }

    public abstract void onHeadsetDisconnected();

    public abstract void onHeadsetConnected();

    public abstract void onScoAudioDisconnected();

    public abstract void onScoAudioConnected();

    /**
     * Register for bluetooth headset connection states and Sco audio states.
     * Try to connect to bluetooth headset audio by calling startBluetoothSco().
     * This is a work around for API < 11 to detect if a headset is connected before
     * the application starts.
     *
     * The official documentation for startBluetoothSco() states
     *
     * "This method can be used by applications wanting to send and received audio to/from
     *  a bluetooth SCO headset while the phone is not in call."
     *
     * Does this mean that startBluetoothSco() would fail if the connected bluetooth device
     * is not a headset?
     *
     * Thus if a call to startBluetoothSco() is successful, i.e mBroadcastReceiver will receive
     * an ACTION_SCO_AUDIO_STATE_CHANGED with intent extra SCO_AUDIO_STATE_CONNECTED, then
     * we assume that a headset is connected.
     *
     * @return  false if device does not support bluetooth or current platform does not supports
     *                use of SCO for off call.
     */
    @SuppressWarnings("deprecation")
    private boolean startBluetooth()
    {
        Log.d(TAG, "startBluetooth"); //$NON-NLS-1$

        // Device support bluetooth
        if (mBluetoothAdapter != null)
        {
            if (mAudioManager.isBluetoothScoAvailableOffCall())
            {
                mContext.registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED));
                mContext.registerReceiver(mBroadcastReceiver,
                        new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
                mContext.registerReceiver(mBroadcastReceiver,
                        new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));

                // Need to set audio mode to MODE_IN_CALL for call to startBluetoothSco() to succeed.
                mAudioManager.setMode(AudioManager.MODE_IN_CALL);

                mIsCountDownOn = true;
                // mCountDown repeatedly tries to start bluetooth Sco audio connection.
                mCountDown.start();

                // need for audio sco, see mBroadcastReceiver
                mIsStarting = true;

                return true;
            }
        }

        return false;
    }

    /**
     * Register a headset profile listener
     * @return false    if device does not support bluetooth or current platform does not supports
     *                  use of SCO for off call or error in getting profile proxy.
     */
    private boolean startBluetooth11()
    {
        Log.d(TAG, "startBluetooth11"); //$NON-NLS-1$

        // Device support bluetooth
        if (mBluetoothAdapter != null)
        {
            if (mAudioManager.isBluetoothScoAvailableOffCall())
            {
                // All the detection and audio connection are done in mHeadsetProfileListener
                if (mBluetoothAdapter.getProfileProxy(mContext,
                        mHeadsetProfileListener,
                        BluetoothProfile.HEADSET))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * API < 11
     * Unregister broadcast receivers and stop Sco audio connection
     * and cancel count down.
     */
    private void stopBluetooth()
    {
        Log.d(TAG, "stopBluetooth"); //$NON-NLS-1$

        if (mIsCountDownOn)
        {
            mIsCountDownOn = false;
            mCountDown.cancel();
        }

        // Need to stop Sco audio connection here when the app
        // change orientation or close with headset still turns on.
        mContext.unregisterReceiver(mBroadcastReceiver);
        mAudioManager.stopBluetoothSco();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    /**
     * API >= 11
     * Unregister broadcast receivers and stop Sco audio connection
     * and cancel count down.
     */
    protected void stopBluetooth11()
    {
        Log.d(TAG, "stopBluetooth11"); //$NON-NLS-1$

        if (mIsCountDownOn)
        {
            mIsCountDownOn = false;
            mCountDown11.cancel();
        }

        if (mBluetoothHeadset != null)
        {
            // Need to call stopVoiceRecognition here when the app
            // change orientation or close with headset still turns on.
            mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);
            mContext.unregisterReceiver(mHeadsetBroadcastReceiver);
            mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
            mBluetoothHeadset = null;
        }
    }

    /**
     * Broadcast receiver for API < 11
     * Handle headset and Sco audio connection states.
     */
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @SuppressWarnings({"deprecation", "synthetic-access"})
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED))
            {
                mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass bluetoothClass = mConnectedHeadset.getBluetoothClass();
                if (bluetoothClass != null)
                {
                    // Check if device is a headset. Besides the 2 below, are there other
                    // device classes also qualified as headset?
                    int deviceClass = bluetoothClass.getDeviceClass();
                    if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE
                            || deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET)
                    {
                        // start bluetooth Sco audio connection.
                        // Calling startBluetoothSco() always returns faIL here,
                        // that why a count down timer is implemented to call
                        // startBluetoothSco() in the onTick.
                        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
                        mIsCountDownOn = true;
                        mCountDown.start();

                        // override this if you want to do other thing when the device is connected.
                        onHeadsetConnected();
                    }
                }

                Log.d(TAG, mConnectedHeadset.getName() + " connected"); //$NON-NLS-1$
            }
            else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED))
            {
                Log.d(TAG, "Headset disconnected"); //$NON-NLS-1$

                if (mIsCountDownOn)
                {
                    mIsCountDownOn = false;
                    mCountDown.cancel();
                }

                mAudioManager.setMode(AudioManager.MODE_NORMAL);

                // override this if you want to do other thing when the device is disconnected.
                onHeadsetDisconnected();
            }
            else if (action.equals(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED))
            {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE,
                        AudioManager.SCO_AUDIO_STATE_ERROR);

                if (state == AudioManager.SCO_AUDIO_STATE_CONNECTED)
                {
                    mIsOnHeadsetSco = true;

                    if (mIsStarting)
                    {
                        // When the device is connected before the application starts,
                        // ACTION_ACL_CONNECTED will not be received, so call onHeadsetConnected here
                        mIsStarting = false;
                        onHeadsetConnected();
                    }

                    if (mIsCountDownOn)
                    {
                        mIsCountDownOn = false;
                        mCountDown.cancel();
                    }

                    // override this if you want to do other thing when Sco audio is connected.
                    onScoAudioConnected();

                    Log.d(TAG, "Sco connected"); //$NON-NLS-1$
                }
                else if (state == AudioManager.SCO_AUDIO_STATE_DISCONNECTED)
                {
                    Log.d(TAG, "Sco disconnected"); //$NON-NLS-1$

                    // Always receive SCO_AUDIO_STATE_DISCONNECTED on call to startBluetooth()
                    // which at that stage we do not want to do anything. Thus the if condition.
                    if (!mIsStarting)
                    {
                        mIsOnHeadsetSco = false;

                        // Need to call stopBluetoothSco(), otherwise startBluetoothSco()
                        // will not be successful.
                        mAudioManager.stopBluetoothSco();

                        // override this if you want to do other thing when Sco audio is disconnected.
                        onScoAudioDisconnected();
                    }
                }
            }
        }
    };

    /**
     * API < 11
     * Try to connect to audio headset in onTick.
     */
    private CountDownTimer mCountDown = new CountDownTimer(10000, 1000)
    {

        @SuppressWarnings("synthetic-access")
        @Override
        public void onTick(long millisUntilFinished)
        {
            // When this call is successful, this count down timer will be canceled.
            mAudioManager.startBluetoothSco();

            Log.d(TAG, "\nonTick start bluetooth Sco"); //$NON-NLS-1$
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void onFinish()
        {
            // Calls to startBluetoothSco() in onStick are not successful.
            // Should implement something to inform user of this failure
            mIsCountDownOn = false;
            mAudioManager.setMode(AudioManager.MODE_NORMAL);

            Log.d(TAG, "\nonFinish fail to connect to headset audio"); //$NON-NLS-1$
        }
    };

    /**
     * API >= 11
     * Check for already connected headset and if so start audio connection.
     * Register for broadcast of headset and Sco audio connection states.
     */
    private BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener()
    {

        /**
         * This method is never called, even when we closeProfileProxy on onPause.
         * When or will it ever be called???
         */
        @Override
        public void onServiceDisconnected(int profile)
        {
            Log.d(TAG, "Profile listener onServiceDisconnected"); //$NON-NLS-1$
            stopBluetooth11();
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy)
        {
            Log.d(TAG, "Profile listener onServiceConnected"); //$NON-NLS-1$

            // mBluetoothHeadset is just a headset profile,
            // it does not represent a headset device.
            mBluetoothHeadset = (android.bluetooth.BluetoothHeadset) proxy;

            // If a headset is connected before this application starts,
            // ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
            // So we need to check for already connected headset.
            List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
            if (devices.size() > 0)
            {
                // Only one headset can be connected at a time,
                // so the connected headset is at index 0.
                mConnectedHeadset = devices.get(0);

                onHeadsetConnected();

                // Should not need count down timer, but just in case.
                // See comment below in mHeadsetBroadcastReceiver onReceive()
                mIsCountDownOn = true;
                mCountDown11.start();

                Log.d(TAG, "Start count down"); //$NON-NLS-1$
            }

            // During the active life time of the app, a user may turn on and off the headset.
            // So register for broadcast of connection states.
            mContext.registerReceiver(mHeadsetBroadcastReceiver,
                    new IntentFilter(android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
            // Calling startVoiceRecognition does not result in immediate audio connection.
            // So register for broadcast of audio connection states. This broadcast will
            // only be sent if startVoiceRecognition returns true.
            mContext.registerReceiver(mHeadsetBroadcastReceiver,
                    new IntentFilter(android.bluetooth.BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
        }
    };

    /**
     *  API >= 11
     *  Handle headset and Sco audio connection states.
     */
    private BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver()
    {

        @SuppressWarnings("synthetic-access")
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            int state;
            if (action.equals(android.bluetooth.BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED))
            {
                state = intent.getIntExtra(android.bluetooth.BluetoothHeadset.EXTRA_STATE,
                        android.bluetooth.BluetoothHeadset.STATE_DISCONNECTED);
                Log.d(TAG, "\nAction = " + action + "\nState = " + state); //$NON-NLS-1$ //$NON-NLS-2$
                if (state == android.bluetooth.BluetoothHeadset.STATE_CONNECTED)
                {
                    mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    // Calling startVoiceRecognition always returns false here,
                    // that why a count down timer is implemented to call
                    // startVoiceRecognition in the onTick.
                    mIsCountDownOn = true;
                    mCountDown11.start();

                    // override this if you want to do other thing when the device is connected.
                    onHeadsetConnected();

                    Log.d(TAG, "Start count down"); //$NON-NLS-1$
                }
                else if (state == android.bluetooth.BluetoothHeadset.STATE_DISCONNECTED)
                {
                    // Calling stopVoiceRecognition always returns false here
                    // as it should since the headset is no longer connected.
                    if (mIsCountDownOn)
                    {
                        mIsCountDownOn = false;
                        mCountDown11.cancel();
                    }
                    mConnectedHeadset = null;

                    // override this if you want to do other thing when the device is disconnected.
                    onHeadsetDisconnected();

                    Log.d(TAG, "Headset disconnected"); //$NON-NLS-1$
                }
            }
            else // audio
            {
                state = intent.getIntExtra(android.bluetooth.BluetoothHeadset.EXTRA_STATE, android.bluetooth.BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
                Log.d(TAG, "\nAction = " + action + "\nState = " + state); //$NON-NLS-1$ //$NON-NLS-2$
                if (state == android.bluetooth.BluetoothHeadset.STATE_AUDIO_CONNECTED)
                {
                    Log.d(TAG, "\nHeadset audio connected");  //$NON-NLS-1$

                    mIsOnHeadsetSco = true;

                    if (mIsCountDownOn)
                    {
                        mIsCountDownOn = false;
                        mCountDown11.cancel();
                    }

                    // override this if you want to do other thing when headset audio is connected.
                    onScoAudioConnected();
                }
                else if (state == android.bluetooth.BluetoothHeadset.STATE_AUDIO_DISCONNECTED)
                {
                    mIsOnHeadsetSco = false;

                    // The headset audio is disconnected, but calling
                    // stopVoiceRecognition always returns true here.
                    mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);

                    // override this if you want to do other thing when headset audio is disconnected.
                    onScoAudioDisconnected();

                    Log.d(TAG, "Headset audio disconnected"); //$NON-NLS-1$
                }
            }
        }
    };

    /**
     * API >= 11
     * Try to connect to audio headset in onTick.
     */
    private CountDownTimer mCountDown11 = new CountDownTimer(10000, 1000)
    {
        @SuppressWarnings("synthetic-access")
        @Override
        public void onTick(long millisUntilFinished)
        {
            // First stick calls always returns false. The second stick
            // always returns true if the countDownInterval is set to 1000.
            // It is somewhere in between 500 to a 1000.
            mBluetoothHeadset.startVoiceRecognition(mConnectedHeadset);

            Log.d(TAG, "onTick startVoiceRecognition"); //$NON-NLS-1$
        }

        @SuppressWarnings("synthetic-access")
        @Override
        public void onFinish()
        {
            // Calls to startVoiceRecognition in onStick are not successful.
            // Should implement something to inform user of this failure
            mIsCountDownOn = false;
            Log.d(TAG, "\nonFinish fail to connect to headset audio"); //$NON-NLS-1$
        }
    };

}
