package com.fit.serviceImpl;

import com.fit.entity.*;
import com.fit.model.ApiResponse;
import com.fit.model.RegistrationRequestDTO;
import com.fit.repository.AuthorityRepository;
import com.fit.repository.UserRepository;
import com.fit.repository.ForgetPasswordOtpRepository;
import com.fit.repository.RegistrationOtpRepository;
import com.fit.service.LoginSignUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.mongodb.core.query.Update;
import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


@Service
public class LoginSignUpServiceImpl implements LoginSignUpService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MongoOperations mongoOperations;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private  AuthorityRepository authorityRepository;

    @Autowired
    private RegistrationOtpRepository registrationOtpRepository;

    @Autowired
    ForgetPasswordOtpRepository passwordResetOtpRepository;


    @Override
    public void registerUser(RegistrationOtp latestRegistrationOtp) {
        try {
            String collectionName = User.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for User class");
            }
            User user = new User();
            user.setId(getNextSequenceIdValue(collectionName));
            user.setEmail(latestRegistrationOtp.getEmail());
            //user.setPwd(latestRegistrationOtp.getPwd());
            String hashPwd = passwordEncoder.encode(latestRegistrationOtp.getPassword());
            user.setPassword(hashPwd);
            user.setStatus("INACTIVE");
            user.setRegistrationDate(LocalDateTime.now());
            User savedUser = userRepository.save(user);

            String authorityCollectionName = Authority.class.getAnnotation(Document.class).collection();
                Authority roleAdmin = new Authority();
                roleAdmin.setId(getNextSequenceIdValue(authorityCollectionName));
                roleAdmin.setName("ROLE_ADMIN");
                roleAdmin.setUser(savedUser); // Associate with the customer
                authorityRepository.save(roleAdmin);

                Authority roleUser = new Authority();
                roleUser.setId(getNextSequenceIdValue(authorityCollectionName));
                roleUser.setName("ROLE_USER");
                roleUser.setUser(savedUser); // Associate with the customer
                authorityRepository.save(roleUser);


        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error registering user", e);
            throw new RuntimeException("Error registering user", e);
        }
    }

    @Override
    public RegistrationOtp saveRegistrationOtp(RegistrationRequestDTO registrationRequest, String otp) {
        try {
            String collectionName = RegistrationOtp.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for User class");
            }
            Long id = getNextSequenceIdValue(collectionName);
            RegistrationOtp registrationOtp = new RegistrationOtp();
            registrationOtp.setId(id);
            registrationOtp.setEmail(registrationRequest.email());
            registrationOtp.setPassword(registrationRequest.password());
            registrationOtp.setOtp(otp);
            Instant expirationTime = Instant.now().plusSeconds(10 * 60); // OTP valid for 10 minutes
            registrationOtp.setExpirationTime(expirationTime);
            registrationOtp.setCreatedDate(LocalDateTime.now());
            return registrationOtpRepository.save(registrationOtp);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error registering user", e);
            throw new RuntimeException("Error registering user", e);
        }
    }



    public long getNextSequenceIdValue(String seqName) {
        // Find and increment the sequence value atomically
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true), // Create if not exists
                DatabaseSequence.class);

        return counter != null ? counter.getSeq() : 1L; // Return long value, default is 1L
    }


    @Override
    public ForgetPasswordOtp saveForgetPasswordOtp(String email, String otp) {
        try {
            String collectionName = ForgetPasswordOtp.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for User class");
            }
            Long id = getNextSequenceIdValue(collectionName);
            // Generate expiration time
            Instant expirationTime = Instant.now().plusSeconds(10 * 60); // OTP valid for 10 minutes
            // Create a new ForgetPasswordOtp entity and set values
            ForgetPasswordOtp forgetPasswordOtp = new ForgetPasswordOtp();
            forgetPasswordOtp.setId(id);
            forgetPasswordOtp.setEmail(email);
            forgetPasswordOtp.setOtp(otp);
            forgetPasswordOtp.setExpirationTime(expirationTime);
            forgetPasswordOtp.setCreatedDate(LocalDateTime.now());
            forgetPasswordOtp.setUsed(false);
            return passwordResetOtpRepository.save(forgetPasswordOtp);
        } catch (Exception e) {
            // Log error
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error saving OTP for email: " + email, e);
            throw new RuntimeException("Error saving OTP to database", e); // Rethrow as runtime exception
        }
    }

    @Override
    public ForgetPasswordOtp updateForgetPasswordOtp(ForgetPasswordOtp latestRegistrationOtp, String resetToken) {
        Instant resetTokenExpiration = Instant.now().plusSeconds(10 * 60);  // Token expires in 10 minutes
        latestRegistrationOtp.setResetToken(resetToken);
        latestRegistrationOtp.setExpirationTime(resetTokenExpiration); // Update expiration time for the reset token
        latestRegistrationOtp.setUsed(false); // Token hasn't been used yet
        return passwordResetOtpRepository.save(latestRegistrationOtp);
    }

    @Override
    public boolean resetForgetPassword(String email, String newPassword) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User existingUserDetails = existingUser.get();
            existingUserDetails.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(existingUserDetails);
            return true;
        }
        return false;
    }


    @Override
    public ResponseEntity<ApiResponse> changeUserPassword(String email, String oldPassword, String newPassword) {
        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            User existingUserDetails = existingUser.get();
            // Verify the old password
            if (passwordEncoder.matches(oldPassword, existingUserDetails.getPassword()) && !oldPassword.equals(newPassword)) {
                // Encode and set the new password
                existingUserDetails.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(existingUserDetails);
                ApiResponse response = new ApiResponse( LocalDateTime.now(), HttpStatus.OK.value(), "Password changed successfully.", null);
                return ResponseEntity.ok(response);
            }else if (passwordEncoder.matches(oldPassword, existingUserDetails.getPassword())  && oldPassword.equals(newPassword) ){
                ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Please Enter Different Password", null);
                return ResponseEntity.badRequest().body(response);
            }
        }
        ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Invalid Current Password", null);
        return ResponseEntity.badRequest().body(response);

    }





}
