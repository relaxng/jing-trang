<?xml version="1.0"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:strip-space elements="xsdtest datatype equiv class"/>

<xsl:output indent="yes" encoding="utf-8"/>

<xsl:template match="xsdtest">
  <testSuite>
    <xsl:apply-templates/>
  </testSuite>
</xsl:template>

<xsl:template match="datatype">
<testCase>
<requires datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes"/>
<correct>
<element xmlns="http://relaxng.org/ns/structure/1.0" name="doc"
         datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
  <data type="{@name}">
    <xsl:for-each select="param">
      <param name="{@name}"><xsl:value-of select="."/></param>
    </xsl:for-each>
  </data>
</element>
</correct>
<xsl:apply-templates select="valid|invalid"/>
</testCase>
<xsl:apply-templates select="equiv/class|length"/>
</xsl:template>

<xsl:template match="valid">
 <valid><doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc></valid>
</xsl:template>

<xsl:template match="invalid">
 <invalid><doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc></invalid>
</xsl:template>

<xsl:template match="class">
<testCase>
<correct>
<element xmlns="http://relaxng.org/ns/structure/1.0" name="doc"
         datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
  <value>
    <xsl:copy-of select="value[1]/namespace::*"/>
    <xsl:attribute name="type"><xsl:value-of select="../../@name"/></xsl:attribute>
    <xsl:value-of select="value[1]"/>
  </value>
</element>
</correct>
<xsl:for-each select="value[position() != 1]">
  <valid><doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc></valid>
</xsl:for-each>
<xsl:for-each select="preceding-sibling::class/value|following-sibling::class/value">
  <invalid><doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc></invalid>
</xsl:for-each>
</testCase>
</xsl:template>

<xsl:template match="length">
<testCase>
<correct>
<element xmlns="http://relaxng.org/ns/structure/1.0" name="doc"
         datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
  <data type="{../@name}">
    <param name="length"><xsl:value-of select="@value"/></param>
  </data>
</element>
</correct>
<valid>
<doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc>
</valid>
</testCase>

<testCase>
<correct>
<element xmlns="http://relaxng.org/ns/structure/1.0" name="doc"
         datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
  <data type="{../@name}">
    <param name="length"><xsl:value-of select="@value + 1"/></param>
  </data>
</element>
</correct>
<invalid>
<doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc>
</invalid>
</testCase>

<xsl:if test="@value != 0">
  <testCase>
  <correct>
  <element xmlns="http://relaxng.org/ns/structure/1.0" name="doc"
	   datatypeLibrary="http://www.w3.org/2001/XMLSchema-datatypes">
    <data type="{../@name}">
      <param name="length"><xsl:value-of select="@value - 1"/></param>
    </data>
  </element>
  </correct>
  <invalid>
  <doc><xsl:copy-of select="namespace::*"/><xsl:value-of select="."/></doc>
  </invalid>
  </testCase>
</xsl:if>

</xsl:template>

</xsl:stylesheet>
