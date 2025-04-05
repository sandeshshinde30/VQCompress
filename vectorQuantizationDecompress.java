import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class vectorQuantizationDecompress {
    private int tileSize;
    private int tilesPerRow;
    private int tilesPerColumn;
    private int[][] codeBook;
    private int[][] decompressedIndices;

    public void loadCompressedData(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Line 1: metadata -> imageWidth imageHeight tileSize codebookSize
            String[] header = reader.readLine().trim().split("\\s+");
            int imageWidth = Integer.parseInt(header[0]);
            int imageHeight = Integer.parseInt(header[1]);
            tileSize = Integer.parseInt(header[2]);
            int codebookSize = Integer.parseInt(header[3]);

            // Compute tiles per row/column
            tilesPerRow = imageHeight / tileSize;
            tilesPerColumn = imageWidth / tileSize;

            // Initialize arrays
            decompressedIndices = new int[tilesPerRow][tilesPerColumn];
            codeBook = new int[codebookSize][tileSize * tileSize];

            // Read tile indices (tilesPerRow lines with tilesPerColumn values)
            for (int i = 0; i < tilesPerRow; i++) {
                String[] indices = reader.readLine().trim().split("\\s+");
                for (int j = 0; j < tilesPerColumn; j++) {
                    decompressedIndices[i][j] = Integer.parseInt(indices[j]); // Now reading as decimal
                }
            }

            // Read codebook vectors (codebookSize lines)
            for (int i = 0; i < codebookSize; i++) {
                String[] pixels = reader.readLine().trim().split("\\s+");
                for (int j = 0; j < tileSize * tileSize; j++) {
                    codeBook[i][j] = Integer.parseInt(pixels[j]);
                }
            }

            System.out.println("Compressed data loaded successfully.");

            // Reconstruct and save the image
            BufferedImage img = reconstructImage(imageWidth, imageHeight);
            saveImage(img, "DecompressedImage.png");

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }
    }

    private BufferedImage reconstructImage(int width, int height) {
        BufferedImage reconstructedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int row = 0; row < tilesPerRow; row++) {
            for (int col = 0; col < tilesPerColumn; col++) {
                int index = decompressedIndices[row][col];
                int[] tile = codeBook[index];

                for (int i = 0; i < tileSize; i++) {
                    for (int j = 0; j < tileSize; j++) {
                        int pixel = tile[i * tileSize + j];
                        int rgbVal = (pixel << 16) | (pixel << 8) | pixel; // grayscale to RGB
                        reconstructedImage.setRGB(col * tileSize + j, row * tileSize + i, rgbVal);
                    }
                }
            }
        }

        System.out.println("Image reconstructed successfully.");
        return reconstructedImage;
    }

    private void saveImage(BufferedImage image, String path) {
        try {
            ImageIO.write(image, "png", new File(path));
            System.out.println("Decompressed image saved successfully to " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        vectorQuantizationDecompress vqd = new vectorQuantizationDecompress();
        vqd.loadCompressedData("Compressed.txt");
    }
}
