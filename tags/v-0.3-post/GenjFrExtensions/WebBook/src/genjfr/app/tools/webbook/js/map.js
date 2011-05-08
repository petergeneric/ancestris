<script type="text/javascript">
//<![CDATA[
    var mapXml = "map.xml";
    var map;
    var gmarkers = [];
    var gicons = [];
    var bounds = new GLatLngBounds();
    var params=self.location.href.split("?");
    var cityParam = (params.length > 1) ? params[1] : "";
    var iconHeigth = 0;
    var iconWidth;
    var shadowWidth;

    // Entry point
    function displayMap() {
       if (GBrowserIsCompatible()) {
          createMap();
          adjustZoom();
          createIcons();
          loadMarkers();
          }
       else {
          alert("Sorry, the Google Maps feature is not compatible with this browser");
          }
       }

    function displayMarkers() {
       if (GBrowserIsCompatible()) {
          createIcons();
          loadMarkers();
          }
       else {
          alert("Sorry, the Google Maps feature is not compatible with this browser");
          }
       }

    // main functions
    function createIcons() {
       // Sizes
       iconSize = parseInt(document.getElementById("markersize").value);
       if (iconSize == 0) {
          iconHeigth = 32;
          }
       else {
          iconHeigth = iconSize;
          }
       // base icon
       var baseIcon = new GIcon();
       baseIcon.shadow = "z.png";
       iconWidth = iconHeigth*1.5;
       shadowWidth = iconHeigth*2;
       factor = 1.30;

       // tiny
       gicons["l"] = new GIcon(baseIcon);
       gicons["l"].iconSize = new GSize(iconWidth, iconHeigth);
       gicons["l"].shadowSize = new GSize(shadowWidth, iconHeigth);
       gicons["l"].iconAnchor = new GPoint(iconWidth/2, iconHeigth);
       gicons["l"].infoWindowAnchor = new GPoint(iconWidth/2, iconHeigth);
       // medium
       gicons["m"] = new GIcon(baseIcon);
       gicons["m"].iconSize = new GSize(iconWidth * factor, iconHeigth * factor);
       gicons["m"].shadowSize = new GSize(shadowWidth * factor, iconHeigth * factor);
       gicons["m"].iconAnchor = new GPoint(iconWidth/2 * factor, iconHeigth * factor);
       gicons["m"].infoWindowAnchor = new GPoint(iconWidth/2 * factor, iconHeigth * factor);
       // big
       gicons["h"] = new GIcon(baseIcon);
       gicons["h"].iconSize = new GSize(iconWidth * factor * factor, iconHeigth * factor * factor);
       gicons["h"].shadowSize = new GSize(shadowWidth * factor * factor, iconHeigth * factor * factor);
       gicons["h"].iconAnchor = new GPoint(iconWidth/2 * factor * factor, iconHeigth * factor * factor);
       gicons["h"].infoWindowAnchor = new GPoint(iconWidth/2 * factor * factor, iconHeigth * factor * factor);
       }

    function createMap() {
       map = new GMap2(document.getElementById("map"));
       map.setCenter(new GLatLng(30, 5), 2);
       map.addControl(new GLargeMapControl());
       map.addControl(new GMapTypeControl());
       map.addControl(new GScaleControl());
       map.addControl(new GOverviewMapControl());
       map.setMapType(G_NORMAL_MAP);
       map.enableDoubleClickZoom();
       map.enableScrollWheelZoom();
       }

    function loadMarkers() {
       var request = GXmlHttp.create();
       request.open('GET', mapXml, true);
       request.onreadystatechange = function() {
          if (request.readyState == 4) {
             var center;
             var ls = request.responseXML.documentElement.getElementsByTagName("l");
             for (var l = 0; l < ls.length; l++) {
                // read data
                var lng        = parseFloat(ls[l].getAttribute("x"));
                var lat        = parseFloat(ls[l].getAttribute("y"));
                var size       = ls[l].getAttribute("s");
                var ancestor   = ls[l].getAttribute("a");
                var type       = ls[l].getAttribute("t");
                var density    = ls[l].getAttribute("d");
                var min        = ls[l].getAttribute("min");
                var max        = ls[l].getAttribute("max");
                var linkpage   = ls[l].getAttribute("lkp");
                var linkIn     = ls[l].getAttribute("lki");
                var linkOut    = ls[l].getAttribute("lko");
                var city       = ls[l].getAttribute("cty");
                var html       = ls[l].firstChild.nodeValue;

                // create marker
                var point = new GLatLng(lat,lng);
                var marker = createMarker(point, size, ancestor, type, density, min, max, linkpage, linkOut, city, html);
                map.addOverlay(marker);
                marker.hide();
                if (cityParam == linkIn) {
                   GEvent.trigger(marker,'click');
                   center = point;
                   }
                bounds.extend(point);
                }
             if (cityParam != "") {
                map.setZoom(7);
                map.setCenter(center);
                }
             boxclick();
             }
          }
       request.send(null);
       }

    function adjustZoom() {
       var mt = map.getMapTypes();
       for (var i=0; i<mt.length; i++) {
         mt[i].getMinimumResolution = function() {return 2;}
         mt[i].getMaximumResolution = function() {return 19;}
         }
       }

    // secondary entry points
    function boxclick() {
      //mysize
      var volume = "*";
      if (document.getElementById("vola").checked) volume = "*";
      if (document.getElementById("volh").checked) volume = "h";
      if (document.getElementById("volm").checked) volume = "m";
      if (document.getElementById("voll").checked) volume = "l";
      //myancestor
      var ancestor = "*";
      if (document.getElementById("anca").checked) ancestor = "*";
      if (document.getElementById("ancs").checked) ancestor = "s";
      if (document.getElementById("ancc").checked) ancestor = "c";
      if (document.getElementById("anco").checked) ancestor = "o";
      //mytype
      var type = "*";
      if (document.getElementById("evea").checked) type = "*";
      if (document.getElementById("even").checked) type = "b";
      if (document.getElementById("evem").checked) type = "m";
      if (document.getElementById("eved").checked) type = "d";
      //mymin
      var min  = document.getElementById("min").value;
      //mymax
      var max  = document.getElementById("max").value;
      //mydensity
      var density = 1;
      if (document.getElementById("den1").checked) density = 1;
      if (document.getElementById("den2").checked) density = 2;
      if (document.getElementById("den3").checked) density = 3;
      for (var i=0; i<gmarkers.length; i++) {
        var show = true;
        if ((volume != "*")   && (gmarkers[i].mysize != volume))           { gmarkers[i].hide(); continue; }
        if ((ancestor != "*") && (gmarkers[i].myancestor != ancestor))     { gmarkers[i].hide(); continue; }
        if ((type != "*")     && (gmarkers[i].mytype.indexOf(type) == -1)) { gmarkers[i].hide(); continue; }
        if ((gmarkers[i].mymin != 0) && (gmarkers[i].mymin < min))         { gmarkers[i].hide(); continue; }
        if ((gmarkers[i].mymax != 0) && (gmarkers[i].mymax > max))         { gmarkers[i].hide(); continue; }
        if (gmarkers[i].mydensity < density)                               { gmarkers[i].hide(); continue; }
        gmarkers[i].show();
        }
    }

    // secondary entry points
    function add() {
      iconSize += 4; set();
      }

    function sub() {
      iconSize -= 4; set();
      }

    function chg() {
      iconSize = parseInt(document.getElementById("markersize").value); set();
      }

    function set() {
      document.getElementById("markersize").value = iconSize;
      map.clearOverlays();
      gmarkers.length = 0;
      gicons.length = 0;
      bounds.length = 0;
      createIcons(iconSize);
      loadMarkers();
      }

    // sub functions
    function createMarker(point, size, ancestor, type, density, min, max, linkpage, linkOut, city, text) {
       iconType = new GIcon(gicons[size], "../theme/"+ancestor+type+".png");
       markerIcon = { icon:iconType };
       var marker = new GMarker(point, markerIcon);
       marker.mysize = size;
       marker.myancestor = ancestor;
       marker.mytype = type;
       marker.mydensity = density;
       marker.mymin = min;
       marker.mymax = max;
       marker.mycity = city;
       var link = "../citiesdetails/"+linkpage+"#"+linkOut;
       GEvent.addListener(marker, 'click', function() {
          var lines = text;
          var html = "<div style=text-align:center;font-weight:bold;font-size:150%;>"+city+"</div><div style=white-space:nowrap;>";
          while (lines.indexOf(';')>=0) {
             html += lines.substring(0, lines.indexOf(';'));
             html += "<br />";
             lines = lines.substring(lines.indexOf(';')+1);
             }
          html += lines;
          html += "<br /><br />";
          html += "<a href="+link+" target=\"_blank\">"+"Voir les événements détaillés pour "+city+"</a>";
          html += "</div>";
          marker.openInfoWindowHtml(html);
          });
       gmarkers.push(marker);
       return marker;
       }

    function e(id) {
       return document.getElementById(id);
       }

    function getWindowHeight() {
       if (window.self && self.innerHeight) {
          return self.innerHeight;
          }
       if (document.documentElement && document.documentElement.clientHeight) {
          return document.documentElement.clientHeight;
          }
       return 0;
       }

    function getWindowWidth() {
       if (window.self && self.innerWidth) {
          return self.innerWidth;
          }
       if (document.documentElement && document.documentElement.clientWidth) {
          return document.documentElement.clientWidth;
          }
       return 0;
       }

    function resizeApp() {
       var offsetTop = 0;
       var mapElem = e("map");
       for (var elem = mapElem; elem; elem = elem.offsetParent) {
          offsetTop += elem.offsetTop;
          }
       var width = getWindowWidth();
       var footerHeight;
       if (width < 950) { footerHeight = 120 }
       else if (width < 1170) { footerHeight = 90 }
       else { footerHeight = 60 }
       var height = getWindowHeight() - offsetTop - footerHeight - 10;
       if (height >= 0) {
          mapElem.style.height = height + "px";
          }
       }


//]]>
</script>
