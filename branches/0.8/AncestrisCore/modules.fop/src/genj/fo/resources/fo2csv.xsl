<?xml version='1.0' encoding='ISO-8859-1'?>

<!-- =============================================================== -->
<!--                                                                 -->
<!-- Convert XSL FO to CSV (comma separated values)                  -->
<!--                                                                 -->
<!-- Author: Nils Meier, nmeier at users dot sourceforge dot net     -->
<!--                                                                 -->
<!-- =============================================================== -->

<xsl:stylesheet version="1.0"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:fo="http://www.w3.org/1999/XSL/Format"
 xmlns:genj="http://genj.sourceforge.net/XSL/Format"
 exclude-result-prefixes="fo">
 
<xsl:output method="text" encoding="utf-8" indent="no"/>

<xsl:template match="fo:table">
  <xsl:if test="@genj:csv='true'">
   <xsl:apply-templates/>
  </xsl:if>
</xsl:template>

<xsl:template match="fo:table-row">
 <xsl:if test="../../@genj:csvprefix"><xsl:value-of select="../../@genj:csvprefix"/>;</xsl:if>
 <xsl:apply-templates select="fo:table-cell"/>
 <xsl:value-of select="'&#xA;'"/>
</xsl:template>
 
<xsl:template match="fo:table-cell">
 <xsl:value-of select="."/>
 <xsl:if test="position()!=last()">;</xsl:if>
</xsl:template>
 
<xsl:template match="text()">
</xsl:template>
 
</xsl:stylesheet>