/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;
import java.util.Vector;

/**
 * Created by Pasi on 07/03/2016.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    //Here we have all the fragments saved into a list
    private List<Fragment> m_fragments;

    public PagerAdapter(FragmentManager fm, User user){
        super(fm);
        ListFragment listFrag = new ListFragment();
        listFrag.setArguments(user);
        //Here we create the Fragments
        m_fragments = new Vector<Fragment>();
        m_fragments.add(listFrag);
        m_fragments.add(new SingleCacheFragment());
        m_fragments.add(new MapFragment());

    }

    @Override
    public Fragment getItem(int i) {
        return m_fragments.get(i);
    }

    @Override
    public int getCount() {
        return m_fragments.size();
    }

    public List<Fragment> getFragments() {
        return m_fragments;
    }
}
