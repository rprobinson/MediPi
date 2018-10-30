package uk.gov.nhs.digital.telehealth.activities;

import android.content.Context;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import uk.gov.nhs.digital.telehealth.R;
import uk.gov.nhs.digital.telehealth.domain.GeographicalLocation;
import uk.gov.nhs.digital.telehealth.fragments.BloodPressureFragment;
import uk.gov.nhs.digital.telehealth.fragments.FingerHeartRateFragment;
import uk.gov.nhs.digital.telehealth.fragments.NavigationDrawerFragment;
import uk.gov.nhs.digital.telehealth.fragments.OximeterFragment;
import uk.gov.nhs.digital.telehealth.fragments.VoiceTestFragment;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeScreenActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private NavigationDrawerFragment navigationDrawerFragment;
    private CharSequence fragmentTitle;

    public static final String SECTION_NUMBER = "section_number";

    //Define a request code to send to Google Play services
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Geocoder geocoder;
    private GeographicalLocation geoLocation;

    public GeographicalLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(GeographicalLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        fragmentTitle = getTitle();

        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mLocationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(10 * 1000).setFastestInterval(1 * 1000);
        geocoder = new Geocoder(this, Locale.getDefault());
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        switch (position + 1) {
            case 1:
                fragmentTitle = getString(R.string.label_activity_oximeter);
                fragmentManager.beginTransaction().replace(R.id.container, new OximeterFragment()).commit();
                break;
            case 2:
                fragmentTitle = getString(R.string.label_activity_blood_pressure);
                fragmentManager.beginTransaction().replace(R.id.container, new BloodPressureFragment()).commit();
                break;
            case 3:
                fragmentTitle = getString(R.string.label_activity_diagnostic_scale);
                fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
                break;
            case 4:
                fragmentTitle = getString(R.string.label_activity_sugar);
                fragmentManager.beginTransaction().replace(R.id.container, PlaceholderFragment.newInstance(position + 1)).commit();
                break;
            case 5:
                fragmentTitle = getString(R.string.label_activity_voice);
                fragmentManager.beginTransaction().replace(R.id.container, new VoiceTestFragment()).commit();
                break;
            case 6:
                fragmentTitle = getString(R.string.label_activity_finger_heart_rate);
                fragmentManager.beginTransaction().replace(R.id.container, new FingerHeartRateFragment()).commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                fragmentTitle = getString(R.string.label_activity_oximeter);
                break;
            case 2:
                fragmentTitle = getString(R.string.label_activity_blood_pressure);
                break;
            case 3:
                fragmentTitle = getString(R.string.label_activity_diagnostic_scale);
                break;
            case 4:
                fragmentTitle = getString(R.string.label_activity_sugar);
                break;
            case 5:
                fragmentTitle = getString(R.string.label_activity_voice);
                break;
            case 6:
                fragmentTitle = getString(R.string.label_activity_finger_heart_rate);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(fragmentTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Only show items in the action bar relevant to this screen
        // if the drawer is not showing. Otherwise, let the drawer
        // decide what to show in the action bar.
        if (!navigationDrawerFragment.isDrawerOpen()) {
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public static class PlaceholderFragment extends Fragment {

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            ((HomeScreenActivity) context).onSectionAttached(getArguments().getInt(SECTION_NUMBER));
        }
    }

    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*Log.v(this.getClass().getSimpleName(), "onPause()");

        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }*/
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            geoLocation = getGeographicalLocation();
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "onConnected()", e);
        }
    }


    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            geoLocation = getGeographicalLocation();
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "onConnected()", e);
        }
    }
    private GeographicalLocation getGeographicalLocation(Location location) throws IOException {
        GeographicalLocation geoLocation = null;
        Address address = getAddress(location);
        if(address != null && location != null) {
            String displayAddress = getDisplayAddress(address);
            if (StringUtils.isNotEmpty(displayAddress)) {
                geoLocation = new GeographicalLocation(location.getLatitude(), location.getLongitude(), displayAddress);
            }
        }
        return geoLocation;
    }

    private GeographicalLocation getGeographicalLocation() throws IOException {
        Location location = getLocation();
        return getGeographicalLocation(location);
    }

    private Location getLocation() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        return location;
    }

    private Address getAddress(Location location) throws IOException {
        Address address = null;
        if(location != null) {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0);
        }
        return address;
    }

    private String getDisplayAddress(Address address) {
        StringBuilder displayAddress = new StringBuilder();
        if(address != null) {
            final int noOfAddressLines = address.getMaxAddressLineIndex();// If any additional address line present then only, check with max available address lines by getMaxAddressLineIndex()
            final String city = address.getLocality();
            final String state = address.getAdminArea();
            final String country = address.getCountryName();
            final String postalCode = address.getPostalCode();
            final String knownName = address.getFeatureName(); // Only if available else return NULL

            for (int addressLineCounter = 0; addressLineCounter < noOfAddressLines; addressLineCounter++) {
                String addressLine = address.getAddressLine(addressLineCounter);
                if (StringUtils.isNotEmpty(addressLine)) {
                    displayAddress.append(addressLine + ",\n");
                }
            }

            if (StringUtils.isNotEmpty(knownName) && !displayAddress.toString().toLowerCase().contains(knownName.trim().toLowerCase())) {
                displayAddress.append(knownName.trim() + ",\n");
            }

            if (StringUtils.isNotEmpty(city) && !displayAddress.toString().toLowerCase().contains(city.trim().toLowerCase())) {
                displayAddress.append(city.trim() + " ");
            }

            if (StringUtils.isNotEmpty(postalCode) && !displayAddress.toString().toLowerCase().contains(postalCode.trim().toLowerCase())) {
                displayAddress.append(postalCode + ", ");
            }

            if (StringUtils.isNotEmpty(state) && !displayAddress.toString().toLowerCase().contains(state.trim().toLowerCase())) {
                displayAddress.append(" " + state.trim() + ", ");
            }

            if (StringUtils.isNotEmpty(country) && !displayAddress.toString().toLowerCase().contains(country.trim().toLowerCase())) {
                displayAddress.append(country.trim());
            }
            /*displayAddress.append("\nAdminArea:" + address.getAdminArea());
            displayAddress.append("\nCountryCode:" + address.getCountryCode());
            displayAddress.append("\nCountryName:" + address.getCountryName());
            displayAddress.append("\nFeatureName:" + address.getFeatureName());
            displayAddress.append("\nLocality:" + address.getLocality());
            displayAddress.append("\nPhone:" + address.getPhone());
            displayAddress.append("\nPostalCode:" + address.getPostalCode());
            displayAddress.append("\nPremises:" + address.getPremises());
            displayAddress.append("\nSubAdminArea:" + address.getSubAdminArea());
            displayAddress.append("\nSubLocality:" + address.getSubLocality());
            displayAddress.append("\nSubThoroughfare:" + address.getSubThoroughfare());
            displayAddress.append("\nThoroughfare:" + address.getThoroughfare());
            displayAddress.append("\nUrl:" + address.getUrl());
            displayAddress.append("\nExtras:" + address.getExtras());*/
        } else {
            displayAddress.append(R.string.message_location_untraceable);
        }
        return displayAddress.toString();
    }
}