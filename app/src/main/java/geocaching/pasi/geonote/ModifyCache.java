/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Pasi on 10/03/2016.
 */
public class ModifyCache extends DialogFragment implements AsyncCacheGet.HttpCacheListener {
    private Cache m_cache;
    private CacheListener m_listener;
    private String m_oldName;
    private boolean m_downloadStarted;

    @Override
    public void gotCache(String result) {
        Log.v("GeoNote", "gotCache started");
        parseCacheInformationFromString(result);
        m_downloadStarted = false;
    }

    private void parseCacheInformationFromString(String result) {
        //Find type
        int ind1 = result.indexOf("cacheImage");
        if(ind1 == -1){return;}
        ind1 = result.indexOf("WptTypes/");
        if(ind1 == -1){return;}
        String str = result.substring(ind1 + 9,ind1 + 10);
        Log.v("GeoNote", "Cache type number is: " + str);
        changeCacheType(Integer.parseInt(str));

        //First find cache name
        ind1 = result.indexOf("CacheName");
        if(ind1 == -1){return;}
        int ind2 = result.indexOf("<",ind1 + 11);
        if(ind2 == -1){return;}
        ind1 += 11;
        Log.v("GeoNote", "CacheName == " + result.substring(ind1,ind2));
        //Set name
        ((EditText)getDialog().findViewById(R.id.cacheName)).setText(result.substring(ind1,ind2));

        //Then find difficulty
        ind1 = result.indexOf("ContentBody_diffTerr",ind2);
        if(ind1 == -1){return;}
        ind1 = result.indexOf("alt=", ind1);
        if(ind1 == -1){return;}
        ind1 += 5;
        ind2 = result.indexOf("out", ind1);
        ind2 -= 1;
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Difficulty == " + result.substring(ind1, ind2));
        Double difficulty = Double.valueOf(result.substring(ind1, ind2));
        Spinner spinner = ((Spinner)getDialog().findViewById(R.id.cache_difficulty_spinner));
        if(difficulty == 1){spinner.setSelection(0);}
        else if(difficulty ==  1.5){spinner.setSelection(1);}
        else if(difficulty == 2){spinner.setSelection(2);}
        else if(difficulty ==  2.5){spinner.setSelection(3);}
        else if(difficulty == 3){spinner.setSelection(4);}
        else if(difficulty ==  3.5){spinner.setSelection(5);}
        else if(difficulty == 4){spinner.setSelection(6);}
        else if(difficulty == 4.5){spinner.setSelection(7);}
        else if(difficulty == 5){spinner.setSelection(8);}


        //Then find Terrain
        ind1 = result.indexOf("alt=",ind2);
        ind1 += 5;
        ind2 = result.indexOf("out", ind1);
        ind2 -= 1;
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Terrain == " + result.substring(ind1, ind2));

        Double terrain = Double.valueOf(result.substring(ind1,ind2));
        Log.v("GeoNote", "Terrain == " + terrain);
        if(terrain == 1){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(0);}
        else if(terrain ==  1.5){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(1);}
        else if(terrain == 2){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(2);}
        else if(terrain ==  2.5){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(3);}
        else if(terrain == 3){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(4);}
        else if(terrain ==  3.5){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(5);}
        else if(terrain == 4){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(6);}
        else if(terrain == 4.5){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(7);}
        else if(terrain == 5){((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner)).setSelection(8);}


        //Then find size
        ind1 = result.indexOf("alt=",ind2);
        ind1 += 11;
        ind2 = result.indexOf("\"", ind1);
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Size == " + result.substring(ind1,ind2));

        spinner =((Spinner)getDialog().findViewById(R.id.cache_size_spinner));
        String size = result.substring(ind1,ind2);
        if(size.contains("micro")){spinner.setSelection(0);}
        else if(size.contains("small")){spinner.setSelection(1);}
        else if(size.contains("regular")){spinner.setSelection(2);}
        else if(size.contains("large")){spinner.setSelection(3);}
        else{spinner.setSelection(4);}

        //Find if available in winter
        ind1 = result.indexOf("available in winter");
        ind2 = result.indexOf("not available for winter");
        if(ind1 != -1 ){
            Log.v("GeoNote", "Available in winter");
        }
        else if(ind2 !=  -1 ){
            Log.v("GeoNote", "Not available in winter");
        }
        else{
            Log.v("GeoNote", "Information not available");
        }

        //Find hint if available
        String hint = getHintFromString(result);
        ((EditText)getDialog().findViewById(R.id.cache_add_note)).setText(hint);

    }

