package org.woheller69.weather.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.ForecastCityActivity;
import org.woheller69.weather.database.CurrentWeatherData;
import org.woheller69.weather.database.Forecast;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.database.WeekForecast;
import org.woheller69.weather.ui.RecycleList.CityWeatherAdapter;
import org.woheller69.weather.ui.RecycleList.OnSwipeDownListener;
import org.woheller69.weather.ui.updater.IUpdateableCityUI;
import org.woheller69.weather.ui.updater.ViewUpdater;
import org.woheller69.weather.ui.viewPager.WeatherPagerAdapter;

import java.util.List;

public class WeatherCityFragment extends Fragment implements IUpdateableCityUI {
    private static final int MINGRIDWIDTH = 500;
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
                int widthPixels = getContext().getResources().getDisplayMetrics().widthPixels;
                float density = getContext().getResources().getDisplayMetrics().density;
                float width = widthPixels / density;
                int firstVisiblePosition;
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (width <=MINGRIDWIDTH){  //view has LinearLayoutManager -> find first visible position
                    LinearLayoutManager llm = (LinearLayoutManager) manager;
                    assert llm != null;
                    firstVisiblePosition = llm.findFirstVisibleItemPosition();
                } else {
                    StaggeredGridLayoutManager glm = (StaggeredGridLayoutManager) manager;
                    assert glm != null;
                    int[] into = new int[2]; //span count 2
                    glm.findFirstVisibleItemPositions(into);
                    firstVisiblePosition =into[0];
                }
                if (firstVisiblePosition ==0) { //Reload on swipeDown if scrolled to top
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

        if (width > MINGRIDWIDTH) {
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
