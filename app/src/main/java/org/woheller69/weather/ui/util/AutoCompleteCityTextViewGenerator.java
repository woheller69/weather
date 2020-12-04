package org.woheller69.weather.ui.util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.woheller69.weather.database.City;
import org.woheller69.weather.database.PFASQLiteHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * This class provides an AutoCompleteTextView which shows a drop down list with cities that match
 * the input string.
 */
public class AutoCompleteCityTextViewGenerator {

    /**
     * Member variables
     */
    private Context context;
    //private DatabaseHelper dbHelper;
    private PFASQLiteHelper dbHelper;
    private ArrayAdapter<City> cityAdapter;
    private Runnable selectAction;
    private AutoCompleteTextView editField;
    private MyConsumer<City> cityConsumer;
    private int listLimit;
    private City selectedCity;

    /**
     * Constructor.
     *
     * @param context  The context in which the AutoCompleteTextView is to be used.
     * @param dbHelper An instance of a DatabaseHelper. This object is used to make the database
     *                 queries.
     */
    public AutoCompleteCityTextViewGenerator(Context context, PFASQLiteHelper dbHelper) {
        this.context = context;
        this.dbHelper = dbHelper;
    }

    /**
     * @param editField The component to "transform" into one that shows a city drop down list
     *                  based on the current input. Make sure to pass an initialized object,
     *                  else a java.lang.NullPointerException will be thrown.
     * @param listLimit Determines how many items shall be shown in the drop down list at most.
     */
    public void generate(AutoCompleteTextView editField, int listLimit, final int enterActionId, final MyConsumer<City> cityConsumer, final Runnable selectAction) {
        cityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new ArrayList<City>());
        this.editField = editField;
        this.cityConsumer = cityConsumer;
        this.listLimit = listLimit;
        editField.setAdapter(cityAdapter);
        editField.addTextChangedListener(new TextChangeListener());

        editField.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = (City) parent.getItemAtPosition(position);
                cityConsumer.accept(selectedCity);
            }
        });

        editField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == enterActionId) {
                    Boolean checkCity = checkCity();
                    if (checkCity) {
                        selectAction.run();
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private boolean checkCity() {
        if (selectedCity == null) {
            String current = editField.getText().toString();
            if (current.length() > 2) {
                List<City> cities = dbHelper.getCitiesWhereNameLike(current, listLimit);
                if (cities.size() == 1) {
                    selectedCity = cities.get(0);
                    cityConsumer.accept(selectedCity);
                    return true;
                }
            }

            Toast.makeText(context, "NO City selected", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * The following listener implementation provides the functionality / logic for the lookahead
     * dropdown.
     */
    private class TextChangeListener implements TextWatcher {
        private TextChangeListener() {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            getCityFromText(false);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }

    public void getCityFromText(Boolean selectWhenUnique) {
        selectedCity = null;
        cityConsumer.accept(null);
        if (dbHelper != null) {
            //List<City> allCities = dbHelper.getAllCities();

            String content = editField.getText().toString();
            if (content.length() > 2) {
                // Get the matched cities
                //List<City> cities = dbHelper.getCitiesWhereNameLike(content, allCities, dropdownListLimit);
                List<City> cities = dbHelper.getCitiesWhereNameLike(content, listLimit);
                // Set the drop down entries

                if (selectWhenUnique && cities.size() == 1) {
                    selectedCity = cities.get(0);
                    cityConsumer.accept(selectedCity);
                } else {
                    cityAdapter.clear();
                    cityAdapter.addAll(cities);
                    editField.showDropDown();
                }
            } else {
                editField.dismissDropDown();
            }
        }
    }

}
