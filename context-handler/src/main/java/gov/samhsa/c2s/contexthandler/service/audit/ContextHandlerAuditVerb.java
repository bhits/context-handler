package gov.samhsa.c2s.contexthandler.service.audit;

import gov.samhsa.c2s.common.audit.AuditVerb;

public enum ContextHandlerAuditVerb implements AuditVerb {
    // PEP-registryStoredQuery (ITI-18)
    REGISTRY_STORED_QUERY_REQUEST,
    REGISTRY_STORED_QUERY_RESPONSE,

    // PEP-retrieveDocumentSet (ITI-43)
    RETRIEVE_DOCUMENT_SET_REQUEST,
    RETRIEVE_DOCUMENT_SET_RESPONSE,

    // XDS-registryStoredQuery (ITI-18)
    XDS_REGISTRY_STORED_QUERY_RESPONSE,
    // XDS-retrieveDocumentSet (ITI-43)
    XDS_RETRIEVE_DOCUMENT_SET_RESPONSE,

    // PDP
    DEPLOY_POLICY,
    EVALUATE_PDP,

    // DocumentSegmentation
    SEGMENT_DOCUMENT,

    // DirectEmailSend
    DIRECT_EMAIL_SEND_REQUEST,
    DIRECT_EMAIL_SEND_RESPONSE,

    // ERROR-DocQuery
    REGISTRY_STORED_QUERY_RESPONSE_ERROR_SYSTEM,
    REGISTRY_STORED_QUERY_RESPONSE_ERROR_CONSENT,
    // ERROR-DocRetrieve
    RETRIEVE_DOCUMENT_SET_RESPONSE_ERROR_SYSTEM,
    RETRIEVE_DOCUMENT_SET_RESPONSE_ERROR_CONSENT,
    // ERROR-DirectEmailSend
    DIRECT_EMAIL_SEND_RESPONSE_ERROR_SYSTEM,
    DIRECT_EMAIL_SEND_RESPONSE_ERROR_CONSENT,;

    @Override
    public String getAuditVerb() {
        return this.toString();
    }
}
