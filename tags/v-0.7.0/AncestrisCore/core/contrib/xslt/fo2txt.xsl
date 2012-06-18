<?xml version='1.0' encoding='ISO-8859-1'?>

<!-- =============================================================== -->
<!--                                                                 -->
<!-- Convert XSL FO to TXT                                           -->
<!--                                                                 -->
<!-- Author: Nils Meier, nmeier at users dot sourceforge dot net     -->
<!--                                                                 -->
<!-- =============================================================== -->

<!DOCTYPE xsl:stylesheet [
  <!ENTITY anchor "<xsl:apply-templates select='@id' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'/>">
  <!ENTITY newline "<xsl:text>&#xA;</xsl:text>">
]>

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 xmlns:genj="http://genj.sourceforge.net/XSL/Format"
 exclude-result-prefixes="fo">
 
<xsl:output method="text" encoding="utf-8" indent="no"/>

<!-- =============================================================== -->
<!-- A simple block - add a leading space on @start-indent, recurse  -->
<!-- and add a newline                                               -->
<!-- =============================================================== -->
<xsl:template match="fo:block">
 <xsl:if test="@start-indent">
  <xsl:text> </xsl:text>
 </xsl:if>
 <xsl:apply-templates/>
 <xsl:if test=".!=''">
  &newline;
 </xsl:if>
</xsl:template>

<!-- =============================================================== -->
<!-- A table                                                         -->
<!-- =============================================================== -->
<xsl:template match="fo:table">
</xsl:template>
 
<!-- =============================================================== -->
<!-- A list item                                                     -->
<!-- =============================================================== -->
<xsl:template match="fo:list-item-label">
 <xsl:value-of select="'+'"/>
</xsl:template>
 
<!-- =============================================================== -->
<!-- A text node - copied                                            -->
<!-- =============================================================== -->
<xsl:template match="text()">
 <xsl:value-of select="."/>
</xsl:template>
 
</xsl:stylesheet>