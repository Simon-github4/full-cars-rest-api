package com.fullcars.restapi.service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fullcars.restapi.enums.EventType;
import com.fullcars.restapi.event.SaleEvent;
import com.fullcars.restapi.model.Sale;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    /*@Async
    @EventListener
    public void handleSaleEvent(SaleEvent event) {
        if (event.getEventType() == EventType.INSERT) {
            try {
                Sale sale = event.getEntity();
        		File remito = Paths.get(sale.getRemitoPath()).toFile();
                sendEmail(
                    sale.getCustomer().getEmail(),
                    "Nueva venta registrada",
                    "<h1>Se generó una venta</h1><p>ID: " + sale.getId() + "</p>",
                    remito
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
    @Async
    public void sendEmail(String to, String subject, String text) throws Exception {
        if (!validarEmail(to)) {
            throw new IllegalArgumentException("Correo inválido: " + to);
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);
    }

    @Async
    public void sendEmail(String to, String subject, String text, java.io.File file) throws Exception {
        if (!validarEmail(to)) {
            throw new IllegalArgumentException("Correo inválido: " + to);
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        if (file != null) {
            helper.addAttachment(file.getName(), file);
        }

        mailSender.send(message);
    }

    private boolean validarEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    
}


