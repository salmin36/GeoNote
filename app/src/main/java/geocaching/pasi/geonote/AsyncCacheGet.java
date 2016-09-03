package geocaching.pasi.geonote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Pasi on 10/03/2016.
 */
public class AsyncCacheGet extends AsyncTask<String, String, String> {

    interface HttpCacheListener{
        void gotCache(String result);
    }
    //Used to keep track of registered listeners
    private List<HttpCacheListener> listeners = new ArrayList<HttpCacheListener>();

    private ReportCache m_reportCacheListener = null;

    private static final String GEO_BASE_URL = "https://www.geocaching.com/geocache/";

    private HttpURLConnection m_urlConnection;
    private String m_url = "";

    private boolean m_getUserSpecificCacheInformation = false;


    //Set gc to url that is to be used to get information about single cache
    public void setGc(String gc){
        if(gc != null && gc.length() != 0){
            m_url = GEO_BASE_URL + gc;
        }
    }

    @Override
    protected String doInBackground(String... params) {
        int responseCode = 0;
        //If for some reason url is empty then just return empty string without trying to download anything
        if(m_url.length() == 0){
            return "";
        }

        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(m_url);
            Log.v("GeoNote", "URL " + url);

            m_urlConnection = (HttpURLConnection) url.openConnection();
            m_urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
            InputStream in = new BufferedInputStream(m_urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
//                Log.v("GeoNote",line);
            }
            responseCode = m_urlConnection.getResponseCode();
  //          Log.v("GeoNote", "HTTP response code is " + responseCode);

        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            m_urlConnection.disconnect();
        }




        return String.valueOf(responseCode) + ":" + result.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        //If nothing was returned from doInBackground function then:
        if(result.length() == 0){
            return;
        }

        int ind1 = result.indexOf("cacheImage");
        if(ind1 != -1){
            ind1 = result.indexOf("WptTypes/");
            if(ind1 != -1){
                String str = result.substring(ind1 + 9,ind1 + 10);
                if(str.contains("2"))
                {
                    m_getUserSpecificCacheInformation = true;
                }
            }

        }



        // Notify everybody that may be interested.
        for (HttpCacheListener hl : listeners)
            hl.gotCache(result);

        if(m_getUserSpecificCacheInformation) {
            AsyncPreLogin asyncPre = new AsyncPreLogin(m_urlConnection.getURL().toString(), m_reportCacheListener);
            asyncPre.execute();
        }
    }

    //Function to register listener to this Action
    public void addListener(HttpCacheListener toAdd) {
        listeners.add(toAdd);
    }

    public void addUniqueListener(ReportCache reportCache)
    {
        m_reportCacheListener = reportCache;
    }
}
