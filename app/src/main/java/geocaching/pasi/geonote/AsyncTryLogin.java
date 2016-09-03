package geocaching.pasi.geonote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Pasi on 22/03/2016.
 */
public class AsyncTryLogin extends AsyncTask<String, String, String> {
    private static final String GEO_BASE_URL = "https://www.geocaching.com/login/default.aspx";
    private HttpURLConnection m_urlConnection;
    private String m_viewState;
    private String m_viewStateGenerator;
    private String m_cookie;
    private String m_gspkauth = "";
    private String m_sessionId = "";
    private boolean m_doAnotherRound;
    private static int m_round = 0;
    private String m_urlString = "";
    private ReportCache m_reportCache = null;

    public void setParameters(String viewState, String viewStateGenerator, String cookie, boolean doAnotherRound, String sessionID, String gspkauth, String urlString, ReportCache reportCache){
        m_viewState = viewState;
        m_viewStateGenerator = viewStateGenerator;
        m_cookie = cookie;
        m_doAnotherRound = doAnotherRound;
        m_sessionId = sessionID;
        m_gspkauth = gspkauth;
        m_urlString = urlString;
        m_reportCache = reportCache;
    }

    @Override
    protected String doInBackground(String... params) {
        StringBuilder result = new StringBuilder();

        try {
            m_round++;
            switch (m_round)
            {
                case 1:
                    firstTimePost();
                    break;
                case 2:
                    secondTimeGet();
                    break;
                case 3:
                    thirdTimeGet();
                    break;
                /*case 4:
                    fourthTimeGetFinallyPage();
                    break;*/
            }

            InputStream in = new BufferedInputStream(m_urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
                //Log.v("GeoNote",line);
            }

            /*
            Map<String, List<String>> headerFields = m_urlConnection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            Log.v("GeoNote", "Cookie: ");

            m_cookie = "";
            for(String cookieHeader : cookiesHeader)
            {
                Log.v("GeoNote", cookieHeader);
                if(cookiesHeader.contains("gspkauth"))
                {
                    m_cookie = cookieHeader;
                }
            }
            Log.v("GeoNote", "End cookie");

            Log.v("GeoNote", "Here Post response header");
            m_cookie = "";
            for (String key : headerFields.keySet()){
                for (String value : headerFields.get(key)){
                    Log.v("GeoNote", key + ":" + value);
                    if(!m_cookie.contains(value))
                    {
                        m_cookie += value + "; ";
                    }

                }
            }

            if(m_cookie.charAt(m_cookie.length()-1) == ';')
            {
                m_cookie = m_cookie.substring(0,m_cookie.length()-1);
            }
            Log.v("GeoNote", "new cookies: " + m_cookie);

            Log.v("GeoNote", "Response code: " + m_urlConnection.getResponseCode());
            */

            Map<String, List<String>> headerFields = m_urlConnection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");
            Log.v("GeoNote", "Cookie: ");
            m_cookie = m_sessionId +"; ";
            for(String headerField: cookiesHeader)
            {
                if(headerField.contains("gspkauth"))
                {
                    m_gspkauth = headerField;
                    m_cookie += headerField +"; ";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(m_urlConnection != null)
                m_urlConnection.disconnect();
        }



        return result.toString();
    }

    private void fourthTimeGetFinallyPage() {
        Log.v("GeoNote","FourthTime");

    }

    private void thirdTimeGet() throws IOException {
        Log.v("GeoNote","thirdTime");
        URL url = new URL(m_urlString);
        m_cookie = m_sessionId +"; " + m_gspkauth + "; Culture=fi-FI";
        m_urlConnection = (HttpURLConnection) url.openConnection();
        m_urlConnection.setRequestMethod("GET");
        m_urlConnection.setDoOutput(true);
        m_urlConnection.setInstanceFollowRedirects(false);
        Log.v("GeoNote", "Show cookies: " + m_cookie);
        m_urlConnection.setRequestProperty("Connection", "keep-alive");
        m_urlConnection.setRequestProperty("Cookie", m_cookie);
        m_urlConnection.connect();
        m_doAnotherRound = false;
        m_round = 0;
    }

    private void secondTimeGet() throws IOException {
        URL url = new URL(GEO_BASE_URL);
        Log.v("GeoNote","secondTime");
        m_cookie = m_sessionId + "; " + "Culture=fi-FI";

        m_urlConnection = (HttpURLConnection) url.openConnection();
        m_urlConnection.setRequestMethod("GET");
        m_urlConnection.setDoOutput(true);
        m_urlConnection.setInstanceFollowRedirects( false );
        Log.v("GeoNote", "Show cookies: " + m_cookie);
        m_urlConnection.setRequestProperty("Connection", "keep-alive");
        m_urlConnection.setRequestProperty("Cookie", m_cookie );
        m_urlConnection.connect();
    }



    private void firstTimePost() throws IOException {
        Log.v("GeoNote","firsTime");
        int index1 = m_cookie.indexOf("ASP.NET_SessionId");
        if(index1 != -1)
        {
            m_cookie = m_cookie.substring(index1, m_cookie.indexOf(";",index1+1));
            m_sessionId = m_cookie;
            Log.v("GeoNote", "m_cookie == " + m_cookie);
        }

        URL url = new URL(GEO_BASE_URL);
        String postData = "__EVENTTARGET&__EVENTARGUMENT&ctl00$ContentBody$tbPassword=pasi3064&ctl00$ContentBody$tbUsername=salmin36&ctl00$ContentBody$cbRememberMe=On&ctl00$ContentBody$btnSignIn=Login";
        Log.v("GeoNote", "Here is data to be posted");
        Log.v("GeoNote", postData);

        m_urlConnection = (HttpURLConnection) url.openConnection();
        m_urlConnection.setRequestMethod("POST");
        m_urlConnection.setDoOutput(true);
        m_urlConnection.setInstanceFollowRedirects( false );
        m_cookie += "; _gali=ctl00_ContentBody_btnSignIn";
        Log.v("GeoNote", "Show cookies: " + m_cookie);
        m_urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        m_urlConnection.setRequestProperty("Cookie", m_cookie );
        m_urlConnection.connect();

        //send POST Data
        OutputStreamWriter out = new OutputStreamWriter(m_urlConnection.getOutputStream());
        out.write(postData);
        out.flush();
        out.close();
    }




    @Override
    protected void onPostExecute(String result) {
        Log.v("GeoNote", "AsyncTryLogin.onPostExcute");
        int responseCode = 0;
        //Log.v("GeoNote", result);
        if(result.indexOf("salmin36") != -1){
            Log.v("GeoNote", "Found username ");
        }
        else{
            Log.v("GeoNote", "Didn´t find username");
        }

        try
        {
            responseCode = m_urlConnection.getResponseCode();
            Log.v("GeoNote", "Response code: "+ responseCode);
        }
        catch(IOException ex){}
        if( m_doAnotherRound)
        {
            Log.v("GeoNote", "Another go, jee");
            AsyncTryLogin async = new AsyncTryLogin();
            async.setParameters(m_viewState, m_viewStateGenerator, m_cookie, m_doAnotherRound, m_sessionId, m_gspkauth, m_urlString, m_reportCache);

            async.execute();
            return;
        }



        int index1 = result.indexOf("<span id=\"uxLatLon\">");
        if(index1 != -1)
        {
            Log.v("GeoNote", "Found location tag");
            int index2 = result.indexOf("</span>", index1);
            if(index2 != -1)
            {
                String location = result.substring(index1+20, index2);
                Log.v("GeoNote", "Found location: " +  location);
                if(m_reportCache != null)
                {
                    m_reportCache.locationFound(location);
                }

            }
        }
        else
        {
            Log.v("GeoNote", "Didn´t find location");
        }


    }



    private String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {

        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
