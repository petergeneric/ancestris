package com.nmeier.iou;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/** the main business class instantiated once */
public class IOU {
	
  public final static String TAG = "IOU";
  
	private Context context;
	
	private static IOU instance;
	
	/** constructor */
	public IOU(Bundle inState, Context context) {
	  this.context = context;
    instance = this;
	}
	
	public static IOU getInstance() {
	  return instance;
	}

	/** save current state */
	public void saveState(Bundle outState) {
		// store parties
	}

	/** perform a borrow transaction */
	public void borrow(Contact from, Contact to, int cents) {
		Log.d(TAG, "borrow transaction - $"+(int)(cents/100)+"."+(cents%100)+" from "+from+" to "+to);
	}
	
  /** perform a lend transaction */
  public void lend(Contact from, Contact to, int cents) {
    Log.d(TAG, "lend transaction - $"+(int)(cents/100)+"."+(cents%100)+" from "+from+" to "+to);
  }
  
	private void remember(CharSequence party) {
		String value = party.toString();
	}

	/** perform a repay transaction */
	public void repay(Contact from, Contact to, double amount) {
		Log.d(TAG, "repay transaction - $"+amount+" from "+from+" to "+to);
	}

//	/** lookup of all known parties (includes contacts) */
//	public List<? extends String> getParties() {
//	  
//    // find parties and contacts
//    List<String> values = new ArrayList<String>();
//    values.addAll(parties);
//    
//    Cursor c = context.getContentResolver().query(People.CONTENT_URI, null, null, null, null);
//    if (c.moveToFirst()) {
//      int n = c.getColumnIndex(People.NAME);
//      int i = c.getColumnIndex(People._ID);
//      do {
//        String name = c.getString(n); 
//        if (!values.contains(name))
//          values.add(name);
//        Log.d(TAG, "Found contact id="+c.getString(i)+" name="+name);
//      } while (c.moveToNext());
//    }
//
//    // sort
//    Collections.sort(values);
//    
//		return values;
//	}

}
