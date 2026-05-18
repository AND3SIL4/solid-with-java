package ean.solid.services;

import ean.solid.interfaces.Notification;

public class EmailService implements Notification {
    @Override
    public void sendNotification(String message) {
        System.out.println("Enviando email por SMTP: " + message);
    }
}