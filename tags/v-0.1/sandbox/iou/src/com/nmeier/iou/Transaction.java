package com.nmeier.iou;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


/** our borrow/lend/repay dialog */
public abstract class Transaction extends Activity {
	
	private int cents = 0;
	private String contact;
	private TextView textAmount;
	private Button buttonFrom, buttonTo, buttonOK;
	private Context context;

  protected static final String
    SELF = "";
  
  public static final String 
    CONTACT = "contact";

  private final static int RC_PICK_CONTACT = 0;
  private int preposition; 
  
  /** constructor */
  protected Transaction(int preposition) {
    this.preposition = preposition;
  }
	
	/** callback - activity getting visible*/
	@Override
	protected void onStart() {
    super.onStart();
    // simulate change
    validate();
	}
	
	private void validate() {
	  
	  // calculate contact name
	  if (contact==null)
	    buttonFrom.setText(R.string.contact);
	  else
	    buttonFrom.setText(Contact.getContact(this, contact).getName());
	  
    ((TextView)findViewById(R.id.textPreposition)).setText(preposition);
	  
		// amount, from, to?
		buttonOK.setEnabled(contact!=null && cents>0);
	}
	
	/** callback - activity creation */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	  
	  // check initial from, to
	  contact = getIntent().getExtras()!=null ? getIntent().getExtras().getString(CONTACT) : null;
		
		// basic layout
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transaction);
		
		// setup currency field (filter & listen)
		textAmount = (TextView)findViewById(R.id.textAmount);
		textAmount.setFilters(new InputFilter[]{ new CurrencyFilter() });
		textAmount.addTextChangedListener(new TextWatcher() {
		  public void afterTextChanged(Editable s) {
		    String t = textAmount.getText().toString().trim();
				cents = t.length()==0 ? 0 : (int)(Float.parseFloat(t)*100);
				validate();
			}
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }
    });
    
    // listen to checks
    final RadioGroup checks = (RadioGroup)findViewById(R.id.groupAmount);
    final RadioButton checkAmount = (RadioButton)findViewById(R.id.checkAmount);
    checks.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        checkAmount.setChecked(checkedId==-1);
      }
    });
    ((RadioButton)findViewById(R.id.checkAmount)).setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) checks.clearCheck();
        textAmount.setEnabled(isChecked);
      }
    });
    checkAmount.setChecked(true);
    
    // setup contact fields
    buttonFrom = (Button)findViewById(R.id.buttonContact);
    buttonFrom.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // select contact from contact list
        startActivityForResult(new Intent(Intent.ACTION_PICK, People.CONTENT_URI), RC_PICK_CONTACT);
      }
    });
    
    // add time to note
    //  ((TextView)findViewById(R.id.textNote)).setText(
    //      "\n\n--- "+new SimpleDateFormat(android.provider.Settings.System.getString(this.getContentResolver(), System.DATE_FORMAT)).format(new Date()));
    
    // listen to buttons
    buttonOK = (Button)findViewById(R.id.buttonOk);
    buttonOK.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        perform(Contact.getContact(Transaction.this, contact), cents);
        finish();
      }
    });
    ((Button)findViewById(R.id.buttonCancel)).setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
      	finish();
      }
    });
    
    // done
	}
	
  protected abstract void perform(Contact contact, int cents);
  
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  
	  // check result code
	  if (resultCode != Activity.RESULT_OK) { 
	    Log.d(IOU.TAG, "Select contact cancelled");
	    return;
	  }

	  // access result data
    Uri contactData = data.getData();
    Cursor c =  managedQuery(contactData, null, null, null, null);
    if (!c.moveToFirst()) {
      Log.d(IOU.TAG, "Contact selected but can't be read");
      return;
    }

    // grab contact
    String name = c.getString(c.getColumnIndexOrThrow(People.NAME));
    String id = c.getString(c.getColumnIndexOrThrow(People._ID));
    Log.d(IOU.TAG, "Contact "+name+" selected");

    // set state/view
    if (requestCode==RC_PICK_CONTACT) 
      contact = id;
      
    validate();

    // done
	}
	
}