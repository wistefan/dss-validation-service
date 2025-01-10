package com.ficodes.iam.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for the trust sources
 */
@Data
@ConfigurationProperties("trust")
public class TrustProperties {

	/**
	 * Configuration of trusted lists to be used as trust source
	 */
	private List<TrustedListConfig> lists = new ArrayList<>();
	/**
	 * Configuration of trust stores to be used as trust source
	 */
	private List<StoreConfig> stores = new ArrayList<>();
}
