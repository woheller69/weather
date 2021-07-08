package org.woheller69.weather.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.ForecastCityActivity;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.services.UpdateDataService;
import org.woheller69.weather.ui.RecycleList.CityWeatherAdapter;
import org.woheller69.weather.ui.RecycleList.OnSwipeDownListener;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;

import java.util.List;

import static androidx.core.app.JobIntentService.enqueueWork;
import static org.woheller69.weather.services.UpdateDataService.SKIP_UPDATE_INTERVAL;

public class WeatherCityFragment extends Fragment implements IUpdateableCityUI {

    private int mCityId = -1;
    private int[] mDataSetTypes = new int[]{};

    private CityWeatherAdapter mAdapter;

    private RecyclerView recyclerView;

    public static WeatherCityFragment newInstance(Bundle args)
    {
        WeatherCityFragment weatherCityFragment = new WeatherCityFragment();
        weatherCityFragment.setArguments(args);
        return weatherCityFragment;
    }

    public void setAdapter(CityWeatherAdapter adapter) {
        mAdapter = adapter;

        if (recyclerView != null) {
            recyclerView.setAdapter(mAdapter);
            recyclerView.setFocusable(false);
            recyclerView.setLayoutManager(getLayoutManager(getContext()));  //fixes problems with StaggeredGrid: After refreshing data only empty space shown below tab
        }
    }

    public void loadData() {
                CurrentWeatherData currentWeatherData = PFASQLiteHelper.getInstance(getContext()).getCurrentWeatherByCityId(mCityId);

                if (currentWeatherData.getCity_id() == 0) {
                    currentWeatherData.setCity_id(mCityId);
                }

                mAdapter = new CityWeatherAdapter(currentWeatherData, mDataSetTypes, getContext());
                setAdapter(mAdapter);
            }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        ViewUpdater.addSubscriber(this);
    }

    @Override
    public void onDetach() {
        ViewUpdater.removeSubscriber(this);

        super.onDetach();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_weather_forecast_city_overview, container, false);

        recyclerView = v.findViewById(R.id.weatherForecastRecyclerView);
        recyclerView.setLayoutManager(getLayoutManager(getContext()));
        recyclerView.setOnTouchListener(new OnSwipeDownListener(getContext()) {
            public void onSwipeDown() {
                if (v.getScrollY()==0) { //Reload on swipeDown if scrolled to top
                    WeatherPagerAdapter.refreshSingleData(getContext(),true,mCityId);
                    ForecastCityActivity.startRefreshAnimation();
                }
            }
        });

        Bundle args = getArguments();
        mCityId = args.getInt("city_id");
        mDataSetTypes = args.getIntArray("dataSetTypes");

        loadData();

        return v;
    }

    public RecyclerView.LayoutManager getLayoutManager(Context context) {
        int widthPixels = context.getResources().getDisplayMetrics().widthPixels;
        float density = context.getResources().getDisplayMetrics().density;
        float width = widthPixels / density;

        if (width > 500) {
            return new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        } else {
            return new LinearLayoutManager(context);
        }
    }

    @Override
    public void processNewCurrentWeatherData(CurrentWeatherData data) {
        if (data != null && data.getCity_id() == mCityId) {
            setAdapter(new CityWeatherAdapter(data, mDataSetTypes, getContext()));
        }
    }

    @Override
    public void processNewForecasts(List<Forecast> forecasts) {
        if (forecasts != null && forecasts.size() > 0 && forecasts.get(0).getCity_id() == mCityId) {
            if (mAdapter != null) {
                mAdapter.updateForecastData(forecasts);
            }
        }
    }

    @Override
    public void processNewWeekForecasts(List<WeekForecast> forecasts) {
        if (forecasts != null && forecasts.size() > 0 && forecasts.get(0).getCity_id() == mCityId) {
            if (mAdapter != null) {
                mAdapter.updateWeekForecastData(forecasts);
            }
        }
    }
}
