package com.influencer.serviceImpl;

import com.influencer.entity.*;
import com.influencer.model.ApiResponse;
import com.influencer.model.RegistrationRequestDTO;
import com.influencer.repository.AuthorityRepository;
import com.influencer.repository.InfluencerRepository;
import com.influencer.repository.ForgetPasswordOtpRepository;
import com.influencer.repository.RegistrationOtpRepository;
import com.influencer.service.LoginSignUpService;
import com.mongodb.DBObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
import java.util.Date;
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
    private InfluencerRepository influencerRepository;

    @Autowired
    private  AuthorityRepository authorityRepository;


    @Autowired
    private RegistrationOtpRepository registrationOtpRepository;

    @Autowired
    ForgetPasswordOtpRepository passwordResetOtpRepository;


    @Override
    public void registerUser(RegistrationOtp latestRegistrationOtp) {
        try {
            String collectionName = Influencer.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for Influencer class");
            }
            Influencer influencer = new Influencer();
            influencer.setId(generateUniqueIntegerId(collectionName));
            influencer.setEmail(latestRegistrationOtp.getEmail());
            //influencer.setPwd(latestRegistrationOtp.getPwd());
            String hashPwd = passwordEncoder.encode(latestRegistrationOtp.getPassword());
            influencer.setPassword(hashPwd);
//            influencer.setStatus("ACTIVE");
//            influencer.setRegisteredAt(LocalDateTime.now());
            Influencer savedInfluencer = influencerRepository.save(influencer);

            String authorityCollectionName = Authority.class.getAnnotation(Document.class).collection();
                Authority roleAdmin = new Authority();
                roleAdmin.setId(generateSequence(authorityCollectionName));
                roleAdmin.setName("ROLE_ADMIN");
                roleAdmin.setInfluencer(savedInfluencer); // Associate with the customer
                authorityRepository.save(roleAdmin);

                Authority roleUser = new Authority();
                roleUser.setId(generateSequence(authorityCollectionName));
                roleUser.setName("ROLE_USER");
                roleUser.setInfluencer(savedInfluencer); // Associate with the customer
                authorityRepository.save(roleUser);


        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error registering influencer", e);
            throw new RuntimeException("Error registering influencer", e);
        }
    }

    @Override
    public RegistrationOtp saveRegistrationOtp(RegistrationRequestDTO registrationRequest, String otp) {
        try {
            String collectionName = RegistrationOtp.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for Influencer class");
            }
            int id = generateUniqueIntegerId(collectionName);
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error registering influencer", e);
            throw new RuntimeException("Error registering influencer", e);
        }
    }


    private int generateUniqueIntegerId(String collectionName) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group().max("_id").as("maxId")
        );
        AggregationResults<DBObject> results = mongoOperations.aggregate(aggregation, collectionName, DBObject.class);
        DBObject result = results.getUniqueMappedResult();
        int id = 1; // Default ID if no documents exist yet
        if (result != null && result.containsField("maxId")) {
            Object maxId = result.get("maxId");
            if (maxId != null) {
                try {
                    id = ((Integer) maxId).intValue() + 1;
                } catch (ClassCastException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error casting maxId to Integer", e);
                }
            }
        }
        return id;
    }


    public int generateSequence(String seqName) {
        // Find and increment the sequence value atomically
        DatabaseSequence counter = mongoOperations.findAndModify(
                Query.query(Criteria.where("_id").is(seqName)),
                new Update().inc("seq", 1),
                options().returnNew(true).upsert(true), // Create if not exists
                DatabaseSequence.class);
        return counter != null ? counter.getSeq() : 1;
    }


    @Override
    public ForgetPasswordOtp saveForgetPasswordOtp(String email, String otp) {
        try {
            String collectionName = ForgetPasswordOtp.class.getAnnotation(Document.class).collection();
            if (collectionName == null || collectionName.isEmpty()) {
                throw new IllegalStateException("Collection name is not defined for Influencer class");
            }
            int id = generateUniqueIntegerId(collectionName);
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
        Optional<Influencer> existingInfluencer = influencerRepository.findByEmail(email);
        if (existingInfluencer.isPresent()) {
            Influencer existingInfluencerDetails = existingInfluencer.get();
            existingInfluencerDetails.setPassword(passwordEncoder.encode(newPassword));
            influencerRepository.save(existingInfluencerDetails);
            return true;
        }
        return false;
    }


    @Override
    public ResponseEntity<ApiResponse> changeInfluencerPassword(String email, String oldPassword, String newPassword) {
        Optional<Influencer> existingInfluencer = influencerRepository.findByEmail(email);
        if (existingInfluencer.isPresent()) {
            Influencer existingInfluencerDetails = existingInfluencer.get();
            // Verify the old password
            if (passwordEncoder.matches(oldPassword, existingInfluencerDetails.getPassword()) && !oldPassword.equals(newPassword)) {
                // Encode and set the new password
                existingInfluencerDetails.setPassword(passwordEncoder.encode(newPassword));
                influencerRepository.save(existingInfluencerDetails);
                ApiResponse response = new ApiResponse( LocalDateTime.now(), HttpStatus.OK.value(), "Password changed successfully.", null);
                return ResponseEntity.ok(response);
            }else if (passwordEncoder.matches(oldPassword, existingInfluencerDetails.getPassword())  && oldPassword.equals(newPassword) ){
                ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Please Enter Different Password", null);
                return ResponseEntity.badRequest().body(response);
            }
        }
        ApiResponse response = new ApiResponse(LocalDateTime.now(), HttpStatus.BAD_REQUEST.value(), "Invalid Current Password", null);
        return ResponseEntity.badRequest().body(response);

    }





}
