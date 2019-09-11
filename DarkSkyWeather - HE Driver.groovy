/***********************************************************************************************************************
*  Copyright 2019 Kent Kester
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
*  DarkSky Weather Driver
*
*  Author: Kent Kester
*
*  Code can be found at:  https://github.com/klkester/klkester
*
*  Date: 2019-09-10
*
*  attribution: weather data courtesy: https://www.darksky.com/
*  attribution: heavily adapted from ApiXU Driver by bangali
*  attribution: icons shamelessly stolen from and hosted by bangali
*
*	Seriously, I mostly swiped the idea for this from Bangali.  There's a lot more data available than what I've presented.  I just wanted a working current+2 forecast for my dashboard.
*   I saw no need to make each value available as an attribute of the device.  I do understand why some of them might be usefull as such but it wasn't something I cared to do.
*   The CustomTile1 (populated by CustomTile1Text) is something you may need to tweak to fit your dashboard.  Mine is designed for am 8" Android tablet with a 6X4 grid dashboard.  The weather takes up 3X3 of that.
*
* features:
* - supports weather data with free api key from darksky.com (limited to 1000 requests per day on free API key)
* - does not provide calculated illuminance data based on time of day and weather condition code.
* - no local server setup needed
* - no personal weather station needed
*
***********************************************************************************************************************/

public static String version()	{return "v0.3.0"}

/**********************************************************************************************************************
*
*	Version: 0.1.0
*		Wondering why this is my first attempt at anything in Groovy.  But why not?
*	
*	Version 0.2.0
*		First working version with a Current+2 Day forcast Tile.  Option for SI units instead of Imperial
*
*	Version 0.3.0
*		Added input option to use a different host than bangali's hosting site for the images.
***********************************************************************************************************************/

import groovy.transform.Field

metadata	{
	definition (name: "DarkSky Weather Driver", namespace: "kkDSW", author: "Kent Kester")	{
        capability "Actuator"
        capability "Sensor"
        capability "Polling"
        capability "Illuminance Measurement"
        capability "Temperature Measurement"
        capability "Relative Humidity Measurement"
        capability "Pressure Measurement"
        capability "Ultraviolet Index"
		
		attribute "CustomTile1", "string"
	}
	
	preferences	{
		input name:"DarkSkyAPIKey", type:"text", title:"DarkSky API Key", required:true
		input name:"Latitude", type:"text", title:"Location Latitude", required:true
		input name:"Longitude", type:"text", title:"Location Longitude", required:true
		input name:"Units", type: "enum", description: "", title: "Units", options: [["us":"US (Imperial)"],["si":"SI"],["uk2":"United Kingdom"]], defaultValue: "us"
        input "PollEvery", "enum", title:"Poll DarkSky how frequently?\nrecommended setting 30 minutes.", required:true, defaultValue:30, options:[5:"5 minutes",10:"10 minutes",15:"15 minutes",30:"30 minutes"]
		input name:"LogsEnabled", type:"bool", title:"Logs Enabled", defaultValue:true
		input name:"ImageHostURL", type:"text", title:"Image Host URL", required: true, defaultValue: "https://cdn.rawgit.com/adey/bangali/master/resources/icons/weather/"
	}
		
}

def installed()	{
	log.debug "installed"
	initialize()
}

def updated()	{
	log.debug "updated"
    poll()
    "runEvery${PollEvery}Minutes"(poll)
	initialize()
}

def initialize()	{
	log.debug "initialize"
	// unschedule()
}

