package de.cherry;

public class ImageDimensionMismatchException extends RuntimeException {
	private static final long serialVersionUID = -4180466070101293144L;
	
	public ImageDimensionMismatchException() {
		super("The given images do not have the same dimensions");
	}
}
