package org.woheller69.weather.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import org.woheller69.weather.R;
import org.woheller69.weather.activities.ManageLocationsActivity;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.ui.util.AutoCompleteCityTextViewGenerator;
import org.woheller69.weather.ui.util.MyConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yonjuni on 04.01.17.
 */

public class AddLocationDialog extends DialogFragment {

    Activity activity;
    View rootView;
    PFASQLiteHelper database;

    private AutoCompleteTextView autoCompleteTextView;
    private AutoCompleteCityTextViewGenerator cityTextViewGenerator;
    City selectedCity;

    final int LIST_LIMIT = 100;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       if (context instanceof Activity){
            this.activity=(Activity) context;
        }
    }


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = inflater.inflate(R.layout.dialog_add_location, null);

        rootView = view;

        builder.setView(view);
        builder.setTitle(getActivity().getString(R.string.dialog_add_label));

        this.database = PFASQLiteHelper.getInstance(getActivity());
        final WebView webview= rootView.findViewById(R.id.webViewAddLocation);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setBackgroundColor(0x00000000);
        webview.setBackgroundResource(R.drawable.map_back);
        cityTextViewGenerator = new AutoCompleteCityTextViewGenerator(getContext(), database);
        autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTvAddDialog);
        cityTextViewGenerator.generate(autoCompleteTextView, LIST_LIMIT, EditorInfo.IME_ACTION_DONE, new MyConsumer<City>() {
            @Override
            public void accept(City city) {
                selectedCity = city;
                if(selectedCity!=null) {
                    //Hide keyboard to have more space
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                    //Show city on map
                    webview.loadUrl("file:///android_asset/map.html?lat=" + selectedCity.getLatitude() + "&lon=" + selectedCity.getLongitude());
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                performDone();
            }
        });

        builder.setPositiveButton(getActivity().getString(R.string.dialog_add_add_button), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                performDone();
            }
        });

        builder.setNegativeButton(getActivity().getString(R.string.dialog_add_close_button), null);

        return builder.create();
    }

    private void performDone() {
        if (selectedCity == null) {
            Toast.makeText(activity, R.string.dialog_add_no_city_found, Toast.LENGTH_SHORT).show();
            return;
        }
  //      if (database != null && !database.isCityWatched(selectedCity.getCityId())) {
        if (database != null ) {
            List<CityToWatch> citytowatch;
            citytowatch = database.getAllCitiesToWatch();
            boolean duplicate=false;
            for (CityToWatch C : citytowatch){ // Do not add city if latitude and longitude are very close to a city already watched. Otherwise there may be problems in ProcessOwmForecastOneCallAPIRequest
                if ((Math.abs(C.getLatitude() - selectedCity.getLatitude())<=0.01) && (Math.abs(C.getLongitude() - selectedCity.getLongitude())<=0.01)) {
                    duplicate = true;
                    break;
                }
            }
            if (!duplicate) {
                addCity();
                ((ManageLocationsActivity) activity).addCityToList(convertCityToWatched());
            }
            else Toast.makeText(activity, R.string.error_dialog_add_city_too_close, Toast.LENGTH_SHORT).show();
        }
        dismiss();
    }

    private CityToWatch convertCityToWatched() {


        return new CityToWatch(
                database.getMaxRank() + 1,
                selectedCity.getCountryCode(),
                -1,
                selectedCity.getCityId(), selectedCity.getLongitude(),selectedCity.getLatitude(),
                selectedCity.getCityName()
        );
    }

    public void addCity() {
        new AsyncTask<CityToWatch, Void, Void>() {
            @Override
            protected Void doInBackground(CityToWatch... params) {
                database.addCityToWatch(params[0]);
                return null;
            }
        }.doInBackground(convertCityToWatched());
    }
}
