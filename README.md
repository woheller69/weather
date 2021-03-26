# RadarWeather

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.png" width="150"/> <img src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.png" width="150"/>

This application is forked from Privacy Friendly Weather (https://github.com/SecUSo/privacy-friendly-weather) a privacy friendly weather app.
The original function has been modified to support the new OpenWeather One Call API, which provides a lot more features, like precipitation forecast for the next 60 minutes,
hourly forecasts for the next 2 days, 8 day week forecasts, etc. In addition a rain radar functionality powered by RainViewer API (https://www.rainviewer.com/api.html) has been added. More weather categories were added, most images and icons were replaced. RadiusSearch now also shows the results on a map with weather icons.
A chart showing forecasts with min/max temperature and precipitation for the next week has been added. In addition to the built in city list it is now also possible to use search-as-you-type location search via the photon API (https://photon.komoot.io) which is based on OpenStreetMap.

There is also a new widget:

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/10.png" width="150"/>

As One Call API only allows 1000 calls per day an own OpenWeatherMap API key is mandatory.

Please register for free account at: https://home.openweathermap.org/users/sign_up

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" height="75">](https://f-droid.org/de/packages/org.woheller69.weather/)


## License

This app - like the original app Privacy Friendly Weather - is licensed under the GPLv3.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.


The app also uses:
- The weather data service is provided by [OpenWeatherMap](https://openweathermap.org/), under <a href='http://creativecommons.org/licenses/by-sa/4.0/'>Creative Commons licence CC BY-SA 4.0</a>.
- Icons from [Google Material Design Icons](https://material.io/resources/icons/) licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
- Leaflet which is licensed under the very permissive 2-clause BSD License. (https://github.com/Leaflet/Leaflet/blob/master/FAQ.md)
- Leaflet.TileLayer.ColorFilter which is licensed under MIT License. (https://github.com/xtk93x/Leaflet.TileLayer.ColorFilter/blob/master/LICENSE)
- RainViewer API which is free (https://www.rainviewer.com/api.html) & RainViewer API Example (https://github.com/rainviewer/rainviewer-api-example)
- WilliamChart (com.db.chart) (https://github.com/diogobernardino/williamchart) which is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
- Android SQLiteAssetHelper (com.readystatesoftware.sqliteasset) which is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
- AutoSuggestTextViewAPICall (https://github.com/Truiton/AutoSuggestTextViewAPICall) which is licensed under Apache License Version 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
- Map data from OpenStreetMap, licensed under the Open Data Commons Open Database License (ODbL) by the OpenStreetMap Foundation (OSMF) (https://www.openstreetmap.org/copyright)
- Search-as-you-type location search is provided by [photon API](https://photon.komoot.io), based on OpenStreetMap. See also (https://github.com/komoot/photon)

## Contributing

If you find a bug, please open an issue in the Github repository, assuming one does not already exist.
  - Clearly describe the issue including steps to reproduce when it is a bug. In some cases screenshots can be supportive.
  - Make sure you mention the Android version and the device you have used when you encountered the issue.
  - Make your description as precise as possible.

If you know the solution to a bug please report it in the corresponding issue and if possible modify the code and create a pull request.