def logsOff(){
    log.warn "debug logging disabled..."
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def poll()	{
	def obs = getDSdata()
    log.debug "DarkSky Updated"
	
	if (!obs)   {
		log.warn "No response from DarkSky API"
		return
    }

	def LocationName = obs.timezone
	def CurrentTemp = Math.round(obs.currently.temperature)
	def CurrentFeelsLike = Math.round(obs.currently.apparentTemperature)
	def TodayLowTemp = Math.round(obs.daily.data[0].temperatureLow)
	def TodayHighTemp = Math.round(obs.daily.data[0].temperatureHigh)
	def Day1LowTemp = Math.round(obs.daily.data[1].temperatureLow)
	def Day1HighTemp = Math.round(obs.daily.data[1].temperatureHigh)
	def Day2LowTemp = Math.round(obs.daily.data[2].temperatureLow)
	def Day2HighTemp = Math.round(obs.daily.data[2].temperatureHigh)
	def CurrentIcon = obs.currently.icon
	def CurrentSummary = obs.currently.summary
	def Sunrise = obs.daily.data[0].sunriseTime
	def Sunset = obs.daily.data[0].sunsetTime
	def Day1Icon = obs.daily.data[1].icon
	def Day1Summary = obs.daily.data[1].summary
	def Day2Icon = obs.daily.data[2].icon
	def Day2Summary = obs.daily.data[2].summary
	def IsDay = isDay(Sunrise, Sunset)
	
	def CurrentImage = getImgName(CurrentIcon, IsDay)
	def Day1Image = getImgName(Day1Icon, 1)
	def Day2Image = getImgName(Day2Icon, 1)
	
	def CurrentDate = new Date()
	def CurrentDoW = CurrentDate.format("EEEE")
	def Day1DoW = CurrentDate.plus(1).format("EEEE")
	def Day2DoW = CurrentDate.plus(2).format("EEEE")
	
	def CustomTile1Text = '<style>'
	CustomTile1Text += '.WcInfo {'
	CustomTile1Text += '	float: left;'
	CustomTile1Text += '	text-align: left;'
	CustomTile1Text += '}'

	CustomTile1Text += '.WcHead {'
	CustomTile1Text += '	float: left;'
	CustomTile1Text += '	width: 30%;'
	CustomTile1Text += '	height: 30%;'
	CustomTile1Text += '	padding: none;'
	CustomTile1Text += '	display: block;'
	CustomTile1Text += '}'

	CustomTile1Text += '.WctHead {'
	CustomTile1Text += '	float: left;'
	CustomTile1Text += '	width: 40%;'
	CustomTile1Text += '	padding: none;'
	CustomTile1Text += '	display: block;'
	CustomTile1Text += '}'
	
	CustomTile1Text += '.Wrow {'
	CustomTile1Text += '	content: "";'
	CustomTile1Text += '	clear: both;'
	CustomTile1Text += '}'
    	
	CustomTile1Text += '.WrowFooter {'
	CustomTile1Text += '	content: "";'
	CustomTile1Text += '	clear: both;'
	CustomTile1Text += '	font-size: 8px;'
	CustomTile1Text += '}'

	CustomTile1Text += '.WImg {'
	CustomTile1Text += '	height: 3em;'
	CustomTile1Text += '}'
	

	CustomTile1Text += '</style>'

	CustomTile1Text += '<div class="Wrow">'
	CustomTile1Text += '<div class="WctHead">'
	CustomTile1Text += 'Today'
	CustomTile1Text += '<p><img class="WImg" src=' + CurrentImage + '> '
	CustomTile1Text += '<div class="WcInfo">'
	CustomTile1Text += 'Now:' + CurrentTemp
	CustomTile1Text += '<br>Feels Like: ' + CurrentFeelsLike
	CustomTile1Text += '<br>Low: ' + TodayLowTemp 
	CustomTile1Text += '<br>High: ' + TodayHighTemp
	CustomTile1Text += '</div>'	
	CustomTile1Text += '</div>'	
	CustomTile1Text += '<div class="WcHead">'
	CustomTile1Text += Day1DoW
	CustomTile1Text += '<p><img class="WImg" src=' + Day1Image + '> '
	CustomTile1Text += '<div class="WcInfo">'
	CustomTile1Text += 'Low: ' + Day1LowTemp
	CustomTile1Text += '<br>High: ' + Day1HighTemp
	CustomTile1Text += '</div>'	
	CustomTile1Text += '</div>'	
	CustomTile1Text += '<div class="WcHead">'
	CustomTile1Text += Day2DoW
	CustomTile1Text += '<p><img class="WImg" src=' + Day2Image + '> '
	CustomTile1Text += '<div class="WcInfo">'
	CustomTile1Text += 'Low: ' + Day2LowTemp 
	CustomTile1Text += '<br>High: ' + Day2HighTemp
	CustomTile1Text += '</div>'	
	CustomTile1Text += '</div>'	
	CustomTile1Text += '</div>'
	CustomTile1Text += '<div class="WrowFooter">'
	CustomTile1Text += '<href="https://darksky.net/poweredby/">Powered by DarkSky</href>'
	CustomTile1Text += '</div>'
	
	sendEvent(name:"CustomTile1", value:CustomTile1Text, descriptionText:"CustomTile1")

}

private getDSdata()   {
    def obs = [:]
	def uritext = "https://api.darksky.net/forecast/" + DarkSkyAPIKey + "/" + Latitude + "," + Longitude + "?exclude=hourly"
	def params = [ uri: "https://api.darksky.net/forecast/" + DarkSkyAPIKey + "/" + Latitude + "," + Longitude + "?exclude=hourly&exclude=minutely&units=$Units" ]

    try {
        httpGet(params)		{ resp ->
            if (resp?.data)     obs << resp.data;
            else                log.error "http call for DarkSky weather api did not return data: $resp";
        }
    } catch (e) { log.error "http call failed for DarkSky weather api: $e" }
    return obs
}

private isDay(Sunrise, Sunset)	{
	def returnVal = 0
	Date CurrentTime = new Date()
	Date SunriseLocal = new Date((long)Sunrise)
	Date SunsetLocal = new Date((long)Sunset)
	if (CurrentTime > SunriseLocal && CurrentTime < SunsetLocal){
		returnVal = 1
	}
	return returnVal
}

private getImgName(Icon, IsDay)	{
    def url = ImageHostURL
    def imgItem = imgNames.find{ it.code == Icon && it.day == IsDay }
    return (url + (imgItem ? imgItem.img : 'na.png'))
}

@Field final List    imgNames =     [
		[code: 'clear-day', day: 1, img: '32.png', ],
		[code: 'rain', day: 1, img: '12.png', ],
		[code: 'snow', day: 1, img: '16.png', ],
		[code: 'sleet', day: 1, img: '6.png', ],
		[code: 'wind', day: 1, img: '15.png', ],
		[code: 'fog', day: 1, img: '21.png', ],
		[code: 'cloudy', day: 1, img: '28.png', ],
		[code: 'partly-cloudy-day', day: 1, img: '30.png', ],
		[code: 'clear-day', day: 0, img: '31.png', ],
		[code: 'rain', day: 0, img: '12.png', ],
		[code: 'snow', day: 0, img: '16.png', ],
		[code: 'sleet', day: 0, img: '6.png', ],
		[code: 'wind', day: 0, img: '15.png', ],
		[code: 'fog', day: 0, img: '21.png', ],
		[code: 'cloudy', day: 0, img: '27.png', ],
		[code: 'partly-cloudy-day', day: 0, img: '32.png', ],
		[code: 'clear-night', day: 0, img: '31.png', ],
		[code: 'partly-cloudy-night', day: 0, img: '32.png', ]
]
