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

/**
 * Created by Pasi on 08/03/2016.
 */
public class AddCache extends DialogFragment implements AsyncCacheGet.HttpCacheListener{

    private Cache m_cache;
    private CacheListener m_listener;
    private boolean m_downloadedOnce;

    @Override
    public void gotCache(String result) {
        if(m_downloadedOnce){
            parseCacheInformationFromString(result);
            return;
        }
        m_downloadedOnce = true;
        //Then we got redirect and let´s find the proper site
        int ind1 = result.indexOf("Object moved to");
        //Prevents unlimited download loops
        if(ind1 != -1){
            Log.v("GeoNote", "gotCache object moved to ");
            ind1 = result.indexOf("\"",ind1);
            if(ind1 != -1){
                ind1 += 32;
                int ind2 = result.indexOf("\"", ind1);
                ind1 = result.indexOf("/",ind1);
                if(ind1 != -1 && ind2 != -1 && ind2 > ind1){
                    ind1 += 1;
                    AsyncCacheGet async = new AsyncCacheGet();
                    Log.v("GeoNote", "So far so good");
                    Log.v("GeoNote",result.substring(ind1, ind2));
                    async.setGc(result.substring(ind1, ind2));
                    async.addListener(this);
                    async.execute();
                }
            }
        }
    }

    public interface CacheListener{
        public void onCacheNew(Cache cache);
        public boolean isThereSameNameCache(String name);
    }


    static AddCache newInstance(Cache cache) {
        AddCache objectCache = new AddCache();
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
                        if(((EditText) getDialog().findViewById(R.id.cacheName)).getText().length() == 0){
                            //Set alert dialog that informs cache name being empty
                            android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(getContext());
                            TextView textView = new TextView(getContext());
                            textView.setText(getResources().getString(R.string.error_cache_name_empty));
                            textView.setTextSize(20);
                            textView.setTextColor(Color.BLACK);
                            alertDialogBuilder.setCustomTitle(textView);
                            alertDialogBuilder.show();
                        }
                        else if (m_listener.isThereSameNameCache((((EditText) getDialog().findViewById(R.id.cacheName)).getText().toString()))) {
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
                if(!hasFocus){
                    getGcAttributes(((EditText)v).getText().toString());
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
        if(gc != null && gc.length() != 0){
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

        //First find cache name
        int ind1 = result.indexOf("CacheName");
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


    //Function finds the hint from string and decrypts it and returns it
    private String getHintFromString(String str){
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
}
