package com.reportsapp.util;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatter {

    private DateFormatter() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Converts a date string from multiple possible formats to the standard format dd-MM-yyyy.
     *
     * @param date The input date string in one of the supported formats.
     * @return The date string formatted as dd-MM-yyyy.
     * @throws DateTimeParseException If the input date cannot be parsed.
     */
    public static String convertToStandardFormat(String date) {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter[] inputFormatters = getSupportedInputFormatters();

        LocalDate parsedDate = getParsedDate(date, inputFormatters);
        return parsedDate.format(outputFormatter);
    }

    /**
     * Converts a date string from multiple possible formats to the ISO format yyyy-MM-dd.
     *
     * @param date The input date string in one of the supported formats.
     * @return The date string formatted as yyyy-MM-dd.
     * @throws DateTimeParseException If the input date cannot be parsed.
     */
    public static String convertToISOFormat(String date) {
        DateTimeFormatter outputFormatter = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd format
        DateTimeFormatter[] inputFormatters = getSupportedInputFormatters();

        LocalDate parsedDate = getParsedDate(date, inputFormatters);
        return parsedDate.format(outputFormatter);
    }

    /**
     * Attempts to parse the date string using multiple supported formats.
     *
     * @param date            The input date string.
     * @param inputFormatters Array of supported input formatters.
     * @return The parsed LocalDate object.
     * @throws DateTimeParseException If none of the formats match the input date.
     */
    @NotNull
    private static LocalDate getParsedDate(String date, DateTimeFormatter[] inputFormatters) {
        for (DateTimeFormatter formatter : inputFormatters) {
            try {
                return LocalDate.parse(date, formatter);
            } catch (DateTimeParseException e) {
                // Ignore and try the next format
            }
        }
        throw new DateTimeParseException("Date provided in unsupported format.", date, 0);
    }

    /**
     * Provides an array of supported input date formatters.
     *
     * @return An array of DateTimeFormatter objects.
     */
    @NotNull
    private static DateTimeFormatter[] getSupportedInputFormatters() {
        return new DateTimeFormatter[]{
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd-MM-yy"),
                DateTimeFormatter.ofPattern("yy-MM-dd")
        };
    }
}
