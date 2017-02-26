package fr.xephi.authme.command.executable.authme.debug;

import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.mail.SendMailSSL;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.util.List;

/**
 * Sends out a test email.
 */
class TestEmailSender implements DebugSection {

    @Inject
    private DataSource dataSource;

    @Inject
    private SendMailSSL sendMailSSL;

    @Inject
    private Server server;


    @Override
    public String getName() {
        return "mail";
    }

    @Override
    public String getDescription() {
        return "Sends out a test email";
    }

    @Override
    public void execute(CommandSender sender, List<String> arguments) {
        if (!sendMailSSL.hasAllInformation()) {
            sender.sendMessage(ChatColor.RED + "You haven't set all required configurations in config.yml " +
                "for sending emails. Please check your config.yml");
            return;
        }

        String email = getEmail(sender, arguments);

        // getEmail() takes care of informing the sender of the error if email == null
        if (email != null) {
            boolean sendMail = sendTestEmail(email);
            if (sendMail) {
                sender.sendMessage("Test email sent to " + email + " with success");
            } else {
                sender.sendMessage(ChatColor.RED + "Failed to send test mail to " + email + "; please check your logs");
            }
        }
    }

    private String getEmail(CommandSender sender, List<String> arguments) {
        if (arguments.isEmpty()) {
            PlayerAuth auth = dataSource.getAuth(sender.getName());
            if (auth == null) {
                sender.sendMessage(ChatColor.RED + "Please provide an email address, "
                    + "e.g. /authme debug mail test@example.com");
                return null;
            }
            String email = auth.getEmail();
            if (email == null || "your@email.com".equals(email)) {
                sender.sendMessage(ChatColor.RED + "No email set for your account! Please use /authme debug mail <email>");
                return null;
            }
            return email;
        } else {
            String email = arguments.get(0);
            if (email.contains("@")) {
                return email;
            }
            sender.sendMessage(ChatColor.RED + "Invalid email! Usage: /authme debug mail test@example.com");
            return null;
        }
    }

    private boolean sendTestEmail(String email) {
        HtmlEmail htmlEmail;
        try {
            htmlEmail = sendMailSSL.initializeMail(email);
        } catch (EmailException e) {
            ConsoleLogger.logException("Failed to create email for sample email:", e);
            return false;
        }

        htmlEmail.setSubject("AuthMe test email");
        String message = "Hello there!<br />This is a sample email sent to you from a Minecraft server ("
            + server.getName() + ") via /authme debug mail. If you're seeing this, sending emails should be fine.";
        return sendMailSSL.sendEmail(message, htmlEmail);
    }
}
