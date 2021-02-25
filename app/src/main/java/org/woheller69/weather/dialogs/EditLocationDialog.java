package org.woheller69.weather.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import android.widget.AutoCompleteTextView;
import android.widget.Toast;


import androidx.appcompat.widget.AppCompatEditText;

import androidx.fragment.app.DialogFragment;

import org.woheller69.weather.R;
import org.woheller69.weather.database.City;
import org.woheller69.weather.database.CityToWatch;
import org.woheller69.weather.database.PFASQLiteHelper;
import org.woheller69.weather.ui.util.AutoCompleteCityTextViewGenerator;
import org.woheller69.weather.ui.util.MyConsumer;


public class EditLocationDialog extends DialogFragment {

    Activity activity;
    View rootView;
    PFASQLiteHelper database;

    private AutoCompleteTextView autoCompleteTextView;
    private AutoCompleteCityTextViewGenerator cityTextViewGenerator;
    City selectedCity;
    AppCompatEditText editID;
    AppCompatEditText editName;
    AppCompatEditText editLat;
    AppCompatEditText editLon;
    AppCompatEditText editCC;

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
        View view = inflater.inflate(R.layout.dialog_edit_location, null);

        rootView = view;

        builder.setView(view);
        builder.setTitle(getActivity().getString(R.string.dialog_edit_label));

        this.database = PFASQLiteHelper.getInstance(getActivity());
        editID=rootView.findViewById((R.id.EditLocation_ID));
        editName=rootView.findViewById((R.id.EditLocationName));
        editLat=rootView.findViewById((R.id.EditLocation_Lat));
        editLon=rootView.findViewById((R.id.EditLocationLon));
        editCC=rootView.findViewById((R.id.EditLocationCounty));
        editID.setEnabled(false); //ID shall not be changed

        cityTextViewGenerator = new AutoCompleteCityTextViewGenerator(getContext(), database);
        autoCompleteTextView = (AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTvAddDialog);
        cityTextViewGenerator.generate(autoCompleteTextView, LIST_LIMIT, EditorInfo.IME_ACTION_DONE, new MyConsumer<City>() {
            @Override
            public void accept(City city) {
                if(city!=null) {
                    selectedCity = city;
                    //Hide keyboard to have more space
                    final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(rootView.getWindowToken(), 0);
                    //Fill edit fields
                    editName.append(selectedCity.getCityName());
                    editCC.append(selectedCity.getCountryCode());
                    editLat.append(Float.toString(selectedCity.getLatitude()));
                    editLon.append(Float.toString(selectedCity.getLongitude()));
                    editID.append((Integer.toString(selectedCity.getCityId())));
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                performDone();
            }
        });

        builder.setPositiveButton(getActivity().getString(R.string.dialog_edit_change_button), new DialogInterface.OnClickListener() {

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
        }else {
            selectedCity.setCityName(String.valueOf(editName.getText()));
            selectedCity.setLongitude(Float.parseFloat(String.valueOf(editLon.getText())));
            selectedCity.setLatitude(Float.parseFloat(String.valueOf(editLat.getText())));
            selectedCity.setCountryCode(String.valueOf(editCC.getText()));

            if ((Math.abs(selectedCity.getLatitude()) <= 90) && (Math.abs(selectedCity.getLongitude()) <= 180)) {
                if (database != null) {
                    database.updateCity(selectedCity);  // store changed properties of city

                    if (database.isCityWatched(selectedCity.getCityId())) {   // if city is watched, delete it. Can be added again with AddLocation Dialog
                        CityToWatch c = database.getCityToWatch(selectedCity.getCityId());
                        database.deleteCityToWatch(c);
                    }
                }
            } else {
                Toast.makeText(activity,R.string.edit_location_range_error,Toast.LENGTH_LONG).show();
            }
        }
        dismiss();
    }
}
