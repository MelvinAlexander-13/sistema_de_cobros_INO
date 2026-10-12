package com.example.proyecto_cobros.service;

import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.entity.Pago;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ComprobantePdfService {

    private static final PDColor NARANJA = new PDColor(new float[]{1f, 0.416f, 0f}, PDDeviceRGB.INSTANCE);
    private static final PDColor GRIS_OSCURO = new PDColor(new float[]{0.13f, 0.13f, 0.16f}, PDDeviceRGB.INSTANCE);
    private static final PDColor GRIS_CLARO = new PDColor(new float[]{0.95f, 0.95f, 0.96f}, PDDeviceRGB.INSTANCE);
    private static final PDColor VERDE = new PDColor(new float[]{0.117f, 0.494f, 0.204f}, PDDeviceRGB.INSTANCE);
    private static final PDColor BLANCO = new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE);
    private static final PDColor GRIS_TEXTO = new PDColor(new float[]{0.4f, 0.4f, 0.4f}, PDDeviceRGB.INSTANCE);

    public byte[] generarComprobantePdf(Pago pago, Estudiante estudiante) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float pageWidth = PDRectangle.LETTER.getWidth();
            float margin = 60;
            float contentWidth = pageWidth - (margin * 2);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float y = 760;

                content.setNonStrokingColor(NARANJA);
                content.addRect(0, y + 30, pageWidth, 6);
                content.fill();

                content.setNonStrokingColor(GRIS_OSCURO);
                content.beginText();
                content.setFont(fontBold, 16);
                content.newLineAtOffset(margin, y);
                content.showText("INSTITUTO NACIONAL DE OSICALA");
                content.endText();
                y -= 16;

                content.setNonStrokingColor(GRIS_TEXTO);
                content.beginText();
                content.setFont(fontNormal, 10);
                content.newLineAtOffset(margin, y);
                content.showText("Codigo: 13624");
                content.endText();
                y -= 34;

                content.setNonStrokingColor(GRIS_OSCURO);
                content.addRect(margin, y - 8, contentWidth, 34);
                content.fill();

                content.setNonStrokingColor(BLANCO);
                content.setFont(fontBold, 15);
                float tituloTexto = fontBold.getStringWidth("COMPROBANTE DE PAGO") / 1000 * 15;
                content.beginText();
                content.newLineAtOffset(margin + (contentWidth - tituloTexto) / 2, y + 2);
                content.showText("COMPROBANTE DE PAGO");
                content.endText();
                y -= 50;

                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                String seccion = (estudiante != null && estudiante.getSeccion() != null) ? estudiante.getSeccion() : "N/D";
                String metodo = (pago.getMetodo_pago() != null) ? pago.getMetodo_pago() : "efectivo";

                content.setNonStrokingColor(GRIS_CLARO);
                content.addRect(margin, y - 70, contentWidth, 88);
                content.fill();

                content.setFont(fontNormal, 11);
                content.setNonStrokingColor(GRIS_OSCURO);

                String[] filaIzq = {
                        "No. de comprobante:", "Fecha y hora:", "Estudiante:", "NIE:", "Seccion:"
                };
                String[] filaDer = {
                        pago.getNumeroComprobante(),
                        fechaHora,
                        pago.getEstudiante(),
                        String.valueOf(pago.getNie()),
                        seccion
                };

                float yFila = y - 10;
                for (int i = 0; i < filaIzq.length; i++) {
                    content.beginText();
                    content.setFont(fontBold, 10.5f);
                    content.newLineAtOffset(margin + 14, yFila);
                    content.showText(filaIzq[i]);
                    content.endText();

                    content.beginText();
                    content.setFont(fontNormal, 10.5f);
                    content.newLineAtOffset(margin + 170, yFila);
                    content.showText(filaDer[i]);
                    content.endText();

                    yFila -= 16;
                }

                y -= 100;

                content.setNonStrokingColor(NARANJA);
                content.addRect(margin, y - 70, contentWidth, 88);
                content.fill();

                content.setNonStrokingColor(BLANCO);

                String[] filaIzq2 = { "Mes pagado:", "Monto:", "Metodo de pago:", "Estado:" };
                String[] filaDer2 = {
                        pago.getMes(),
                        "$" + pago.getMonto(),
                        metodo,
                        pago.getEstado()
                };

                float yFila2 = y - 10;
                for (int i = 0; i < filaIzq2.length; i++) {
                    content.beginText();
                    content.setFont(fontBold, 11);
                    content.newLineAtOffset(margin + 14, yFila2);
                    content.showText(filaIzq2[i]);
                    content.endText();

                    content.beginText();
                    content.setFont(fontBold, 11);
                    content.newLineAtOffset(margin + 170, yFila2);
                    content.showText(filaDer2[i]);
                    content.endText();

                    yFila2 -= 18;
                }

                y -= 110;

                content.setNonStrokingColor(GRIS_TEXTO);
                content.setFont(fontNormal, 9);
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Este comprobante es generado automaticamente por el sistema Cobros INO.");
                content.endText();
                y -= 14;
                content.beginText();
                content.newLineAtOffset(margin, y);
                content.showText("Instituto Nacional de Osicala - Codigo 13624");
                content.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}