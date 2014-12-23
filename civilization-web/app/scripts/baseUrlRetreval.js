(function () {
  "use strict";
  var request = new XMLHttpRequest();
  request.open("GET", "konfig/personskade-konfig", false); //Blocking
  request.send();

  //Bad programmer is bad :(
  window.civBaseurl = JSON.parse(request.responseText).baseurl;
})();
