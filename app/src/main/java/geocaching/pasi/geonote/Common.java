
/*
 * ----------------------------------------------------------------------------
 * "THE BEER-WARE LICENSE" (Revision 42):
 * salmin36@gmail.com wrote this file.  As long as you retain this notice you
 * can do whatever you want with this stuff. If we meet some day, and you think
 * this stuff is worth it, you can buy me a beer in return.   Pasi Salminen
 * ----------------------------------------------------------------------------
 */

package geocaching.pasi.geonote;

import java.util.StringTokenizer;

/**
 * Created by Pasi on 08/03/2016.
 */
public class Common{
      public enum Size{
          MICRO,
          SMALL,
          REGULAR,
          LARGE,
          OTHER
      }

    public enum Type{
        MULTI,
        MYSTERY,
        REGULAR,
        HAPPENING,
        OTHER
    }

    public enum Winter{
        AVAILABLE,
        NOT_AVAILABLE,
        NO_INFORMATION
    }

    public static class CoordinateConverter{
        //This method gives repairs the Location.convert problem of not allowing minutes with 59.xxx
        public static double convert(String coordinate) {
            // IllegalArgumentException if bad syntax
            if (coordinate == null) {
                throw new NullPointerException("coordinate");
            }

            boolean negative = false;
            if (coordinate.charAt(0) == '-') {
                coordinate = coordinate.substring(1);
                negative = true;
            }

            StringTokenizer st = new StringTokenizer(coordinate, ":");
            int tokens = st.countTokens();
            if (tokens < 1) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            }
            try {
                String degrees = st.nextToken();
                double val;
                if (tokens == 1) {
                    val = Double.parseDouble(degrees);
                    return negative ? -val : val;
                }

                String minutes = st.nextToken();
                int deg = Integer.parseInt(degrees);
                double min;
                double sec = 0.0;

                if (st.hasMoreTokens()) {
                    min = Integer.parseInt(minutes);
                    String seconds = st.nextToken();
                    sec = Double.parseDouble(seconds);
                } else {
                    min = Double.parseDouble(minutes);
                }

                boolean isNegative180 = negative && (deg == 180) &&
                        (min == 0) && (sec == 0);

                // deg must be in [0, 179] except for the case of -180 degrees
                if ((deg < 0.0) || (deg > 179 && !isNegative180)) {
                    throw new IllegalArgumentException("coordinate=" + coordinate);
                }
                if (min < 0 || min >= 60) {
                    throw new IllegalArgumentException("coordinate=" +
                            coordinate);
                }
                if (sec < 0 || sec >= 60) {
                    throw new IllegalArgumentException("coordinate=" +
                            coordinate);
                }

                val = deg*3600.0 + min*60.0 + sec;
                val /= 3600.0;
                return negative ? -val : val;
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("coordinate=" + coordinate);
            }
        }
    }

}
