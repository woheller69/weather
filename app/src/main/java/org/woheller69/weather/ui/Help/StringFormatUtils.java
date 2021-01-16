package org.woheller69.weather.ui.Help;

import android.content.Context;
import android.preference.PreferenceManager;

import org.woheller69.weather.R;
import org.woheller69.weather.preferences.AppPreferencesManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public final class StringFormatUtils {

    private static DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private static DecimalFormat intFormat = new DecimalFormat("0");

    public static String formatDecimal(float decimal) {
        return decimalFormat.format(decimal);
    }

    public static String formatInt(float decimal) {
        return intFormat.format(decimal);
    }

    public static String formatInt(float decimal, String appendix) {
        return String.format("%s\u200a%s", formatInt(decimal), appendix); //\u200a adds tiny space
    }

    public static String formatDecimal(float decimal, String appendix) {
        return String.format("%s\u200a%s", formatDecimal(decimal), appendix);
    }

    public static String formatTemperature(Context context, float temperature) {
        AppPreferencesManager prefManager = new AppPreferencesManager(PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()));
        return formatDecimal(prefManager.convertTemperatureFromCelsius(temperature), prefManager.getWeatherUnit());
    }

    public static String formatTimeWithoutZone(long time) {
        SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(time);
    }

    public static String formatWindSpeed(Context context, float wind_speed) {
        if (wind_speed < 0.3) {
            return formatInt(0, context.getString(R.string.units_Bft)); // Calm
        } else if (wind_speed < 1.5) {
            return formatInt(1, context.getString(R.string.units_Bft)); // Light air
        } else if (wind_speed < 3.3) {
            return formatInt(2, context.getString(R.string.units_Bft)); // Light breeze
        } else if (wind_speed < 5.5) {
            return formatInt(3, context.getString(R.string.units_Bft)); // Gentle breeze
        } else if (wind_speed < 7.9) {
            return formatInt(4, context.getString(R.string.units_Bft)); // Moderate breeze
        } else if (wind_speed < 10.7) {
            return formatInt(5, context.getString(R.string.units_Bft)); // Fresh breeze
        } else if (wind_speed < 13.8) {
            return formatInt(6, context.getString(R.string.units_Bft)); // Strong breeze
        } else if (wind_speed < 17.1) {
            return formatInt(7, context.getString(R.string.units_Bft)); // High wind
        } else if (wind_speed < 20.7) {
            return formatInt(8, context.getString(R.string.units_Bft)); // Gale
        } else if (wind_speed < 24.4) {
            return formatInt(9, context.getString(R.string.units_Bft)); // Strong gale
        } else if (wind_speed < 28.4) {
            return formatInt(10, context.getString(R.string.units_Bft)); // Storm
        } else if (wind_speed < 32.6) {
            return formatInt(11, context.getString(R.string.units_Bft)); // Violent storm
        } else {
            return formatInt(12, context.getString(R.string.units_Bft)); // Hurricane
        }
    }


    public static String formatWindDir(Context context, float wind_direction) {
        if (wind_direction < 22.5) {
            return Character.toString((char) 0x2193); // North
        } else if (wind_direction < 67.5) {
            return Character.toString((char) 0x2199); // North East
        } else if (wind_direction < 112.5) {
            return Character.toString((char) 0x2190); // East
        } else if (wind_direction < 157.5) {
            return Character.toString((char) 0x2196); // South East
        } else if (wind_direction < 202.5) {
            return Character.toString((char) 0x2191); // South
        } else if (wind_direction < 247.5) {
            return Character.toString((char) 0x2197); // South West
        } else if (wind_direction < 292.5) {
            return Character.toString((char) 0x2192); // West
        } else if (wind_direction < 337.5) {
            return Character.toString((char) 0x2198); // North West
        } else {
            return Character.toString((char) 0x2193); // North
        }
    }

    public static Integer getDayShort(int day){

        switch(day)    {
            case Calendar.MONDAY:
                day = R.string.abbreviation_monday;
                break;
            case Calendar.TUESDAY:
                day = R.string.abbreviation_tuesday;
                break;
            case Calendar.WEDNESDAY:
                day = R.string.abbreviation_wednesday;
                break;
            case Calendar.THURSDAY:
                day = R.string.abbreviation_thursday;
                break;
            case Calendar.FRIDAY:
                day = R.string.abbreviation_friday;
                break;
            case Calendar.SATURDAY:
                day = R.string.abbreviation_saturday;
                break;
            case Calendar.SUNDAY:
                day = R.string.abbreviation_sunday;
                break;
            default:
                day = R.string.abbreviation_monday;
        }
        return day;
    }

    public static Integer getDayLong(int day){

        switch(day)    {
            case Calendar.MONDAY:
                day = R.string.monday;
                break;
            case Calendar.TUESDAY:
                day = R.string.tuesday;
                break;
            case Calendar.WEDNESDAY:
                day = R.string.wednesday;
                break;
            case Calendar.THURSDAY:
                day = R.string.thursday;
                break;
            case Calendar.FRIDAY:
                day = R.string.friday;
                break;
            case Calendar.SATURDAY:
                day = R.string.saturday;
                break;
            case Calendar.SUNDAY:
                day = R.string.sunday;
                break;
            default:
                day = R.string.monday;
        }
        return day;
    }
}
