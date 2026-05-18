package ean.solid.services;

import ean.solid.interfaces.OrderStorage;

public class MySQLDatabase implements OrderStorage {
    @Override
    public void saveOrder(String orderDetails) {
        System.out.println("Guardando en base de datos MySQL: " + orderDetails);
    }
}