/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */


package geocaching.pasi.geonote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SingleCacheFragment extends Fragment {
    private View m_fragmentsView;
    private Cache m_lastSetCache;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        m_fragmentsView = inflater.inflate(R.layout.fragment_single_cache, container, false);
        initUi();
        return m_fragmentsView;
    }

    private void initUi() {
        //Disappear all the names of view components
        ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_e)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_n)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_difficulty_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_size_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_terrain_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_winter_name)).setVisibility(View.INVISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_remove_button)).setVisibility(View.INVISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_modify_button)).setVisibility(View.INVISIBLE);

        //Set remove button handeler
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_remove_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Set alert dialog that asks if you really want to remove Cache
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                alertDialogBuilder.setTitle("Are you sure that you want to delete this Cache?");
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((MainActivity) getActivity()).removeSelectedCache();
                        emptyView();
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialogBuilder.show();
            }
        });
        //Set modify button handler
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_modify_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_lastSetCache != null){
                    Log.v("GeoNote", "m_lasSetCache: " + m_lastSetCache.getName() + "," + m_lastSetCache.getCoordinates());
                    DialogFragment newFragment = ModifyCache.newInstance(m_lastSetCache);
                    newFragment.show(getActivity().getSupportFragmentManager(), "customDialog");
                }
            }
        });

    }

    public void setupCache(Cache cache){
        m_lastSetCache = cache;
        //Make appear
        ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_e)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_n)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_difficulty_name)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_size_name)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_terrain_name)).setVisibility(View.VISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_winter_name)).setVisibility(View.VISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_remove_button)).setVisibility(View.VISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_modify_button)).setVisibility(View.VISIBLE);

        //Update name
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_name)).setText(cache.getName());
        //Update gc code
        ((TextView)m_fragmentsView.findViewById(R.id.single_cacheGc)).setText(cache.getGc());

        //Update type
        if(cache.getTypeString().contains("multi")){
            ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setBackground(getContext().getResources().getDrawable(R.drawable.yellow_circle));
        }
        else if(cache.getTypeString().contains("mystery")){
            ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setBackground(getContext().getResources().getDrawable(R.drawable.blue_circle));
        }
        else if(cache.getTypeString().contains("regular")){
            ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setBackground(getContext().getResources().getDrawable(R.drawable.green_circle));
        }
        else if(cache.getTypeString().contains("happening")){
            ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setBackground(getContext().getResources().getDrawable(R.drawable.red_circle));
        }

        //Update Latitude and Longitude
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_latitude)).setText(cache.getLat());
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_longitude)).setText(cache.getLong());
        //Update difficulty
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_difficulty_value)).setText(cache.getDifficulty().toString());
        //Update Terrain
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_terrain_value)).setText(cache.getTerrain().toString());
        //Update Size
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_size_value)).setText(cache.getSizeString());
        //Update Winter availability
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_winter_value)).setText(cache.getWinterString());
        //Update note
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_note)).setText(cache.getNote());
    }


    //This function is used to empty entier view of all the info when currently shown cache was deleted
    public void emptyView(){
        m_lastSetCache = null;
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_name)).setText("");
        ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setBackground(getContext().getResources().getDrawable(R.drawable.white_circle));
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_latitude)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_longitude)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_difficulty_value)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_terrain_value)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_note)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_winter_value)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_size_value)).setText("");
        ((TextView)m_fragmentsView.findViewById(R.id.single_cacheGc)).setText("");

        //Disappear all the names of view components
        ((View)m_fragmentsView.findViewById(R.id.single_cache_circle)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_e)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_n)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_difficulty_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_size_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_terrain_name)).setVisibility(View.INVISIBLE);
        ((TextView)m_fragmentsView.findViewById(R.id.single_cache_winter_name)).setVisibility(View.INVISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_remove_button)).setVisibility(View.INVISIBLE);
        ((Button)m_fragmentsView.findViewById(R.id.single_cache_modify_button)).setVisibility(View.INVISIBLE);
    }
}
