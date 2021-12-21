
var list_found = [];
const paramMap = new Map();
var input_valid = "0";

function getParameters() {
    find_string = location.href.substring(location.href.indexOf("?") + 1);
    find_string = unescape(find_string);
    arrayofStrings = find_string.split("&");

    for (var i = 0; i < arrayofStrings.length; i++) {
        if (arrayofStrings[i].substring(0, 6) == "key_fn") {
            paramMap.set("key_fn", arrayofStrings[i].substring(7));
            if (paramMap.get("key_fn") !== "") {
                input_valid = "1";
                document.getElementById("key_fn").value = paramMap.get("key_fn");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xfn") {
            paramMap.set("key_xfn", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xfn").checked = true;
        }

        if (arrayofStrings[i].substring(0, 6) == "key_ln") {
            paramMap.set("key_ln", arrayofStrings[i].substring(7));
            if (paramMap.get("key_ln") !== "") {
                input_valid = "1";
                document.getElementById("key_ln").value = paramMap.get("key_ln");
            }
        }

        if (arrayofStrings[i].substring(0, 7) == "key_xln") {
            paramMap.set("key_xln", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xln").checked = true;
        }

        if (arrayofStrings[i].substring(0, 6) == "key_pl") {
            paramMap.set("key_pl", arrayofStrings[i].substring(7));
            if (paramMap.get("key_pl") !== "") {
                input_valid = "1";
                document.getElementById("key_pl").value = paramMap.get("key_pl");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xpl") {
            paramMap.set("key_xpl", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xpl").checked = true;
        }

        if (arrayofStrings[i].substring(0, 6) == "key_id") {
            paramMap.set("key_id", arrayofStrings[i].substring(7));
            if (paramMap.get("key_id") !== "") {
                input_valid = "1";
                document.getElementById("key_id").value = paramMap.get("key_id");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xid") {
            paramMap.set("key_xid", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xid").checked = true;
        }

        if (arrayofStrings[i].substring(0, 6) == "key_so") {
            paramMap.set("key_so", arrayofStrings[i].substring(7));
            if (paramMap.get("key_so") !== "") {
                input_valid = "1";
                document.getElementById("key_so").value = paramMap.get("key_so");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xso") {
            paramMap.set("key_xso", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xso").checked = true;
        }

        if (arrayofStrings[i].substring(0, 7) == "key_1bi") {
            paramMap.set("key_1bi", arrayofStrings[i].substring(8));
            if (paramMap.get("key_1bi") !== "") {
                input_valid = "1";
                document.getElementById("key_1bi").value = paramMap.get("key_1bi");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_2bi") {
            paramMap.set("key_2bi", arrayofStrings[i].substring(8));
            if (paramMap.get("key_2bi") !== "") {
                input_valid = "1";
                document.getElementById("key_2bi").value = paramMap.get("key_2bi");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xbi") {
             paramMap.set("key_xbi", arrayofStrings[i].substring(8).toUpperCase());
             document.getElementById("key_xbi").checked = true;
        }

        if (arrayofStrings[i].substring(0, 7) == "key_1ma") {
            paramMap.set("key_1ma", arrayofStrings[i].substring(8));
            if (paramMap.get("key_1ma") !== "") {
                input_valid = "1";
                document.getElementById("key_1ma").value = paramMap.get("key_1ma");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_2ma") {
            paramMap.set("key_2ma", arrayofStrings[i].substring(8));
            if (paramMap.get("key_2ma") !== "") {
                input_valid = "1";
                document.getElementById("key_2ma").value = paramMap.get("key_2ma");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xma") {
            paramMap.set("key_xma", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xma").checked = true;
        }

        if (arrayofStrings[i].substring(0, 7) == "key_1de") {
            paramMap.set("key_1de", arrayofStrings[i].substring(8));
            if (paramMap.get("key_1de") !== "") {
                input_valid = "1";
                document.getElementById("key_1de").value = paramMap.get("key_1de");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_2de") {
            paramMap.set("key_2de", arrayofStrings[i].substring(8));
            if (paramMap.get("key_2de") !== "") {
                input_valid = "1";
                document.getElementById("key_2de").value = paramMap.get("key_2de");
            }
        }
        if (arrayofStrings[i].substring(0, 7) == "key_xde") {
            paramMap.set("key_xde", arrayofStrings[i].substring(8).toUpperCase());
            document.getElementById("key_xde").checked = true;
        }
        if (arrayofStrings[i].substring(0, 5) == "andor") {
            paramMap.set("andor", arrayofStrings[i].substring(6).toUpperCase());
            if ("AND" === paramMap.get("andor")) {
                document.getElementById("andor1").checked = true;
            } else {
                document.getElementById("andor2").checked = true;
            }
        }
    }
}

function processSearch() {
    nbResults = 0;

    if (input_valid == "0") {
        paramMap.set("key_fn", document.getElementById('key_fn').value);
        paramMap.set("key_xfn", document.getElementById('key_xfn').checked ? document.getElementById('key_xfn').value.toUpperCase() : "OFF");
        paramMap.set("key_ln", document.getElementById('key_ln').value);
        paramMap.set("key_xln", document.getElementById('key_xln').checked ? document.getElementById('key_xln').value.toUpperCase() : "OFF");
        paramMap.set("key_pl", document.getElementById('key_pl').value);
        paramMap.set("key_xpl", document.getElementById('key_xpl').checked ? document.getElementById('key_xpl').value.toUpperCase() : "OFF");
        paramMap.set("key_id", document.getElementById('key_id').value);
        paramMap.set("key_xid", document.getElementById('key_xid').checked ? document.getElementById('key_xid').value.toUpperCase() : "OFF");
        paramMap.set("key_so", document.getElementById('key_so').value);
        paramMap.set("key_xso", document.getElementById('key_xso').checked ? document.getElementById('key_xso').value.toUpperCase() : "OFF");
        paramMap.set("key_1bi", document.getElementById('key_1bi').value);
        paramMap.set("key_2bi", document.getElementById('key_2bi').value);
        paramMap.set("key_xbi", document.getElementById('key_xbi').checked ? document.getElementById('key_xbi').value.toUpperCase() : "OFF");
        paramMap.set("key_1ma", document.getElementById('key_1ma').value);
        paramMap.set("key_2ma", document.getElementById('key_2ma').value);
        paramMap.set("key_xma", document.getElementById('key_xma').checked ? document.getElementById('key_xma').value.toUpperCase() : "OFF");
        paramMap.set("key_1de", document.getElementById('key_1de').value);
        paramMap.set("key_2de", document.getElementById('key_2de').value);
        paramMap.set("key_xde", document.getElementById('key_xde').checked ? document.getElementById('key_xde').value.toUpperCase() : "OFF");
        paramMap.set("andor", document.getElementById('andor1').checked ? document.getElementById('andor1').value.toUpperCase() : document.getElementById('andor2').value.toUpperCase());

        if (paramMap.get("key_fn") !== "")
            input_valid = "1";
        if (input_key_ln != "")
            input_valid = "1";
        if (input_key_pl != "")
            input_valid = "1";
        if (input_key_id != "")
            input_valid = "1";
        if (input_key_so != "")
            input_valid = "1";
        if (input_key_1bi != "")
            input_valid = "1";
        if (input_key_2bi != "")
            input_valid = "1";
        if (input_key_1ma != "")
            input_valid = "1";
        if (input_key_2ma != "")
            input_valid = "1";
        if (input_key_1de != "")
            input_valid = "1";
        if (input_key_2de != "")
            input_valid = "1";
    }

    if (input_valid == "0") {
        document.write("<p class=\"searchdecal\">search_please</p>");
        return;
    }

    if (input_valid == "1") {
        nbResults = 0;
        first = "1";

        if (paramMap.get("key_fn") !== "") {
            nbResults = recherche("string", paramMap.get("key_fn"), "", paramMap.get("key_xfn"), list_firstnames, list_firstnamesID, first);
            first = "0";
        }
        if (paramMap.get("key_ln") !== "") {
            nbResults = recherche("string", paramMap.get("key_ln"), "", paramMap.get("key_xln"), list_lastnames, list_lastnamesID, first);
            first = "0";
        }
        if (paramMap.get("key_pl") !== "") {
            nbResults = recherche("place", paramMap.get("key_pl"), "", paramMap.get("key_xpl"), list_places, list_placesID, first);
            first = "0";
        }
        if (paramMap.get("key_id") !== "") {
            nbResults = recherche("string", paramMap.get("key_id"), "", paramMap.get("key_xid"), list_ids, list_idsID, first);
            first = "0";
        }
        if (paramMap.get("key_so") !== "") {
            nbResults = recherche("string", paramMap.get("key_so"), "", paramMap.get("key_xso"), list_sosas, list_sosasID, first);
            first = "0";
        }
        if (paramMap.get("key_1bi") !== "") {
            nbResults = recherche("number", paramMap.get("key_1bi"), paramMap.get("key_2bi"), paramMap.get("key_xbi"), list_births, list_birthsID, first);
            first = "0";
        }
        if (paramMap.get("key_1ma") !== "") {
            nbResults = recherche("number", paramMap.get("key_1ma"), paramMap.get("key_2ma"), paramMap.get("key_xma"), list_marriages, list_marriagesID, first);
            first = "0";
        }
        if (paramMap.get("key_1de") !== "") {
            nbResults = recherche("number", paramMap.get("key_1de"), paramMap.get("key_2de"), paramMap.get("key_xde"), list_deaths, list_deathsID, first);
            first = "0";
        }
        displayResults(nbResults);
    }
}


function recherche(searchType, keyword1, keyword2, keyflag, list, list_id, init) {
    var nbres = 0;

    keyword1 = keyword1.toUpperCase();
    keyword2 = keyword2.toUpperCase();

    if ((paramMap.get("andor") === "OR") || (init == "1")) {
        nbres = list_found.length;
        // scan list
        for (var i = 0; i < list.length; i++) {
            // if criteria matched
            if (matchSearch(searchType, keyword1, keyword2, keyflag, list[i])) {
                // build array of ids that match from list of ids
                arrayofStrings = list_id[i].split("|");
                // scan array
                for (var j1 = 0; j1 < arrayofStrings.length; j1++) {
                    // if already in list_found, continue...
                    for (var j2 = 0; j2 < list_found.length; j2++) {
                        if (arrayofStrings[j1] == list_found[j2]) {
                            break;
                        }
                    }
                    if (j2 < list_found.length) {
                        continue;
                    }
                    // else add to list_found
                    list_found[nbres] = arrayofStrings[j1];
                    nbres++;
                }
            }
        }
    }

    if ((paramMap.get("andor") === "AND") && (init == "0")) {
        var local_list_found = [];
        nbres = 0;
        // scan list
        for (var i = 0; i < list.length; i++) {
            // if keyword found in item i
            if (matchSearch(searchType, keyword1, keyword2, keyflag, list[i])) {
                // build array of ids that match from list of ids
                arrayofStrings = list_id[i].split("|");
                // scan array
                for (var j1 = 0; j1 < arrayofStrings.length; j1++) {
                    // if already in list_found, continue...
                    for (var j2 = 0; j2 < local_list_found.length; j2++) {
                        if (arrayofStrings[j1] == local_list_found[j2]) {
                            break;
                        }
                    }
                    if (j2 < local_list_found.length) {
                        continue;
                    }
                    // else add to list_found
                    local_list_found[nbres] = arrayofStrings[j1];
                    nbres++;
                }
            }
        }
        // Now build thrid list made of only ids which are in both lists (local and main)
        var third_list_found = [];
        nbres = 0;
        for (var i = 0; i < list_found.length; i++) {
            for (var j = 0; j < local_list_found.length; j++) {
                if (list_found[i] == local_list_found[j]) {
                    third_list_found[nbres] = list_found[i];
                    nbres++;
                    break;
                }
            }
        }
        // reallocate list_found to that third list
        list_found = third_list_found;
    }

    return nbres;
}

function matchSearch(searchType, keyword1, keyword2, keyflag, valueIn) {
    // Case of string
    if (searchType == "string") {
        if (keyflag != "ON")
            return (valueIn.indexOf(keyword1) >= 0);
        x = valueIn.split(" ");
        for (var i = 0; i < x.length; i++) {
            if (x[i] == keyword1)
                return true;
        }
        return false;
    }
    // Case of number
    if (searchType == "number") {
        x = parseInt(valueIn);
        a = parseInt(keyword1);
        b = parseInt(keyword2);
        return ((keyflag != "ON" && x >= a && x <= b) || (keyflag == "ON" && (x <= a || x >= b)));
    }
    // Case of place
    if (searchType == "place") {
        if (keyflag != "ON")
            return (valueIn.indexOf(keyword1) >= 0);
        x = valueIn.split(",");
        for (var i = 0; i < x.length; i++) {
            if (x[i] == keyword1)
                return true;
        }
        return false;
    }

}

function displayResults(matches) {

    stringRes = "";

    if (matches === 0) {
        presult = document.getElementById("result");
        presult.innerHTML = "";
        return;
    }
    stringRes += "<p class=\"searchdecal\">" + matches + " search_results</p><br>";
    stringRes += "<table width=\"100%\" class=\"scrollTable\" >";
    stringRes += "<thead class=\"fixedHeader\"><tr> <th>searcht_sex</th><th>searcht_id</th><th>searcht_name</th><th>searcht_sosa</th><th>searcht_bdate</th><th>searcht_ddate</th></tr></thead>";
    stringRes += "<tbody class=\"scrollContent\">";
    sexString = "";
    // Scan IDs (start with ID loop first to sort results along ids
    for (var i1 = 0; i1 < ID.length; i1++) {
        for (var i2 = 0; i2 < list_found.length; i2++) {
            if (list_found[i2] == ID[i1]) {
                arrayofStrings = IDdisplay[i1].split("|");
                if (arrayofStrings[0] == 1) {
                    sexString = "<img src=\"../theme/m.gif\" alt=\"Homme\" />";
                } else if (arrayofStrings[0] == 2) {
                    sexString = "<img src=\"../theme/f.gif\" alt=\"Femme\" />";
                } else {
                    sexString = "<img src=\"../theme/u.gif\" alt=\"Sexe inconnu\" />";
                }
                stringRes += "<tr><td style=\"text-align:center;\">" + sexString + "</td><td>" + arrayofStrings[1] + "</td><td><a href=\"../details/personsdetails_" + arrayofStrings[2] + ".html#" + arrayofStrings[1] + "\" target=\"_top\">" + arrayofStrings[3] + "</a></td><td>" + arrayofStrings[4] + "</td><td>" + arrayofStrings[5] + "</td><td>" + arrayofStrings[6] + "</td></tr>";
            }
        }
    }
    stringRes += "</tbody></table>";
    presult = document.getElementById("result");
    presult.innerHTML = stringRes;
}

function removeClassName(elem, className) {
    elem.className = elem.className.replace(className, "").trim();
}

function addCSSClass(elem, className) {
    removeClassName(elem, className);
    elem.className = (elem.className + " " + className).trim();
}

String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/, "");
}

function stripedTable() {
    if (document.getElementById && document.getElementsByTagName) {
        var allTables = document.getElementsByTagName('table');
        if (!allTables) {
            return;
        }

        for (var i = 0; i < allTables.length; i++) {
            if (allTables[i].className.match(/[\w\s ]*scrollTable[\w\s ]*/)) {
                var trs = allTables[i].getElementsByTagName("tr");
                for (var j = 0; j < trs.length; j++) {
                    removeClassName(trs[j], 'alternateRow');
                    addCSSClass(trs[j], 'normalRow');
                }
                for (var k = 0; k < trs.length; k += 2) {
                    removeClassName(trs[k], 'normalRow');
                    addCSSClass(trs[k], 'alternateRow');
                }
            }
        }
    }
}

window.onload = function () {
    stripedTable();
    getParameters();
    processSearch();
}


