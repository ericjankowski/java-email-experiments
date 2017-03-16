package com.erj.practice.email;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.junit.Test;

public class GmailTest {

    /**
     * Proof of concept for testing sending and receiving email to a Gmail account for testing.
     * 
     * Sends an email to a specified gmail account using Google's SMTP server.
     * 
     * Retreives an email via IMAP from that account and asserts that the content of the read email matches the content of what was sent.
     * 
     * Assumes that "Allow less secure apps:" is turned ON for the Gmail account being used.
     */
    @Test
    public void sendSMTPAndReceiveIMAP() {
        Properties secretProperties = loadSecretProperties();
        final String username = secretProperties.getProperty("username");
        final String password = secretProperties.getProperty("password");

        String expectedSubject = "Test email subject: " + System.currentTimeMillis();
        String expectedContent = "Test email text: " + System.currentTimeMillis();

        // Send
        sendOutgoingSMTPMessage(username, password, expectedSubject, expectedContent);

        // Receive (IMAP)
        Map<String, String> message = readIncomingIMAPMessage(username, password);

        assertEquals(expectedSubject, message.get("subject"));
        assertEquals(expectedContent.trim(), message.get("content"));
    }

    private Properties loadSecretProperties() {
        String filename = "secret.properties";
        String unableToReadPropertiesMessage = "Unable to read secret.properties for username and password.";
        Properties secretProperties = new Properties();
        InputStream input = null;

        input = getClass().getClassLoader().getResourceAsStream(filename);
        if (input == null) {
            fail(unableToReadPropertiesMessage);
        }
        try {
            secretProperties.load(input);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(unableToReadPropertiesMessage);
        }
        return secretProperties;
    }
    
    private void sendOutgoingSMTPMessage(final String username, final String password, String subject, String content) {
        String toAddress = username;
        String fromAddress = username;

        Properties smtpProperties = new Properties();
        smtpProperties.put("mail.smtp.auth", "true");
        smtpProperties.put("mail.smtp.starttls.enable", "true");
        smtpProperties.put("mail.smtp.host", "smtp.gmail.com");

        Session smtpSession = Session.getInstance(smtpProperties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage outgoingMessage = new MimeMessage(smtpSession);
            outgoingMessage.setFrom(new InternetAddress(fromAddress));
            outgoingMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            outgoingMessage.setSubject(subject);
            outgoingMessage.setText(content);
            Transport.send(outgoingMessage);
        } catch (MessagingException ex) {
            ex.printStackTrace();
            fail("Message Send Failure " + ex.getMessage());
        }
    }

    private Map<String, String> readIncomingIMAPMessage(final String username, final String password) {
        Map<String, String> message = new HashMap<>();
        Properties imapProperties = new Properties();
        imapProperties.put("mail.store.protocol", "imaps");
        Session popSession = Session.getDefaultInstance(imapProperties);
        try {
            Store store = popSession.getStore("imaps");
            store.connect("imap.gmail.com", username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.getMessages();
            message.put("subject", messages[0].getSubject().trim());
            message.put("content", messages[0].getContent().toString().trim());
            delete(messages);
            inbox.close(true);
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        }

        return message;
    }

    private void delete(Message[] messages) throws MessagingException {
        for (Message incomingMessage : messages) {
            incomingMessage.setFlag(Flags.Flag.DELETED, true);
        }
    }

    /**
     * Proof of concept for testing sending and receiving email to a Gmail account for testing.
     * 
     * Sends an email to a specified gmail account using Google's SMTP server.
     * 
     * Retreives an email via POP3 from that account and asserts that the content of the read email matches the content of what was sent.
     * 
     * Assumes that "Allow less secure apps:" is turned ON for the Gmail account being used.
     * 
     * Commented out and moved to the bottom due to a delete functionality discrepancy between POP and IMAP
     */
    //@Test
    public void sendSMTPAndReceivePOP3() {

        Properties secretProperties = loadSecretProperties();
        final String username = secretProperties.getProperty("username");
        final String password = secretProperties.getProperty("password");

        String expectedSubject = "Test email subject: " + System.currentTimeMillis();
        String expectedContent = "Test email text: " + System.currentTimeMillis();

        // Send
        sendOutgoingSMTPMessage(username, password, expectedSubject, expectedContent);

        // Receive (POP)
        Map<String, String> message = readIncomingPOP3Message(username, password);

        assertEquals(expectedSubject, message.get("subject"));
        assertEquals(expectedContent.trim(), message.get("content"));
    }
    
    private Map<String, String> readIncomingPOP3Message(final String username, final String password) {
        Map<String, String> message = new HashMap<>();
        Properties popProperties = new Properties();// can't be null, but doesn't need any properties set
        Session popSession = Session.getDefaultInstance(popProperties);
        try {
            Store store = popSession.getStore("pop3s");
            store.connect("pop.gmail.com", username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.getMessages();
            message.put("subject", messages[0].getSubject().trim());
            message.put("content", messages[0].getContent().toString().trim());
            delete(messages);
            inbox.close(true);
            store.close();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            fail("Message Read Failure: " + e.getMessage());
        }

        return message;
    }
}
