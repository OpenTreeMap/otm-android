This is an example OpenTreeMap Android project.

It contains settings customizable for each app, and references the OpenTreeMap
Android library.

You will need to add an AndroidManifest.xml, App.java and some string resources
  * Templates of these are in the `templates/` directory

* Almost all of the code and string resources are in the OpenTreeMap library project.  The only things in the OpenTreeMapSkinned project are:
  * The AndroidManifest.xml
  * An App.java file, containing a class that subclasses org.azavea.otm.App
  * HTML assets and images that are referenced in those HTML files, located in assets/
  * The URLs and and keys needed to run a version of the app, located in an XML file(s) in res/values/:
    * `accesskey`: API access key (sent with every request)
    * `secretkey`: The secret key used to sign API requests
    * `baseurl`: The URL to your OTM2 server
    * `tilerurl`: The URL to your OTM2-tiler server
    * `google_maps_api_key`: Your google maps API key
    * `platform_ver_build`: A version number of the app, which will be sent on every request
    * `url_name`: The url name of your OTM2 instance, if the app only supports one instance (optional)
