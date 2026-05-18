package ean.solid.entities.discounts;

import ean.solid.interfaces.CalculateDiscount;

public class DiscountWorker implements CalculateDiscount {
  @Override
  public double calculateDiscount(double price) {
    return price * 0.30;
  }

}
