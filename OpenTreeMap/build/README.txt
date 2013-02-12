Creating a new OTM Android app:
    
    Android apps reference the OTM library and override drawables, strings, defaults and config.    

    STEPS
    =====    
    + (Eclipse) Copy an existing android theme.
    + (Eclipse) Edit the project in src/App.js to reflect your new project.
    + (Eclipse) Edit the project in the android manifest.
    + Swap in new drawables for 9 patch buttons and app icons.
    + Redefine the colors in res/colors.xml
    + Redefine the app name in res/strings.cxml
    + Redefine the parameters in defaults.xml for your specific backend
    + Set up a debugging Google API Maps key in manifest.html
    + The assets folder is not inherited.  Create a new about page html file in assets.

   
    Notes about step #1 above:
    A new android theme from scratch: 
        + imports the OTM library
        + contains a near duplicate of the base libraries Manifest, 
                + all activities point to the base library 
                + only the current package name is customized.  
                + Don't duplicate the permissions
