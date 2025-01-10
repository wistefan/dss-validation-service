package com.ficodes.iam;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ficodes.iam.domain.CrlConfig;
import com.ficodes.iam.domain.FakeOnlineSource;
import com.ficodes.iam.domain.TestSignature;
import eu.europa.esig.dss.crl.CRLBinary;
import eu.europa.esig.dss.crl.CRLUtils;
import eu.europa.esig.dss.enumerations.TokenExtractionStrategy;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLToken;
import eu.europa.esig.dss.ws.dto.RemoteDocument;
import eu.europa.esig.dss.ws.validation.dto.DataToValidateDTO;
import eu.europa.esig.dss.ws.validation.dto.WSReportsDTO;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Clock;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
@RequiredArgsConstructor
class ValidationServiceTest {

	private final static List<TestSignature> testSignatures = new ArrayList<>();
	private final ValidationService validationService;
	private final FakeOnlineSource fakeOnlineSource = new FakeOnlineSource();


	@BeforeAll
	public static void initializeTestData() throws IOException {
		File signaturesFile = new File(ValidationServiceTest.class.getClassLoader().getResource("testSignatures.json").getFile());
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.readValue(
						signaturesFile,
						new TypeReference<List<TestSignature>>() {
						})
				.forEach(testSignatures::add);
	}


	@MockBean(CRLSource.class)
	public CRLSource crlSource() throws IOException {
		return fakeOnlineSource;
	}

	@ParameterizedTest
	@MethodSource("provideSignaturesToTest")
	public void test(byte[] signature, int signatureCount, int validSignatures, List<CrlConfig> crlConfigs, String message) throws IOException {
		initializeCRLs(crlConfigs);

		RemoteDocument remoteDocument = new RemoteDocument(signature, "RemoteDocument");
		DataToValidateDTO dataToValidateDTO = new DataToValidateDTO();
		dataToValidateDTO.setSignedDocument(remoteDocument);
		dataToValidateDTO.setTokenExtractionStrategy(TokenExtractionStrategy.NONE);
		WSReportsDTO reportsDTO = validationService.validateSignature(dataToValidateDTO, Optional.of(false), Optional.of(false));
		assertNotNull(reportsDTO.getSimpleReport(), "The simple report should be returned.");
		XmlSimpleReport simpleReport = reportsDTO.getSimpleReport();
		assertEquals(signatureCount, simpleReport.getSignaturesCount(), message);
		assertEquals(validSignatures, simpleReport.getValidSignaturesCount(), message);
	}

	private void initializeCRLs(List<CrlConfig> crlConfigs) throws IOException {
		Map<String, CRLBinary> crlBinaryMap = new HashMap<>();
		// we want the test to fail with exception, thus no streaming
		for (CrlConfig crlConfig : crlConfigs) {
			File crlFile = new File(ValidationServiceTest.class.getClassLoader().getResource("crl.pem").getFile());
			byte[] crlBytes = Files.readAllBytes(crlFile.toPath());
			CRLBinary crlBinary = CRLUtils.buildCRLBinary(crlBytes);
			crlBinaryMap.put(crlConfig.getUrl(), crlBinary);
		}
		fakeOnlineSource.setCrlBinaries(crlBinaryMap);
	}

	private static Stream<Arguments> provideSignaturesToTest() {
		return testSignatures.stream()
				.map(testSignature ->
						Arguments.of(testSignature.getSignature(), testSignature.getSignatureCount(), testSignature.getValidSignatures(), testSignature.getCrls(), testSignature.getMessage())
				);
	}

}