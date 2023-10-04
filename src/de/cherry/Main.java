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
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

public class Main {
	public static final int TARGET_WIDTH = 256;
	public static final int TARGET_HEIGHT = 256;
	
	public static final double MAX_DIFFERENCE = 90d;
	
	public static void main(String[] args) {
		File dir = new File(args[0]);
		
		System.out.printf("loading from %s%n", dir.getAbsolutePath());
		
		// list all files
		List<File> inFiles = Arrays.asList(dir.listFiles(File::isFile));
		
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
				
				double diff = compareImages(img1, img2);
				
				System.out.printf("%s - %s: %f%n", f1.getName(), f2.getName(), diff);
				
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
	 * @return the pixel difference
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
	      if (imgA.getRGB(x, y) == imgB.getRGB(x, y)) {
	        counter++;
	      }
	    }
	  }
	  
//	  System.out.println();
//	  System.out.println(counter);
//	  System.out.println(size);
//	  System.out.println((double) counter / size);

	  return ((double) counter) / size;
	}
}
