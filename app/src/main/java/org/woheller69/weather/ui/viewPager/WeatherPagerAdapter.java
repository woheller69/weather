package org.woheller69.weather.ui.viewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import org.woheller69.weather.R;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.WeatherCityFragment;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.CHART;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.DAY;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.DETAILS;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.ERROR;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.OVERVIEW;
import static org.woheller69.weather.ui.RecycleList.CityWeatherAdapter.WEEK;

/**
 * Created by thomagglaser on 07.08.2017.
 */

public class WeatherPagerAdapter extends FragmentStatePagerAdapter implements IUpdateableCityUI {

    private Context mContext;

    private PFASQLiteHelper database;
    long lastUpdateTime;

    private List<CityToWatch> cities;
    private List<CurrentWeatherData> currentWeathers;

    private static int[] mDataSetTypes = {OVERVIEW, DETAILS, DAY, WEEK, CHART}; //TODO Make dynamic from Settings
    private static int[] errorDataSetTypes = {ERROR};

    public WeatherPagerAdapter(Context context, FragmentManager supportFragmentManager) {
        super(supportFragmentManager,FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;
        this.database = PFASQLiteHelper.getInstance(context);
        this.currentWeathers = database.getAllCurrentWeathers();
        this.cities = database.getAllCitiesToWatch();
        try {
            cities = database.getAllCitiesToWatch();
            Collections.sort(cities, new Comparator<CityToWatch>() {
                @Override
                public int compare(CityToWatch o1, CityToWatch o2) {
                    return o1.getRank() - o2.getRank();
                }

            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public WeatherCityFragment getItem(int position) {
        Bundle args = new Bundle();
        args.putInt("city_id", cities.get(position).getCityId());
        args.putIntArray("dataSetTypes", mDataSetTypes);

        return WeatherCityFragment.newInstance(args);
    }

    @Override
    public int getCount() {
        return cities.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (cities.size() == 0) {
            return mContext.getString(R.string.app_name);
        }
        return cities.get(position).getCityName();
    }

    public static void refreshSingleData(Context context, Boolean asap, int cityId) {
        Intent intent = new Intent(context, UpdateDataService.class);
        intent.setAction(UpdateDataService.UPDATE_SINGLE_ACTION);
        intent.putExtra(SKIP_UPDATE_INTERVAL, asap);
        intent.putExtra("cityId",cityId);
        enqueueWork(context, UpdateDataService.class, 0, intent);
    }

    private CurrentWeatherData findWeatherFromID(List<CurrentWeatherData> currentWeathers, int ID) {
        for (CurrentWeatherData weather : currentWeathers) {
            if (weather.getCity_id() == ID) return weather;
        }
        return null;
    }

    @Override
    public void processNewCurrentWeatherData(CurrentWeatherData data) {
        lastUpdateTime = data.getTimestamp();
        int id = data.getCity_id();
        CurrentWeatherData old = findWeatherFromID(currentWeathers, id);
        if (old != null) currentWeathers.remove(old);
        currentWeathers.add(data);
        notifyDataSetChanged();
    }

    @Override
    public void processNewForecasts(List<Forecast> forecasts) {
        //empty because Fragments are subscribers themselves
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        //empty because Fragments are subscribers themselves
    }

    public int getCityIDForPos(int pos) {
            CityToWatch city = cities.get(pos);
                 return city.getCityId();
    }

    public int getPosForCityID(int cityID) {
        for (int i = 0; i < cities.size(); i++) {
            CityToWatch city = cities.get(i);
            if (city.getCityId() == cityID) {
                return i;
            }
        }
        return 0;
    }

    public float getLatForPos(int pos) {
        CityToWatch city = cities.get(pos);
        return city.getLatitude();
    }

    public float getLonForPos(int pos) {
        CityToWatch city = cities.get(pos);
        return city.getLongitude();
    }


}