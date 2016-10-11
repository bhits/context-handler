package gov.samhsa.c2s.contexthandler.service;

        import java.util.Map;

public interface PolicyCombiningAlgIdValidator {

    Map<String, String> getCombiningAlgs();

    String validateAndReturn(String policyCombiningAlgId);
}