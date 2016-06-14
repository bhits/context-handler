package gov.samhsa.c2s.contexthandler.service;

public enum XslResource {

	CDAR2XSLNAME("c2cdar2.xsl"), XACMLXSLNAME("c2xacml.xsl"), XACMLPDFCONSENTFROMXSLNAME(
			"c2xacmlpdfConsentFrom.xsl"), XACMLPDFCONSENTTOXSLNAME(
			"c2xacmlpdfConsentTo.xsl"), PDPREQUESTXSLNAME(
					"pdpRequest.xsl");

	private String fileName;

	XslResource(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return this.fileName;
	}
}
