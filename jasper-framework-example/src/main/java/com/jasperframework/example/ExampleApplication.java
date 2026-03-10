package com.jasperframework.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot example application demonstrating the Jasper Framework.
 * <p>
 * Provides REST endpoints for generating invoice, product, and order reports.
 *
 * <pre>
 * # Generate an invoice as PDF
 * curl http://localhost:8080/reports/invoice -o invoice.pdf
 *
 * # Generate a product catalogue as XLSX
 * curl http://localhost:8080/reports/product?format=XLSX -o products.xlsx
 *
 * # Generate an order report as CSV
 * curl http://localhost:8080/reports/order?format=CSV -o order.csv
 * </pre>
 */
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
