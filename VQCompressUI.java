import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class VQCompressUI extends JFrame {
    private JTextField filePathField;
    private JButton browseButton;
    private JComboBox<Integer> tileSizeBox;
    private JComboBox<String> qualityBox;
    private JButton compressButton;
    private JButton decompressButton;
    private JTextArea statusArea;
    private JLabel imageLabel;
    private JLabel decompressedImageLabel;
    private BufferedImage selectedImage;
    private BufferedImage decompressedImage;

    public VQCompressUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        setTitle("VQ Image Compressor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JComponent)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Compression Settings"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 7, 7, 7);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // File chooser
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Image File:"), gbc);
        filePathField = new JTextField(22);
        gbc.gridx = 1;
        inputPanel.add(filePathField, gbc);
        browseButton = new JButton("Browse");
        gbc.gridx = 2;
        inputPanel.add(browseButton, gbc);

        // Tile size
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Tile Size:"), gbc);
        tileSizeBox = new JComboBox<>(new Integer[]{1, 2, 4, 6, 8});
        gbc.gridx = 1;
        inputPanel.add(tileSizeBox, gbc);

        // Quality
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Quality:"), gbc);
        qualityBox = new JComboBox<>(new String[] {"Low Compression (High Quality)", "Medium Compression", "High Compression (Low Quality)"});
        gbc.gridx = 1;
        inputPanel.add(qualityBox, gbc);

        // Buttons
        compressButton = new JButton("Compress");
        decompressButton = new JButton("Decompress");
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(compressButton);
        buttonPanel.add(decompressButton);

        // Status area
        statusArea = new JTextArea(5, 40);
        statusArea.setEditable(false);
        statusArea.setLineWrap(true);
        statusArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(statusArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Status"));

        // Image display panel
        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBorder(BorderFactory.createTitledBorder("Image Preview"));
        imageLabel = new JLabel("No image selected", SwingConstants.CENTER);
        imageLabel.setVerticalTextPosition(JLabel.BOTTOM);
        imageLabel.setHorizontalTextPosition(JLabel.CENTER);
        imageLabel.setBorder(BorderFactory.createEtchedBorder());
        decompressedImageLabel = new JLabel("Decompressed image will appear here", SwingConstants.CENTER);
        decompressedImageLabel.setVerticalTextPosition(JLabel.BOTTOM);
        decompressedImageLabel.setHorizontalTextPosition(JLabel.CENTER);
        decompressedImageLabel.setBorder(BorderFactory.createEtchedBorder());
        imagePanel.add(imageLabel);
        imagePanel.add(decompressedImageLabel);

        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.add(inputPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(imagePanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Browse action
        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int ret = chooser.showOpenDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                filePathField.setText(file.getAbsolutePath());
                try {
                    selectedImage = ImageIO.read(file);
                    if (selectedImage != null) {
                        imageLabel.setIcon(new ImageIcon(selectedImage.getScaledInstance(300, 220, Image.SCALE_SMOOTH)));
                        imageLabel.setText("");
                    } else {
                        imageLabel.setIcon(null);
                        imageLabel.setText("Cannot display image");
                    }
                } catch (Exception ex) {
                    imageLabel.setIcon(null);
                    imageLabel.setText("Cannot display image");
                }
            }
        });

        // Compress action
        compressButton.addActionListener(e -> {
            String filePath = filePathField.getText().trim();
            int tileSize = (Integer) tileSizeBox.getSelectedItem();
            int qualityOption = qualityBox.getSelectedIndex() + 1;
            if (filePath.isEmpty()) {
                showStatus("Please select an image file.");
                return;
            }
            try {
                vectorQuantizationCompress compressor = new vectorQuantizationCompress();
                compressor.tileSize = tileSize;
                compressor.qualityOption = qualityOption;
                switch (qualityOption) {
                    case 1: compressor.codeBookSize = 256; break;
                    case 2: compressor.codeBookSize = 128; break;
                    case 3: compressor.codeBookSize = 16; break;
                    default: compressor.codeBookSize = 128; break;
                }
                compressor.loadImage(filePath);
                compressor.initializeCodebook();
                compressor.quantizeImage();
                compressor.saveCompressedFile("Compressed.txt");
                showStatus("Compression successful! Saved as Compressed.txt");
                JOptionPane.showMessageDialog(this, "Compressed.txt stored at: " + new File("Compressed.txt").getAbsolutePath(), "Compression Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                ex.printStackTrace();
                showStatus("Compression failed: " + ex.getMessage());
            }
        });

        // Decompress action
        decompressButton.addActionListener(e -> {
            try {
                vectorQuantizationDecompress decompressor = new vectorQuantizationDecompress();
                decompressor.loadCompressedData("Compressed.txt");
                showStatus("Decompression successful! Saved as DecompressedImage.png");
                String outPath = new File("DecompressedImage.png").getAbsolutePath();
                JOptionPane.showMessageDialog(this, "Decompressed image stored at: " + outPath, "Decompression Complete", JOptionPane.INFORMATION_MESSAGE);
                // Display decompressed image
                try {
                    decompressedImage = ImageIO.read(new File("DecompressedImage.png"));
                    if (decompressedImage != null) {
                        decompressedImageLabel.setIcon(new ImageIcon(decompressedImage.getScaledInstance(300, 220, Image.SCALE_SMOOTH)));
                        decompressedImageLabel.setText("");
                    } else {
                        decompressedImageLabel.setIcon(null);
                        decompressedImageLabel.setText("Cannot display image");
                    }
                } catch (Exception ex) {
                    decompressedImageLabel.setIcon(null);
                    decompressedImageLabel.setText("Cannot display image");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showStatus("Decompression failed: " + ex.getMessage());
            }
        });
    }

    private void showStatus(String msg) {
        statusArea.append(msg + "\n");
        statusArea.setCaretPosition(statusArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VQCompressUI().setVisible(true);
        });
    }
} 