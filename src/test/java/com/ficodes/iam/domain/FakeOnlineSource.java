package com.ficodes.iam.domain;

import eu.europa.esig.dss.crl.CRLBinary;
import eu.europa.esig.dss.crl.CRLUtils;
import eu.europa.esig.dss.crl.CRLValidity;
import eu.europa.esig.dss.enumerations.RevocationOrigin;
import eu.europa.esig.dss.model.x509.CertificateToken;
import eu.europa.esig.dss.service.crl.OnlineCRLSource;
import eu.europa.esig.dss.spi.exception.DSSExternalResourceException;
import eu.europa.esig.dss.spi.x509.revocation.crl.CRLToken;
import eu.europa.esig.dss.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FakeOnlineSource extends OnlineCRLSource {

	@Setter
	@Getter
	private Map<String, CRLBinary> crlBinaries = new HashMap<>();

	@Override
	public CRLToken getRevocationToken(CertificateToken certificateToken, CertificateToken issuerToken, List<String> alternativeUrls) {

		final List<String> crlUrls = getCRLAccessURLs(certificateToken, alternativeUrls);
		if (Utils.isCollectionEmpty(crlUrls)) {
			throw new DSSExternalResourceException(String.format(
					"No CRL location found for certificate with Id '%s'", certificateToken.getDSSIdAsString()));
		}
		for (String crlUrl : crlUrls) {
			try {
				if (crlBinaries.containsKey(crlUrl)) {
					CRLBinary crlBinary = crlBinaries.get(crlUrl);
					final CRLValidity crlValidity = CRLUtils.buildCRLValidity(crlBinary, issuerToken);
					final CRLToken crlToken = new CRLToken(certificateToken, crlValidity);
					crlToken.setExternalOrigin(RevocationOrigin.EXTERNAL);
					crlToken.setSourceURL(crlUrl);
					return crlToken;
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		// if nothing is found in the test data, use the "normal" service
		return super.getRevocationToken(certificateToken, issuerToken, alternativeUrls);
	}
}
