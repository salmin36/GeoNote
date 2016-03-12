package geocaching.pasi.geonote;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListFragment extends Fragment {
    private View m_fragmentsView;
    private CacheAdapter m_cacheAdapter;
    private Cache m_currentlyShowing;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }

        //Save view so it can be used to change content if neccesary
        m_fragmentsView = inflater.inflate(R.layout.list_fragment, container, false);
        ListView list = (ListView) (m_fragmentsView.findViewById(R.id.cacheListView));
        m_cacheAdapter = new CacheAdapter(getContext(), R.layout.listview_item_row, ((MainActivity)getActivity()).getCacheList());
        list.setAdapter(m_cacheAdapter);
        list.setClickable(true);


        setHandlers();



        return m_fragmentsView;
    }

    private void setHandlers() {
        ((Button)m_fragmentsView.findViewById(R.id.new_list_item_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = AddCache.newInstance(new Cache());
                newFragment.show(getActivity().getSupportFragmentManager(), "customDialog");
            }
        });
        ((ListView)m_fragmentsView.findViewById(R.id.cacheListView)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                m_currentlyShowing = (Cache) parent.getItemAtPosition(position);
                ((MainActivity) getActivity()).routeUpdateCallFromListToSingle(m_currentlyShowing);
                ((MainActivity) getActivity()).routeUpdateCallFromListToMap(m_currentlyShowing);
                ((MainActivity) getActivity()).changePageToSingleFragment();
            }
        });
    }


    //This fuction is called by MainActivity to update the listview
    public void updateListView(){
        if(m_cacheAdapter != null){;
            m_cacheAdapter.notifyDataSetChanged();
        }
    }

    public Cache getSelectedCache(){
        return m_currentlyShowing;
    }

    public void removeSelected(){m_currentlyShowing = null;}

    //Used to set what cache is to be shown currently
    public void setSelectedCache(Cache cache){
        m_currentlyShowing = cache;
        ((MainActivity)getActivity()).routeUpdateCallFromListToSingle(m_currentlyShowing);
        ((MainActivity)getActivity()).routeUpdateCallFromListToMap(m_currentlyShowing);
    }
}


