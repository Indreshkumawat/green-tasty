package com.restaurantapp.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.restaurantapp.dto.SignIn;
import com.restaurantapp.dto.SignUp;
import com.restaurantapp.dto.UpdatePasswordRequest;
import com.restaurantapp.dto.UserProfileUpdateRequest;
import com.restaurantapp.exception.UnauthorizedException;
import com.restaurantapp.exception.UserAlreadyExistsException;
import com.restaurantapp.exception.UserDoesNotExistsException;
import com.restaurantapp.exception.ValidationException;
import com.restaurantapp.repo.AdminRepo;
import com.restaurantapp.repo.CustomerRepo;
import com.restaurantapp.repo.WaiterRepo;
import com.restaurantapp.security.JWTService;
import com.restaurantapp.util.ValidationUtil;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.Date;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserService {
    private WaiterRepo waiterRepo;
    private CustomerRepo customerRepo;
    private AdminRepo adminRepo;
    private BCryptPasswordEncoder encoder;
    private AmazonS3 s3Client;

    private static final String BUCKET_NAME = "run8-team9-deployment-bucket"; // Replace with your bucket name
    private static final String PROFILE_FOLDER = "images/profile";

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authManager;

    @Autowired
    public UserService(WaiterRepo waiterRepo, CustomerRepo customerRepo, AdminRepo adminRepo, BCryptPasswordEncoder encoder, AmazonS3 s3Client) {
        this.waiterRepo = waiterRepo;
        this.customerRepo = customerRepo;
        this.adminRepo = adminRepo;
        this.encoder = encoder;
        this.s3Client = s3Client;
    }

    public Object userSignUp(SignUp signUp) throws Exception {
        String email = signUp.getEmail();

        String validationError = ValidationUtil.validateSignUp(signUp);
        if (validationError != null) throw new ValidationException(validationError);

        signUp.setPassword(encoder.encode(signUp.getPassword()));

        if (customerRepo.isCustomer(email) ||
                adminRepo.isAdmin(email) ||
                (waiterRepo.isWaiter(email) && waiterRepo.isWaiterSignedUp(email))
        ) throw new UserAlreadyExistsException("A user with this email address already exists.");

        if (waiterRepo.isWaiter(email)) {
            waiterRepo.updateWaiterDetails(signUp);
        } else if (adminRepo.isAdmin(email)) {
            adminRepo.addAdmin(signUp);
        } else {
            customerRepo.addCustomer(signUp);
        }

        return Map.of("message", "User registered successfully.");
    }

    public Object userSignIn(SignIn signIn) throws Exception {
        String validationError = ValidationUtil.validateSignIn(signIn);
        if (validationError != null) throw new ValidationException(validationError);

        String username;
        String role;
        String email = signIn.getEmail();

        if (customerRepo.isCustomer(email) ||
                adminRepo.isAdmin(email) ||
                (waiterRepo.isWaiter(email) && waiterRepo.isWaiterSignedUp(email))
        ) {
            String idToken;

            try {
                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, signIn.getPassword())
                );
                idToken = jwtService.generateToken(email);
            } catch (BadCredentialsException ex) {
                throw new UnauthorizedException("Unauthorized request. Please verify credentials and try again.", ex);
            }

            if (waiterRepo.isWaiter(email)) {
                Map<String, Object> waiterDetails = waiterRepo.getWaiterDetails(email);
                username = waiterDetails.get("first_name").toString() + " " + waiterDetails.get("last_name").toString();
                role = "WAITER";
            } else if (customerRepo.isCustomer(email)) {
                Map<String, Object> custDetails = customerRepo.getCustomerDetails(email);
                username = custDetails.get("first_name").toString() + " " + custDetails.get("last_name").toString();
                role = "CLIENT";
            } else {
                Map<String, Object> adminDetails = adminRepo.getAdminDetails(email);
                username = adminDetails.get("first_name").toString() + " " + adminDetails.get("last_name").toString();
                role = "ADMIN";
            }

            return Map.of("accessToken", idToken,
                    "username", username,
                    "role", role
            );
        } else throw new UserDoesNotExistsException("User doesn't exist.");
    }

    public Map<String,Object> getUsersProfile(String email) {
        try {
            Map<String, Object> userProfile = null;
            if (waiterRepo.isWaiter(email)) userProfile = waiterRepo.getWaiterDetails(email);
            else userProfile = customerRepo.getCustomerDetails(email);
            String fileName = (String) userProfile.get("image_url");

            return Map.of("firstName", userProfile.get("first_name"),"lastName", userProfile.get("last_name"),"imageUrl", s3Client.generatePresignedUrl(new GeneratePresignedUrlRequest(BUCKET_NAME, fileName).withExpiration(new Date(System.currentTimeMillis() + (5L * 60 * 1000)))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProfile(String email, UserProfileUpdateRequest request) throws Exception {

        String base64Image = request.getBase64encodedImage();
        String firstName = request.getFirstName();
        String lastName = request.getLastName();

        String validationError = ValidationUtil.validateUpdateProfile(firstName, lastName);

        if (validationError != null) {
            throw new ValidationException(validationError);
        }

        String imageValidationError = ValidationUtil.validateBase64Image(base64Image);

        if (imageValidationError != null) {
            throw new ValidationException(imageValidationError);
        }

        String imageUrl = null;

        if (base64Image != null && !base64Image.isBlank()) {

            byte[] imageBytes = Base64.decodeBase64(base64Image);

            String fileName = PROFILE_FOLDER + "/" + email + "-" + UUID.randomUUID() + ".jpg";

            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/jpeg");

            s3Client.putObject(BUCKET_NAME, fileName, inputStream, metadata);

            imageUrl = fileName;
        }

        if (customerRepo.isCustomer(email)) {
            customerRepo.updateCustomerDetails(email, request.getFirstName(), request.getLastName(), imageUrl == null ? "" : imageUrl);
        } else if (waiterRepo.isWaiter(email)) {
            waiterRepo.updateWaiterInfo(email, request.getFirstName(), request.getLastName(), imageUrl == null ? "" : imageUrl);
        } else if (adminRepo.isAdmin(email)) {
            adminRepo.updateAdminInfo(email, request.getFirstName(), request.getLastName(), imageUrl == null ? "" : imageUrl);
        } else {
            throw new UserDoesNotExistsException("User not found.");
        }

    }


    public void updateProfilePassword(String email, UpdatePasswordRequest request) throws Exception {

        if(request.newPassword().equals(request.oldPassword())){
            throw new ValidationException("Old Password should not match with new password");
        }


        if (customerRepo.isCustomer(email)) {
            customerRepo.updateCustomerPassword(email, request);
        } else if (waiterRepo.isWaiter(email)) {
            waiterRepo.updateWaiterPassword(email, request);

        } else if (adminRepo.isAdmin(email)) {
            adminRepo.updateAdminPassword(email, request);
        } else {
            throw new UserDoesNotExistsException("User not found.");
        }

    }

    public List<Map<String, String>> getAllCustomers() throws Exception {
        return customerRepo.getAllCustomers();
    }
}
