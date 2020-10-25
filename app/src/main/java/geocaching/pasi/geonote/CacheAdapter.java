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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Pasi on 07/03/2016.
 */
public class CacheAdapter extends ArrayAdapter<Cache> {
    private ArrayList<Cache> m_objects;
    private Context m_context;

    public CacheAdapter(Context context, int resource, ArrayList<Cache> objects) {
        super(context, resource, objects);
        m_context = context;
        m_objects = objects;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        PartHolder holder = null;
        if(view == null){
            view = LayoutInflater.from(this.getContext()).inflate(R.layout.listview_item_row,parent,false);
            holder = new PartHolder();
            holder.key = (TextView)view.findViewById(R.id.key);
            holder.value = ((View)view.findViewById(R.id.circle));
            view.setTag(holder);
        }
        else{
            holder = (PartHolder)view.getTag();
        }
        holder.key.setText(m_objects.get(position).getName());
        // Is mystery cache
        if(m_objects.get(position).getTypeString().indexOf("mystery") != -1){
            ((View)holder.value).findViewById(R.id.circle).setBackground(getContext().getResources().getDrawable(R.drawable.blue_circle));
        }
        else if(m_objects.get(position).getTypeString().indexOf("multi") != -1){
            ((View)holder.value).findViewById(R.id.circle).setBackground(getContext().getResources().getDrawable(R.drawable.yellow_circle));
        }
        else if(m_objects.get(position).getTypeString().indexOf("regular") != -1){
            ((View)holder.value).findViewById(R.id.circle).setBackground(getContext().getResources().getDrawable(R.drawable.green_circle));
        }
        else if(m_objects.get(position).getTypeString().indexOf("happening") != -1){
            ((View)holder.value).findViewById(R.id.circle).setBackground(getContext().getResources().getDrawable(R.drawable.red_circle));
        }
        return view;
    }

    static class PartHolder{
        TextView key;
        View value;
    }
}

