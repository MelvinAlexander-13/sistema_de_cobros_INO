package com.example.proyecto_cobros.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarComprobante(String destinatario,
                                   String nombreEstudiante,
                                   String numeroComprobante,
                                   byte[] pdfBytes) throws MessagingException {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject("Comprobante de pago - Instituto Nacional de Osicala");
        helper.setText(
                "Estimado/a,\n\n" +
                "Adjunto encontrara el comprobante de pago N. " + numeroComprobante +
                " correspondiente a " + nombreEstudiante + ".\n\n" +
                "Instituto Nacional de Osicala\n" +
                "Codigo: 13624",
                false
        );

        helper.addAttachment(
                "comprobante_" + numeroComprobante + ".pdf",
                new ByteArrayResource(pdfBytes)
        );

        mailSender.send(mensaje);
    }
    
        public void enviarReporte(String destinatario,
                               String nombreEstudiante,
                               int anio,
                               byte[] pdfBytes) throws MessagingException {

        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject("Reporte anual de pagos " + anio + " - Instituto Nacional de Osicala");
        helper.setText(
                "Estimado/a,\n\n" +
                "Adjunto encontrara el reporte de pagos correspondiente al año " + anio +
                " de " + nombreEstudiante + ".\n\n" +
                "Instituto Nacional de Osicala\n" +
                "Codigo: 13624",
                false
        );

        helper.addAttachment(
                "reporte_" + anio + ".pdf",
                new ByteArrayResource(pdfBytes)
        );

        mailSender.send(mensaje);
    }
}