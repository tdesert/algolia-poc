# algolia-poc
POC using Algolia API under Android


## Indexing cities
Use `script/fetch_cities.rb` to populate your Algolia index with a list of cities retrieved from [http://geonames.org](http://geonames.org):

	./script/fetch_cities.rb
	
You can edit the following configuration constants in the script:

Constant | Description
------------ | ------------- 
ALGOLIA_APP_ID | Application identifier retrieved from your Algolia account 
ALGOLIA_API_KEY | Algolia API key retrieved from your account
ALGOLIA_INDEX_NAME | Name of the Algolia's index that yould be populated by the script
TMP_DIR | Directory where temporary data retrieved from geonames services should be saved

## Searching Cities

The **SearchCities** android application uses the [Algolia API Client](https://github.com/algolia/algoliasearch-client-android) to browse indexed cities around the world. The results are sorted by proximity when the GPS is enabled on the device.


![Smaller icon](https://raw.githubusercontent.com/tdesert/algolia-poc/master/preview.gif)

### Prerequisities

* The project was built on Android Studio v1.3.2
* Android API 22 or higher
* If you run the project on an emulator, you should have the [Google Play Services](https://developers.google.com/android/guides/overview) installed on it
* Please check the following [guide](http://developer.android.com/guide/topics/location/strategies.html#MockData) to simulate a GPS location on your emulated device
 

### Setup

Import the project in Android Studio, then hit *Run*.

On Android M, when launched for the first time, the application will prompt you for GPS geolocation permission.<br />
Tap *Allow* to enable the geosearch features from Algolia services.

You can configure the app the same way you configured the import script by editing `app/SearchCities/app/src/main/res/values/config.xml`:

Key | Description
------------ | ------------- 
algolia_app_id | Application identifier retrieved from your Algolia account 
algolia_api_key | Algolia API key retrieved from your account
algolia_cities_index | Name of the Algolia's index to browse

<br />

