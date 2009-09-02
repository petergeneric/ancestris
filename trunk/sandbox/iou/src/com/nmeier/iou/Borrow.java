package com.nmeier.iou;

public class Borrow extends Transaction {
  
  public Borrow() {
    super(R.string.from);
  }

  @Override
  protected void perform(Contact contact, int cents) {
    IOU.getInstance().borrow(null, contact, cents);
  }

}
