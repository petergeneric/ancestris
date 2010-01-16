package com.nmeier.iou;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;

public class Contact {
  
  private String name, id;
  
  protected Contact(String name, String id) {
    this.name = name;
    this.id = id;
  }
  
  public static Contact getContact(Context context, String id) {
    
    Uri uri = Uri.withAppendedPath(People.CONTENT_URI, id);
    
    Cursor c = context.getContentResolver().query(uri, null, null, null, null);
    if (!c.moveToFirst()) 
      return new Contact(context.getString(R.string.contact_unknown)+"("+id+")", id);
    
    int n = c.getColumnIndex(People.NAME);
    int i = c.getColumnIndex(People._ID);

    return new Contact(c.getString(n), c.getString(i)); 
  }

  public String getName() {
    return name;
  }
}
