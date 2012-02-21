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
	var numbers = 2;
	while (id > 99) {
		var curr = id % 100;
		id = (id - curr) / 100;
		numbers += 2;
		link = curr + "/" + link;
		if (curr < 10) link = "0" + link;
	}
	link = id + "/" + link;
	if (id < 10) link = "0" + link;
	return "indi" + numbers + "/" + link + "{indexFile}";
}

function displayAdvanced() {
	document.getElementById('searchAdvanced').style.display = '';
	document.getElementById('searchSimple').style.display = 'none';
}
function displaySimple() {
	document.getElementById('searchAdvanced').style.display = 'none';
	document.getElementById('searchSimple').style.display = '';
}