package gov.samhsa.c2s.contexthandler.service;

import gov.samhsa.c2s.common.consentgen.ConsentBuilder;
import gov.samhsa.c2s.common.consentgen.ConsentDto;
import gov.samhsa.c2s.common.consentgen.ConsentGenException;
import gov.samhsa.c2s.contexthandler.infrastructure.PcmService;
import gov.samhsa.c2s.contexthandler.service.dto.ConsentXacmlDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyContainerDto;
import gov.samhsa.c2s.contexthandler.service.dto.PolicyDto;
import gov.samhsa.c2s.contexthandler.service.dto.XacmlRequestDto;
import gov.samhsa.c2s.contexthandler.service.exception.NoPolicyFoundException;
import gov.samhsa.c2s.contexthandler.service.exception.PolicyProviderException;
import gov.samhsa.c2s.contexthandler.service.util.PolicyCombiningAlgIds;
import lombok.extern.slf4j.Slf4j;
import org.herasaf.xacml.core.policy.Evaluatable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@ConditionalOnProperty(name = "c2s.context-handler.policy-provider", havingValue = "PcmServicePolicyProviderImpl")
public class PcmServicePolicyProviderImpl implements PolicyProvider {
    private final ConsentBuilder consentBuilder;
    private final PcmService pcmService;
    private final XacmlPolicySetService xacmlPolicySetService;

    @Autowired
    public PcmServicePolicyProviderImpl(ConsentBuilder consentBuilder, PcmService pcmService, XacmlPolicySetService
            xacmlPolicySetService) {
        this.consentBuilder = consentBuilder;
        this.pcmService = pcmService;
        this.xacmlPolicySetService = xacmlPolicySetService;
    }

    @Override
    public List<Evaluatable> getPolicies(XacmlRequestDto xacmlRequest) throws NoPolicyFoundException,
            PolicyProviderException {

        List<PolicyDto> policyDtoList = convertConsentDtoListToXacmlPolicyDtoList(xacmlRequest);

        PolicyContainerDto policyContainerDto = PolicyContainerDto.builder().policies(policyDtoList).build();

        Evaluatable policySet = xacmlPolicySetService.getPoliciesCombinedAsPolicySet(
                policyContainerDto,
                UUID.randomUUID().toString(),
                PolicyCombiningAlgIds.DENY_OVERRIDES.getUrn()
        );

        return Arrays.asList(policySet);
    }

    private List<PolicyDto> convertConsentDtoListToXacmlPolicyDtoList(XacmlRequestDto xacmlRequest) {
        List<PolicyDto> policyDtoList = new ArrayList<>();

        try {
            ConsentXacmlDto consentXacmlDto = (ConsentXacmlDto) pcmService.getXACMLConsent(xacmlRequest);
            PolicyDto policyDto = new PolicyDto();
            policyDto.setId(consentXacmlDto.getConsentRefId());
            policyDto.setPolicy(consentXacmlDto.getConsentXacml().getBytes(StandardCharsets.UTF_8));

            policyDtoList.add(policyDto);

            log.info("Conversion of ConsentDto list to XACML PolicyDto list complete.");
        } catch (ConsentGenException e) {
            log.error("ConsentGenException occurred while trying to convert ConsentDto object(s) to XACML PolicyDto " +
                    "object(s)", e);
            throw new PolicyProviderException("Unable to process FHIR consent(s)", e);
        }

        return policyDtoList;
    }


}
