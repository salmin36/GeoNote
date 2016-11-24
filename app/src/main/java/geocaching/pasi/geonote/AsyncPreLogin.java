package geocaching.pasi.geonote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Pasi on 23/03/2016.
 */




public class AsyncPreLogin extends AsyncTask<String, String, String> {


    private static final String GEO_BASE_URL = "https://www.geocaching.com/login/default.aspx?RESETCOMPLETE=y&redir=%2fplay%2fsearch";
    private HttpURLConnection m_urlConnection;
    private String m_url = "";

    private String m_cookie = "";
    private String m_urlString = "";
    private ReportCache m_reportCache = null;
    private User myUser = null;

    public AsyncPreLogin(String url, ReportCache reportCache)
    {
        Log.v("GeoNote","AsyncPreLogin constructor url: " + url );
        m_urlString = url;
        m_reportCache = reportCache;

    }

    public void setUser(User user){
        myUser = user;
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder result = new StringBuilder();

        try {
            URL url = new URL(GEO_BASE_URL);

            m_urlConnection = (HttpURLConnection) url.openConnection();
            InputStream in = new BufferedInputStream(m_urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Map<String, List<String>> headerFields = m_urlConnection.getHeaderFields();



            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            Log.v("GeoNote", "Here is cookies");
            for (int i = 0; i < cookiesHeader.size(); i++){
                Log.v("GeoNote", "Cookie " + i + ":" + cookiesHeader.get(i));
                m_cookie += cookiesHeader.get(i);
            }


            Log.v("GeoNote", "Here header");
            for (String key : headerFields.keySet()){
                for (String value : headerFields.get(key)){
                    Log.v("GeoNote", key + ":" +value);
                }
            }

        }catch( Exception e) {
            e.printStackTrace();
        }
        finally {
            if(m_urlConnection != null) {
                m_urlConnection.disconnect();
            }
        }

        return result.toString();
    }


    @Override
    protected void onPostExecute(String result) {
        Log.v("GeoNote", "AsyncPreLogin.onPostExcute");
        //Do something with the JSON string
        Log.v("GeoNote", result);
        String viewState = "";
        String viewStateGenerator = "";
        //Find viewstate
        String str = "id=\"__VIEWSTATE\" value=";
        int ind1 = result.indexOf(str);
        int ind2 = -1;
        if(ind1 != -1){
            ind2 = result.indexOf("\"", ind1 + 1 + str.length() );
            if (ind2 != -1 && ind1 < ind2) {
                ind1 = result.indexOf("value=", ind1);
                if(ind1 != -1){
                    ind1 += 7;
                    viewState = result.substring(ind1, ind2);
                    Log.v("GeoNote", "__VIEWSTATE = " + viewState);
                }

            }

        }

        str = "id=\"__VIEWSTATEGENERATOR\"";
        ind1 = result.indexOf(str);
        ind2 = -1;
        if(ind1 != -1){
            ind1 = result.indexOf("\"", ind1 + str.length());
            if(ind1 != -1) {
                ind2 = result.indexOf("\"", ind1 + 1);
                if (ind2 != -1 && ind1 < ind2) {
                    ind1 += 1;
                    viewStateGenerator = result.substring(ind1, ind2);
                    Log.v("GeoNote", "__VIEWSTATEGENERATOR = " + viewStateGenerator);
                }
            }
        }

        //Test if we can login to website with post
        if(myUser.isValidCredentials()) {
            AsyncTryLogin async = new AsyncTryLogin();
            async.setParameters(viewState, viewStateGenerator, m_cookie,true,"","", m_urlString, m_reportCache);
            async.setUser(myUser);
            async.execute();
        }
    }
}
