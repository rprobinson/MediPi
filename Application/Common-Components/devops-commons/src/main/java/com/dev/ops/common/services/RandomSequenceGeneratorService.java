package com.dev.ops.common.services;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class RandomSequenceGeneratorService {

	private final SecureRandom secureRandomKeyGenerator;

	/*public static void main(String... arguments) throws NoSuchAlgorithmException {
		RandomSequenceGenerator randomSequenceGenerator = new RandomSequenceGenerator();
		System.out.println(randomSequenceGenerator.getRandomSequence());
	}*/

	public RandomSequenceGeneratorService() throws NoSuchAlgorithmException {
		this.secureRandomKeyGenerator = SecureRandom.getInstance("SHA1PRNG");
	}

	public String getRandomSequence() throws NoSuchAlgorithmException {
		final String randomNumber = new Long(this.secureRandomKeyGenerator.nextInt()).toString();

		final MessageDigest shaDigits = MessageDigest.getInstance("SHA-1");
		final byte[] result = shaDigits.digest(randomNumber.getBytes());

		final String randomSequence = this.hexEncode(result);
		return randomSequence;
	}

	/**
	 * The byte[] returned by MessageDigest does not have a nice textual
	 * representation, so some form of encoding is usually performed.
	 *
	 * This implementation follows the example of David Flanagan's book
	 * "Java In A Nutshell", and converts a byte array into a String of hex
	 * characters.
	 *
	 * Another popular alternative is to use a "Base64" encoding.
	 */
	private String hexEncode(final byte[] aInput) {
		final StringBuilder result = new StringBuilder();
		final char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

		for(final byte b : aInput) {
			result.append(digits[(b & 0xf0) >> 4]);
			result.append(digits[b & 0x0f]);
		}
		return result.toString();
	}
}