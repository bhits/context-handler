package gov.samhsa.c2s.contexthandler.service.audit;

import gov.samhsa.c2s.common.audit.PredicateKey;

public enum ContextHandlerPredicateKey implements PredicateKey {
    PATIENT_UNIQUE_ID,
    RECIPIENT_SUBJECT_NPI,
    INTERMEDIARY_SUBJECT_NPI,
    PURPOSE_OF_USE,
    XACML_POLICY,
    XACML_POLICY_ID,
    REQUEST_BODY,
    RESPONSE_BODY,
    SECTION_OBLIGATIONS_APPLIED,
    CATEGORY_OBLIGATIONS_APPLIED,
    ORIGINAL_DOCUMENT,
    SEGMENTED_DOCUMENT,
    ORIGINAL_DOCUMENT_VALID,
    SEGMENTED_DOCUMENT_VALID,
    RULES_FIRED,
    DOMAIN_ID,
    POLICY_ID,
    STATUS;

    @Override
    public String getPredicateKey() {
        return this.toString();
    }

}
