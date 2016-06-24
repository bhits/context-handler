<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs"
                version="2.0">

    <xsl:variable name="currentDateTimeUtc" select="string(current-dateTime())"/>

    <xsl:template match="/">
        <Request xmlns="urn:oasis:names:tc:xacml:2.0:context:schema:os"     xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
            <Subject>
            <xsl:for-each select="PdpRequest/SubjectAttributes">
                <Attribute AttributeId="{attributeId}"   DataType="{attributeType}"   >
                    <AttributeValue><xsl:value-of select="attributeValue" /></AttributeValue>
                </Attribute>
            </xsl:for-each>
            </Subject>

            <Resource>
                <xsl:for-each select="PdpRequest/ResourceAttributes">
                    <Attribute AttributeId="{attributeId}"   DataType="{attributeType}" >
                        <AttributeValue><xsl:value-of select="attributeValue" /></AttributeValue>
                    </Attribute>
                </xsl:for-each>
            </Resource>

            <Action>
                <xsl:for-each select="PdpRequest/ActionAttributes">
                    <Attribute AttributeId="{attributeId}"   DataType="{attributeType}" >
                        <AttributeValue><xsl:value-of select="attributeValue" /></AttributeValue>
                    </Attribute>
                </xsl:for-each>
            </Action>

            <Environment>
                <xsl:for-each select="PdpRequest/EnvironmentAttributes">
                    <Attribute AttributeId="{attributeId}"   DataType="{attributeType}" >
                        <AttributeValue><xsl:value-of select="$currentDateTimeUtc" /></AttributeValue>
                    </Attribute>
                </xsl:for-each>
            </Environment>

        </Request>
    </xsl:template>

</xsl:stylesheet>