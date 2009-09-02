package com.nmeier.iou;

public class Lend extends Transaction {
  
  public Lend() {
    super(R.string.to);
  }

  @Override
  protected void perform(Contact contact, int cents) {
    IOU.getInstance().lend(null, contact, cents);
  }

}
