package org.medipi.utilities;

/**
 * BytesManipulator class to contains the methods which are useful in bytes manipulation.
 *
 * @author krishna.kuntala@mastek.com
 */
public class BytesManipulator {

	/**
	 * Convert byte array to an integer array.
	 *
	 * @param reading the reading
	 * @return the int[]
	 */
	public static int[] convertBytesToIntegers(final byte[] reading) {
		final int[] integerReadings = new int[reading.length / 2];
		for(int counter = 0, integerCounter = 0; counter < reading.length;) {
			integerReadings[integerCounter] = convertTwoBytesToInteger(reading[counter], reading[counter + 1]);
			counter += 2;
			integerCounter++;
		}
		return integerReadings;
	}

	/**
	 * Convert two bytes to integer. In the process of doing the same
	 * convert 2 bytes to 1 integer value.
	 * e.g. Two bytes 2, 137 are converted as 2*256 + 137 = 649
	 *
	 * @param byte1 the first byte
	 * @param byte2 the second byte
	 * @return the converted integer value
	 */
	private static int convertTwoBytesToInteger(final byte byte1, final byte byte2) {
		final int unsignedInteger1 = getUnsignedInteger(byte1);
		final int unsignedInteger2 = getUnsignedInteger(byte2);
		return unsignedInteger1 * 256 + unsignedInteger2;
	}

	/**
	 * Gets the unsigned integer by adding 256 if the value is negative
	 *
	 * @param b the byte which needs to be converted
	 * @return the unsigned integer
	 */
	public static int getUnsignedInteger(final byte b) {
		int unsignedInteger = b;
		if(b < 0) {
			unsignedInteger = b + 256;
		}
		return unsignedInteger;
	}

	/**
	 * Transpose the byte matrix m[x][y] to m[y][x].
	 *
	 * @param inputArray the input array
	 * @return the transposed array
	 */
	public static int[][] transpose(final int[][] inputArray) {
		if(inputArray == null || inputArray.length == 0) {
			return inputArray;
		}

		final int columns = inputArray.length;
		final int rows = inputArray[0].length;

		final int[][] transposedArray = new int[rows][columns];

		for(int columnCounter = 0; columnCounter < columns; columnCounter++) {
			for(int rowCounter = 0; rowCounter < rows; rowCounter++) {
				transposedArray[rowCounter][columnCounter] = inputArray[columnCounter][rowCounter];
			}
		}
		return transposedArray;
	}
}