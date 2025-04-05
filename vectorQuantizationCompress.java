import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class vectorQuantizationCompress {

    // Define the tile size (each tile is tileSize x tileSize pixels)
    int tileSize = 8;
    int tilesPerRow;
    int tilesPerColumn;

    // 3D array to store image tiles
    private int[][][] tileGrid;

    // Codebook and compressed image
    private int[][] codeBook;
    private int[][] compressedImage;

    // Quality and codebook settings
    int qualityOption;
    int codeBookSize;
    int imageWidth, imageHeight;

    // Allow user to choose compression quality
    public void chooseQuality() {
        Scanner sc = new Scanner(System.in);
        System.out.println("Select compression quality option: ");
        System.out.println("1. Low Compression (High Quality)");
        System.out.println("2. Medium Compression (Medium Quality)");
        System.out.println("3. High Compression (Low Quality)");
        qualityOption = sc.nextInt();

        // Determine codebook size based on quality
        switch (qualityOption) {
            case 1: codeBookSize = 256; break;
            case 2: codeBookSize = 128; break;
            case 3: codeBookSize = 16; break;
            default:
                System.out.println("Invalid option; defaulting to Medium Compression.");
                codeBookSize = 128;
                break;
        }
    }

    // Load image and split into 8x8 grayscale tiles
    public void loadImage(String imgPath) {
        try {
            BufferedImage image = ImageIO.read(new File(imgPath));
            imageWidth = image.getWidth();
            imageHeight = image.getHeight();

            tilesPerRow = imageHeight / tileSize;
            tilesPerColumn = imageWidth / tileSize;

            tileGrid = new int[tilesPerRow][tilesPerColumn][tileSize * tileSize];
            compressedImage = new int[tilesPerRow][tilesPerColumn];

            for (int row = 0; row < tilesPerRow; row++) {
                for (int col = 0; col < tilesPerColumn; col++) {
                    for (int i = 0; i < tileSize; i++) {
                        for (int j = 0; j < tileSize; j++) {
                            int pixel = image.getRGB(col * tileSize + j, row * tileSize + i);
                            int grayscale = (pixel >> 16) & 0xFF; // Extract grayscale value
                            tileGrid[row][col][i * tileSize + j] = grayscale;
                        }
                    }
                }
            }

            System.out.println("Image loaded and split into " + tilesPerRow + " x " + tilesPerColumn + " tiles.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Improved codebook initialization: sample tiles from across the entire image
    public void initializeCodebook() {
        codeBook = new int[codeBookSize][tileSize * tileSize];
        int totalTiles = tilesPerRow * tilesPerColumn;
        int step = Math.max(1, totalTiles / codeBookSize);
        int count = 0;

        for (int row = 0; row < tilesPerRow; row++) {
            for (int col = 0; col < tilesPerColumn; col++) {
                int flatIndex = row * tilesPerColumn + col;
                if (flatIndex % step == 0 && count < codeBookSize) {
                    codeBook[count++] = tileGrid[row][col].clone();
                }
                if (count >= codeBookSize) break;
            }
            if (count >= codeBookSize) break;
        }

        // Fill any remaining codebook vectors if image had fewer tiles
        while (count < codeBookSize) {
            codeBook[count++] = new int[tileSize * tileSize]; // blank tiles
        }

        System.out.println("Codebook initialized with " + count + " vectors.");
    }

    // Quantize the image using the closest codebook vector
    public void quantizeImage() {
        for (int row = 0; row < tilesPerRow; row++) {
            for (int col = 0; col < tilesPerColumn; col++) {
                compressedImage[row][col] = findClosestCodebookVector(tileGrid[row][col]);
            }
        }
    }

    // Find the closest vector in the codebook using Euclidean distance
    public int findClosestCodebookVector(int[] tile) {
        int minDistance = Integer.MAX_VALUE;
        int bestIndex = 0;

        for (int i = 0; i < codeBookSize; i++) {
            int distance = 0;
            for (int j = 0; j < tileSize * tileSize; j++) {
                int diff = tile[j] - codeBook[i][j];
                distance += diff * diff;
            }
            if (distance < minDistance) {
                minDistance = distance;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    // Save compressed image and codebook to file
    public void saveCompressedFile(String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write metadata: image width, height, tile size, codebook size
            bw.write(imageWidth + " " + imageHeight + " " + tileSize + " " + codeBookSize);
            bw.newLine();

            // Write compressed image (tile indices)
            for (int i = 0; i < tilesPerRow; i++) {
                for (int j = 0; j < tilesPerColumn; j++) {
                    bw.write(compressedImage[i][j] + " ");
                }
                bw.newLine();
            }

            // Write codebook vectors
            for (int i = 0; i < codeBookSize; i++) {
                for (int j = 0; j < tileSize * tileSize; j++) {
                    bw.write(codeBook[i][j] + " ");
                }
                bw.newLine();
            }

            System.out.println("Compression successful. Data saved to " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Main method
    public static void main(String[] args) {
        vectorQuantizationCompress compressor = new vectorQuantizationCompress();
        Scanner sc = new Scanner(System.in);
        System.out.print("Give file path : ");
        String filePath = sc.nextLine();
        compressor.chooseQuality();
        compressor.loadImage(filePath);
        compressor.initializeCodebook();
        compressor.quantizeImage();
        compressor.saveCompressedFile("Compressed.txt");
    }
}
