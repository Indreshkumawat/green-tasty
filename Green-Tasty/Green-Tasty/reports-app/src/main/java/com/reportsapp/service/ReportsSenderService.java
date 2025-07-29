package com.reportsapp.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import com.reportsapp.feign_client.ReportsFeignClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
public class ReportsSenderService {
    private final ReportsFeignClient reportsFeignClient;
    private final AmazonSimpleEmailService sesClient;
    private final SpringTemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(ReportsSenderService.class);

    @Autowired
    public ReportsSenderService(ReportsFeignClient reportsFeignClient, @Qualifier("sesClient")AmazonSimpleEmailService sesClient,SpringTemplateEngine templateEngine) {
        this.reportsFeignClient = reportsFeignClient;
        this.sesClient = sesClient;
        this.templateEngine = templateEngine;
    }

    public void sendReports(){
        // Generate links for Staff Performance (Waiter) reports
        String waiterPdfUrl = generateReport("Staff_Performance", "pdf");
        String waiterCsvUrl = generateReport("Staff_Performance", "csv");

        // Generate links for Location Performance reports
        String locationPdfUrl = generateReport("Location_Performance", "pdf");
        String locationCsvUrl = generateReport("Location_Performance", "csv");

        String emailHTMLBody = generateEmailBody(waiterCsvUrl, waiterPdfUrl, locationCsvUrl, locationPdfUrl);

        sendEmail(
                "krishnendubose29@gmail.com",               // Sender email
                "krishnendu_bose@epam.com",                       // Recipient email
                emailHTMLBody
        );
    }

    private String generateReport(String reportType, String format) {
        try {
            // Trigger the report generation request
            Map<String, Object> response = reportsFeignClient.getReports(reportType, null, null, null, null);

            // Check and return links dynamically based on the format
            if ("pdf".equalsIgnoreCase(format)) {
                return (String) response.get("downloadLinkPDF"); // Extract PDF link from the map
            } else if ("csv".equalsIgnoreCase(format)) {
                return (String) response.get("downloadLinkCSV"); // Extract CSV link from the map
            } else {
                throw new IllegalArgumentException("Unsupported format: " + format);
            }
        } catch (Exception e) {
            // Log an error and gracefully handle it
            logger.error("Failed to generate report of type: {} due to {}" ,reportType, e.getMessage());
            return null;
        }
    }

    private String generateEmailBody(String csvWaiterUrl, String pdfWaiterUrl, String csvLocationUrl, String pdfLocationUrl) {
        // Prepare the Thymeleaf context with dynamic variables
        Context context = new Context();
        context.setVariable("csvWaiterUrl", csvWaiterUrl);
        context.setVariable("pdfWaiterUrl", pdfWaiterUrl);
        context.setVariable("csvLocationUrl", csvLocationUrl);
        context.setVariable("pdfLocationUrl", pdfLocationUrl);

        // Process the Thymeleaf template
        return templateEngine.process("report-email-template", context);
    }

    private void sendEmail(String sender, String recipient, String emailBody) {
        // Prepare SES email request
        SendEmailRequest emailRequest = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(recipient))
                .withMessage(new Message()
                        .withSubject(new Content().withData("Weekly Reports"))
                        .withBody(new Body().withHtml(new Content(emailBody))))
                .withSource(sender);

        sesClient.sendEmail(emailRequest);
    }
}
