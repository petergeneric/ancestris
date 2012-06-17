<?php

if ($_POST['profil'] != '' && $_POST['code'] != '') {
   $_SESSION['profil'] = "public";
   foreach ($ident as $p => $c) {
      if ($_POST['profil'] == $p && $_POST['code'] == $c) {
         $_SESSION['profil'] = $p;
         }
      }
   }




function authgen() {
   global $authgen;
   return $authgen[$_SESSION['profil']];
}

?>
