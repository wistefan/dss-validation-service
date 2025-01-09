package com.ficodes.iam.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties("trust")
public class TrustProperties {

	private List<String> listUrls = new ArrayList<>();
	private List<StoreConfig> stores = new ArrayList<>();
}
