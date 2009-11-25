package com.nmeier.iou;

import android.text.InputFilter;
import android.text.Spanned;

/** currency filter for text views */
public class CurrencyFilter implements InputFilter {

	public final static char DELIM = '.';
	
	@Override
	public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

		StringBuffer result = new StringBuffer();
		
		// add 0 prefix if necessary
		if (dstart==0 && (end!=start && source.charAt(start)==DELIM))
			result.append('0');
		
		// check for existence of surviving delimiters
		boolean delim = false;
		int max = Integer.MAX_VALUE;
		for (int i=0;i<dstart;i++) {
			if (dest.charAt(i)==DELIM) {
				delim = true;
				max = Math.max(0, i+3-dstart);
				break;
			}
		}
		for (int i=dend;i<dest.length();i++) if (dest.charAt(i)==DELIM) delim = true;
		
		// insert the source data 
		for (int i=start,j=0 ; i<end && j<max ; i++) {
			char c = source.charAt(i);
			
			// check delimiters
			if (c==DELIM) {
				if (delim) continue;
				delim = true;
				max = 2;
			}
			
			// check leading zero
			if (c=='0'&&dstart==0&&result.length()==0)
				continue;
			
			// take it
			result.append(c);
			j++;
		}
		
		// done
		return result;
	}
}
