<?xml version="1.0"?>
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://icl.com/saxon"
  extension-element-prefixes="saxon">

<xsl:output method="text"/>

<xsl:param name="dir" select="'.'"/>

<xsl:template match="/">
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$dir"/>
  </xsl:call-template>
  <xsl:apply-templates select="//testCase"/>
</xsl:template>

<xsl:variable name="incorrectSchemaName" select="'i'"/>
<xsl:variable name="correctSchemaName" select="'c'"/>
<xsl:variable name="xmlSuffix" select="'.rng'"/>
<xsl:variable name="nonxmlSuffix" select="'.rnx'"/>
<xsl:variable name="xmlDir" select="'xml'"/>
<xsl:variable name="nonxmlDir" select="'nonxml'"/>
<xsl:variable name="outDir" select="'out'"/>

<xsl:template match="testCase">
  <xsl:variable name="b" select="concat($dir, '/', format-number(position(),'000'))"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$b"/>
  </xsl:call-template>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="concat($b, '/', $outDir)"/>
  </xsl:call-template>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$b"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="xml//resource">
  <xsl:param name="base"/>
  <saxon:output href="{$base}/{@name}" method="xml">
    <xsl:copy-of select="node()"/>
  </saxon:output>
</xsl:template>

<xsl:template match="xml//correct">
  <xsl:param name="base"/>
  <saxon:output href="{$base}/{$correctSchemaName}{$xmlSuffix}" method="xml">
    <xsl:copy-of select="node()"/>
  </saxon:output>
</xsl:template>

<xsl:template match="nonxml//incorrect">
  <xsl:param name="base"/>
  <saxon:output href="{$base}/{$incorrectSchemaName}{$nonxmlSuffix}" method="text">
    <xsl:value-of select="."/>
  </saxon:output>
</xsl:template>

<xsl:template match="nonxml//correct">
  <xsl:param name="base"/>
  <saxon:output href="{$base}/{$correctSchemaName}{$nonxmlSuffix}" method="text">
    <xsl:value-of select="."/>
  </saxon:output>
</xsl:template>

<xsl:template match="nonxml//resource">
  <xsl:param name="base"/>
  <saxon:output href="{$base}/{@name}" method="text" encoding="utf-8">
    <xsl:value-of select="."/>
  </saxon:output>
</xsl:template>

<xsl:template match="nonxml">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', $nonxmlDir)"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$d"/>
  </xsl:call-template>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="xml">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', $xmlDir)"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$d"/>
  </xsl:call-template>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="dir">
  <xsl:param name="base"/>
  <xsl:variable name="d" select="concat($base, '/', @name)"/>
  <xsl:call-template name="mkdir">
    <xsl:with-param name="dir" select="$d"/>
  </xsl:call-template>
  <xsl:apply-templates select="*">
    <xsl:with-param name="base" select="$d"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template name="mkdir">
  <xsl:param name="dir"/>
  <xsl:value-of select="substring(File:mkdir(File:new($dir)),0,0)" 
                xmlns:File="java:java.io.File"/>
</xsl:template>

</xsl:stylesheet>
