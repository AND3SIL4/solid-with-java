package ean.solid.services;

import ean.solid.entities.OrderProcessor;
import ean.solid.entities.Product;
import ean.solid.interfaces.CalculateDiscount;
import ean.solid.interfaces.Notification;
import ean.solid.interfaces.OrderStorage;

public class OrderService {
  Notification notification;
  OrderStorage storage;
  OrderProcessor processor;

  public OrderService(OrderProcessor processor, Notification notification, OrderStorage storage) {
    this.processor = processor;
    this.notification = notification;
    this.storage = storage;
  }

  public void processOrder(Product product, CalculateDiscount userType) {
    double total = processor.processOrder(product, userType);

    storage.saveOrder("Producto: " + product.getName() + ", Total: $" + total);
    notification.sendNotification("Su orden de " + product.getName() + " ha sido procesada.");
  }
}
