package de.cherry;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

public class Main {

    public static void main(String[] args) throws IOException {
        File folder = new File("C:\\Users\\jkirs\\OneDrive\\Bilder\\Java_test");
        File[] files = folder.listFiles();

        if (files != null) {
            BufferedImage[] images = new BufferedImage[files.length];
            
            int loadedImageCount = 0; // Zählvariable für die geladenen Bilder

            // Load all images into the BufferedImage array
            for (int i = 0; i < files.length; i++) {
                images[i] = ImageIO.read(files[i]);
                images[i] = resizeImage(images[i], 512, 512);
                loadedImageCount++; // Zähler erhöhen, da ein Bild geladen wurde
                System.out.print("\r"+"Loaded " + loadedImageCount + " of " + files.length + " images.");
            }

            // Speichert die Ähnlichkeiten zwischen den Bildern
            Map<String, Map<String, Double>> imageSimilarities = new HashMap<>();

            for (int i = 0; i < images.length; i++) {
                for (int j = i + 1; j < images.length; j++) {
                    String hash1 = calculatePHash(images[i]);
                    String hash2 = calculatePHash(images[j]);

                    double similarity = hammingDistance(hash1, hash2);

                    String image1Name = files[i].getName();
                    String image2Name = files[j].getName();

                    // Speichert die Ähnlichkeit zwischen den Bildern
                    imageSimilarities.computeIfAbsent(image1Name, k -> new HashMap<>()).put(image2Name, similarity);
                }
            }

            // Ergebnisse zusammenfassen und ausgeben
            System.out.println("\nImage Similarities:");
            
            // Sortieren Sie die Ähnlichkeiten in absteigender Reihenfolge
            List<Map.Entry<String, Map<String, Double>>> sortedSimilarities = new ArrayList<>(imageSimilarities.entrySet());
            sortedSimilarities.sort(Comparator.comparingDouble(entry -> -Collections.max(entry.getValue().values())));

            for (Map.Entry<String, Map<String, Double>> entry : sortedSimilarities) {
                String image1 = entry.getKey();
                Map<String, Double> similarities = entry.getValue();
                for (Map.Entry<String, Double> similarityEntry : similarities.entrySet()) {
                    String image2 = similarityEntry.getKey();
                    double similarity = similarityEntry.getValue();
                    System.out.println("Similarity between " + image1 + " and " + image2 + ": " + similarity);
                }
            }
        } else {
            System.out.println("No files found in the specified folder.");
        }
    }

    private static String calculatePHash(BufferedImage img) throws IOException {
        long phash = img.hashCode();
        return Long.toHexString(phash);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight)
            throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private static double hammingDistance(String hash1, String hash2) {
        int len = Math.min(hash1.length(), hash2.length());
        int distance = 0;

        for (int i = 0; i < len; i++) {
            if (hash1.charAt(i) != hash2.charAt(i)) {
                distance++;
            }
        }

        return 1.0 - (double) distance / len;
    }
}
