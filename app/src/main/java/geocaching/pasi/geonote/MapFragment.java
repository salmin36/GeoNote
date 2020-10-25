/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;


import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements LocationListener {
    private GoogleMap mMap;
    private View view;
    private Cache m_cacheToBeUpdated;
    //Used to determine if map is ready be interacted with
    private boolean m_measuring;
    private Handler m_handler;
    private Runnable m_runnable;
    private LatLng m_measureCoordinates;
    private LocationManager m_locationManager;
    private boolean m_findLocationOnce = true;
    private LatLng m_myCurrentLocation;
    private Polyline my_betweenLine = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        m_myCurrentLocation = new LatLng(0,0);
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }

        try {
            view = inflater.inflate(R.layout.fragment_map, container, false);
        } catch (InflateException e) {
        /* map is already there, just return view as it is */
        }
        // Getting LocationManager object from System Service LOCATION_SERVICE
        m_locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Log.v("GeoNote", "isGPSEnabled == " + m_locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        //Find location once and zoom into that only if there is no point selected where to calculate distance

        // Does this really do anything
        if(m_locationManager != null){
            try{
                Criteria criteria = new Criteria();
                // Getting the name of the best provider
                String provider = m_locationManager.getBestProvider(criteria, true);
                if(provider != null) {
                    Location loc = m_locationManager.getLastKnownLocation(provider);
                }
            }catch (SecurityException ex){
                Log.v("GeoNote", ex.getMessage());
            }
        }

        m_handler = new Handler();
        m_measuring = false;
        ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.INVISIBLE);
        return view;

    }

    /***** Sets up the map if it is possible to do so *****/
    public void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapView)).getMap();
            //If there is a request for cahe to be shown that has not been processed then do that
            if (m_cacheToBeUpdated != null) {
                updateMapNewLocation(m_cacheToBeUpdated);
                m_cacheToBeUpdated = null;
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.INVISIBLE);
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.mapView)).getMap(); // getMap is deprecated
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    ((MainActivity) getActivity()).mapCanBeUpdatedNow();
                }
            });
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    ((MainActivity) getActivity()).mapChangesSelectedCache(marker.getTitle());
                    //If no measuring when clicked then do measuring
                    measureAndUpdate();
                    if(!m_measuring){
                        startMeasure(m_measureCoordinates);
                    }

                    return false;
                }
            });

        }
        if (m_cacheToBeUpdated != null) {
            updateMapNewLocation(m_cacheToBeUpdated);
            m_cacheToBeUpdated = null;
        }
        ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.INVISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public void updateMapNewLocation(Cache cache) {
        //Here we check if ui is intilialized and if not so lets´s wait before it is to try to
        //update the map component
        if (mMap == null) {
            m_cacheToBeUpdated = cache;
            return;
        }
        m_measureCoordinates = cache.getCoordinates();
        try {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_measureCoordinates, 12.0f));
        } catch (java.lang.IllegalArgumentException ex) {
            Toast.makeText(getContext(), "Wrong GPS coordinates", Toast.LENGTH_SHORT).show();
        }
    }


    public void addMarkerToMap(Cache cache) {
        if (mMap == null && cache != null) {
            m_cacheToBeUpdated = cache;
            return;
        } else if (m_cacheToBeUpdated != null) {
            LatLng coordinate = m_cacheToBeUpdated.getCoordinates();
            String name = m_cacheToBeUpdated.getName();
            String gc = m_cacheToBeUpdated.getGc();
            // For dropping a marker at a point on the Map
            mMap.addMarker(new MarkerOptions().position(coordinate).title(name).snippet(gc));
            m_cacheToBeUpdated = null;
        }
        if (cache != null && cache.getCoordinates().longitude != 0.0 && cache.getCoordinates().latitude != 0.0) {
            LatLng coordinate = cache.getCoordinates();
            String name = cache.getName();
            String gc = cache.getGc();
            // For dropping a marker at a point on the Map
            mMap.addMarker(new MarkerOptions().position(coordinate).title(name).snippet(gc));
        }
    }


    //Used to tell map that measure distance between current location and given point
    public void startMeasure(LatLng coordinates) {
        if(m_handler != null){
            Log.v("GeoNote", "StartMeasure");
            m_measureCoordinates = coordinates;
            if(m_locationManager != null) {
                try {
                    Criteria criteria = new Criteria();
                    // Getting the name of the best provider
                    //String provider = m_locationManager.getBestProvider(criteria, true);
                    //Location loc = m_locationManager.getLastKnownLocation(provider);
                    m_locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 1, this);
                } catch (SecurityException ex) {
                    Log.v("GeoNote", ex.getMessage());
                }
            }
            //((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.VISIBLE);
            m_measuring = true;
        }
    }

    private void measureAndUpdate() {
        Log.v("GeoNote","m_measureCoordinates == "  + m_measureCoordinates);

        if(m_measureCoordinates == null || m_measureCoordinates.latitude == 0.0 && m_measureCoordinates.longitude == 0.0){
            ((TextView) view.findViewById(R.id.map_distance_screen)).setText("");
            ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.INVISIBLE);
            if(my_betweenLine != null){
                my_betweenLine.remove();
                my_betweenLine = null;
            }
            return;
        }
        Location location = null;
        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = m_locationManager.getBestProvider(criteria, true);
        if(provider != null ){
            try {
                location = m_locationManager.getLastKnownLocation(provider);
            }catch (SecurityException ex){
                location = null;
            }
        }

        if(location == null){
            return;
        }

        ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.VISIBLE);
        float[] results = {0};
        Location.distanceBetween(m_myCurrentLocation.latitude,m_myCurrentLocation.longitude, m_measureCoordinates.latitude,m_measureCoordinates.longitude,results);
        Double distance = Double.valueOf(results[0]);

        //Set precision to two decimal
        DecimalFormat precision = new DecimalFormat("0.00");
        String resultString = "Distance: ";
        if(distance >= 1000){
            resultString += precision.format((Double)(distance / 1000)) + " km";
        }
        else{
            resultString =  precision.format(distance) + " m";
        }
        ((TextView) view.findViewById(R.id.map_distance_screen)).setText(resultString);
        drawLineBetweenPoints(m_myCurrentLocation,m_measureCoordinates, mMap);
    }

    private void drawLineBetweenPoints(LatLng currentLocation, LatLng measureCoordinates, GoogleMap map) {
        if(currentLocation != null && measureCoordinates != null && map != null){
            if(my_betweenLine == null) {
                my_betweenLine = map.addPolyline(new PolylineOptions()
                        .add(currentLocation, measureCoordinates)
                        .width(5)
                        .color(Color.parseColor("#00bfff")));
            }
            else{
                my_betweenLine.remove();
                my_betweenLine = map.addPolyline(new PolylineOptions()
                        .add(currentLocation, measureCoordinates)
                        .width(5)
                        .color(Color.parseColor("#00bfff")));
            }
        }
    }

    //Used to stop measuring distance
    public void stopMeasure(){
        if(m_handler != null){
            Log.v("GeoNote", "StopMeasure");
            try {
                m_locationManager.removeUpdates(this);
            }catch (SecurityException ex)
            {}
            m_measuring = false;
            ((TextView)view.findViewById(R.id.map_distance_screen)).setVisibility(View.INVISIBLE);
        }
    }


    public void removeSelected(){
        m_measureCoordinates = null;
        ((TextView) view.findViewById(R.id.map_distance_screen)).setText("");
        if(mMap != null) {
            //Remove all markers
            mMap.clear();
        }
        //Set to true so that inside onGpsStatusChanged we can get the current location once and center the
        // camera into it
        m_findLocationOnce = true;
    }


    @Override
    public void onLocationChanged(Location location) {
        m_myCurrentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.v("GeoNote", "Jee: " + new LatLng(location.getLatitude(), location.getLongitude()).toString());
        measureAndUpdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.v("GeoNote", "onStatusChanged");
        try{
            if(m_measureCoordinates == null && m_findLocationOnce){
                Criteria criteria = new Criteria();
                // Getting the name of the best provider
                provider = m_locationManager.getBestProvider(criteria, true);
                Location loc = m_locationManager.getLastKnownLocation(provider);
                try {
                    Log.v("GeoNote", "Move camera to gps position");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(loc.getLatitude(), loc.getLongitude()), 12.0f));
                    //Set to false so that we only move the camera to gps coordinate once
                    m_findLocationOnce = false;
                } catch (java.lang.IllegalArgumentException ex) {
                }

            }
        }catch (SecurityException ex){
            Log.v("GeoNote", ex.getMessage());
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v("GeoNote", "onProvideEnabled");

    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v("GeoNote", "onProviderDisabled");
    }
}

