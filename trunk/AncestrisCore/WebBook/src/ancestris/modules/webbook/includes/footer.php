<div class="spacer">&nbsp;</div>
<hr/>
<div class="legal">
<form name="login" action="" method="post"><p class="left">Profil <?php echo $_SESSION['profil'] == '' ? 'public' : $_SESSION['profil']; ?> | Autre profil:<input type="text" name="profil" size="10" maxlength="10">&nbsp;Code:<input type="password" name="code" size="8" maxlength="8"><button type="submit" value="login">login</button></form></p>
</div>
</body>
</html>
