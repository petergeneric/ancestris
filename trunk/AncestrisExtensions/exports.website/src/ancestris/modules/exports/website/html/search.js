function displayResult() {
	var result = document.getElementById('searchResult');

	var searchString = document.getElementById('searchName').value.toLowerCase();
	var searchStrings = searchString.split(" ");
	if (searchStrings.length > 1) {
		searchString = searchStrings[0];
	}
	
	// Clear previous result
	while (result.hasChildNodes()) { 
		result.removeChild(result.lastChild);
	}
	var resultP = document.createElement("p");
	result.appendChild(resultP);

	// Find and display result
	var found = false;
	for (i = 0; i < searchValues.length; i++) {
		if (searchValues[i][0].match(searchString)) {
			var match = true;
			if (searchStrings.length > 1) {
				for(j = 1; j < searchStrings.length; j++){
					if (! searchValues[i][0].match(searchStrings[j])) {
						match = false;
						continue;
					}
				}
			}
			if (match) {
				var link = document.createElement("a");
				link.setAttribute("href", makeLinkToIndi(searchValues[i][1]));
				link.appendChild(document.createTextNode(searchValues[i][2]));
				resultP.appendChild(link);
				resultP.appendChild(document.createElement("br"));
				found = true;
			}
		}
	}
	if (! found) {
		resultP.appendChild(document.createTextNode("{noSearchResults}"));
	}

	// Display it
	result.style.display='';
	return false; // Do not submit form...
}

function jumpToSosa() {
	var sosaId = document.getElementById('searchSosa').value;
	for (i = 0; i < searchValues.length; i++) {
		if ((searchValues[i][3] == sosaId) || ((searchValues[i][3].indexOf(";",0) >= 0) && (searchValues[i][3].match("(^|;)"+sosaId+"(;|$)","g")))) {
			document.location.href = makeLinkToIndi(searchValues[i][1]);
			return false;
		}
	}
	var result = document.getElementById('searchResult');
	// Write no hits found
	while (result.hasChildNodes()) { 
		result.removeChild(result.lastChild);
	}
	var resultP = document.createElement("p");
	result.appendChild(resultP);
	resultP.appendChild(document.createTextNode("{noSearchResults}"));
	return false;
}

function makeLinkToIndi(id) {
	var link = "";
	var i = id.length;
	if (i%2 == 1){
            i += 1;
            id = "0" + id;
	}
        while (id.length > 0) {
            link = link + "/" + id.substring(0, 2);
            id = id.substring(2);
        }
	return "indi" + i + link + "/"+"index.html";
}

function displayAdvanced() {
	document.getElementById('searchAdvanced').style.display = '';
	document.getElementById('searchSimple').style.display = 'none';
}
function displaySimple() {
	document.getElementById('searchAdvanced').style.display = 'none';
	document.getElementById('searchSimple').style.display = '';
}