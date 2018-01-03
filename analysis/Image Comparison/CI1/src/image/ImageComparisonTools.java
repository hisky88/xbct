/**
 * 
 */
package image;
import image.model.Rectangle;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author huisk
 *  * Tools for the {@link ImageComparison} object.
 *
 */
public class ImageComparisonTools {

    /**
     * Create GUI for represents the resulting image.
     * @param image resulting image.
     */
    public static Frame createGUI( BufferedImage image ) {
        JFrame frame = new JFrame( "The result of the comparison" );
        frame.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
        JLabel label = new JLabel();
        label.setIcon( new ImageIcon( image, "Result") );
        frame.getContentPane().add( label, BorderLayout.CENTER);
        frame.setPreferredSize(new Dimension( image.getWidth(), ( int )( image.getHeight() * 1.1 ) ) );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );
        return frame;
    }

    /**
     * Make a copy of the {@code BufferedImage} object.
     * @param image the provided image.
     * @return copy of the provided image.
     */
    static BufferedImage deepCopy( BufferedImage image ) {
        ColorModel cm = image.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = image.copyData( null);
        return new BufferedImage( cm, raster, isAlphaPremultiplied, null);
    }

    /**
     * Checks images for equals their widths and heights.
     * @param image1 {@code BufferedImage} object of the first image.
     * @param image2 {@code BufferedImage} object of the second image.
     */
    public static void checkCorrectImageSize( BufferedImage image1, BufferedImage image2 ) {
        if( image1.getHeight() != image2.getHeight() || image1.getWidth() != image2.getWidth() )
            throw new IllegalArgumentException( "Images dimensions mismatch" );
    }

    /**
     * Says if the two pixels equal or not. The rule is the difference between two pixels
     * need to be more then 10%.
     * @param x the X value of the binary matrix.
     * @param y the Y value of the binary matrix.
     * @param image1 {@code BufferedImage} object of the first image.
     * @param image2 {@code BufferedImage} object of the second image.
     * @return {@code true} if they' are difference, {@code false} otherwise.
     */
    public static boolean isDifferent( int x, int y, BufferedImage image1, BufferedImage image2 ){
        int[] im1= image1.getRaster().getPixel( x,y,new int[4] );
        int[] im2= image2.getRaster().getPixel( x,y,new int[4] );
        //gets modules of the images:
        double mod1 = Math.sqrt( im1[0] * im1[0] + im1[1] * im1[1] + im1[2] * im1[2] );
        double mod2 = Math.sqrt( im2[0] * im2[0] + im2[1] * im2[1] + im2[2] * im2[2] );
        // gets module of the difference of images.
        double mod3 = Math.sqrt( Math.abs( im1[0] - im2[0] ) * Math.abs( im1[0] - im2[0] ) +
                Math.abs( im1[1] - im2[1] ) * Math.abs( im1[1] - im2[1] ) +
                Math.abs( im1[2] - im2[2] ) * Math.abs( im1[2] - im2[2] ) );
        double imageChanges1 = mod3 / mod1;
        double imageChanges2 = mod3 / mod2;
        return imageChanges1 > 0.1 && imageChanges2 > 0.1;
    }

    /**
     * Create a {@link Rectangle} object.
     * @param matrix the matrix of the Conformity pixels.
     * @param counter the number from marks regions.
     * @return the {@link Rectangle} object.
     */
    public static Rectangle createRectangle( int[][] matrix, int counter ) {
        Rectangle rect = new Rectangle();

        for ( int y = 0; y < matrix.length; y++ ) {
            for ( int x = 0; x < matrix[0].length; x++ ) {
                if ( matrix[y][x] == counter ) {
                    if ( x < rect.getMinX() ) rect.setMinX( x );
                    if ( x > rect.getMaxX() ) rect.setMaxX( x );

                    if ( y < rect.getMinY() ) rect.setMinY( y );
                    if ( y > rect.getMaxY() ) rect.setMaxY( y );
                }
            }
        }
        return rect;
    }

    /**
     * Populate binary matrix by "0" and "1". If the pixels are difference set it as "1", otherwise "0".
     * @param image1 {@code BufferedImage} object of the first image.
     * @param image2 {@code BufferedImage} object of the second image.
     * @return populated binary matrix.
     */
    static int[][] populateTheMatrixOfTheDifferences( BufferedImage image1, BufferedImage image2 ) {
        int[][] matrix = new int[image1.getWidth()][image1.getHeight()];
        for ( int y = 0; y < image1.getHeight(); y++ ) {
            for ( int x = 0; x < image1.getWidth(); x++ ) {
                matrix[x][y] = isDifferent( x, y, image1, image2 ) ? 1 : 0;
            }
        }
        return matrix;
    }

    /**
     * Reads image from the provided path.
     * @param path the path where contains image.
     * @return the {@code BufferedImage} object of this specific image.
     * @throws IOException
     * @throws URISyntaxException
     */
    public static BufferedImage readImageFromResources( String path ) throws IOException, URISyntaxException {
        return ImageIO.read( new File( ImageComparison.class.getClassLoader().getResource ( path ).toURI().getPath() ) );
    }

    /**
     * Save image to the provided path.
     * @param path the path to the saving image.
     * @param image the {@code BufferedImage} object of this specific image.
     * @throws IOException
     */
    public static void saveImage(String path, BufferedImage image ) throws IOException {
        // make dir if it's not using from Gradle.
        new File( path ).mkdirs();
        ImageIO.write( image, "png", new File( path ) );
    }
}
