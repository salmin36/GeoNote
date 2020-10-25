/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */


package geocaching.pasi.geonote;

import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;

/**
 * Created by Pasi on 20/11/2016.
 */

public  class CacheInfoParser {

    public static final String PREMIUM_STR = "<section class=\"pmo-banner\">";

    public static Cache parseInfomation(String source) {
        Cache cache = new Cache();
        if(source.length() == 0){return cache;}

        //Lets find if we have premium or non premium cache
        if(source.indexOf(PREMIUM_STR) != -1){
            parseAsPremium(source, cache);
        }
        else{parseAsNonPremium(source, cache);}

        return cache;
    }

    private static void parseAsNonPremium(String source, Cache cache) {
        //Find type
        int ind1 = source.indexOf("cacheImage");
        if(ind1 == -1){return;}
        ind1 = source.indexOf("WptTypes/");
        if(ind1 == -1){return;}
        String str = source.substring(ind1 + 9,ind1 + 10);
        Log.v("GeoNote", "Cache type number is: " + str);
        cache.setType(Integer.parseInt(str));

        //First find cache name
        ind1 = source.indexOf("CacheName");
        if(ind1 == -1){return;}
        int ind2 = source.indexOf("<",ind1 + 11);
        if(ind2 == -1){return;}
        ind1 += 11;
        Log.v("GeoNote", "CacheName == " + source.substring(ind1,ind2));
        //Set name
        cache.setName(source.substring(ind1, ind2));

        //Then find difficulty
        ind1 = source.indexOf("ContentBody_diffTerr",ind2);
        if(ind1 == -1){return;}
        ind1 = source.indexOf("alt=", ind1);
        if(ind1 == -1){return;}
        ind1 += 5;
        ind2 = source.indexOf("out", ind1);
        ind2 -= 1;
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Difficulty == " + source.substring(ind1, ind2));
        Double difficulty = Double.valueOf(source.substring(ind1, ind2));
        cache.setDifficulty(difficulty);

        //Then find Terrain
        ind1 = source.indexOf("alt=",ind2);
        ind1 += 5;
        ind2 = source.indexOf("out", ind1);
        ind2 -= 1;
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Terrain == " + source.substring(ind1, ind2));

        Double terrain = Double.valueOf(source.substring(ind1,ind2));
        Log.v("GeoNote", "Terrain == " + terrain);
        cache.setTerrain(terrain);

        //Then find size
        ind1 = source.indexOf("alt=",ind2);
        ind1 += 11;
        ind2 = source.indexOf("\"", ind1);
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Size == " + source.substring(ind1,ind2));
        String size = source.substring(ind1,ind2).toLowerCase();
        cache.setSize(size);

        //Find if available in winter
        ind1 = source.indexOf("available in winter");
        ind2 = source.indexOf("not available for winter");
        if(ind1 != -1 ){
            Log.v("GeoNote", "Available in winter");
            cache.setWinter("yes");
        }
        else if(ind2 !=  -1 ){
            Log.v("GeoNote", "Not available in winter");
            cache.setWinter("no");
        }
        else{
            cache.setWinter("");
            Log.v("GeoNote", "Information not available");
        }

        //Find hint if available
        String hint = getHintFromString(source);
        cache.setNote(hint);
    }

    private static void parseAsPremium(String source, Cache cache) {
        //Find type
        Log.v("GeoNote", source);
        int ind1 = source.indexOf("/play/Content/images/cache-types/");
        if(ind1 == -1){return;}
        String str = source.substring(ind1 + 33,ind1 + 34);
        Log.v("GeoNote", "Cache type number is: " + str);
        cache.setType(Integer.valueOf(str));

        //First find cache name
        ind1 = source.indexOf("<h1 class=\"heading-3\">");
        if(ind1 == -1){return;}
        int ind2 = source.indexOf("<",ind1 + 22);
        if(ind2 == -1){return;}
        ind1 += 22;
        Log.v("GeoNote", "CacheName == " + source.substring(ind1,ind2));
        //Set name
        cache.setName(source.substring(ind1,ind2));

        //Then find difficulty
        ind1 = source.indexOf("ctl00_ContentBody_lblDifficulty");
        if(ind1 == -1){return;}
        ind1 = source.indexOf("<span>",ind1);
        if(ind1 == -1){return;}
        ind1 = source.indexOf(">", ind1);
        if(ind1 == -1){return;}
        ind1 += 1;
        ind2 = source.indexOf("<", ind1);
        //ind2 -= 1;
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Difficulty == " + source.substring(ind1, ind2));
        Double difficulty = Double.valueOf(source.substring(ind1, ind2));
        cache.setDifficulty(difficulty);


        //Then find Terrain
        ind1 = source.indexOf("ctl00_ContentBody_lblTerrain");
        ind1 = source.indexOf("<span>", ind1);
        ind1 += 6;
        ind2 = source.indexOf("<", ind1);
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Terrain == " + source.substring(ind1, ind2));

        Double terrain = Double.valueOf(source.substring(ind1,ind2));
        Log.v("GeoNote", "Terrain == " + terrain);
        cache.setTerrain(terrain);

        //Then find size
        ind1 = source.indexOf("ctl00_ContentBody_lblSize");
        ind1 = source.indexOf("<span>",ind1);
        ind1 += 6;
        ind2 = source.indexOf("<", ind1);
        if(ind2 == -1 || ind2 <= ind1){return;}
        Log.v("GeoNote", "Size == " + source.substring(ind1,ind2));

        String size = source.substring(ind1,ind2).toLowerCase();
        cache.setSize(size);
    }


    //Function finds the hint from string and decrypts it and returns it
    private static String getHintFromString(String str){
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
