package de.cherry;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Main {
	public static final int TARGET_WIDTH = 256;
	public static final int TARGET_HEIGHT = 256;
	
	public static final double MAX_PIXEL_DIFFERENCE = 0.9d;
	public static final double MAX_IMAGE_DIFFERENCE = 0.9d;
	
	static Set<String> allowedExtensions = Set.of(".png", ".jpeg", ".jpg");
	
	public static void main(String[] args) {
		File dir = new File(args[0]);
		
		System.out.printf("loading from %s%n", dir.getAbsolutePath());
		
		// list all files
		List<File> inFiles = Arrays.asList(dir.listFiles(f -> f.isFile() && allowedExtensions.contains(f.getName().substring(f.getName().lastIndexOf('.')))));
		
		System.out.printf("found %d files%n", inFiles.size());
		
		// load all files
		Map<File, BufferedImage> images = inFiles.stream().collect(Collectors.toMap(f -> f, f -> {
			try {
				return resize(ImageIO.read(f), TARGET_WIDTH, TARGET_HEIGHT);
			} catch(IOException e) {
				e.printStackTrace();
				return null;
			}
		}));
		
		System.out.printf("loaded %d files, resizing...%n", inFiles.size());
		
		// filter, in case an error occurred
		images = images.entrySet().stream().filter(e -> e.getValue() != null).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		List<File> files = images.keySet().stream().toList();
		
		final int size = files.size();
		
		System.out.printf("resizing done, comparing %d images%n", size);
		
		for(int i = 0; i < size; i++) {
			for(int j = i; j < size; j++) {
				if(i == j) {
					continue;
				}
				
				File f1 = files.get(i);
				File f2 = files.get(j);
				
				BufferedImage img1 = images.get(f1);
				BufferedImage img2 = images.get(f2);
				
//				System.out.println(f1.getName());
//				System.out.println(f2.getName());
				
				double diff = compareImages(img1, img2);
				
				if(diff > MAX_IMAGE_DIFFERENCE) {
					System.out.print(">> ");
				}
				
				System.out.printf("%s - %s: %.2f%%%n", f1.getName(), f2.getName(), diff * 100);
				
			}
		}
	}
	
	public static BufferedImage resize(BufferedImage img, int newW, int newH) { 
	    Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
	    BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

	    Graphics2D g2d = dimg.createGraphics();
	    g2d.drawImage(tmp, 0, 0, null);
	    g2d.dispose();

	    return dimg;
	}
	
	/**
	 * Compares two images pixel by pixel.
	 *
	 * @param imgA the first image.
	 * @param imgB the second image.
	 * @return The pixel difference. Higher means images are <b>more</b> similar.
	 */
	public static double compareImages(BufferedImage imgA, BufferedImage imgB) {
	  // The images must be the same size.
	  if (imgA.getWidth() != imgB.getWidth() || imgA.getHeight() != imgB.getHeight()) {
	    throw new ImageDimensionMismatchException();
	  }

	  final int width  = imgA.getWidth();
	  final int height = imgA.getHeight();
	  
	  final int size = width * height;
	  int counter = 0;

	  // Loop over every pixel.
	  for (int y = 0; y < height; y++) {
	    for (int x = 0; x < width; x++) {
	      // Compare the pixels for equality.
	      int pixel1 = imgA.getRGB(x, y);
	      int pixel2 = imgB.getRGB(x, y);
	      
	      int pxDelta = pixelDelta(pixel1, pixel2);
	      double pxDiff = pxDelta / (255.0 * 3);
	      
//	      System.out.println(new Pixel(pixel1));
//	      System.out.println(new Pixel(pixel2));
//	      System.out.println(pxDelta);
//	      System.out.println(pxDiff);
	      
	      // pxDiff: higher is worse
	      if(pxDiff < (1 - MAX_PIXEL_DIFFERENCE)) {
	    	  counter++;
	      }
	      
//	      throw new IllegalStateException();
	    }
	  }
	  
//	  System.out.println();
//	  System.out.println(counter);
//	  System.out.println(size);
//	  System.out.println((double) counter / size);

	  return ((double) counter) / size;
	}
	
	static class Pixel {
		public final int alpha;
		public final int red;
		public final int green;
		public final int blue;
		
		public Pixel(int value) {
			this.alpha = mask(value, ColourMasks.ALPHA);
			this.red = mask(value, ColourMasks.RED);
			this.green = mask(value, ColourMasks.GREEN);
			this.blue = mask(value, ColourMasks.BLUE);
		}
		
		@Override
		public String toString() {
			return String.format("{%d | %d | %d} @ %d", red, green, blue, alpha);
		}
	}
	
	enum ColourMasks {
		ALPHA(0xFF000000, 24),
		RED(0xFF0000, 16),
		GREEN(0xFF00, 8),
		BLUE(0xFF, 0);
		
		public final int mask;
		public final int shift;
		
		private ColourMasks(int mask, int shift) {
			this.mask = mask;
			this.shift = shift;
		}
	}
	
	public static int pixelDelta(int px1, int px2) {
		int diff = 0;
		
		for(ColourMasks mask : ColourMasks.values()) {
			diff += Math.abs(mask(px1, mask) - mask(px2, mask));
		}
		
		return diff;
	}
	
	public static int mask(int value, ColourMasks mask) {
		return ((value & mask.mask) >> mask.shift) & 0xFF;
	}
}
