package gov.samhsa.c2s.contexthandler.service;


import gov.samhsa.mhc.common.url.ResourceUrlProvider;

public interface XacmlXslUrlProvider extends ResourceUrlProvider {
	public abstract String getUrl(XslResource xslResource);
}
