package com.jasperframework.example.controller;

import com.jasperframework.core.ExportFormat;
import com.jasperframework.core.ReportContext;
import com.jasperframework.core.ReportEngine;
import com.jasperframework.example.model.InvoiceItem;
import com.jasperframework.example.model.OrderItem;
import com.jasperframework.example.model.Product;
import com.jasperframework.exporter.ExportService;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing report generation endpoints.
 *
 * <pre>
 * GET /reports/invoice          — generate sample invoice (PDF by default)
 * GET /reports/product          — generate product catalogue
 * GET /reports/order            — generate order with items
 *
 * All endpoints accept ?format=PDF|XLSX|CSV
 * </pre>
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportEngine engine;
    private final ExportService exportService;

    public ReportController(ReportEngine engine, ExportService exportService) {
        this.engine = engine;
        this.exportService = exportService;
    }

    @GetMapping("/invoice")
    public ResponseEntity<byte[]> invoice(
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        List<InvoiceItem> items = List.of(
                new InvoiceItem("Widget A", 10, 25.00),
                new InvoiceItem("Widget B", 5, 49.99),
                new InvoiceItem("Service Fee", 1, 150.00)
        );
        double total = items.stream().mapToDouble(InvoiceItem::getLineTotal).sum();

        ReportContext context = ReportContext.builder("reports/invoice.jrxml")
                .parameter("invoiceNumber", "INV-2024-001")
                .parameter("customerName", "Acme Corp")
                .parameter("invoiceDate", "2024-12-01")
                .parameter("totalAmount", String.format("%.2f", total))
                .dataSource(new JRBeanCollectionDataSource(items))
                .build();

        return exportReport(context, format, "invoice");
    }

    @GetMapping("/product")
    public ResponseEntity<byte[]> product(
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        List<Product> products = List.of(
                new Product("Laptop Pro 15", "LAP-001", "Electronics", 1299.99, 45),
                new Product("Wireless Mouse", "MOU-002", "Accessories", 29.99, 200),
                new Product("USB-C Hub", "HUB-003", "Accessories", 59.99, 150),
                new Product("Monitor 27\"", "MON-004", "Electronics", 449.99, 30),
                new Product("Keyboard Mech", "KEY-005", "Accessories", 89.99, 120)
        );

        ReportContext context = ReportContext.builder("reports/product.jrxml")
                .parameter("reportTitle", "Product Catalogue")
                .dataSource(new JRBeanCollectionDataSource(products))
                .build();

        return exportReport(context, format, "product");
    }

    @GetMapping("/order")
    public ResponseEntity<byte[]> order(
            @RequestParam(defaultValue = "PDF") ExportFormat format) {

        List<OrderItem> items = List.of(
                new OrderItem("Laptop Pro 15", 2, 1299.99),
                new OrderItem("Wireless Mouse", 2, 29.99),
                new OrderItem("USB-C Hub", 1, 59.99)
        );

        ReportContext context = ReportContext.builder("reports/order_with_items.jrxml")
                .parameter("orderId", "ORD-2024-042")
                .parameter("customerName", "Jane Smith")
                .parameter("orderDate", "2024-12-15")
                .parameter("orderStatus", "SHIPPED")
                .dataSource(new JRBeanCollectionDataSource(items))
                .build();

        return exportReport(context, format, "order");
    }

    private ResponseEntity<byte[]> exportReport(ReportContext context, ExportFormat format,
                                                 String filename) {
        JasperPrint print = engine.generateReport(context);
        byte[] bytes = exportService.exportToBytes(print, format);

        String extension = format.name().toLowerCase();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "." + extension + "\"")
                .contentType(mediaTypeFor(format))
                .body(bytes);
    }

    private static MediaType mediaTypeFor(ExportFormat format) {
        return switch (format) {
            case PDF -> MediaType.APPLICATION_PDF;
            case XLSX -> MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
        };
    }
}
