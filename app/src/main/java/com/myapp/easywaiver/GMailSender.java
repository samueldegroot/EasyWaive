package com.myapp.easywaiver;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GMailSender {

    final String emailPort = "587";// gmail's smtp port
    final String smtpAuth = "true";
    final String starttls = "true";
    final String emailHost = "smtp.gmail.com";


    String fromEmail;
    String fromPassword;
    String toEmailList;
    String emailSubject;
    String emailBody;
    String emailCC;
    String pdfName;

    Properties emailProperties;
    Session mailSession;
    MimeMessage emailMessage;

    public GMailSender() {

    }

    public GMailSender(String fromEmail, String fromPassword,
                 String toEmailList, String emailSubject, String emailBody, String emailCC, String pdfName) {
        this.fromEmail = fromEmail;
        this.fromPassword = fromPassword;
        this.toEmailList = toEmailList;
        this.emailSubject = emailSubject;
        this.emailBody = emailBody;
        this.emailCC = emailCC;
        this.pdfName = pdfName;

        emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", emailPort);
        emailProperties.put("mail.smtp.auth", smtpAuth);
        emailProperties.put("mail.smtp.starttls.enable", starttls);
        Log.i("GMail", "Mail server properties set.");
    }

    public MimeMessage createEmailMessage() throws AddressException,
            MessagingException, UnsupportedEncodingException {

        mailSession = Session.getDefaultInstance(emailProperties, null);
        emailMessage = new MimeMessage(mailSession);

        emailMessage.setFrom(new InternetAddress(fromEmail, fromEmail));
        Log.i("GMail", "toEmail: " + toEmailList);
        emailMessage.addRecipient(Message.RecipientType.TO,
                new InternetAddress(toEmailList));

        //add CC recipient
        if (isValidEmail(emailCC)) {
            emailMessage.addRecipient(Message.RecipientType.BCC, new InternetAddress(emailCC));
        }

        emailMessage.setSubject(emailSubject);
        //emailMessage.setContent(emailBody, "text/html");// for a html email
        //emailMessage.setText(emailBody);// for a text email

        //attachment
        BodyPart messageBodyPart1 = new MimeBodyPart();
        messageBodyPart1.setText(emailBody);

        MimeBodyPart messageBodyPart2 = new MimeBodyPart();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "EasyPhotoWaiver/" + pdfName);
        DataSource source = new FileDataSource(file);
        messageBodyPart2.setDataHandler(new DataHandler(source));
        messageBodyPart2.setFileName(pdfName);

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart1);
        multipart.addBodyPart(messageBodyPart2);

        emailMessage.setContent(multipart);

        Log.i("GMail", "Email Message created.");
        return emailMessage;
    }

    public void sendEmail() throws AddressException, MessagingException {

        Transport transport = mailSession.getTransport("smtp");
        transport.connect(emailHost, fromEmail, fromPassword);
        Log.i("GMail", "allrecipients: " + emailMessage.getAllRecipients());
        transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
        transport.close();
        Log.i("GMail", "Email sent successfully.");
    }

    public static boolean isValidEmail(String email) {
        boolean isValid = false;
        try {
            InternetAddress internetAddress = new InternetAddress(email);
            internetAddress.validate();
            isValid = true;
        }
        catch (AddressException e) {
            e.printStackTrace();
        }
        return isValid;
    }
}