package org.woheller69.weather.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;


public class AppPreferencesManagerTest {


    private SharedPreferences preferences;
    private AppPreferencesManager manager;

    @Before
    public void setup() {
        preferences = Mockito.mock(SharedPreferences.class);
        manager =  new AppPreferencesManager(preferences);
    }

    @Test
    public void is_first_time_launch_return_true() {
        when(preferences.getString("API_key_value", "")).then((Answer<String>) invocation -> "");
        assertTrue(manager.isFirstTimeLaunch());
    }

    @Test
    public void is_first_time_launch_return_false() {
        when(preferences.getString("API_key_value", "")).then((Answer<String>) invocation -> "api_key_value");
        assertFalse(manager.isFirstTimeLaunch());
    }

    @Test
    public void convert_temperature_from_celsius_zero_celsius_return_zero() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(0.0f,manager.convertTemperatureFromCelsius(0.0f),1);

    }

    @Test
    public void convert_temperature_from_celsius_zero_fahrenheit_return_32() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "2");

        assertEquals(32.0f,manager.convertTemperatureFromCelsius(0.0f),1);
    }


    @Test
    public void convert_temperature_from_celsius_minus_1_fahrenheit_return_30() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "2");

        assertEquals(30.2f,manager.convertTemperatureFromCelsius(-1.0f),1);
    }

    @Test
    public void convert_temperature_from_celsius_1_fahrenheit_return_33() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "2");

        assertEquals(33.8f,manager.convertTemperatureFromCelsius(1.0f),1);
    }

    @Test
    public void convert_temperature_from_celsius_minus_1_celsius_return_minus_1() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(-1.0f,manager.convertTemperatureFromCelsius(-1.0f),1);
    }

    @Test
    public void convert_temperature_from_celsius_1_celsius_return_1() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(1.0f,manager.convertTemperatureFromCelsius(1.0f),1);
    }

    @Test
    public void convert_distance_from_kilometers_zero_kilometers_return_zero() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(0.0f,manager.convertDistanceFromKilometers(0.0f),1);

    }

    @Test
    public void convert_distance_from_kilometers_zero_miles_return_0() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(0.0f,manager.convertDistanceFromKilometers(0.0f),1);
    }


    @Test
    public void convert_distance_from_kilometers_minus_1_miles_return_zero_point_six() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "2");

        assertEquals(-0.6f,manager.convertDistanceFromKilometers(-1.0f),1);
    }

    @Test
    public void convert_distance_from_kilometers_1_mile_return_one_point_six() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "2");

        assertEquals(1.6f,manager.convertDistanceFromKilometers(1.0f),1);
    }

    @Test
    public void convert_distance_from_kilometers_minus_1_kilometer_return_minus_1() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(-1.0f,manager.convertDistanceFromKilometers(-1.0f),1);
    }

    @Test
    public void convert_distance_from_kilometer_1_kilometer_return_1() {
        when(preferences.getString("distanceUnit", "1"))
                .then((Answer<String>) invocation -> "1");

        assertEquals(1.0f,manager.convertDistanceFromKilometers(1.0f),1);
    }

    @Test
    public void is_distance_unit_miles_return_true() {
        when(preferences.getString("distanceUnit", "0"))
                .then((Answer<String>) invocation -> "2");
        assertTrue(manager.isDistanceUnitMiles());
    }


    @Test
    public void is_distance_unit_miles_return_false() {
        when(preferences.getString("distanceUnit", "0"))
                .then((Answer<String>) invocation -> "1");
        assertFalse(manager.isDistanceUnitMiles());
    }

    @Test
    public void get_temperature_unit_return_celsius() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "1");
        assertEquals("°C",manager.getTemperatureUnit());
    }

    @Test
    public void get_temperature_unit_return_fahrenheit() {
        when(preferences.getString("temperatureUnit", "1"))
                .then((Answer<String>) invocation -> "2");
        assertEquals("°F",manager.getTemperatureUnit());
    }


}