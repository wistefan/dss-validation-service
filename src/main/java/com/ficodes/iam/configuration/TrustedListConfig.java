package com.ficodes.iam.configuration;

/**
 * Configuration for qualified trust providers as trust source, f.e:
 * - https://tl.bundesnetzagentur.de/TL-DE.XML
 * - https://ec.europa.eu/tools/lotl/eu-lotl.xml
 *
 * @param refreshSeconds interval to reload the trust providers from url
 * @param url            of the trust provider
 */
public record TrustedListConfig(int refreshSeconds, String url) {
}
