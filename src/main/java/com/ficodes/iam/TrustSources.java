package com.ficodes.iam;

import com.ficodes.iam.configuration.TrustProperties;
import com.ficodes.iam.configuration.TrustedListConfig;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads and provides trust sources for signature validation
 */
@Slf4j
@Singleton
public class TrustSources {

	private final TrustProperties trustProperties;
	private final TaskScheduler taskScheduler;
	@Getter
	private final List<CommonTrustedCertificateSource> commonTrustedCertificateSources = new ArrayList<>();

	public TrustSources(TrustProperties trustProperties, TaskScheduler taskScheduler) {
		this.trustProperties = trustProperties;
		this.taskScheduler = taskScheduler;
	}

	/**
	 * Startup listener method to initialize the various sources
	 */
	@EventListener
	public void onApplicationEvent(ServerStartupEvent e) {
		log.warn("Start the trust sources");
		if (trustProperties.getLists() != null && !trustProperties.getLists().isEmpty()) {
			initializeTrustedListSources();
		}
		if (trustProperties.getStores() != null && !trustProperties.getStores().isEmpty()) {
			log.warn("Found trust store");
			initializeKeystoreSources();
		}
	}

	/**
	 * Initialize url-based trusted list sources and schedules the continuous update of them.
	 */
	private void initializeTrustedListSources() {
		TrustedListsCertificateSource trustedListsCertificateSource = new TrustedListsCertificateSource();
		CommonsDataLoader dataLoader = new CommonsDataLoader();
		FileCacheDataLoader fileCacheDataLoader = new FileCacheDataLoader(dataLoader);

		// We set an instance of TrustAllStrategy to rely on the Trusted Lists content
		// instead of the JVM trust store.
		dataLoader.setTrustStrategy(TrustAllStrategy.INSTANCE);
		trustProperties.getLists()
				.forEach(listConfig -> {
					TLValidationJob tlValidationJob = new TLValidationJob();
					tlValidationJob.setOnlineDataLoader(fileCacheDataLoader);
					TLSource tlSource = createTLSource(listConfig);// Configure the relevant TrustedList
					tlValidationJob.setTrustedListSources(tlSource);
					// Initialize the trusted list certificate source to fill with the information extracted from TLValidationJob
					tlValidationJob.setTrustedListCertificateSource(trustedListsCertificateSource);
					tlValidationJob.onlineRefresh();
					taskScheduler.scheduleAtFixedRate(
							Duration.ofSeconds(0),
							Duration.ofSeconds(listConfig.refreshSeconds()),
							tlValidationJob::onlineRefresh);
				});

		commonTrustedCertificateSources.add(trustedListsCertificateSource);
	}

	/**
	 * Initialize keystore based sources
	 */
	private void initializeKeystoreSources() {
		trustProperties.getStores()
				.forEach(storeConfig -> {
					CommonTrustedCertificateSource commonTrustedCertificateSource = new CommonTrustedCertificateSource();
					try {
						KeyStoreCertificateSource keyStoreCertificateSource = new KeyStoreCertificateSource(
								storeConfig.path(),
								storeConfig.type(),
								storeConfig.password().toCharArray());
						commonTrustedCertificateSource.importAsTrusted(keyStoreCertificateSource);
						log.warn("The store {}", keyStoreCertificateSource.getCertificate("test-keystore"));
						commonTrustedCertificateSources.add(commonTrustedCertificateSource);
					} catch (IOException ex) {
						log.warn("Was not able to import certificates from keystore {}.", storeConfig.path(), ex);
					}
				});
	}

	private static TLSource createTLSource(TrustedListConfig trustedListConfig) {
		TLSource tlSource = new TLSource();
		tlSource.setUrl(trustedListConfig.url());
		return tlSource;
	}

}
