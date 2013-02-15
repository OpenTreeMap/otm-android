Creating a new OTM Android app:
    
    Android apps reference the OTM library and override drawables, strings, defaults and config.    

    STEPS
    =====    
    + (Eclipse) Copy an existing android theme.
        A new android theme:
            + imports the OTM library
            + contains a near duplicate of the base libraries Manifest, 
                    + all activities point to the base library 
                    + only the current package name is customized.  
                    + Don't duplicate the permissions

    + (Eclipse) Edit the project name in src/App.js to reflect your new project.
        = IE rename from org.azavea.phillytreemap
            to
           org.azavea.urbanforestmap

    + (Eclipse) Edit the project name in the android manifest.
    
    + (File system, refresh eclipse) Swap in new drawables for 9 patch buttons and app icons.
        You can override any drawable that exists in the base package
    + Redefine the colors in res/colors.xml
        You can override any color...
    + Redefine the app name in res/strings.cxml
        and string...

    + (Eclipse) Redefine the parameters in defaults.xml for your specific backend
        = here we specify api endpoint, geoserver...etc

    + The assets folder is not inherited.  Create a new about page html file in assets.

    + parse the choices.py file and install in res/raw/config.xml (see fab task)

    + (Editor)Set up a debugging Google API Maps key in buildconf.json (OTM project on fileshare in deploy/android folder for example)

    + Add relevant other stanzas to otm_buildconf.json 
        = these include api keys, and paths.

    + add relevant function to fabfile
        = each new build has its own subroutine that sets the build env.
