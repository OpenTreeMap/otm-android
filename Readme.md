OpenTreeMap for Android
=======================

Development Instructions
------------------------

### Android Studio Setup

* This project uses [RetroLambda](https://github.com/evant/gradle-retrolambda) to support Java-8 Lambdas on Android, which requires some setup:
  * Install JDK8 (If you are running Ubuntu, [see here](http://www.webupd8.org/2012/09/install-oracle-java-8-in-ubuntu-via-ppa.html))
  * Setup a JAVA8_HOME environment variable (On Ubuntu with the Oracle JVM this will be `/usr/lib/jvm/java-8-oracle/`)

* download Android Studio

* unzip it wherever you want

* Using the SDK Manager, install necessary software:
  * From Extras, install:
    * `Google Play services`
    * `Google Repository`
    * `Android Support Repository`
  * For all the SDK versions that you will use, (currently API 11, API 16, API [latest]), grab:
    * `SDK Platform`
    * `Google APIs` (ARM if there's a choice)
    * `System Image` (if you are using an emulator)

### Device Setup

You'll need to setup your device for debugging, because emulators aren't really viable.
Follow these instructions:
http://developer.android.com/tools/device.html

### App Setup

* clone the OpenTreeMap_Android repo to wherever you store code

* Put the appropriate values into the templates in OpenTreeMapSkinned (for more information, see the [OpenTreeMapSkinned README](OpenTreeMapSkinned/README.md))

* Setup google maps API key. See [PDF](https://github.com/OpenTreeMap/OpenTreeMap-Android/blob/9b67bd669825ac0d87f7799d5ad79695f08c95a7/howto.pdf) for suggestions.

### Weird Bugs

If your debug.keystore is not generated, you probably won't be able to run the [keytool command](https://developers.google.com/maps/documentation/android/start#obtain_a_google_maps_api_key). When you first try to run the OpenTreeMapSkinned project, Android Studio should create a debug.keystore if you do not already have one.

USDA Grant
---------------
Portions of OpenTreeMap are based upon work supported by the National Institute of Food and Agriculture, U.S. Department of Agriculture, under Agreement No. 2010-33610-20937, 2011-33610-30511, 2011-33610-30862 and 2012-33610-19997 of the Small Business Innovation Research Grants Program. Any opinions, findings, and conclusions, or recommendations expressed on the OpenTreeMap website are those of Azavea and do not necessarily reflect the view of the U.S. Department of Agriculture.
