package ancestris.modules.commonAncestor.graphics;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author michel
 */


public final class GraphicsOutputFactory {
  public String[] output_types = null;

  private Map<String, IGraphicsOutput> outputs = new LinkedHashMap<>();
  public List<IGraphicsOutput> outputList = new ArrayList<>();


  /**
   * Creates the object
   */
  public GraphicsOutputFactory()
  {
      add("pdf", new PdfWriter());
      add("png", new PngWriter());
      add("svg", new SvgWriter());
      add("screen", new ScreenOutput());
  }

  /**
   * Creates the output class for the given type.
   *
   * @param type output type
   * @param report Containing report. Used to show dialogs and translate strings.
   */
  public IGraphicsOutput createOutput(File  file, String fileTypeName )
  {
      IGraphicsOutput output = outputs.get(fileTypeName);

      if (output == null)
          return null;

      if (output instanceof GraphicsFileOutput)
      {
          GraphicsFileOutput fileOutput = (GraphicsFileOutput)output;
          String extension = fileOutput.getFileExtension();

          if (file == null)
              return null;

          // Add appropriate file extension
          String suffix = "." + extension;
          if (!file.getPath().endsWith(suffix))
              file = new File(file.getPath() + suffix);
          fileOutput.setFile(file);
      }

      return output;
  }

  public void add(String name, IGraphicsOutput output) {
      outputs.put(name, output);
      outputList.add(output);
      output_types = outputs.keySet().toArray(new String[0]);
  }

  public Map<String, IGraphicsOutput>  getOutputList() {
    return  outputs;
  }

  public List<String> getFileTypeNames() {
    List<String> fileOuputNames = new ArrayList<>();
    for ( String fileTypeName : outputs.keySet() ) {
      if ( outputs.get(fileTypeName) instanceof GraphicsFileOutput) {
        fileOuputNames.add(fileTypeName);
      }
    }
    return  fileOuputNames;
 }

}
