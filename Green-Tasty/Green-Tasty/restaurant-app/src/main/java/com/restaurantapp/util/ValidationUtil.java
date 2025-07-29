package com.restaurantapp.util;

import com.restaurantapp.dto.PreOrderState;
import com.restaurantapp.dto.SignIn;
import com.restaurantapp.dto.SignUp;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.regex.Pattern;

public class ValidationUtil {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_PASSWORD_LENGTH = 16;
    private static final int MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    public static String validateSignUp(SignUp signUp) {
        if (signUp.getEmail() == null || signUp.getEmail().isEmpty()) {
            return "Email is missing or empty.";
        }
        if (!isValidEmail(signUp.getEmail())) {
            return "Invalid email format.";
        }
        if (signUp.getFirstName() == null || signUp.getFirstName().isEmpty()) {
            return "First name is missing or empty.";
        }
        if (signUp.getFirstName().length() > MAX_NAME_LENGTH) {
            return "First name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters.";
        }
        if (signUp.getLastName() == null || signUp.getLastName().isEmpty()) {
            return "Last name is missing or empty.";
        }
        if (signUp.getLastName().length() > MAX_NAME_LENGTH) {
            return "Last name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters.";
        }
        if (signUp.getPassword() == null || signUp.getPassword().isEmpty()) {
            return "Password is missing or empty.";
        }
        if (!isValidPassword(signUp.getPassword())) {
            return "Password does not meet the required criteria.";
        }
        return null;
    }

    public static String validateSignIn(SignIn signIn) {
        if (signIn.getEmail() == null || signIn.getEmail().isEmpty()) {
            return "Email is missing or empty.";
        }
        if (!isValidEmail(signIn.getEmail())) {
            return "Invalid email format.";
        }
        if (signIn.getPassword() == null || signIn.getPassword().isEmpty()) {
            return "Password is missing or empty.";
        }
        return null;
    }

    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email) && !email.contains("..");
    }

    public static boolean isValidPassword(String password) {
        if (password.length() > MAX_PASSWORD_LENGTH) {
            return false; // Password exceeds maximum length
        }
        if (!password.matches(".*[A-Z].*")) {
            return false; // Missing uppercase letter
        }
        if (!password.matches(".*[a-z].*")) {
            return false; // Missing lowercase letter
        }
        if (!password.matches(".*\\d.*")) {
            return false; // Missing number
        }
        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            return false; // Missing special character
        }
        return true;
    }

    public static String validateUpdateProfile(String firstName,String lastName) {

        if (firstName == null || firstName.isEmpty()) {
            return "First name is missing or empty.";
        }
        if (firstName.length() > MAX_NAME_LENGTH) {
            return "First name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters.";
        }
        if (lastName == null || lastName.isEmpty()) {
            return "Last name is missing or empty.";
        }
        if (lastName.length() > MAX_NAME_LENGTH) {
            return "Last name exceeds the maximum length of " + MAX_NAME_LENGTH + " characters.";
        }
        return null;
    }


    public static String isValidGuestNumber(int guestNumber)
    {
        if(guestNumber <= 0)
            return "Guest number can't be less than 1";
        return null;
    }

    public static String isValidTimeSlot(String timeFrom, String timeTo)
    {
        if(timeFrom == null || timeFrom.isEmpty() || timeTo == null || timeTo.isEmpty())
            return "Start or end time of booking can't be empty or missing";

        return null;
    }

    public static String isValidString(String original , String strName)
    {
        if(original == null || original.isBlank()){
            return strName + " can't be empty or null!!!";
        }
        return null;
    }

    public static String isValidQuantity(long quantity, int guestNumber)
    {
        if(quantity > guestNumber * 3)
            return "Sorry, for a guest number of : " + guestNumber + ", you can Pre-Order upto only " + guestNumber*3 + " number of units per Dish item";
        return null;
    }

    public static String validatePreOrderStateValue(String preOrderState)
    {
        String msg = isValidString(preOrderState, "Pre_Order_State");
        if(msg != null)
            return msg;

        for(PreOrderState state : PreOrderState.values())
        {
            if((preOrderState.equalsIgnoreCase(state.name())))
                return null;
        }
        return "Invalid value of Pre_Order_State!!!";
    }

    private static boolean hasValidBase64Padding(String base64) {
        int length = base64.length();

        // Must be multiple of 4
        if (length % 4 != 0) return false;

        // Count '=' padding characters at the end
        int paddingCount = 0;
        if (length >= 1 && base64.charAt(length - 1) == '=') paddingCount++;
        if (length >= 2 && base64.charAt(length - 2) == '=') paddingCount++;

        // Padding should be only 0, 1 or 2
        if (paddingCount > 2) return false;

        // '=' must appear only at the end
        if (paddingCount > 0 && !base64.endsWith("=".repeat(paddingCount))) return false;

        return true;
    }

    public static String validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.isBlank()) {
            return null;
        }
        try {
            // Validate that the string is Base64-encoded
            if (!Base64.isBase64(base64Image)) {
                return "The uploaded image is not a valid Base64-encoded string.";
            }

            // Decode Base64 to a byte array
            byte[] imageBytes = Base64.decodeBase64(base64Image);

            // Check that the byte array is not empty
            if (imageBytes.length == 0) {
                return "The uploaded image is empty or invalid.";
            }

            // Check file size (e.g., maximum 5MB)
            if (imageBytes.length > MAX_IMAGE_SIZE) {
                return "The uploaded image exceeds the maximum allowed size of 5MB.";
            }

            // Check that the file starts with the expected JPEG signature (optional: add PNG support if needed)
            // JPEG files usually start with the bytes `0xFF 0xD8`
            if (imageBytes[0] != (byte) 0xFF || imageBytes[1] != (byte) 0xD8) {
                return "The uploaded image is not in a valid JPEG format.";
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);

            if (!(imageBytes[0] == (byte) 0xFF && imageBytes[1] == (byte) 0xD8)) {
                return "The uploaded image is not a valid JPEG image.";
            }

            if(!hasValidBase64Padding(base64Image))
                return "The uploaded image is not a valid Base64 image";

            if (image == null) {
                return "The uploaded data is not a valid image.";
            }

            // Validation passed
            return null;

        } catch (Exception e) {
            // Handle decoding or unexpected errors
            return "An error occurred while processing the uploaded image: " + e.getMessage();
        }
    }
}
