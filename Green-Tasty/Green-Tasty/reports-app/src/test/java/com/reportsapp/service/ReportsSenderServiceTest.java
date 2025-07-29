package com.reportsapp.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.reportsapp.feign_client.ReportsFeignClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

class ReportsSenderServiceTest {

    @Mock
    private ReportsFeignClient reportsFeignClient;

    @Mock
    private AmazonSimpleEmailService sesClient;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private ReportsSenderService reportsSenderService;

    @Captor
    ArgumentCaptor<SendEmailRequest> emailRequestCaptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendReports_shouldGenerateReportsAndSendEmail() {
        // Arrange
        Map<String, Object> dummyResponse = new HashMap<>();
        dummyResponse.put("downloadLinkPDF", "http://example.com/report.pdf");
        dummyResponse.put("downloadLinkCSV", "http://example.com/report.csv");

        when(reportsFeignClient.getReports(any(), any(), any(), any(), any()))
                .thenReturn(dummyResponse);

        when(templateEngine.process(eq("report-email-template"), any(Context.class)))
                .thenReturn("Rendered Email Body");

        // Act
        reportsSenderService.sendReports();

        // Assert
        verify(reportsFeignClient, times(4)).getReports(any(), any(), any(), any(), any());

        verify(templateEngine).process(eq("report-email-template"), any(Context.class));

        verify(sesClient).sendEmail(emailRequestCaptor.capture());
        SendEmailRequest emailRequest = emailRequestCaptor.getValue();

        // Validate email contents
        assert emailRequest.getSource().equals("krishnendubose29@gmail.com");
        assert emailRequest.getDestination().getToAddresses().contains("krishnendu_bose@epam.com");
        assert emailRequest.getMessage().getSubject().getData().equals("Weekly Reports");
        assert emailRequest.getMessage().getBody().getHtml().getData().equals("Rendered Email Body");
    }
}
