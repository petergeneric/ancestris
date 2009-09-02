package genj.reportrunner;

import java.io.CharArrayWriter;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;

/**
 * Opens a file to write to only when a write is requested.
 * Otherwise the file is never opened.
 *
 * @author Przemek Wiech <pwiech@losthive.org>
 * @version $Id: OnDemandFileWriter.java,v 1.1 2008/11/19 10:03:28 pewu Exp $
 */
public class OnDemandFileWriter extends FilterWriter
{
	/**
	 * Name of file to write.
	 */
	private String filename;

	/**
	 * Whether the file has been opened.
	 */
	private boolean open = false;

	/**
	 * Constructs the object.
	 */
	public OnDemandFileWriter(String filename)
	{
		super(new CharArrayWriter()); // Dummy writer is needed
		this.filename = filename;
	}

	/**
	 * If the file isn't opened yet, opens the file.
	 */
	private void ensureOpen() throws IOException
	{
		if (!open)
		{
			out = new FileWriter(filename);
			open = true;
		}
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException
	{
		ensureOpen();
		out.write(cbuf, off, len);
	}

	@Override
	public void write(int c) throws IOException {
		ensureOpen();
		out.write(c);
	}

	@Override
	public void write(String str, int off, int len) throws IOException {
		ensureOpen();
		out.write(str, off, len);
	}
}
