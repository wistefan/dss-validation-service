package com.ficodes.iam;

import com.ficodes.iam.configuration.TrustProperties;
import eu.europa.esig.dss.service.http.commons.CommonsDataLoader;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import eu.europa.esig.dss.spi.tsl.TrustedListsCertificateSource;
import eu.europa.esig.dss.spi.x509.CommonTrustedCertificateSource;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.job.TLValidationJob;
import eu.europa.esig.dss.tsl.source.TLSource;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import io.micronaut.scheduling.TaskScheduler;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Singleton
public class TrustSources {

	private final TrustProperties trustProperties;
	private final TaskScheduler taskScheduler;
	private final List<CommonTrustedCertificateSource> commonTrustedCertificateSources = new ArrayList<>();

	public TrustSources(TrustProperties trustProperties, TaskScheduler taskScheduler) {
		this.trustProperties = trustProperties;
		this.taskScheduler = taskScheduler;
	}

	private static TLSource createTLSource(String url) {
		TLSource tlSource = new TLSource();
		tlSource.setUrl(url);
		return tlSource;
	}

	@EventListener
	public void onApplicationEvent(ServerStartupEvent e) {
		if (trustProperties.getListUrls() != null && !trustProperties.getListUrls().isEmpty()) {
			initializeTrustedListSources();
		}
		if (trustProperties.getStores() != null && !trustProperties.getStores().isEmpty()) {
			initializeKeystoreSources();
		}
	}

	private void initializeTrustedListSources() {
		TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
		CommonsDataLoader dataLoader = new CommonsDataLoader();

		// We set an instance of TrustAllStrategy to rely on the Trusted Lists content
		// instead of the JVM trust store.
		dataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);
		TLValidationJob tlValidationJob = new TLValidationJob();
		tlValidationJob.setOnlineDataLoader(new FileCacheDataLoader(dataLoader));

		List<TLSource> tlSources = trustProperties
				.getListUrls()
				.stream()
				.map(TrustSources::createTLSource).toList();

		TLSource[] tlSourcesArray = new TLSource[tlSources.size()];
		tlSourcesArray = tlSources.toArray(tlSourcesArray);

		// Configure the relevant TrustedList
		tlValidationJob.setTrustedListSources(tlSourcesArray);
		// Initialize the trusted list certificate source to fill with the information extracted from TLValidationJob
		tlValidationJob.setTrustedListCertificateSource(trustedListsCertificateSource);

		taskScheduler.scheduleAtFixedRate(Duration.ofSeconds(0), Duration.ofSeconds(5), tlValidationJob::onlineRefresh);
		commonTrustedCertificateSources.add(trustedListsCertificateSource);
	}

	private void initializeKeystoreSources() {
		trustProperties.getStores()
				.stream()
				.forEach(storeConfig -> {
					CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
					try {
						KeyStoreCertificateSource keyStoreCertificateSource = new KeyStoreCertificateSource(
								storeConfig.path(),
								storeConfig.type(),
								storeConfig.password().toCharArray());
						commonTrustedCertificateSource.importAsTrusted(keyStoreCertificateSource);
						commonTrustedCertificateSources.add(commonTrustedCertificateSource);
					} catch (IOException ex) {
						log.warn("Was not able to import certificates from keystore {}.", storeConfig.path(), ex);
					}
				});
	}

	public List<CommonTrustedCertificateSource> getCommonTrustedCertificateSources() {
		return commonTrustedCertificateSources;
	}

}
