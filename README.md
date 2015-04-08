Civlization boardgame (Fantasy Flight Games)
=================================

[![Build Status](https://travis-ci.org/dropwizard/dropwizard-java8.svg?branch=master)](https://travis-ci.org/cash1981/civilization-boardgame)
[![Coverage Status](https://coveralls.io/repos/cash1981/civilization-boardgame/badge.svg)](https://coveralls.io/r/cash1981/civilization-boardgame)

##You can find the application 
Here --> (http://civ.asgari.no)

##First version
This is a web application where you can use to play the Civilization boardgame. The first version is targeted for Play By Forums.

This project will consist of server and client, where the server will create games, shuffle cards and hand out items to players.
The client is an HTML 5 enabled mobile friendly web application that can create games, let players join these games and make draws.

##Coming soon
Future version will be a fully playable web client with drag and drop play. For now users have to use Google Presentation and Spreadsheet or the forums to upload pictures and asset files.

##Requires

###civilizaton-rest	
* Maven
* Java 8
* MongoDB
* Lombok (www.projectlombok.org)

###civilization-web
* NodeJS (https://www.npmjs.org/)
* bower (http://bower.io/
* Grunt (http://gruntjs.com/)

##Installation
Install what is required, and then run ```mvn clean install``` or ```mvn exec:java``` on civilization-rest and ```npm install``` and ```bower install``` then ```grunt serve``` on civilization-web