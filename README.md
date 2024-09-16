# Development of this app has concluded!
<pre>
OpenWeatherMap has announced that access to One Call 2.5 will be discontinued in June 2024. 
While the app is compatible with One Call 3.0, the subscription requires a credit card, 
even though no payments will be charged due to the 1000 free daily calls. 
I recommend transitioning to my Cirrus app, which utilizes data from Open-Meteo, 
as I believe it offers superior information.
<a href="https://f-droid.org/de/packages/org.woheller69.omweather/"><img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75"></a>
</pre>

<pre>Send a coffee to woheller69@t-online.de 
<a href= "https://www.paypal.com/signin"><img  align="left" src="https://www.paypalobjects.com/webstatic/de_DE/i/de-pp-logo-150px.png"></a></pre>


| **RadarWeather** | **Gas Prices** | **Smart Eggtimer** |
|:---:|:---:|:---:|
| [<img src="https://github.com/woheller69/weather/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.weather/)| [<img src="https://github.com/woheller69/spritpreise/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.spritpreise/) | [<img src="https://github.com/woheller69/eggtimer/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.eggtimer/) |
| **Bubble** | **hEARtest** | **GPS Cockpit** |
| [<img src="https://github.com/woheller69/Level/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.level/) | [<img src="https://github.com/woheller69/audiometry/blob/new/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audiometry/) | [<img src="https://github.com/woheller69/gpscockpit/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.gpscockpit/) |
| **Audio Analyzer** | **LavSeeker** | **TimeLapseCam** |
| [<img src="https://github.com/woheller69/audio-analyzer-for-android/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.audio_analyzer_for_android/) |[<img src="https://github.com/woheller69/lavatories/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.lavatories/) | [<img src="https://github.com/woheller69/TimeLapseCamera/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.TimeLapseCam/) |
| **Arity** | **Cirrus** | **solXpect** |
| [<img src="https://github.com/woheller69/arity/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.arity/) | [<img src="https://github.com/woheller69/omweather/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.omweather/) | [<img src="https://github.com/woheller69/solXpect/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.solxpect/) |
| **gptAssist** | **dumpSeeker** | **huggingAssist** |
| [<img src="https://github.com/woheller69/gptassist/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.gptassist/) | [<img src="https://github.com/woheller69/dumpseeker/blob/main/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.dumpseeker/) | [<img src="https://github.com/woheller69/huggingassist/blob/master/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.hugassist/) |
| **FREE Browser** | | |
| [<img src="https://github.com/woheller69/browser/blob/newmaster/fastlane/metadata/android/en-US/images/icon.png" width="50">](https://f-droid.org/packages/org.woheller69.browser/) | | |

# RadarWeather

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="150"/>

This application is forked from Privacy Friendly Weather (https://github.com/SecUSo/privacy-friendly-weather) a privacy friendly weather app.
The original function has been modified to support the new OpenWeather One Call API, which provides a lot more features, like precipitation forecast for the next 60 minutes,
hourly forecasts for the next 2 days, 8 day week forecasts, etc. In addition a rain radar functionality powered by RainViewer API (https://www.rainviewer.com/api.html) has been added. More weather categories were added, most images and icons were replaced. RadiusSearch now also shows the results on a map with weather icons.
A chart showing forecasts with min/max temperature and precipitation for the next week has been added. In addition to the built in city list it is now also possible to use search-as-you-type location search via the photon API (https://photon.komoot.io) which is based on OpenStreetMap.

There are also three new widgets:

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/10.png" width="150"/>

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/11.png" width="150"/>

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/12.png" width="150"/>

If permission for GPS is given the widget will automatically update position on a regular base.

As One Call API only allows 1000 free calls per day an own OpenWeatherMap API key is mandatory.

You need a free subscription and **OneCallAPI 3.0** with a limit of 1000 (free) calls per day.
OneCallAPI 2.5 has been discontinued by openWeatherMap

Please register for free account at: https://home.openweathermap.org/users/sign_up

Use [Cirrus](https://github.com/woheller69/omweather) instead if you do not yet have an API key.

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75">](https://f-droid.org/de/packages/org.woheller69.weather/)


## License

This app - like the original app Privacy Friendly Weather - is licensed under the GPLv3.

The app also uses:
- The weather data service is provided by [OpenWeatherMap](https://openweathermap.org/), under <a href='http://creativecommons.org/licenses/by-sa/4.0/'>Creative Commons licence CC BY-SA 4.0</a>
- Icons from [Google Material Design Icons](https://material.io/resources/icons/) licensed under <a href='http://www.apache.org/licenses/LICENSE-2.0'>Apache License Version 2.0</a>
- Material Components for Android (https://github.com/material-components/material-components-android) which is licensed under <a href='https://github.com/material-components/material-components-android/blob/master/LICENSE'>Apache License Version 2.0</a>
- Leaflet which is licensed under the very permissive <a href='https://github.com/Leaflet/Leaflet/blob/master/FAQ.md'>2-clause BSD License</a>
- Leaflet.TileLayer.ColorFilter which is licensed under <a href='https://github.com/xtk93x/Leaflet.TileLayer.ColorFilter/blob/master/LICENSE'>MIT License</a>
- RainViewer API which is free (https://www.rainviewer.com/api.html) & RainViewer API Example (https://github.com/rainviewer/rainviewer-api-example)
- WilliamChart (com.db.chart) (https://github.com/diogobernardino/williamchart) which is licensed under <a href='http://www.apache.org/licenses/LICENSE-2.0'>Apache License Version 2.0</a>
- Android SQLiteAssetHelper (com.readystatesoftware.sqliteasset) (https://github.com/jgilfelt/android-sqlite-asset-helper) which is licensed under <a href='https://github.com/jgilfelt/android-sqlite-asset-helper/blob/master/LICENSE'>Apache License Version 2.0</a>
- Android Volley (com.android.volley) (https://github.com/google/volley) which is licensed under <a href='https://github.com/google/volley/blob/master/LICENSE'>Apache License Version 2.0</a>
- AndroidX libraries (https://github.com/androidx/androidx) which is licensed under <a href='https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt'>Apache License Version 2.0</a>
- AutoSuggestTextViewAPICall (https://github.com/Truiton/AutoSuggestTextViewAPICall) which is licensed under <a href='https://github.com/Truiton/AutoSuggestTextViewAPICall/blob/master/LICENSE'>Apache License Version 2.0</a>
- Map data from OpenStreetMap, licensed under the Open Data Commons Open Database License (ODbL) by the OpenStreetMap Foundation (OSMF) (https://www.openstreetmap.org/copyright)
- Search-as-you-type location search is provided by [photon API](https://photon.komoot.io), based on OpenStreetMap. See also (https://github.com/komoot/photon)

## Contributing

If you find a bug, please open an issue in the Github repository, assuming one does not already exist.
  - Clearly describe the issue including steps to reproduce when it is a bug. In some cases screenshots can be supportive.
  - Make sure you mention the Android version and the device you have used when you encountered the issue.
  - Make your description as precise as possible.

If you know the solution to a bug please report it in the corresponding issue and if possible modify the code and create a pull request.
