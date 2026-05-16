package ean.solid;

// ❌ Viola DIP: Clases concretas en lugar de abstracciones
public class EmailService {
    public void sendEmail(String message) {
        System.out.println("Enviando email por SMTP: " + message);
    }
}