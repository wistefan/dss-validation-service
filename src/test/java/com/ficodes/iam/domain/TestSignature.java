package com.ficodes.iam.domain;

import lombok.Data;

import java.util.List;

@Data
public class TestSignature {

	private byte[] signature;
	private int validSignatures;
	private int signatureCount;
	private List<CrlConfig> crls;
	private String message;
}
