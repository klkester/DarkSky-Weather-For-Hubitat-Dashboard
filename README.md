This is a quick and dirty driver to pull weather data from DarkSky and use it to populate a tile on a Hubitat Dashboard.  It just reads the JSON output of an HTTP get request from Darksky.com and creates a visual current + 2 day forecast for use on a Hubitat Dashboard.

This was literally my first attempt at anything in Groovy and the first time in nearly 15 years I've even said the letters HTML or CSS.  Feel free to critique it, change it, make it better.  I'm under no impression that I've done anything spectacular with this.

I leaned heavily (i.e.: liberally copied code and ideas) on the ApiXU driver written by banganli and used his hosted images for weather conditions.  Lacking a better host and knowing the images were already there, I used what was available.  There is an input field you can point to another host if you want to be a better person than I am.  As for code, I think the only parts that are still mostly Bangali's are the getData and GetImgName functions.  Everything else I've rewritten but used his ApiXU driver as a cheat sheet.

The original art for the weather icons can be found at:  https://www.deviantart.com/vclouds/art/VClouds-Weather-Icons-179152045
Copy them to another host with their existing names and they should work just fine.

Usage:
1)  Copy the code into a new driver on the Hubitat web interface
2)  Create a new Virtual Device using the driver "Dark Sky Weather Driver
3)  Fill in the required input fields:
  a)  DarkSky API Key
  b)  Loocation Latitude
  c)  Location Longitude
4)  Click the "Save Preferences" button
5)  Make the new device available to a dashboard in the apps section of the HE web interface
6)  Add a new tile to a dashboard using the device you created as the device type.  Choose "Attribute" as the template.  Choose "CustomTile1" as the attribute.

Notes:
*  You will almost certainly have to adjust the grid settings for the tile.  On a 6X4 grid on an 8" tablet the tile fits into a 3X3 area quite nicely.  Same setup on a 1080P monitor is a little wasteful space-wise.
*  In the Code you will find the HTML/CSS for the tile output in a variable named "CustomTile1Text".  Feel free to adjust this however you see fit.  
*  Be aware that Hubitat limits the output of an attribute to 1024 characters.  You can't do too much here.
*  DarkSky does request that you leave the "Powered By Darksky" attribution in the output.
