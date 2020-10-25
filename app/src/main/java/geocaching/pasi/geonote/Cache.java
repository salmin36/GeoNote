/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import android.location.Location;
import android.util.Log;
import android.widget.Spinner;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Pasi on 08/03/2016.
 */
public class Cache {
    private String m_name;
    private String m_gc;
    private LatLng m_coordinates;
    private Double m_difficulty;
    private Double m_terrain;
    private Common.Size m_size;
    private String m_note;
    private Common.Type m_type;
    private Common.Winter m_winter;

    public Cache(){
        m_name = "";
        m_gc = "";
        m_coordinates = new LatLng(0,0);
        m_difficulty = 0.0;
        m_terrain = 0.0;
        m_size = Common.Size.MICRO;
        m_note = "";
        m_type = Common.Type.OTHER;
        m_winter = Common.Winter.NO_INFORMATION;
    }


    //Function returns latitude in following format DD MM.MMM
    public String getLat(){
        String latDeg = Location.convert(m_coordinates.latitude, Location.FORMAT_MINUTES);
        String replaceble = '\u00B0' + " ";
        latDeg = latDeg.replace(":", replaceble);
        return latDeg;
    }

    //Function returns longitude in following format DD MM.MMM
    public String getLong(){
        String longDeg = Location.convert(m_coordinates.longitude, Location.FORMAT_MINUTES);
        String replaceble = '\u00B0' + " ";
        longDeg = longDeg.replace(":", replaceble);
        return longDeg;
    }

    public String getLongDegrees(){
        String longDeg = Location.convert(m_coordinates.longitude, Location.FORMAT_MINUTES);
        Log.v("GeoNote", "getLongDegrees: " + longDeg);
        longDeg = longDeg.substring(0, longDeg.indexOf(':'));
        Log.v("GeoNote", "getLongDegrees: " + longDeg);
        return longDeg;
    }

    public String getLongMinutes(){
        String longMin = Location.convert(m_coordinates.longitude, Location.FORMAT_MINUTES);
        Log.v("GeoNote", "getLongMinutes: " + longMin);
        longMin = longMin.substring(longMin.indexOf(':') + 1,longMin.length()).replace(',', '.');
        Log.v("GeoNote", "getLongMinutes: " + longMin);
        return longMin;
    }

    public String getLatDegrees(){
        String latDeg = Location.convert(m_coordinates.latitude, Location.FORMAT_MINUTES);
        Log.v("GeoNote", "getLatDegrees: " + latDeg);
        latDeg = latDeg.substring(0, latDeg.indexOf(':'));
        Log.v("GeoNote", "getLatDegrees: " + latDeg);
        return latDeg;
    }

    public String getLatMinutes(){
        String latMin = Location.convert(m_coordinates.latitude, Location.FORMAT_MINUTES);
        Log.v("GeoNote", "getLatMinutes: " + latMin);
        latMin = latMin.substring(latMin.indexOf(':') + 1,latMin.length()).replace(',', '.');
        Log.v("GeoNote", "getLatMinutes: " + latMin);
        return latMin;
    }


    public String getName() {
        return m_name;
    }

    public void setName(String m_name) {
        this.m_name = m_name;
    }

    public String getGc() {
        return m_gc;
    }

    public void setGc(String m_gc) {
        this.m_gc = m_gc;
    }

    public Double getDifficulty() {
        return m_difficulty;
    }

    public void setDifficulty(Double m_difficulty) {
        this.m_difficulty = m_difficulty;
    }

    public Double getTerrain() {
        return m_terrain;
    }

    public void setTerrain(Double m_terrain) {
        this.m_terrain = m_terrain;
    }


    public void setSize(String size) {
        Log.v("GeoNote","1.Size == " + size);

        if(size.contains("micro") || size.contains("XS")){
            m_size = Common.Size.MICRO;
        }
        else if(size.contains("small") || size.contains("S")){
            m_size = Common.Size.SMALL;
            Log.v("GeoNote","Set to small");

        }
        else if(size.contains("reqular") || size.contains("M")){
            m_size = Common.Size.REGULAR;
        }
        else if(size.contains("large")  || size.contains("L")){
            m_size = Common.Size.LARGE;
        }
        else if(size.contains("other")  || size.contains("Other")){
            m_size = Common.Size.OTHER;
        }
    }

    public String getSizeString(){
        switch (m_size){
            case MICRO: return "micro";
            case SMALL: return "small";
            case REGULAR: return "regular";
            case LARGE: return "large";
            case OTHER: return "other";
        }
        return "";
    }

    public String getNote() {
        return m_note;
    }

    public void setNote(String note) {
        this.m_note = note;
    }


