package com.ficodes.iam;

import eu.europa.esig.dss.model.x509.revocation.crl.CRL;
import eu.europa.esig.dss.model.x509.revocation.ocsp.OCSP;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.service.ocsp.OnlineOCSPSource;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.validation.CertificateVerifier;
import eu.europa.esig.dss.spi.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.aia.AIASource;
import eu.europa.esig.dss.spi.x509.aia.DefaultAIASource;
import eu.europa.esig.dss.spi.x509.revocation.RevocationSource;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLSource;
import eu.europa.esig.dss.spi.x509.revocation.ocsp.OCSPSource;
import eu.europa.esig.dss.ws.validation.common.RemoteDocumentValidationService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

import java.util.List;

@Factory
public class Application {

	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}

	@Singleton
	public TrustedListsCertificateSource trustedListsCertificateSource() {
		return new TrustedListsCertificateSource();
	}

	@Singleton
	public CRLSource crlSource() {
		return new OnlineCRLSource();
	}

	@Singleton
	public OCSPSource ocspSource() {
		return new OnlineOCSPSource();
	}

	@Singleton
	public AIASource aiaSource() {
		return new DefaultAIASource();
	}

	@Singleton
	public RemoteDocumentValidationService validationService(TrustSources trustSources, CRLSource crlSource, OCSPSource ocspSource, AIASource aiaSource) {
		if (trustSources.getCommonTrustedCertificateSources() == null || trustSources.getCommonTrustedCertificateSources().isEmpty()) {
			throw new IllegalArgumentException("No trusted certificate sources configured.");
		}

		List<CommonTrustedCertificateSource> commonTrustedCertificateSources = trustSources.getCommonTrustedCertificateSources();
		CommonTrustedCertificateSource[] tcsArray = new CommonTrustedCertificateSource[commonTrustedCertificateSources.size()];
		tcsArray = commonTrustedCertificateSources.toArray(tcsArray);

		RemoteDocumentValidationService remoteDocumentValidationService = new RemoteDocumentValidationService();

		RevocationSource<OCSP> revocationOcspCRL = ocspSource;
		RevocationSource<CRL> revocationSourceCRL = crlSource;

		// Thirdly, we need to configure the CertificateVerifier
		CertificateVerifier certificateVerifier = new CommonCertificateVerifier();
		certificateVerifier.setTrustedCertSources(tcsArray); // configured trusted list certificate source
		certificateVerifier.setAIASource(aiaSource); // configured AIA Access
		certificateVerifier.setOcspSource(revocationOcspCRL); // configured OCSP Access
		certificateVerifier.setCrlSource(revocationSourceCRL); // configured CRL Access
		remoteDocumentValidationService.setVerifier(certificateVerifier);
		return remoteDocumentValidationService;
	}
}
