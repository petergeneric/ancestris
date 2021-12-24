//<![CDATA[

// On initialise la latitude et la longitude du centre du monde
var lat = 30;
var lon = 0;
var zoom = 2;
var macarte = null;

window.onload = function () {
    // Fonction d'initialisation qui s'exécute lorsque le DOM est chargé
	getCurrentPoint(window.location.search);
    initMap();
};

// Exploitation du lien entrant
function getCurrentPoint(queryString) {
	var ls = getMarkers();
    for (var l = 0; l < ls.length; l++) {
        // read data
        var loclng = parseFloat(ls[l].x);
        var loclat = parseFloat(ls[l].y);
        var linkIn = ls[l].lki;
		if (queryString.substring(1) == linkIn ) {
				lat = loclat;
				lon = loclng;
				zoom = 13;
				break;
		}
    }
}

// Fonction d'initialisation de la carte
function initMap() {
    macarte = L.map('map').setView([lat, lon], zoom);
    L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png', {
        attribution: 'données © <a href="//osm.org/copyright">OpenStreetMap</a>/ODbL - rendu <a href="//openstreetmap.fr">OSM France</a>',
        minZoom: 1,
        maxZoom: 20
    }).addTo(macarte);

    loadMarkers();
}

function loadMarkers() {
    var ls = getMarkers();
    for (var l = 0; l < ls.length; l++) {
        // read data
        var lng = parseFloat(ls[l].x);
        var lat = parseFloat(ls[l].y);
        var size = ls[l].s;
        var ancestor = ls[l].a;
        var type = ls[l].t;
        var density = ls[l].d;
        var min = ls[l].min;
        var max = ls[l].max;
        var linkpage = ls[l].lkp;
        var linkIn = ls[l].lki;
        var linkOut = ls[l].lko;
        var city = ls[l].cty;
        var lines = ls[l].text;
        var link = "../citiesdetails/" + linkpage + "#" + linkOut;
        var myIcon = L.icon({iconUrl: "../theme/" + ancestor + type + ".png", iconSize: [48, 32], iconAnchor: [24, 32], popupAnchor: [0, -30], });
        var marker = L.marker([lat, lng], {icon: myIcon}).addTo(macarte);
        var htmlLabel = "<div style=text-align:center;font-weight:bold;font-size:150%;>" + city + "</div><div style=white-space:nowrap;>";
        while (lines.indexOf(';') >= 0) {
            htmlLabel += lines.substring(0, lines.indexOf(';'));
            htmlLabel += "<br />";
            lines = lines.substring(lines.indexOf(';') + 1);
        }
        htmlLabel += lines;
        htmlLabel += "<br /><br />";
        htmlLabel += "<a href=" + link + " target=\"_blank\">" + "Voir les événements détaillés pour " + city + "</a>";
        htmlLabel += "</div>";
        marker.bindPopup(htmlLabel);
    }
}
//]]>