    private String getHintFromString(String str) {
        if(str == null || str.length() == 0){return "";}
        int ind1 = str.indexOf("ctl00_ContentBody_hints");
        if(ind1 == -1){return "";}
        ind1 = str.indexOf("div_hint",ind1);
        if(ind1 == -1){return "";}
        ind1 = str.indexOf(">",ind1);
        if(ind1 == -1){return "";}
        ind1 +=1;
        int ind2 = str.indexOf("</div>", ind1);
        if(ind2 == -1){return "";}
        //Now we have the tip between ind1 and ind2 indexes from string str

        String hint = str.substring(ind1,ind2);
        hint = hint.replaceAll("<br>","\n");
        char[] chars = hint.toCharArray();
        char c;
        int ascii;
        //Go throught all the components in string and add 13 to all the capital letters
        for(int i = 0; i < chars.length; i++){
            if(chars[i] == '['){
                //Find corresponding closing ]
                ind1 = hint.indexOf("]",i);
                //No closing ] something went wrong
                if(ind1 == -1){return "";}
                i = ind1 + 1;
            }
            c = chars[i];
            ascii = (int)c;
            //So if we have lowercase letter
            if(ascii >= 97 && ascii <= 109){
                c = (char)( c + 13);
                chars[i] = c;
            }
            else if(ascii >= 110 && ascii <= 122){
                c = (char)( c - 13);
                chars[i] = c;
            }
            //If we have uppercase letters
            else if(ascii >= 65 && ascii <= 77){
                c = (char)( c + 13);
                chars[i] = c;
            }
            else if(ascii >= 78 && ascii <= 90){
                c = (char)( c - 13);
                chars[i] = c;
            }
        }

        hint = String.copyValueOf(chars);
        hint = hint.trim();
        Log.v("GeoNote","result: " + hint);

        return hint;
    }


