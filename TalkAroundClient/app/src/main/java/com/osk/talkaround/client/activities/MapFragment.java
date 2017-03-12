package com.osk.talkaround.client.activities;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.example.osk.talkaroundclient.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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
        GoogleMap.OnInfoWindowClickListener,
        OnMapReadyCallback,
        LocationListener {
    private static final int ZOOM = 14;

    MapView mMapView;
    private GoogleMap googleMap;
    private GoogleApiClient mGoogleApiClient;
    private boolean isMapReady;
    private Location mLastLocation;
    private Circle lastCircle;
    private GroundOverlay circle;
    private ValueAnimator valueAnimator;
    private List<Marker> lastMarkers = new ArrayList<>();

    private Talk[] talks;
    private int metres = MainActivity.DISTANCE_SMALL;
    private boolean wasCancelled;


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
            wasCancelled = true;
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
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
            wasCancelled = true;
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

        if (wasCancelled) {
            mGoogleApiClient.reconnect();
            wasCancelled = false;
        }

        if (!isMapReady) {
            return;
        }

        for (Marker lastMarker : lastMarkers) {
            lastMarker.remove();
        }

        lastMarkers.clear();

        for (Talk talk : talks) {
            CustomLocation l = talk.getLocation();
            MarkerOptions marker = new MarkerOptions().position(
                    new LatLng(l.getLatitude(), l.getLongitude())).title(talk.getTitle());
            marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.person));
            lastMarkers.add(googleMap.addMarker(marker));
            googleMap.setOnInfoWindowClickListener(this);

        } //TODO ICON

    }



    @Override
    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        for (Talk talk : this.talks) {
            if (talk.getTitle().equals(title)) {
                Intent intent = new Intent(getContext(), DisplayTalkActivity.class);
                intent.putExtra(TALK_ID_PARAM, String.valueOf(talk.getId()));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDistChanged(int metres) {
        if (mLastLocation == null || !isMapReady) return;
//        updatePosition();
        drawCircleOnMapWithRadius(metres);
        this.metres = metres;
    }

    private void drawCircleOnMapWithRadius(final int metres) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setSize(500,500);
        gradientDrawable.setColor(ContextCompat.getColor(getContext(), R.color.colorAccentAlpha));
        gradientDrawable.setStroke(5, Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(gradientDrawable.getIntrinsicWidth()
                , gradientDrawable.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        gradientDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        gradientDrawable.draw(canvas);
        // Radius of the circle
        final int radius = metres;
//        if (circle != null) circle.remove();
        // Add the circle to the map
        if(circle==null) {
            circle = googleMap.addGroundOverlay(new GroundOverlayOptions()
                    .position(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()),
                            2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));
        }
        if(valueAnimator==null) {
            valueAnimator = new ValueAnimator();
            valueAnimator.setDuration(1000);
            valueAnimator.setEvaluator(new IntEvaluator());
            valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        }
        valueAnimator.setIntValues(this.metres, metres);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int animatedFraction = (int) valueAnimator.getAnimatedValue();
                circle.setDimensions(animatedFraction * 2);
            }
        });
        valueAnimator.start();
    }

    private void updatePosition() {
        if (lastCircle != null) lastCircle.remove();
//        CircleOptions co = new CircleOptions();
//        co.center(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
//        co.radius(metres);
//        co.fillColor(ContextCompat.getColor(getContext(), R.color.colorAccentAlpha));
//        co.strokeWidth(0);
//        lastCircle = googleMap.addCircle(co);
    }

}
