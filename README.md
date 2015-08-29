Civlization boardgame (Fantasy Flight Games)
=================================

[![Build Status](https://travis-ci.org/cash1981/civilization-boardgame-rest.svg?branch=master)](https://travis-ci.org/cash1981/civilization-boardgame-rest)
[![Coverage Status](https://coveralls.io/repos/cash1981/civilization-boardgame/badge.svg)](https://coveralls.io/r/cash1981/civilization-boardgame)

#You can find the application deployed here 
Here --> (http://playciv.com)

##Source code

###civilization-web
Can be located here --> (https://github.com/cash1981/civilization-boardgame-web)

###civilization-rest
Can be located here --> (https://github.com/cash1981/civilization-boardgame-rest)

This is a multiplayer responsive web application where you can use to play the Civilization boardgame.

This project will consist of server and client, where the server will create games, shuffle cards and hand out items to players.
The client is an HTML 5 enabled mobile friendly web application that can create games, let players join these games and make draws.

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
