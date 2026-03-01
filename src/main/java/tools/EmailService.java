package tools;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {

    // WARNING: For demonstration only. In a real app, use environment variables or
    // config files.
    private static final String FROM_EMAIL = "rayenhafian72@gmail.com";
    private static final String PASSWORD = "your-app-password";
    private static final String ADMIN_EMAIL = "admin@test.com";

    public static void sendComplaintNotificationToAdmin(String titre, String description, String userEmail) {
        String subject = "New Complaint Received";
        String body = "A new complaint has been submitted:\n\n"
                + "Title: " + titre + "\n"
                + "Description: " + description + "\n"
                + "From User: " + userEmail + "\n\n"
                + "Please check the admin panel to respond.";

        sendEmail(ADMIN_EMAIL, subject, body);
    }

    public static void sendReviewNotificationToAdmin(int note, String commentaire, String userEmail) {
        String subject = "New Review Received";
        String body = "A new review has been submitted:\n\n"
                + "Rating: " + note + "/5\n"
                + "Comment: " + commentaire + "\n"
                + "From User: " + userEmail + "\n\n"
                + "Please check the admin panel to respond.";

        sendEmail(ADMIN_EMAIL, subject, body);
    }

    public static void sendResponseNotificationToUser(String userEmail, String titre, String reponse) {
        String subject = "Response to Your Complaint: " + titre;
        String body = "Dear User,\n\n"
                + "The administrator has responded to your complaint:\n\n"
                + "Your Complaint: " + titre + "\n"
                + "Admin Response: " + reponse + "\n\n"
                + "Thank you for using our service.";

        sendEmail(userEmail, subject, body);
    }

    private static void sendEmail(String toEmail, String subject, String body) {
        // Try-catch to prevent application crash if email fails
        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            // In a real environment, this blocks. Best performed in a separate thread.
            // Transport.send(message);
            System.out.println("[MOCK EMAIL] Sent to: " + toEmail + "\nSubject: " + subject);

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
