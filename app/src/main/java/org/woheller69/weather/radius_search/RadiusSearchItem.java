package org.woheller69.weather.radius_search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Instances of this class represent locations that are to be used for evaluating the result of
 * radius searches.
 */
public class RadiusSearchItem implements Parcelable {

    /**
     * Member variables
     */
    private String cityName;
    private int weatherCategory;
    private float temperature;
    private float lat;
    private float lon;


    /**
     * Constructor.
     *
     * @param cityName        The name of the city / location.
     * @param temperature     The current temperature of the location.
     * @param weatherCategory The current weather category of the location.s
     */
    public RadiusSearchItem(String cityName, float temperature, int weatherCategory,float lat, float lon) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.weatherCategory = weatherCategory;
        this.lat=lat;
        this.lon=lon;

    }

    protected RadiusSearchItem(Parcel in) {
        cityName = in.readString();
        weatherCategory = in.readInt();
        temperature = in.readFloat();
        lat=in.readFloat();
        lon=in.readFloat();


    }

    /**
     * @return Returns the name of the city.
     */
    public String getCityName() {
        return cityName;
    }

    /**
     * @param cityName The name of the city to set,
     */
    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    /**
     * @return Returns the current temperature of the city.
     */
    public double getTemperature() {
        return temperature;
    }

    /**
     * @param temperature The current temperature of the city to set.
     */
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    /**
     * @return Returns the current weather category (numerical representation of 'rain', 'snow' etc;
     * see IApiToDatabaseConversion#WeatherCategories for details) of the city.
     */
    public int getWeatherCategory() {
        return weatherCategory;
    }

    /**
     * @param weatherCategory The numerical weather category of the city (see
     *                        IApiToDatabaseConversion#WeatherCategories for details).
     */
    public void setWeatherCategory(int weatherCategory) {
        this.weatherCategory = weatherCategory;
    }

    /**
     * @return Returns the latitude of the city.
     */
    public double getLat() {
        return lat;
    }

    /**
     * @return Returns the longitude of the city.
     */
    public double getLon() {
        return lon;
    }


    /**
     * @see Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * @see Parcelable#writeToParcel(Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cityName);
        dest.writeInt(weatherCategory);
        dest.writeFloat(temperature);
        dest.writeFloat(lat);
        dest.writeFloat(lon);
    }

    /**
     * This field is needed for Android to be able to create new objects, individually or as arrays.
     * This also means that you can use use the default constructor to create the object and use
     * another method to hydrate it as necessary.
     * (This has been taken
     * from http://stackoverflow.com/questions/6743084/how-to-pass-an-object-to-another-activity
     * as of 2016-08-04)
     */
    @SuppressWarnings("unchecked")
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        @Override
        public RadiusSearchItem createFromParcel(Parcel in) {
            return new RadiusSearchItem(in);
        }

        @Override
        public Object[] newArray(int size) {
            return new RadiusSearchItem[size];
        }
    };

}
