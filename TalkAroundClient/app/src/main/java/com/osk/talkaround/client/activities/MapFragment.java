package com.osk.talkaround.client.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.osk.talkaroundclient.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.dynamic.IObjectWrapper;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.osk.talkaround.model.CustomLocation;
import com.osk.talkaround.model.Talk;

import java.util.ArrayList;
import java.util.List;

import static com.osk.talkaround.client.activities.MainActivity.TALK_ID_PARAM;


/**
 * Created by GZaripov1 on 11.03.2017.
 */

public class MapFragment extends UpdatableFragment implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleMap.OnMarkerClickListener,
        OnMapReadyCallback,
        LocationListener {
    private static final int ZOOM = 13;

    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean isMapReady;
    private Location mLastLocation;
    private Circle lastCircle;
    private List<Marker> lastMarkers = new ArrayList<>();

    private Talk[] talks;
    private int metres = 0;
    public MapFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .enableAutoManage(getActivity() /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }


        // latitude and longitude
/*      double latitude = 17.385044;
        double longitude = 78.486671;

        // create marker
        MarkerOptions marker = new MarkerOptions().position(
                new LatLng(latitude, longitude)).title("Hello Maps");

        // Changing marker icon
        marker.icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ROSE));

        // adding marker
        googleMap.addMarker(marker);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(17.385044, 78.486671)).zoom(12).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));*/
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat
                .checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        LocationRequest request = new LocationRequest();
        request.setInterval(2000);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        mMapView.getMapAsync(this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updatePosition();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        // For showing a move to my location button
        if (ActivityCompat
                .checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat
                .checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(true);
        isMapReady = true;
        if (talks != null)
            updateTalks(talks);
        if (metres != 0)
            onDistChanged(metres);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()))
                .zoom(ZOOM).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void updateTalks(Talk[] talks) {
        this.talks = talks;
        if (!isMapReady) {
            return;
        }

        for (Marker lastMarker : lastMarkers) {
            lastMarker.remove();
        }

        lastMarkers.clear();

        for (final Talk talk : talks) {
            CustomLocation l = talk.getLocation();
            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(l.getLatitude(), l.getLongitude())).title(talk.getTitle());
            lastMarkers.add(googleMap.addMarker(marker));
            googleMap.setOnMarkerClickListener(this);
        } //TODO ICON

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String title = marker.getTitle();
        for (Talk talk : this.talks) {
            if (talk.getTitle().equals(title)) {
                Intent intent = new Intent(getContext(), DisplayTalkActivity.class);
                intent.putExtra(TALK_ID_PARAM, talk.getId());
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public void onDistChanged(int metres) {
        if (mLastLocation == null || !isMapReady) {
            this.metres = metres;
            return;
        }
       updatePosition();
    }

    private void updatePosition() {
        if (lastCircle != null) lastCircle.remove();
        CircleOptions co = new CircleOptions();
        co.center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        co.radius(metres);
        co.fillColor(ContextCompat.getColor(getContext(), R.color.colorAccentAlpha));
        co.strokeWidth(0);
        lastCircle = googleMap.addCircle(co);
    }

}
