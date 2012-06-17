/**
 * 
 */
package genj.util;



/**
 * A text and mnemonic wrapper
 */
public class MnemonicAndText {
  
  private char mnemonic = '\0';
  private String text;

  public MnemonicAndText(String label) {
    
    // keep it first
    text = label!=null ? label : "";
    
    // look for mnemonic
    int i = text.indexOf('~');    
    if (i<0) {
      // e.g. "New File"
      mnemonic = text.length()>0 ? text.charAt(0) : 0;
    } else if (i==text.length()-1) {
      // e.g. "New File~"
      mnemonic = text.length()>0 ? text.charAt(0) : 0;
      text = text.substring(0, text.length()-1);
    } else {
      // e.g. "New ~File"
      mnemonic = text.charAt(i+1);
      text = text.substring(0,i)+text.substring(i+1);
    }
    
    // 20060218 convert tilded char to uppercase - otherwise the KeyCode generated
    // by e.g. BasicButtonListener.updateMnemonicBinding() doesn't work since it 
    // assumes JButtons.getMnemonic() returns a valid VK_ code - apparently lower
    // case characters don't fit that
    mnemonic = Character.toUpperCase(mnemonic);
    
    // done
  }
  
  /** mnemonic representation */
  public char getMnemonic() {
    return mnemonic;
  }
  
  /** text representation */
  public String getText(String markup) {
    if (mnemonic==0)
      return getText();
    return text + " ["+markup+mnemonic+"]";
  }

  /** text representation */
  public String getText() {
    return text;
  }

  /** string representation */
  public String toString() {
    return mnemonic==0 ? text : text+"["+mnemonic+"]";
  }
  
}