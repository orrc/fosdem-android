# FOSDEM Android app

This is a native Android app for using the FOSDEM schedule offline. It is available in the [Google Play Store](https://play.google.com/store/apps/details?id=org.fosdem).

## Features

* View the different sessions by day and tracks
* Room plans
* Favorites and notifications
* Search
* Share a session with your social network

## License

[GPL](http://www.gnu.org/licenses/gpl.html)

## Development

* This application uses ActionBarSherlock to provide the action bar for older Android versions. You need to [include it](http://actionbarsherlock.com/usage.html) to build the application. The official documentation says:

> If you’re using the Eclipse Development Environment with the ADT plugin version 0.9.7 or greater you can include ActionBarSherlock as a library project. Create a new Android project in Eclipse using the library/ folder as the existing source. Then, in your project properties, add the created project under the ‘Libraries’ section of the ‘Android’ category.

* Another dependency is the library [StickyListHeaders](https://github.com/emilsjolander/StickyListHeaders) - you need to include it the same way: download/clone it and add it as an Eclipse project (the /library folder as existing source) and add it as library to the FOSDEM app project.

* The project needs the android-support-v4.jar to build. If your Eclipse doesn't find it on its own, you have to specify its path (it resides in your Android SDK folder as extras/android/support/v4/android-support-v4.jar) in the project settings > Java Build Path > Libraries (via "Add External JARs").

## To-Do

* Implementation with Fragments and Tablet UI

## Changelog

### 2.0.0

* Completely revised Holo-ified UI and new FOSDEM logo
* Fixed to work with new HTTPS URL
* Added fallback room images from 2012 because the room image download is currently not available.

### 1.0.3

* Minor updates and points towards 2011 edition

### 1.0.2

* Issue with notifications/background service solved. Thx to Christopher Orr.

## Contributors

### Original creators

* Michaël Uyttersprot
* Pieter Iserbyt
* Christophe Vandeplas

### Contributors

* Christopher Orr
* Raphael Kallensee

## Links

* [Official website](http://sourceforge.net/projects/fosdem-android/)
* [App in the Google Play store](https://play.google.com/store/apps/details?id=org.fosdem)
* [Announcement blog post](http://labs.emich.be/2010/01/29/fosdem-android-application/)
