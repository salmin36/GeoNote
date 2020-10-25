/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;


public class MainActivity extends FragmentActivity implements AddCache.CacheListener, ModifyCache.CacheListener {
    //Used to keep track pages and change them
    private PagerAdapter m_pager;
    private AdView mAdView;
    //private ArrayList<Pair<String, String>> m_listItems;
    private ArrayList<Cache> m_listItems;
    private DBHelper m_dbHelper;
    private User myUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Load banner
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        //
        //To able to test the application please add valid username and password into this
        myUser = new User("user","password");
        //Intialize Fragments
        initFragments();

        m_dbHelper = new DBHelper(getBaseContext());
        m_listItems = m_dbHelper.getAllCaches();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
            }
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            DialogFragment newFragment = MenuView.newInstance(myUser);
            newFragment.show(getSupportFragmentManager(), "customDialog");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void initFragments() {
        //Create new PagerAdapter
        m_pager = new PagerAdapter(getSupportFragmentManager(),myUser);
        //Get the pager
        ViewPager pager = (ViewPager) super.findViewById(R.id.view_pager);
        pager.setAdapter(this.m_pager);
        //Set listener to determine what page we are changed to
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                //Page changed
                if (i == 2) {
                    Cache cache = ((ListFragment) m_pager.getItem(0)).getSelectedCache();
                    if (cache != null) {
                        ((MapFragment) m_pager.getItem(2)).startMeasure(cache.getCoordinates());
                    }
                } else {
                    ((MapFragment) m_pager.getItem(2)).stopMeasure();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }




    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }




    @Override
    public void onBackPressed() {
        //If we are at map fragment then override the back button behaviour so it goeas back to previous fragment
        if(((ViewPager)findViewById(R.id.view_pager)).getCurrentItem() == 2){
            ((ViewPager)findViewById(R.id.view_pager)).setCurrentItem(1);
        }
        else if(((ViewPager)findViewById(R.id.view_pager)).getCurrentItem() == 1){
            ((ViewPager)findViewById(R.id.view_pager)).setCurrentItem(0);
        }
        else{
            super.onBackPressed();
        }
    }

    //This function is called when from AddORModifyCache-object ui add button has been clicked
    @Override
    public void onCacheNew(Cache cache) {
        m_listItems.add(cache);
        m_dbHelper.addCache(cache);
        ((ListFragment) m_pager.getItem(0)).updateListView();
        ((MapFragment) m_pager.getItem(2)).addMarkerToMap(cache);
    }

    @Override
    public boolean isThereSameNameCache(String name) {
        for(int i = 0;  i < m_listItems.size(); i++){
            if(m_listItems.get(i).getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void redirectToastMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public ArrayList<Cache> getCacheList(){
        return m_listItems;
    }

    //This function is called by ListFragment when listView item is selected
    //It is going to call SingleCacheFragment to update its ui
    public void routeUpdateCallFromListToSingle(Cache cache){
        ((SingleCacheFragment)m_pager.getItem(1)).setupCache(cache);
    }

    public void routeUpdateCallFromListToMap(Cache cache){
        if(cache.getCoordinates().latitude != 0.0 && cache.getCoordinates().longitude != 0.0){
            ((MapFragment)m_pager.getItem(2)).updateMapNewLocation(cache);
            //((MapFragment) m_pager.getItem(2)).addMarkerToMap(cache);
        }
    }

    //This function is called by SingleCacheFragment when user verifies that he really wants to remove selected cache
    public void removeSelectedCache(){
        Cache cache = ((ListFragment) m_pager.getItem(0)).getSelectedCache();
        if(cache != null){
            //Remove item
            m_listItems.remove(cache);
            ((ListFragment) m_pager.getItem(0)).updateListView();
            m_dbHelper.removeCache(cache);
            ((ListFragment) m_pager.getItem(0)).removeSelected();
            ((MapFragment) m_pager.getItem(2)).removeSelected();
            mapCanBeUpdatedNow();
            //Change page to listFragment after removing cache
            ((ViewPager)findViewById(R.id.view_pager)).setCurrentItem(0);
    }
    }

    //This function is called when one item is clicked from listView inside ListFragment
    //It changes the fragment user is currently viewing to SingleCacheFragment
    public void changePageToSingleFragment(){
        ((ViewPager)findViewById(R.id.view_pager)).setCurrentItem(1);
    }


    //This function is called by MapFragment when it is shown so that we can now populate map with points
    public void mapCanBeUpdatedNow(){
        for(int i = 0; i < m_listItems.size(); i++){
            ((MapFragment) m_pager.getItem(2)).addMarkerToMap(m_listItems.get(i));
        }
    }

    //This function is called by MapFragment when user changes the selected cache by clicking on marker in map
    public void mapChangesSelectedCache(String name){
        for(int i = 0; i < m_listItems.size(); i++){
            if(m_listItems.get(i).getName().contains(name)){
                ((ListFragment) m_pager.getItem(0)).setSelectedCache(m_listItems.get(i));
                return;
            }
        }
    }

    @Override
    public void onCacheModified(Cache cache, String oldName) {
        Log.v("GeoNote","MainActivity:onCacheModified started");
        //Go throught all the caches and find the one that corresponds to oldName
        Cache handeled;
        boolean foundOne = false;
        for(int i = 0; i < m_listItems.size(); i++){
            handeled = m_listItems.get(i);
            if(oldName == handeled.getName()){
                handeled.setName(cache.getName());
                handeled.setGc(cache.getGc());
                handeled.setCoordinates(cache.getCoordinates());
                handeled.setDifficulty(cache.getDifficulty());
                handeled.setTerrain(cache.getTerrain());
                handeled.setSize(cache.getSizeString());
                handeled.setType(cache.getTypeString());
                handeled.setWinter(cache.getWinterString());
                handeled.setNote(cache.getNote());
                foundOne = true;
                break;
            }
        }

        //If for some reason modified Cache not found from list then make a new one
        if(!foundOne){
            m_listItems.add(cache);
            m_dbHelper.addCache(cache);
            ((ListFragment) m_pager.getItem(0)).updateListView();
            ((MapFragment) m_pager.getItem(2)).addMarkerToMap(cache);
        }
        //If one cache corresponding to oldName was found then modify it and relay information to db and list and map
        else{
            m_dbHelper.updateCache(cache, oldName);
            ((ListFragment) m_pager.getItem(0)).updateListView();
            ((MapFragment) m_pager.getItem(2)).removeSelected();
            mapCanBeUpdatedNow();
        }
        routeUpdateCallFromListToSingle(cache);
    }

}