    /* This function changes ui type spinner so that the given type number corresponds to right type
    *
    *
    *   2 == regular
    *   3 == multi
    *   6 == happening
    *   8 == mystery
    */
    private void changeCacheType(int type){
        Log.v("GeoNote","changeCacheType == " + type);
        switch (type){
            //Regular cache
            case 2:
                ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).setSelection(2);
                break;
            //Multi cache
            case 3:
                ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).setSelection(1);
                break;
            //Happening cache
            case 6:
                ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).setSelection(3);
                break;
            //Mystery cache
            case 8:
                ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).setSelection(0);
                break;
        }
    }

    public interface CacheListener {
        public void onCacheModified(Cache cache, String oldName);
        public boolean isThereSameNameCache(String name);
    }

    static ModifyCache newInstance(Cache cache) {
        ModifyCache objectCache = new ModifyCache();
        // Supply argument.
        Bundle args = new Bundle();
        args.putString("name", cache.getName());
        args.putString("gc", cache.getGc());
        args.putString("latitude", cache.getLat());
        args.putString("longitude", cache.getLong());
        args.putDouble("difficulty", cache.getDifficulty());
        args.putDouble("terrain", cache.getTerrain());
        args.putString("size", cache.getSizeString());
        args.putString("note", cache.getNote());
        args.putString("type", cache.getTypeString());
        args.putString("winter", cache.getWinterString());
        objectCache.setArguments(args);
        return objectCache;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Update", null);
        builder.setNegativeButton("Cancel", null);
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        m_downloadStarted = false;

        //Create cache with given arguments
        createCache();

        builder.setView(inflater.inflate(R.layout.add_remove_cache_layout, null));

        final AlertDialog ad = builder.create();
        //Override the default behaviour of positive button so decision can be made if dialog is dismissed or not
        ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = ad.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (((EditText) getDialog().findViewById(R.id.cacheName)).getText().length() == 0) {
                            //Set alert dialog that informs cache name being empty
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getResources().getString(R.string.error_cache_name_empty));
                            textView.setTextSize(20);
                            textView.setTextColor(Color.BLACK);
                            alertDialogBuilder.setCustomTitle(textView);
                            alertDialogBuilder.show();
                        }
                        //If there is same name already in listview and if the name is not for this particular cache
                        else if (m_listener.isThereSameNameCache((((EditText) getDialog().findViewById(R.id.cacheName)).getText().toString())) &&
                                !m_cache.getName().equalsIgnoreCase(m_oldName)) {
                            //Set alert dialog that informs about existing cache with similar name
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getResources().getString(R.string.error_cache_exists));
                            textView.setTextSize(20);
                            textView.setTextColor(Color.BLACK);
                            alertDialogBuilder.setCustomTitle(textView);
                            alertDialogBuilder.show();
                        } else {
                            updateCache();
                            m_listener.onCacheModified(m_cache, m_oldName);
                            dismiss();
                        }
                    }
                });
            }
        });

        return ad;
    }

    private void updateCache() {
        String str = ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).getSelectedItem().toString();
        //Set type
        m_cache.setType(str);
        //Set Terrain
        m_cache.setSize(((Spinner) getDialog().findViewById(R.id.cache_size_spinner)).getSelectedItem().toString());
        //Set Difficulty
        m_cache.setDifficulty(Double.valueOf(((Spinner) getDialog().findViewById(R.id.cache_difficulty_spinner)).getSelectedItem().toString()));
        //Set Terrain
        m_cache.setTerrain(Double.valueOf(((Spinner) getDialog().findViewById(R.id.cache_terrain_spinner)).getSelectedItem().toString()));
        //Set name
        m_cache.setName(((EditText) getDialog().findViewById(R.id.cacheName)).getText().toString());

        //Setup latitude and longitude
        String lat = ((EditText) getDialog().findViewById(R.id.cache_latitude_degree)).getText().toString();

        if(lat.length() != 0){
            //Add : to conform the notations DD:MM.MMMMM
            lat += ":";
            String minutes = ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).getText().toString();
            if(minutes.length() != 0 && minutes.contains(".") && minutes.length() >= 4){
                lat += minutes;
            }
            else{lat += "00.000";}
        }
        else{
            lat = "00:";
            String minutes = ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).getText().toString();
            if(minutes.length() != 0 && minutes.contains(".")&& minutes.length() >= 4){
                lat += minutes;
            }
            else{lat += "00.000";}
        }
        String longi = ((EditText) getDialog().findViewById(R.id.cache_longitude_degree)).getText().toString();
        if(longi.length() != 0){
            //Add : to conform the notations DD:MM.MMMMM
            longi += ":";
            String minutes = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).getText().toString();
            if(minutes.length() != 0 && minutes.contains(".")&& minutes.length() >= 4){
                longi += minutes;
            }
            else {longi += "00.000";}
        }
        else{
            longi = "00:";
            String minutes = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).getText().toString();
            if(minutes.length() != 0 && minutes.contains(".")&& minutes.length() >= 4){
                longi += minutes;
            }
            else{longi += "00.000";}
        }

        Log.v("GeoNote", "Before setting coordinates are: " + lat + ", " + longi);
        m_cache.setLatLong(lat, longi);
        //Set note
        String note = ((EditText)getDialog().findViewById(R.id.cache_add_note)).getText().toString();
        m_cache.setNote(note);
        //Set gc- code
        String gc = ((EditText)getDialog().findViewById(R.id.cacheGc)).getText().toString();
        m_cache.setGc(gc);

        m_cache.setWinter(((Spinner) getDialog().findViewById(R.id.cache_winter_spinner)).getSelectedItem().toString());


    }

    @Override
    public void onStart(){
        super.onStart();
        setupUiHandlers();
    }

    private void setupUiHandlers() {
        String str = "" + '\u00B0';
        ((TextView)getDialog().findViewById(R.id.cache_degree_sign_latitude)).setText(str);
        ((TextView)getDialog().findViewById(R.id.cache_degree_sign_longitude)).setText(str);

        //Populate type spinner
        Spinner spinner = ((Spinner)getDialog().findViewById(R.id.cache_type_spinner));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_type_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String type = m_cache.getTypeString();
        if(type.contains("mystery")){
            spinner.setSelection(0);
        }
        else if(type.contains("multi")){
            spinner.setSelection(1);
        }
        else if(type.contains("regular")){
            spinner.setSelection(2);
        }
        else if(type.contains("happening")){
            spinner.setSelection(3);
        }
        //Set other
        else{
            spinner.setSelection(4);
        }

        //Populate size spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_size_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_size_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        String size = m_cache.getSizeString();
        if(size.contains("micro")){spinner.setSelection(0);}
        else if(size.contains("small")){spinner.setSelection(1);}
        else if(size.contains("regular")){spinner.setSelection(2);}
        else if(size.contains("large")){spinner.setSelection(3);}
        else{spinner.setSelection(4);}

        //Populate difficulty spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_difficulty_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_difficulty_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(Double.toString(m_cache.getDifficulty())));

        //Populate cache spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_terrain_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(Double.toString(m_cache.getTerrain())));

        //Populate winter spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_winter_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_winter_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(adapter.getPosition(m_cache.getWinterString()));
        String winter = m_cache.getWinterString();
        if(winter.toLowerCase().contains("yes")){
            spinner.setSelection(0);
        }
        else if(winter.toLowerCase().contains("no") && winter.length() == 2){
            spinner.setSelection(1);
        }
        else{
            spinner.setSelection(2);
        }
        //Set name
        ((EditText)getDialog().findViewById(R.id.cacheName)).setText(m_cache.getName());
        //Set GC code
        ((EditText)getDialog().findViewById(R.id.cacheGc)).setText(m_cache.getGc());
        //Set N degrees
        ((EditText)getDialog().findViewById(R.id.cache_latitude_degree)).setText(m_cache.getLatDegrees());
        //Set N minutes
        ((EditText)getDialog().findViewById(R.id.cache_latitude_minutes)).setText(m_cache.getLatMinutes());
        //Set E degrees
        ((EditText)getDialog().findViewById(R.id.cache_longitude_degree)).setText(m_cache.getLongDegrees());
        //Set E minutes
        ((EditText)getDialog().findViewById(R.id.cache_longitude_minutes)).setText(m_cache.getLongMinutes());
        //Set Note
        ((EditText)getDialog().findViewById(R.id.cache_add_note)).setText(m_cache.getNote());

        //Set listener for gc editText
        ((EditText)getDialog().findViewById(R.id.cacheGc)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.v("GeoNote", "focus changed to: " + hasFocus);

                if (!hasFocus && !m_downloadStarted) {
                    getGcAttributes(((EditText) v).getText().toString());
                }
            }
        });


        ((EditText)getDialog().findViewById(R.id.cache_latitude_degree)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    Log.v("GeoNote", "Two chars in box");
                    ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //Add . affter two numbers
                if (s.length() == 2 && count > before) {
                    EditText text = ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes));
                    text.append(".");
                } else if (s.length() == 6) {
                    ((EditText) getDialog().findViewById(R.id.cache_longitude_degree)).requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ((EditText)getDialog().findViewById(R.id.cache_longitude_degree)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2) {
                    ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 2 && count > before) {
                    EditText text = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes));
                    //text.setText(text.getText() + ".");
                    text.append(".");
                } else if (s.length() == 6) {
                    ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).clearFocus();
                    InputMethodManager imm = ((InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE));
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        //If coordinates are 0 then make the corresponding minutes and degree fields empty
        EditText textEdit = ((EditText) getDialog().findViewById(R.id.cache_latitude_degree));
        if(textEdit.getText().toString().contentEquals("0")){
            Log.v("GeoNote", "Got here 1: " + textEdit.getText().toString());
            textEdit.setText("");
        }
        textEdit = ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes));
        if(textEdit.getText().toString().contentEquals("0")){
            Log.v("GeoNote", "Got here 2: " + textEdit.getText().toString());
            textEdit.setText("");
        }
        textEdit = ((EditText) getDialog().findViewById(R.id.cache_longitude_degree));
        if(textEdit.getText().toString().contentEquals("0")){
            Log.v("GeoNote", "Got here 3: " + textEdit.getText().toString());
            textEdit.setText("");
        }
        textEdit = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes));
        if(textEdit.getText().toString().contentEquals("0")){
            Log.v("GeoNote", "Got here 4 "  + textEdit.getText().toString());
            textEdit.setText("");
        }


        Log.v("GeoNote", "now Coordinates:" + m_cache.getCoordinates());

    }


    public void getGcAttributes(String gc){
        Log.v("GeoNote","gc code is " + gc);
        if(gc.length() != 0){
            Log.v("GeoNote", "Starting download");
            m_downloadStarted = true;
            AsyncCacheGet async = new AsyncCacheGet();
            //Setting url that is used to connect to server
            async.setGc(gc.toUpperCase());
            async.addListener(this);
            async.execute();
        }
    }

    private void createCache() {
        m_cache = new Cache();
        m_cache.setName(getArguments().getString("name"));
        m_cache.setGc(getArguments().getString("gc"));
        m_cache.setLatLong(getArguments().getString("latitude"), getArguments().getString("longitude"));
        m_cache.setDifficulty(getArguments().getDouble("difficulty"));
        m_cache.setTerrain(getArguments().getDouble("terrain"));
        m_cache.setSize(getArguments().getString("size"));
        m_cache.setNote(getArguments().getString("note"));
        m_cache.setType(getArguments().getString("type"));
        m_cache.setWinter(getArguments().getString("winter"));
        m_oldName = getArguments().getString("name");
        Log.v("GeoNote", "2now Coordinates:" + m_cache.getLat() + "," + m_cache.getLong());
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            m_listener = (CacheListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement CacheListener");
        }
    }
    @Override
    public void onDetach() {
       m_listener = null; // => avoid leaking
        super.onDetach();
    }

}