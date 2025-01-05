package com.fit.serviceImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;


@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;


    public void sendRegistrationOtpOnMail( String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            // message.setCc("piyush.kumar@sbsind.in", "kanchan.jadon@snva.com");
            message.setSubject("Complete Your Registration: OTP Verification Code");

            StringBuffer body = new StringBuffer();
            body.append("Dear User,").append("\n\n");
            body.append("Your registration OTP is: ").append(otp).append("\n\n");
            body.append("Enter this code to complete your registration.\n\n");
            body.append("Best regards,\n");
            body.append("The User Team");

            message.setText(body.toString());
            javaMailSender.send(message);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void sendForgetPasswordOtpOnMailUsingSMTP(String name, String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            // message.setCc("piyush.kumar@sbsind.in", "kanchan.jadon@snva.com");
            message.setSubject("Reset Your Password: OTP Verification Code");

            StringBuffer body = new StringBuffer();
            body.append("Dear ").append(name).append(",\n\n");
            body.append("Your OTP to reset your password is: ").append(otp).append("\n\n");
            body.append("Please note that this OTP is valid for 10 minutes only.\n\n");
            body.append("Enter this code to reset your password within the given time frame.\n\n");
            body.append("Best regards,\n");
            body.append("The User Team");

            message.setText(body.toString());
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email for Forget password: {}. Error: {}", toEmail, e.getMessage());
        }
    }

    public String generateCode() {
        // Generate a random 4-digit code
        Random rand = new Random();
        int code = rand.nextInt(9999 - 1000) + 1000;
        return String.valueOf(code);
    }
    public String generateCode2() {
        // Generate a random 4-digit code
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 4);
        return code;
    }




//    public  void sendRegistrationOtpOnMail(String personName, String email, String otp) {
//
//        String host = "smtp.gmail.com";
//        String to = email;
//
//        final String user = "orders@thelabelbar.com";//change accordingly
//        final String password = "TLB@snva44";//change accordingly
//
//        Properties props = System.getProperties();
//        props.put("mail.smtp.host", host);
//        props.put("mail.smtp.socketFactory.port", "465");
//        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.port", "465");
//
//        // mail check user and password
//        Session session = Session.getDefaultInstance(props,
//                new jakarta.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(user, password);
//                    }
//
//                });
//
//        try {
//            StringBuffer body = new StringBuffer();
//            body.append("Dear ").append(personName).append(",\n\n");
//            body.append("Your OTP is: ").append(otp).append("\n\n");
//            body.append("Enter this code to complete your registration.\n\n");
//            body.append("Best regards,\n");
//            body.append("The User Team");
//
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(user));
//            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
//            message.setSubject("Complete Your Registration: OTP Verification Code");
//            message.setContent(body, "text/html" );
//
//            Transport.send(message);
//            System.out.println("message sent....");
//
//        } catch (AddressException e) {
//            throw new RuntimeException(e);
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//
//
//    }


}
