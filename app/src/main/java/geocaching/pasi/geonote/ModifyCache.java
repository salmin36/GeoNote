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
public class ModifyCache extends DialogFragment {
    private Cache m_cache;
    private CacheListener m_listener;
    private String m_oldName;

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