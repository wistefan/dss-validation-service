package com.ficodes.iam.configuration;

/**
 * Configuration for loading a keystore
 *
 * @param path to the store file
 * @param type of the keystore
 * @param password for the store to be opend
 */
public record StoreConfig(String path, String type, String password) {
}
