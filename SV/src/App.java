import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

public class App {
    private JFrame frame;
    private ImagePanel originalImagePanel;
    private ImagePanel filteredImagePanel;
    private JComboBox<String> filterTypeComboBox;
    private JComboBox<String> filterSizeComboBox;

    public App() {
        frame = new JFrame("이미지 필터");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 500);

        originalImagePanel = new ImagePanel();
        filteredImagePanel = new ImagePanel();

        JPanel controlPanel = new JPanel();

        filterTypeComboBox = new JComboBox<>(new String[]{"Mean Filter", "Median Filter","Laplacian Filter"});
        filterSizeComboBox = new JComboBox<>(new String[]{"3x3", "5x5"});
        JButton applyButton = new JButton("적용");

        controlPanel.add(filterTypeComboBox);
        controlPanel.add(filterSizeComboBox);
        controlPanel.add(applyButton);

        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilter();
            }
        });

        frame.setLayout(new GridLayout(1, 3));
        frame.add(originalImagePanel);
        frame.add(filteredImagePanel);
        frame.add(controlPanel);
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(frame, "프로그램을 종료하시겠습니까?", "확인", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    System.exit(0); // 프로그램 종료
                }
            }
        });
    }

    private void applyFilter() {
    String filterType = (String) filterTypeComboBox.getSelectedItem();
    String filterSize = (String) filterSizeComboBox.getSelectedItem();

    if (originalImagePanel.getImage() != null) {
        BufferedImage originalImage = originalImagePanel.getImage();
        BufferedImage filteredImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);

        if (filterType.equals("Mean Filter")) {
            int size = filterSize.equals("3x3") ? 3 : 5;

            for (int y = 0; y < originalImage.getHeight(); y++) {
                for (int x = 0; x < originalImage.getWidth(); x++) {
                    int sumRed = 0;
                    int sumGreen = 0;
                    int sumBlue = 0;
                    int count = 0;

                    for (int dy = -size / 2; dy <= size / 2; dy++) {
                        for (int dx = -size / 2; dx <= size / 2; dx++) {
                            int pixelX = x + dx;
                            int pixelY = y + dy;

                            if (pixelX >= 0 && pixelX < originalImage.getWidth() && pixelY >= 0 && pixelY < originalImage.getHeight()) {
                                Color pixelColor = new Color(originalImage.getRGB(pixelX, pixelY));
                                sumRed += pixelColor.getRed();
                                sumGreen += pixelColor.getGreen();
                                sumBlue += pixelColor.getBlue();
                                count++;
                            }
                        }
                    }

                    int avgRed = sumRed / count;
                    int avgGreen = sumGreen / count;
                    int avgBlue = sumBlue / count;
                    Color newColor = new Color(avgRed, avgGreen, avgBlue);
                    filteredImage.setRGB(x, y, newColor.getRGB());
                }
            }
        } else if (filterType.equals("Median Filter")) {
                int size = filterSize.equals("3x3") ? 3 : 5;

                for (int y = 0; y < originalImage.getHeight(); y++) {
                    for (int x = 0; x < originalImage.getWidth(); x++) {
                        ArrayList<Integer> redValues = new ArrayList<>();
                        ArrayList<Integer> greenValues = new ArrayList<>();
                        ArrayList<Integer> blueValues = new ArrayList();

                        for (int dy = -size / 2; dy <= size / 2; dy++) {
                            for (int dx = -size / 2; dx <= size / 2; dx++) {
                                int pixelX = x + dx;
                                int pixelY = y + dy;

                                if (pixelX >= 0 && pixelX < originalImage.getWidth() && pixelY >= 0 && pixelY < originalImage.getHeight()) {
                                    Color pixelColor = new Color(originalImage.getRGB(pixelX, pixelY));
                                    redValues.add(pixelColor.getRed());
                                    greenValues.add(pixelColor.getGreen());
                                    blueValues.add(pixelColor.getBlue());
                                }
                            }
                        }

                        Collections.sort(redValues);
                        Collections.sort(greenValues);
                        Collections.sort(blueValues);

                        int medianRed = redValues.get(redValues.size() / 2);
                        int medianGreen = greenValues.get(greenValues.size() / 2);
                        int medianBlue = blueValues.get(blueValues.size() / 2);

                        Color newColor = new Color(medianRed, medianGreen, medianBlue);
                        filteredImage.setRGB(x, y, newColor.getRGB());
                    }
                }
            }else if (filterType.equals("Laplacian Filter")) {
                int size = 3;

                int[][] laplacianMask = {
                    {-1, -1, -1},
                    {-1, 8, -1},
                    {-1, -1, -1}
                };

                for (int y = 0; y < originalImage.getHeight(); y++) {
                    for (int x = 0; x < originalImage.getWidth(); x++) {
                        int sumRed = 0;
                        int sumGreen = 0;
                        int sumBlue = 0;

                        for (int dy = -size / 2; dy <= size / 2; dy++) {
                            for (int dx = -size / 2; dx <= size / 2; dx++) {
                                int pixelX = x + dx;
                                int pixelY = y + dy;

                                if (pixelX >= 0 && pixelX < originalImage.getWidth() && pixelY >= 0 && pixelY < originalImage.getHeight()) {
                                    Color pixelColor = new Color(originalImage.getRGB(pixelX, pixelY));
                                    int maskValue = laplacianMask[dy + size / 2][dx + size / 2];
                                    sumRed += pixelColor.getRed() * maskValue;
                                    sumGreen += pixelColor.getGreen() * maskValue;
                                    sumBlue += pixelColor.getBlue() * maskValue;
                                }
                            }
                        }

                        sumRed = Math.min(255, Math.max(0, sumRed));
                        sumGreen = Math.min(255, Math.max(0, sumGreen));
                        sumBlue = Math.min(255, Math.max(0, sumBlue));

                        Color newColor = new Color(sumRed, sumGreen, sumBlue);
                        filteredImage.setRGB(x, y, newColor.getRGB());
                    }
                }
            }

        filteredImagePanel.setImage(filteredImage);
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new App();
        });
    }
}

class ImagePanel extends JPanel {
    private BufferedImage image;

    public ImagePanel() {
        setPreferredSize(new Dimension(300, 300));
        setBackground(Color.LIGHT_GRAY);
        setDropTarget(new DropTarget(this, new ImageDropListener()));
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image != null) {
            g.drawImage(image, 0, 0, this);
        }
    }

    private class ImageDropListener implements DropTargetListener {
        @Override
        public void drop(DropTargetDropEvent dtde) {
            try {
                dtde.acceptDrop(DnDConstants.ACTION_COPY);
                Transferable transferable = dtde.getTransferable();
                if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                    if (files.size() > 0) {
                        File file = files.get(0);
                        if (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png")) {
                            setImage(ImageIO.read(file));
                        }
                    }
                }
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void dragEnter(DropTargetDragEvent dtde) {
            // 필요한 경우 구현
        }

        @Override
        public void dragOver(DropTargetDragEvent dtde) {
            // 필요한 경우 구현
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent dtde) {
            // 필요한 경우 구현
        }

        @Override
        public void dragExit(DropTargetEvent dte) {
            // 필요한 경우 구현
        }
    }
}
