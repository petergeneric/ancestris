<?php

	/////////////////////////////////////////////////
	// Open database connection
	function openDB() {
		// read password
		$credentials = file("geoq.ini");
		if (!$credentials) die("error:credentials");
		$host = trim($credentials[0]);
		$db = trim($credentials[1]);
		$user = trim($credentials[2]);
		$pass = trim($credentials[3]);
		// connect to database
		mysql_connect($host, $user, $pass)
			or die("error:connect " . mysql_error());
		mysql_select_db($db) 
			or die("error:db");
		// set utf8
		mysql_query('SET NAMES utf8');
		mysql_query('SET CHARACTER SET utf8');
		// done
	}

	////////////////////////////////////////////////
	// Track user request
	function track($token, $hits) {

		// lookup current ip
		$ip = $_SERVER['REMOTE_ADDR'];

		// update track
		mysql_query("INSERT INTO tracking VALUES (\"$ip\",1,NULL,$hits,\"$token\") ON DUPLICATE KEY UPDATE requests=requests+1, hits=hits+$hits, token=\"$token\"");

		// done
	}

	///////////////////////////////////////////////
	// translate jurisdiction into a valid fips code 
	function findJurisdiction($jurisdictions, $country) {
		
		$jurisdiction = "";
		
		// loop for well known jurisdictions? 
		for ($j=0 ; $j<count($jurisdictions) ; $j++) {

			// stop at first empty jurisdiction	
			$jurisdiction = trim($jurisdictions[$j]);
			if ($jurisdiction=="") break;

			// prepare query for matching names
			$jquery = "SELECT country, jurisdiction FROM jurisdictions WHERE name = \"$jurisdiction\"";
	
			// add country qualifier if available
			if (strlen($country)>0)
	  		$jquery = "$jquery AND country = \"$country\"";
	
			// look for first matching jurisdiction
			$rows = mysql_query($jquery);
			if (!$rows) die("error:select jurisdictions failed");
 			if (mysql_num_rows($rows)==1) {
	  		$row = mysql_fetch_row($rows);
	  		$jurisdiction = $row[1];
  			$country = $row[0]; // always grab the correct country at this point
	  		$j = count($jurisdictions);
 			} else {
				$jurisdiction = "";	
 			}
			mysql_free_result($rows);
			
			// try next jurisdiction
		}
		
		// done
		return $jurisdiction;
	}
	
	///////////////////////////////////////////////
	// query for a location 
	function processQuery($city, $jurisdiction, $country, $op) {
		
		$hits = 0;
		
		// prepare location query "city, jurisdiction name, country, lat, lon"
		$lquery = 
			"SELECT locations.city, jurisdictions.name, locations.country, locations.lat, locations.lon " .
			"FROM locations LEFT JOIN jurisdictions ON jurisdictions.jurisdiction=locations.jurisdiction AND jurisdictions.country=locations.country AND jurisdictions.preferred=1 " .
			"WHERE locations.city".$op."\"$city\"";
				
		$retry = TRUE;
		while ($retry) {
			
			$retry = FALSE;
			$sql = $lquery;
			if (strlen($jurisdiction)>0) {
	  		$sql = "$sql AND locations.jurisdiction=\"$jurisdiction\"";
	  		$retry = TRUE;
	  		$jurisdiction = "";
			}
			if (strlen($country)>0) {
				$sql = "$sql AND locations.country = \"$country\"";
				if (!$retry) $country = "";
				$retry = TRUE;
			}
			$sql = "$sql ORDER BY locations.country, jurisdictions.name, locations.city";
			
			$rows = mysql_query($sql);
			if (!$rows) die("error:select locations failed");
			if (mysql_num_rows($rows)>0) {
				$retry = FALSE;
				for ($i=0 ; $row = mysql_fetch_row($rows) ; $i++) {
					if ($i>0) echo ";";
					echo "$row[0],$row[1],$row[2],$row[3],$row[4]";
					$hits++;
				}
			}
			mysql_free_result($rows);

			// try once more
		}
		
		// nothing found?	
		if ($hits==0) echo "?";
		
		// done
		return $hits;
	}
	
	///////////////////////////////////////////////
	// Parse Input and Respond
	function processInput($in) {

		$hits = 0;

		// read stdinput - lines "city,jurisdiction,jurisdiction,...,country" by one
		for ($l=0 ; ($tokens=fgetcsv($in, 100, ",")) !== FALSE ; $l++) {
	
			// newline?
			if ($l>0) echo "\n";

			// at least 3 tokens?
			if (count($tokens)<3) continue;

			// grab city (first token)
			$city = trim($tokens[0]);
			if (strstr($city, "\"")!=FALSE) continue;
			$like = rtrim($city, "*");
			if (strlen($like)<3) continue;
		
			// equals or LIKE?
			$op = "=";
			if ($like!=$city) {
				$city = "$like%";
				$op = " LIKE ";	
			}

			// check what country we're looking for
			$country = trim($tokens[count($tokens)-1]);
			
			// check what jurisdiction we're looking for
			$jurisdiction = findJurisdiction(array_slice($tokens, 1, count($tokens)-2), &$country);
			
			// query it now
			$hits += processQuery($city, $jurisdiction, $country, $op);
			
			// next input line
		}
		
		// done
		return $hits;
	}

	///////////////////////////////////////////////
	// MAIN

	// read stdinput - check header
	$in = fopen("php://input", "rb");
       $header = explode(":", trim(fgets($in)), 2);
	if ($header[0]!="GEOQ") die("PING");

	// open database
	openDB();

	// parse input
	$hits = processInput($in);

	// close input
	fclose($in);

	// track it
	track($header[1],$hits);

	// close database
	mysql_close();

?>