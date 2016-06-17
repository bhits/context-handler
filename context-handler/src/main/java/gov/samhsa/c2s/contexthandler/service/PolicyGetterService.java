package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;

import java.util.List;

/**
 * Created by sadhana.chandra on 6/16/2016.
 */
public interface PolicyGetterService {

    public List<PolicyDto> getPolicies(String policyId);
}
