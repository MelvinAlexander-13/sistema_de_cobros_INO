package com.example.proyecto_cobros.service;

import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.entity.Mensualidad;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportePdfService {

    private static final String[] MESES = {
        "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre"
    };

    private static final Map<String, Integer> MES_NUMERO = new HashMap<>();
    static {
        MES_NUMERO.put("Febrero", 2);
        MES_NUMERO.put("Marzo", 3);
        MES_NUMERO.put("Abril", 4);
        MES_NUMERO.put("Mayo", 5);
        MES_NUMERO.put("Junio", 6);
        MES_NUMERO.put("Julio", 7);
        MES_NUMERO.put("Agosto", 8);
        MES_NUMERO.put("Septiembre", 9);
        MES_NUMERO.put("Octubre", 10);
        MES_NUMERO.put("Noviembre", 11);
    }

    private static final PDColor NARANJA = new PDColor(new float[]{1f, 0.416f, 0f}, PDDeviceRGB.INSTANCE);
    private static final PDColor GRIS_OSCURO = new PDColor(new float[]{0.13f, 0.13f, 0.16f}, PDDeviceRGB.INSTANCE);
    private static final PDColor GRIS_CLARO = new PDColor(new float[]{0.95f, 0.95f, 0.96f}, PDDeviceRGB.INSTANCE);
    private static final PDColor VERDE = new PDColor(new float[]{0.117f, 0.494f, 0.204f}, PDDeviceRGB.INSTANCE);
    private static final PDColor ROJO = new PDColor(new float[]{0.753f, 0.224f, 0.169f}, PDDeviceRGB.INSTANCE);
    private static final PDColor BLANCO = new PDColor(new float[]{1f, 1f, 1f}, PDDeviceRGB.INSTANCE);

    public byte[] generarReportePdf(Estudiante estudiante, int anio, List<Pago> pagosDelAnio,
                                     List<Mensualidad> mensualidadesDelAnio) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            float pageWidth = PDRectangle.LETTER.getWidth();
            float margin = 50;
            float contentWidth = pageWidth - (margin * 2);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

                float y = 780;

                content.setNonStrokingColor(NARANJA);
                content.addRect(0, y + 25, pageWidth, 6);
                content.fill();

                content.setNonStrokingColor(GRIS_OSCURO);
                content.beginText();
                content.setFont(fontBold, 16);
                content.newLineAtOffset(margin, y);
                content.showText("INSTITUTO NACIONAL DE OSICALA");
                content.endText();
                y -= 16;

                content.setNonStrokingColor(new PDColor(new float[]{0.4f, 0.4f, 0.4f}, PDDeviceRGB.INSTANCE));
                content.beginText();
                content.setFont(fontNormal, 10);
                content.newLineAtOffset(margin, y);
                content.showText("Codigo: 13624");
                content.endText();
                y -= 30;

                content.setNonStrokingColor(NARANJA);
                content.beginText();
                content.setFont(fontBold, 14);
                content.newLineAtOffset(margin, y);
                content.showText("REPORTE ANUAL DE PAGOS - " + anio);
                content.endText();
                y -= 28;

                float cajaAltura = 78;
                content.setNonStrokingColor(GRIS_CLARO);
                content.addRect(margin, y - cajaAltura + 14, contentWidth, cajaAltura);
                content.fill();

                content.setNonStrokingColor(GRIS_OSCURO);
                content.setFont(fontNormal, 10.5f);

                String fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));

                String[] datosIzq = new String[]{
                        "Estudiante: " + estudiante.getNombreCompleto(),
                        "NIE: " + estudiante.getNie(),
                        "Seccion: " + (estudiante.getSeccion() != null ? estudiante.getSeccion() : "N/D")
                };

                String[] datosDer = new String[]{
                        "Generado: " + fechaHora,
                        "Estado: " + (Boolean.TRUE.equals(estudiante.getActivo()) ? "Activo" : "Inactivo"),
                        "Ciclo: Febrero - Noviembre"
                };

                float yDatos = y - 4;
                for (String linea : datosIzq) {
                    content.beginText();
                    content.newLineAtOffset(margin + 14, yDatos);
                    content.showText(linea);
                    content.endText();
                    yDatos -= 16;
                }

                yDatos = y - 4;
                for (String linea : datosDer) {
                    content.beginText();
                    content.newLineAtOffset(margin + 300, yDatos);
                    content.showText(linea);
                    content.endText();
                    yDatos -= 16;
                }

                y -= cajaAltura + 24;

                float[] colX = { margin, margin + 100, margin + 190, margin + 280, margin + 370 };

                content.setNonStrokingColor(GRIS_OSCURO);
                content.addRect(margin, y - 6, contentWidth, 24);
                content.fill();

                content.setNonStrokingColor(BLANCO);
                content.setFont(fontBold, 10);

                String[] cabeceras = { "Mes", "Estado", "Monto", "Fecha", "Comprobante enviado" };
                for (int i = 0; i < cabeceras.length; i++) {
                    content.beginText();
                    content.newLineAtOffset(colX[i] + 8, y);
                    content.showText(cabeceras[i]);
                    content.endText();
                }

                y -= 24;

                content.setFont(fontNormal, 9.5f);
                BigDecimal totalPagado = BigDecimal.ZERO;
                BigDecimal totalAdeudado = BigDecimal.ZERO;
                boolean filaClara = true;

                for (String nombreMes : MESES) {

                    Pago pagoDelMes = pagosDelAnio.stream()
                            .filter(p -> p.getMes().equalsIgnoreCase(nombreMes))
                            .findFirst()
                            .orElse(null);

                    if (filaClara) {
                        content.setNonStrokingColor(GRIS_CLARO);
                        content.addRect(margin, y - 5, contentWidth, 20);
                        content.fill();
                    }
                    filaClara = !filaClara;

                    content.setNonStrokingColor(GRIS_OSCURO);

                    content.beginText();
                    content.newLineAtOffset(colX[0] + 8, y);
                    content.showText(nombreMes);
                    content.endText();

                    if (pagoDelMes != null) {
                        totalPagado = totalPagado.add(pagoDelMes.getMonto());

                        content.setNonStrokingColor(VERDE);
                        content.beginText();
                        content.newLineAtOffset(colX[1] + 8, y);
                        content.showText("Pagado");
                        content.endText();

                        content.setNonStrokingColor(GRIS_OSCURO);

                        content.beginText();
                        content.newLineAtOffset(colX[2] + 8, y);
                        content.showText("$" + pagoDelMes.getMonto());
                        content.endText();

                        content.beginText();
                        content.newLineAtOffset(colX[3] + 8, y);
                        content.showText(pagoDelMes.getFecha_pago().toString());
                        content.endText();

                        content.setNonStrokingColor(Boolean.TRUE.equals(pagoDelMes.getCorreoEnviado()) ? VERDE : ROJO);
                        content.beginText();
                        content.newLineAtOffset(colX[4] + 8, y);
                        content.showText(Boolean.TRUE.equals(pagoDelMes.getCorreoEnviado()) ? "Si" : "No");
                        content.endText();

                    } else {

                        Integer numeroMes = MES_NUMERO.get(nombreMes);
                        Mensualidad mensualidad = mensualidadesDelAnio.stream()
                                .filter(m -> m.getMes() != null && m.getMes().equals(numeroMes))
                                .findFirst()
                                .orElse(null);

                        BigDecimal montoAdeudadoMes = (mensualidad != null && mensualidad.getMonto() != null)
                                ? mensualidad.getMonto() : BigDecimal.ZERO;

                        totalAdeudado = totalAdeudado.add(montoAdeudadoMes);

                        content.setNonStrokingColor(ROJO);
                        content.beginText();
                        content.newLineAtOffset(colX[1] + 8, y);
                        content.showText("Pendiente");
                        content.endText();

                        content.setNonStrokingColor(GRIS_OSCURO);

                        content.beginText();
                        content.newLineAtOffset(colX[2] + 8, y);
                        content.showText(mensualidad != null ? "$" + mensualidad.getMonto() + " (adeudado)" : "--");
                        content.endText();

                        content.beginText();
                        content.newLineAtOffset(colX[3] + 8, y);
                        content.showText("--");
                        content.endText();

                        content.beginText();
                        content.newLineAtOffset(colX[4] + 8, y);
                        content.showText("--");
                        content.endText();
                    }

                    y -= 20;
                }

                y -= 20;

                // ===== TOTAL PAGADO =====
                content.setNonStrokingColor(NARANJA);
                content.addRect(margin, y - 12, contentWidth, 36);
                content.fill();

                content.setNonStrokingColor(BLANCO);
                content.setFont(fontBold, 14);
                content.beginText();
                content.newLineAtOffset(margin + 16, y + 2);
                content.showText("TOTAL PAGADO EN " + anio + ":");
                content.endText();

                content.setFont(fontBold, 16);
                content.beginText();
                content.newLineAtOffset(margin + contentWidth - 110, y);
                content.showText("$" + totalPagado);
                content.endText();

                y -= 46;

                // ===== TOTAL ADEUDADO =====
                content.setNonStrokingColor(ROJO);
                content.addRect(margin, y - 12, contentWidth, 36);
                content.fill();

                content.setNonStrokingColor(BLANCO);
                content.setFont(fontBold, 14);
                content.beginText();
                content.newLineAtOffset(margin + 16, y + 2);
                content.showText("TOTAL ADEUDADO:");
                content.endText();

                content.setFont(fontBold, 16);
                content.beginText();
                content.newLineAtOffset(margin + contentWidth - 110, y);
                content.showText("$" + totalAdeudado);
                content.endText();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }
}