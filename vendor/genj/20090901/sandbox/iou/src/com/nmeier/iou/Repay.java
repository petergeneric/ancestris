package com.nmeier.iou;

public class Repay extends Transaction {

  public Repay() {
    super(R.string.to);
  }
  
  @Override
  protected void perform(Contact contact, int cents) {
    IOU.getInstance().repay(null, contact, cents);
  }

}
