# VQ Compress

## Vector Quantization Image Compression & Decompression

### Overview 
This project demonstrates how to compress and decompress a grayscale image using Vector Quantization (VQ). The main goal is to reduce image size by converting small blocks (tiles) of pixels into codebook indices and reconstructing the image using that codebook.



### 📁 File Structure
 ```bash
VQCompress/
├── vectorQuantizationCompress.java   # Compresses image and generates Compressed.txt
├── vectorQuantizationDecompress.java # Decompresses Compressed.txt and reconstructs the image
├── Compressed.txt                    # Output from compression, input to decompression
├── OriginalImage.png                 # Your original grayscale image (input for compression)
├── DecompressedImage.png             # Final output after decompression
```


### 🔧 Prerequisites
Java 8 or later installed

Grayscale image file 

Basic terminal or IDE like IntelliJ, VS Code


### 🔁 Flow of Execution
#### 1. 📦 Compression (vectorQuantizationCompress.java)
Input: OriginalImage.png

Process:

Load image

Divide into tiles (e.g., 8×8 pixels)

Create a codebook by grouping similar tiles

Replace each tile with its codebook index

Store metadata, tile indices, and codebook vectors in Compressed.txt

Output: Compressed.txt

#### 2. 🧩 Decompression (vectorQuantizationDecompress.java)
Input: Compressed.txt

Process:

Read metadata (image dimensions, tile size, codebook size)

Reconstruct tiles using codebook

Stitch tiles into a full image

Save final output as DecompressedImage.png

Output: DecompressedImage.png


### ▶️ How to Run
#### Compile Java files:
```bash 
javac vectorQuantizationCompress.java
javac vectorQuantizationDecompress.java
```

#### Run compression:
```bash 
java vectorQuantizationCompress
```
Produces Compressed.txt

#### Run decompression:
```bash 
java vectorQuantizationDecompress
```
Produces DecompressedImage.png


### 🧪 Example Compressed.txt Format
```bash
300 192 8 256           # imageWidth imageHeight tileSize codebookSize
0 1 1 2 3 4 5...        # tile indices (tilesPerRow × tilesPerCol)
...                     # more index rows
12 45 67 ...            # codebook vector 0 (tileSize × tileSize values)
...                     # codebook vector 1
...
```

### 📊 Quality Check
You can compare OriginalImage.png with DecompressedImage.png

Optionally compute PSNR (Peak Signal-to-Noise Ratio) for quality assessment
