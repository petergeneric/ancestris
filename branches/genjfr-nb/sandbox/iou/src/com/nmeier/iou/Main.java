package com.nmeier.iou;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

/** IOU main */
public class Main extends Activity {
  
  private IOU iou;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      
      iou = new IOU(savedInstanceState, this);
      
      setContentView(R.layout.main);
      
      super.findViewById(R.id.buttonBorrow).setOnClickListener(new OnClickListener() {
      	public void onClick(View v) {
      	  startActivity(new Intent(Main.this, Borrow.class));
      	}
      });
      super.findViewById(R.id.buttonLend).setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
          startActivity(new Intent(Main.this, Lend.class));
        }
      });
      
  }
    
  @Override
  protected void onSaveInstanceState(Bundle outState) {
  	super.onSaveInstanceState(outState);
  	iou.saveState(outState);
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }
    
}