    //Give String in format DD:MM.MMMM
    public void setLat(String latitude){
        try {
            m_coordinates = new LatLng(Common.CoordinateConverter.convert(latitude), m_coordinates.longitude);
        }
        catch (IllegalArgumentException ex) {
            Log.v("GeoNote","IllegalArgument in setLan: " + latitude);
            m_coordinates = new LatLng(0, 0);
        }
    }

    public void setLong(String longitude){
        try{
            m_coordinates =  new LatLng(m_coordinates.latitude, Common.CoordinateConverter.convert(longitude));
        }
        catch (IllegalArgumentException ex)
        {
            Log.v("GeoNote","IllegalArgument in setLan: " + longitude);
            m_coordinates = new LatLng(0,0);
        }

    }

    public void setLatLong(String latitude, String longitude){
        Log.v("GeoNote","setLatLong: " + latitude + ", " + longitude);
        try{
            Log.v("GeoNote","setLatLong 1");
            if( latitude.length() != 0 && latitude.contains(":") && latitude.contains(".") &&
                longitude.length() != 0 && longitude.contains(":") && longitude.contains(".")){
                Log.v("GeoNote","setLatLong 2");
                m_coordinates =  new LatLng(Common.CoordinateConverter.convert(latitude), Common.CoordinateConverter.convert(longitude));
            }
            else if(latitude.length() != 0 && latitude.contains(String.valueOf('\u00B0')) && latitude.contains(",") &&
                    longitude.length() != 0 && longitude.contains(String.valueOf('\u00B0')) && longitude.contains(",")){
                Log.v("GeoNote","setLatLong 3");
                latitude = latitude.replace(String.valueOf('\u00B0') + " ", ":").replace(',','.');
                longitude = longitude.replace(String.valueOf('\u00B0') + " ", ":").replace(',','.');
                Log.v("GeoNote","latitude == " + latitude + ", longitude == " + longitude);
                m_coordinates =  new LatLng(Common.CoordinateConverter.convert(latitude), Common.CoordinateConverter.convert(longitude));
            }
            Log.v("GeoNote","setLatLong 4");
        }catch (IllegalArgumentException ex){
            Log.v("GeoNote", "IllegalArgumentException during setLatLong");
            m_coordinates = new LatLng(0,0);
        }
        Log.v("GeoNote","setLatLong 5");
    }

    public String getTypeString(){
        switch (m_type){
            case MULTI: return "multi";
            case MYSTERY: return "mystery";
            case OTHER: return "other";
            case REGULAR: return "regular";
            case HAPPENING: return "happening";
        }
        return "";
    }

    public int getTypeInt(){
        switch (m_type){
            case MULTI: return 3;
            case MYSTERY: return 8;
            case OTHER: return 0;
            case REGULAR: return 2;
            case HAPPENING: return 6;
        }
        return 0;
    }




    public void setType(String type){
        if(type.toLowerCase().contains("multi")){
            m_type = Common.Type.MULTI;
        }
        else if(type.toLowerCase().contains("mystery")){
            m_type = Common.Type.MYSTERY;
        }
        else if(type.toLowerCase().contains("regular")){
            m_type = Common.Type.REGULAR;
        }
        else if(type.toLowerCase().contains("happening")){
            m_type = Common.Type.HAPPENING;
        }
        else{
            m_type = Common.Type.OTHER;
        }
    }

    public void setType(int type){
        switch (type){
            //Regular cache
            case 2:
                m_type = Common.Type.REGULAR;
                break;
            //Multi cache
            case 3:
                m_type = Common.Type.MULTI;
                break;
            //Happening cache
            case 6:
                m_type = Common.Type.HAPPENING;
                break;
            //Mystery cache
            case 8:
                m_type = Common.Type.MYSTERY;
                break;
            default:
                m_type = Common.Type.OTHER;
                break;
        }
    }


    public void setCoordinates(LatLng coord){
        try{
            m_coordinates = coord;
        }
        catch (IllegalArgumentException ex){

        }
    }

    public LatLng getCoordinates(){
        return m_coordinates;
    }


    public String getWinterString(){
        switch (m_winter){
            case AVAILABLE: return "Yes";
            case NOT_AVAILABLE: return "No";
            case NO_INFORMATION: return "Don´t know";
        }
        return "";
    }


    public void setWinter(String winter){
        if(winter.toLowerCase().contains("yes")){
            m_winter = Common.Winter.AVAILABLE;
        }
        else if(winter.toLowerCase().contains("no") && winter.length() == 2){
            m_winter = Common.Winter.NOT_AVAILABLE;
        }
        else {
            m_winter = Common.Winter.NO_INFORMATION;
        }
    }
}

