# OpenTreeMap for Android

## Getting Started

OpenTreeMap for Android has 2 major dependencies:

  - An OpenTreeMap web application installation
  - A "skin,"  which is a child application that uses the OTM library.

The source for the OpenTreeMap web application is available on github

<a href="https://github.com/azavea/OpenTreeMap">https://github.com/azavea/OpenTreeMap</a>

A default skin is included in this repository under ExampleApp.


A note on google-play-services_lib
----------------------------------

The google-play-services library is downloaded from the SDK manager

To use:

+ Download google-play-services using the SDK manager
+ include it in your workspace
+ Make sure that the reference to google-play-services_lib in project.properties is  correct.  (In Eclipse : Properties/Android/Reference)

Tested with revision 9

USDA Grant
---------------
Portions of OpenTreeMap are based upon work supported by the National Institute of Food and Agriculture, U.S. Department of Agriculture, under Agreement No. 2010-33610-20937, 2011-33610-30511, 2011-33610-30862 and 2012-33610-19997 of the Small Business Innovation Research Grants Program. Any opinions, findings, and conclusions, or recommendations expressed on the OpenTreeMap website are those of Azavea and do not necessarily reflect the view of the U.S. Department of Agriculture.
