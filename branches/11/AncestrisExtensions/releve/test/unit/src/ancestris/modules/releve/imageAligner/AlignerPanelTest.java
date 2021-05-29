package ancestris.modules.releve.imageAligner;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michel
 */


public class AlignerPanelTest {
    
    public AlignerPanelTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of saveCurrentImage method, of class AlignerPanel.
     */
    @Test
    public void testSaveCurrentImage() {
        System.out.println("saveCurrentImage");
        BufferedImage currentImage = null;
        //AlignerPanel instance = new AlignerPanel();
        
        /*
        try {
            String fileName = "526626-309.JPG";
            File inputDirectory = new File ("D:\\Genealogie\\100PHOTO\\test");
            File outputDirectory = new File ("D:\\Genealogie\\100PHOTO\\testo");
            
            String intputFileName = inputDirectory.getCanonicalPath()+ File.separator + fileName;
            currentImage = ImageIO.read(new File(intputFileName)); 
            AffineTransform tx = new AffineTransform();
            tx.translate(5, 10 );

            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            currentImage = op.filter(currentImage, null);
        
            
            String fullFileName = outputDirectory.getCanonicalPath() + File.separator + fileName;
            File outputFile= new File(fullFileName);
            
            // enregistrement avec compression par defaut 
            //ImageIO.write(currentImage, "jpg", file);
            
            // enregistrement avec compression parametree
            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            jpgWriteParam.setCompressionQuality(0.7f);

            ImageOutputStream  outputStream =  ImageIO.createImageOutputStream(outputFile);
            jpgWriter.setOutput(outputStream);            
            IIOImage outputImage = new IIOImage(currentImage, null, null);
            jpgWriter.write(null, outputImage, jpgWriteParam);
            outputStream.flush();;
            outputStream.close();
            jpgWriter.dispose();
            
            assertTrue("ouput file existe", outputFile.exists());
        } catch (IOException ex) {
            System.err.println("AlignerPanel.showImage error ="+ ex.getMessage()); 
            fail(ex.getMessage());
        }
        */
        
    }

}
