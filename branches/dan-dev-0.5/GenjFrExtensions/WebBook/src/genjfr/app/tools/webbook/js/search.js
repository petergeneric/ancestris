
find_string = location.href.substring(location.href.indexOf("?")+1);
find_string = unescape(find_string);
arrayofStrings = find_string.split("&");

input_key_fn = "";
input_key_xfn = "";
input_key_ln = "";
input_key_xln = "";
input_key_pl = "";
input_key_xpl = "";
input_key_id = "";
input_key_xid = "";
input_key_so = "";
input_key_xso = "";
input_key_1bi = "";
input_key_2bi = "";
input_key_xbi = "";
input_key_1ma = "";
input_key_2ma = "";
input_key_xma = "";
input_key_1de = "";
input_key_2de = "";
input_key_xde = "";
input_andor = "";

input_valid = "0";

var list_found=[];


for (var i=0 ; i < arrayofStrings.length ; i++) {
   if(arrayofStrings[i].substring(0,6)=="key_fn") {
      input_key_fn = arrayofStrings[i].substring(7);
      if (input_key_fn != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xfn") {
      input_key_xfn = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,6)=="key_ln") {
      input_key_ln = arrayofStrings[i].substring(7);
      if (input_key_ln != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xln") {
      input_key_xln = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,6)=="key_pl") {
      input_key_pl = arrayofStrings[i].substring(7);
      if (input_key_pl != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xpl") {
      input_key_xpl = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,6)=="key_id") {
      input_key_id = arrayofStrings[i].substring(7);
      if (input_key_id != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xid") {
      input_key_xid = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,6)=="key_so") {
      input_key_so = arrayofStrings[i].substring(7);
      if (input_key_so != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xso") {
      input_key_xso = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,7)=="key_1bi") {
      input_key_1bi = arrayofStrings[i].substring(8);
      if (input_key_1bi != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_2bi") {
      input_key_2bi = arrayofStrings[i].substring(8);
      if (input_key_2bi != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xbi") {
      input_key_xbi = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,7)=="key_1ma") {
      input_key_1ma = arrayofStrings[i].substring(8);
      if (input_key_1ma != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_2ma") {
      input_key_2ma = arrayofStrings[i].substring(8);
      if (input_key_2ma != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xma") {
      input_key_xma = arrayofStrings[i].substring(8).toUpperCase();
      }

   if(arrayofStrings[i].substring(0,7)=="key_1de") {
      input_key_1de = arrayofStrings[i].substring(8);
      if (input_key_1de != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_2de") {
      input_key_2de = arrayofStrings[i].substring(8);
      if (input_key_2de != "") input_valid = "1";
      }
   if(arrayofStrings[i].substring(0,7)=="key_xde") {
      input_key_xde = arrayofStrings[i].substring(8).toUpperCase();
      }


   if(arrayofStrings[i].substring(0,5)=="andor") {
      input_andor = arrayofStrings[i].substring(6).toUpperCase();
      }
   }



function processSearch() {

	nbResults=0;

	if (input_valid == "0") {
		input_key_fn = document.searchInputForm.key_fn.value;
		input_key_xfn = document.searchInputForm.key_xfn.checked ? document.searchInputForm.key_xfn.value.toUpperCase() : "OFF";
		input_key_ln = document.searchInputForm.key_ln.value;
		input_key_xln = document.searchInputForm.key_xln.checked ? document.searchInputForm.key_xln.value.toUpperCase() : "OFF";
		input_key_pl = document.searchInputForm.key_pl.value;
		input_key_xpl = document.searchInputForm.key_xpl.checked ? document.searchInputForm.key_xpl.value.toUpperCase() : "OFF";
		input_key_id = document.searchInputForm.key_id.value;
		input_key_xid = document.searchInputForm.key_xid.checked ? document.searchInputForm.key_xid.value.toUpperCase() : "OFF";
		input_key_so = document.searchInputForm.key_so.value;
		input_key_xso = document.searchInputForm.key_xso.checked ? document.searchInputForm.key_xso.value.toUpperCase() : "OFF";
		input_key_1bi = document.searchInputForm.key_1bi.value;
		input_key_2bi = document.searchInputForm.key_2bi.value;
		input_key_xbi = document.searchInputForm.key_xbi.checked ? document.searchInputForm.key_xbi.value.toUpperCase() : "OFF";
		input_key_1ma = document.searchInputForm.key_1ma.value;
		input_key_2ma = document.searchInputForm.key_2ma.value;
		input_key_xma = document.searchInputForm.key_xma.checked ? document.searchInputForm.key_xma.value.toUpperCase() : "OFF";
		input_key_1de = document.searchInputForm.key_1de.value;
		input_key_2de = document.searchInputForm.key_2de.value;
		input_key_xde = document.searchInputForm.key_xde.checked ? document.searchInputForm.key_xde.value.toUpperCase() : "OFF";

		input_andor = document.searchInputForm.andor[0].checked ? document.searchInputForm.andor[0].value.toUpperCase() : document.searchInputForm.andor[1].value.toUpperCase();

		if (input_key_fn != "") input_valid = "1";
		if (input_key_ln != "") input_valid = "1";
		if (input_key_pl != "") input_valid = "1";
		if (input_key_id != "") input_valid = "1";
		if (input_key_so != "") input_valid = "1";
		if (input_key_1bi != "") input_valid = "1";
		if (input_key_2bi != "") input_valid = "1";
		if (input_key_1ma != "") input_valid = "1";
		if (input_key_2ma != "") input_valid = "1";
		if (input_key_1de != "") input_valid = "1";
		if (input_key_2de != "") input_valid = "1";
		}

	if (input_valid == "0") {
   	   document.write ("<p class=\"searchdecal\">search_please</p>");
	   return;
	   }

	if (input_valid == "1") {
	   document.searchInputForm.key_fn.value = input_key_fn;
	   if (input_key_xfn == "ON") document.searchInputForm.key_xfn.checked = true;
	   document.searchInputForm.key_ln.value = input_key_ln;
	   if (input_key_xln == "ON") document.searchInputForm.key_xln.checked = true;
	   document.searchInputForm.key_pl.value = input_key_pl;
	   if (input_key_xpl == "ON") document.searchInputForm.key_xpl.checked = true;
	   document.searchInputForm.key_id.value = input_key_id;
	   if (input_key_xid == "ON") document.searchInputForm.key_xid.checked = true;
	   document.searchInputForm.key_so.value = input_key_so;
	   if (input_key_xso == "ON") document.searchInputForm.key_xso.checked = true;
	   document.searchInputForm.key_1bi.value = input_key_1bi;
	   document.searchInputForm.key_2bi.value = input_key_2bi;
	   if (input_key_xbi == "ON") document.searchInputForm.key_xbi.checked = true;
	   document.searchInputForm.key_1ma.value = input_key_1ma;
	   document.searchInputForm.key_2ma.value = input_key_2ma;
	   if (input_key_xma == "ON") document.searchInputForm.key_xma.checked = true;
	   document.searchInputForm.key_1de.value = input_key_1de;
	   document.searchInputForm.key_2de.value = input_key_2de;
	   if (input_key_xde == "ON") document.searchInputForm.key_xde.checked = true;
	   if (input_andor == "AND") document.searchInputForm.andor[0].checked = true; else document.searchInputForm.andor[1].checked = true;
           }

	if (input_valid == "1") {
	   nbResults=0;
	   first = "1";

	   if (input_key_fn != "") {
	      nbResults = recherche("string", input_key_fn, "", input_key_xfn, list_firstnames, list_firstnamesID, first);
	      first = "0";
	      }
	   if (input_key_ln != "") {
	      nbResults = recherche("string", input_key_ln, "",  input_key_xln, list_lastnames, list_lastnamesID, first);
	      first = "0";
	      }
	   if (input_key_pl != "") {
	      nbResults = recherche("place", input_key_pl, "",  input_key_xpl, list_places, list_placesID, first);
	      first = "0";
	      }
	   if (input_key_id != "") {
	      nbResults = recherche("string", input_key_id, "",  input_key_xid, list_ids, list_idsID, first);
	      first = "0";
	      }
	   if (input_key_so != "") {
	      nbResults = recherche("string", input_key_so, "",  input_key_xso, list_sosas, list_sosasID, first);
	      first = "0";
	      }
	   if (input_key_1bi != "") {
	      nbResults = recherche("number", input_key_1bi, input_key_2bi, input_key_xbi, list_births, list_birthsID, first);
	      first = "0";
	      }
	   if (input_key_1ma != "") {
	      nbResults = recherche("number", input_key_1ma, input_key_2ma, input_key_xma, list_marriages, list_marriagesID, first);
	      first = "0";
	      }
	   if (input_key_1de != "") {
	      nbResults = recherche("number", input_key_1de, input_key_2de, input_key_xde, list_deaths, list_deathsID, first);
	      first = "0";
	      }
	   displayResults(nbResults);
	   }
}


function recherche(searchType, keyword1, keyword2, keyflag, list, list_id, init) {
   var nbres=0;

   keyword1 = keyword1.toUpperCase();
   keyword2 = keyword2.toUpperCase();

   if ((input_andor == "OR") || (init == "1")) {
      nbres = list_found.length;
      // scan list
      for (var i = 0 ; i < list.length ; i++) {
         // if criteria matched
         if (matchSearch(searchType, keyword1, keyword2, keyflag, list[i])) {
            // build array of ids that match from list of ids
            arrayofStrings = list_id[i].split("|");
            // scan array
            for (var j1=0 ; j1 < arrayofStrings.length ; j1++) {
               // if already in list_found, continue...
               for (var j2 = 0 ; j2 < list_found.length ; j2++) {
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

   if ((input_andor == "AND") && (init == "0")) {
      var local_list_found=[];
      nbres = 0;
      // scan list
      for (var i = 0 ; i < list.length ; i++) {
         // if keyword found in item i
         if (matchSearch(searchType, keyword1, keyword2, keyflag, list[i])) {
            // build array of ids that match from list of ids
            arrayofStrings = list_id[i].split("|");
            // scan array
            for (var j1=0 ; j1 < arrayofStrings.length ; j1++) {
               // if already in list_found, continue...
               for (var j2 = 0 ; j2 < local_list_found.length ; j2++) {
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
      var third_list_found=[];
      nbres = 0;
      for (var i = 0 ; i < list_found.length ; i++) {
         for (var j = 0 ; j < local_list_found.length ; j++) {
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
      if (keyflag != "ON") return (valueIn.indexOf(keyword1) >= 0);
      x = valueIn.split(" ");
      for (var i = 0 ; i < x.length ; i++) {
         if (x[i] == keyword1) return true;
         }
      return false;
      }
   // Case of number
   if (searchType == "number") {
      x = parseInt(valueIn);
      a = parseInt(keyword1);
      b = parseInt(keyword2);
      return ( (keyflag != "ON" && x >= a && x <= b) || (keyflag == "ON" && (x <= a || x >= b)) );
      }
   // Case of place
   if (searchType == "place") {
      if (keyflag != "ON") return (valueIn.indexOf(keyword1) >= 0);
      x = valueIn.split(",");
      for (var i = 0 ; i < x.length ; i++) {
         if (x[i] == keyword1) return true;
         }
      return false;
      }

   }

function displayResults(matches) {

   stringRes = "";

   if (matches == 0) {
      return;
   }
   stringRes += "<p class=\"searchdecal\">" + matches + " search_results</p><br>";
   stringRes += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\"  class=\"scrollTable\" >";
   stringRes += "<thead class=\"fixedHeader\"><tr> <th>searcht_sex</th><th>searcht_id</th><th>searcht_name</th><th>searcht_sosa</th><th>searcht_bdate</th><th>searcht_ddate</th></tr></thead>";
   stringRes += "<tbody class=\"scrollContent\">";
   sexString = "";
   // Scan IDs (start with ID loop first to sort results along ids
   for (var i1 = 0 ; i1 < ID.length ; i1++) {
      for (var i2 = 0 ; i2 < list_found.length ; i2++) {
         if (list_found[i2] == ID[i1]) {
            arrayofStrings = IDdisplay[i1].split("|");
            if (arrayofStrings[0] == 1) {
               sexString = "<img src=\"../theme/m.gif\" alt=\"Homme\" />";
               }
            else if (arrayofStrings[0] == 2) {
               sexString = "<img src=\"../theme/f.gif\" alt=\"Femme\" />";
               }
            else  {
               sexString = "<img src=\"../theme/u.gif\" alt=\"Sexe inconnu\" />";
               }
            stringRes += "<tr><td>"+sexString+"</td><td>"+arrayofStrings[1]+"</td><td><a href=\"../details/personsdetails_"+arrayofStrings[2]+".html#"+arrayofStrings[1]+"\" target=\"_top\">"+arrayofStrings[3]+"</a></td><td>"+arrayofStrings[4]+"</td><td>"+arrayofStrings[5]+"</td><td>"+arrayofStrings[6]+"</td></tr>";
            }
         }
      }
   stringRes += "</tbody></table>";

   document.write(stringRes);
   return stringRes;
}

function removeClassName (elem, className) {
	elem.className = elem.className.replace(className, "").trim();
}

function addCSSClass (elem, className) {
	removeClassName (elem, className);
	elem.className = (elem.className + " " + className).trim();
}

String.prototype.trim = function() {
	return this.replace( /^\s+|\s+$/, "" );
}

function stripedTable() {
	if (document.getElementById && document.getElementsByTagName) {
		var allTables = document.getElementsByTagName('table');
		if (!allTables) { return; }

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

window.onload = function() { stripedTable(); }


