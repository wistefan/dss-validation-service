package com.ficodes.iam;

import eu.europa.esig.dss.ws.validation.common.RemoteDocumentValidationService;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Implementation of the validate signature endpoint
 */
@RequiredArgsConstructor
@Slf4j
@Controller("${general.basepath:/}")
public class ValidationService {

	private final RemoteDocumentValidationService validationService;

	/**
	 * @param dataToValidate document to be used for validation
	 * @param detailed       should the detailed report be included in the response
	 * @param diagnostic     should the diagnostic data be included in the response
	 */
	@Post("validateSignature")
	public WSReportsDTO validateSignature(@Body DataToValidateDTO dataToValidate, @QueryValue("detailed") Optional<Boolean> detailed, @QueryValue("diagnostic") Optional<Boolean> diagnostic) {
		boolean includeDetailed = detailed.orElse(false);
		boolean includeDiagnosticData = diagnostic.orElse(false);
		WSReportsDTO reportsDTO = validationService.validateDocument(dataToValidate);
		if (!includeDetailed) {
			reportsDTO.setDetailedReport(null);
		}
		if (!includeDiagnosticData) {
			reportsDTO.setDiagnosticData(null);
		}
		// validation report is not required in most cases and sometimes exceeds maximum depth for serialization
		reportsDTO.setValidationReportDataHandler(null);
		reportsDTO.setValidationReport(null);
		return reportsDTO;
	}

}
