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
import android.widget.Toast;


public class AddCache extends DialogFragment implements AsyncCacheGet.HttpCacheListener, ReportCache{

    private Cache m_cache;
    private CacheListener m_listener;
    private boolean m_downloadedOnce;
    private boolean m_downloadStarted;
    private User myUser;


    @Override
    public void gotCache(String result) {
        Log.v("GeoNote", "gotCache started");
        parseCacheInformationFromString(result);
        m_downloadStarted = false;
    }

    @Override
    public void locationFound(String location)
    {
        updateLocation(location);
    }

    @Override
    public void showToastMessage(String message) {
        m_listener.redirectToastMessage(message);
    }

    public interface CacheListener{
        public void onCacheNew(Cache cache);
        public boolean isThereSameNameCache(String name);
        public void redirectToastMessage(String message);
    }


    static AddCache newInstance(Cache cache, User user) {
        AddCache objectCache = new AddCache();
        objectCache.addUser(user);
        // Supply argument.
        Bundle args = new Bundle();
        args.putString("name", cache.getName());
        args.putString("gc", cache.getGc());
        args.putString("latitude",cache.getLat());
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

    private void addUser(User user) {
        myUser = user;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", null);

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        //Create cache with given arguments
        createCache();
        m_downloadedOnce = false;
        m_downloadStarted = false;
        builder.setView(inflater.inflate(R.layout.add_remove_cache_layout, null));
        final AlertDialog d = builder.create();

        //Override the default behaviour of positive button so decision can be made if dialog is dismissed or not
        d.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = d.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v("GeoNote", "Positive button clicked");
                        //Figure out if there is already similar name of existing cache
                        if (((EditText) getDialog().findViewById(R.id.cacheName)).getText().length() == 0) {
                            //Set alert dialog that informs cache name being empty
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getResources().getString(R.string.error_cache_name_empty));
                            textView.setTextSize(20);
                            textView.setTextColor(Color.BLACK);
                            alertDialogBuilder.setCustomTitle(textView);
                            alertDialogBuilder.show();
                        } else if (m_listener.isThereSameNameCache((((EditText) getDialog().findViewById(R.id.cacheName)).getText().toString()))) {
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
                            m_listener.onCacheNew(m_cache);
                            dismiss();
                        }
                    }
                });
            }
        });

        return d;
    }

    private void updateCache() {
        String str = ((Spinner)getDialog().findViewById(R.id.cache_type_spinner)).getSelectedItem().toString();
        Toast.makeText(getContext(), str, Toast.LENGTH_SHORT).show();
        //Set type
        m_cache.setType(str);
        //Set Terrain
        m_cache.setSize(((Spinner) getDialog().findViewById(R.id.cache_terrain_spinner)).getSelectedItem().toString());
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
            if(minutes.length() != 0){
                lat += minutes;
            }
        }
        String longi = ((EditText) getDialog().findViewById(R.id.cache_longitude_degree)).getText().toString();
        if(longi.length() != 0){
            //Add : to conform the notations DD:MM.MMMMM
            longi += ":";
            String minutes = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).getText().toString();
            if(minutes.length() != 0){
                longi += minutes;
            }
        }
        m_cache.setLatLong(lat, longi);
        //Set note
        String note = ((EditText)getDialog().findViewById(R.id.cache_add_note)).getText().toString();
        m_cache.setNote(note);
        //Set gc- code
        String gc = ((EditText)getDialog().findViewById(R.id.cacheGc)).getText().toString();
        Log.v("GeoNote", "AddCache.updateCache: gc == " + gc);
        m_cache.setGc(gc);
        Log.v("GeoNote", "AddCache.updateCache: gc == " + m_cache.getGc());
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

        //Populate spinner
        Spinner spinner = ((Spinner)getDialog().findViewById(R.id.cache_type_spinner));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_type_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Populate spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_size_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_size_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Populate spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_difficulty_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_difficulty_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        //Populate spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_terrain_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_terrain_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //Populate spinner
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_winter_spinner));
        adapter = ArrayAdapter.createFromResource(getContext(),R.array.cache_winter_array,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        //Set listener for gc editText
        ((EditText)getDialog().findViewById(R.id.cacheGc)).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.v("GeoNote", "focus changed to: " + hasFocus);

                if(!hasFocus && !m_downloadStarted){
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
                if(s.length() == 2){
                    EditText text = ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes));
                    //text.setText(text.getText() + ".");
                    text.append(".");
                }
                else if(s.length() == 6){
                    //Here check if there is minutes that are 0< or >= 60
                    try {
                        Double db = Double.valueOf(((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).toString());
                        if(db < 0 || db >= 60)
                        {
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getResources().getString(R.string.error_wrong_minutes));
                            textView.setTextSize(20);
                            textView.setTextColor(Color.BLACK);
                            alertDialogBuilder.setCustomTitle(textView);
                            alertDialogBuilder.show();
                        }
                    }
                    catch(NumberFormatException ex)
                    {
                        Log.v("GeoNote", "Error: trying change minute string to double");
                    }
                    //Then change focus
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
                if(s.length() == 2){
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
                if(s.length() == 2){
                    EditText text = ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes));
                    //text.setText(text.getText() + ".");
                    text.append(".");
                }
                else if(s.length() == 6){
                    ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).clearFocus();
                    InputMethodManager imm = ((InputMethodManager) getContext().getSystemService(Service.INPUT_METHOD_SERVICE));
                    if(imm != null){
                        imm.hideSoftInputFromWindow(((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).getWindowToken(), 0);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




    }

    public void getGcAttributes(String gc){
        Log.v("GeoNote","gc code is " + gc);
        if(gc.length() != 0){
            Log.v("GeoNote", "Starting download");
            m_downloadStarted = true;
            AsyncCacheGet async = new AsyncCacheGet();
            //Setting url that is used to connect to server
            async.setUser(myUser);
            async.setGc(gc.toUpperCase());
            async.addListener(this);
            async.addUniqueListener(this);
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



    private void parseCacheInformationFromString(String result){
        Cache cache = CacheInfoParser.parseInfomation(result);

        changeCacheType(cache.getTypeInt());

        ((EditText)getDialog().findViewById(R.id.cacheName)).setText(cache.getName());

        Double difficulty = cache.getDifficulty();
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


        Double terrain = cache.getTerrain();
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



        spinner =((Spinner)getDialog().findViewById(R.id.cache_size_spinner));
        String size = cache.getSizeString();
        if(size.contains("micro")){spinner.setSelection(0);}
        else if(size.contains("small")){spinner.setSelection(1);}
        else if(size.contains("regular")){spinner.setSelection(2);}
        else if(size.contains("large")){spinner.setSelection(3);}
        else{spinner.setSelection(4);}

        /* Here winter availablity*/
        spinner = ((Spinner)getDialog().findViewById(R.id.cache_winter_spinner));
        String winterString = cache.getWinterString();
        if(winterString.contentEquals("Yes")){spinner.setSelection(0);}
        else if(winterString.contentEquals("No")){spinner.setSelection(1);}
        else{spinner.setSelection(2);}

        ((EditText)getDialog().findViewById(R.id.cache_add_note)).setText(cache.getNote());

    }


    private void updateLocation(String location)
    {
        Log.v("GeoNote", "updateLocation function called");
        if(location != null && location.length() != 0)
        {
            int latitudeDegrees = 0;
            Double latitudeMinutes = 0.000;
            int longitudeDegrees = 0;
            Double longitudeMinutes = 0.0;
            //N 61° 26.484 E 023° 50.241
            char  degree = '\u00B0';
            int index1 = location.indexOf(degree);

            if(index1 != -1)
            {
                latitudeDegrees = Integer.parseInt(location.substring(1, index1).trim());
                int index2 = location.indexOf("E", ++index1);
                if(index2 != -1)
                {
                    latitudeMinutes = Double.parseDouble(location.substring(index1, index2).trim());
                    index1 = location.indexOf(degree, ++index2);
                    if(index1 != -1)
                    {
                        longitudeDegrees = Integer.parseInt(location.substring(index2, index1).trim());
                        if(index1 > index2)
                        {
                            longitudeMinutes = Double.parseDouble(location.substring(index1 + 1, location.length()).trim());
                        }
                    }
                }
            }
            ((EditText) getDialog().findViewById(R.id.cache_latitude_degree)).setText("" + latitudeDegrees);
            ((EditText) getDialog().findViewById(R.id.cache_latitude_minutes)).setText("" + latitudeMinutes);
            ((EditText) getDialog().findViewById(R.id.cache_longitude_degree)).setText("" + longitudeDegrees);
            ((EditText) getDialog().findViewById(R.id.cache_longitude_minutes)).setText("" + longitudeMinutes);
        }
    }


    /* This function changes ui type spinner so that the given type number corresponds to right type
    *
    *
    *   2 == regular
    *   3 == multi
    *   6 == happening
    *   8 == mystery
    */
    void changeCacheType(int type){
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
}


