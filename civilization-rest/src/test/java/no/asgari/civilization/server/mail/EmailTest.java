package no.asgari.civilization.server.mail;

import com.google.common.base.Charsets;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class EmailTest {

    //@Test
    //@Ignore
    public void sendingEmail() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "127.0.0.1"); //Assumes you have an stmp server running locally
        props.put("mail.smtp.port", Integer.toString(25));
        props.put("mail.debug", "true");

        Session session = Session.getInstance(props);
        String emailTo = "yo@foo.com";
        String emailFrom = "yo@foo.com";
        MimeMessage mimeMsg = new MimeMessage(session);
        mimeMsg.setFrom(new InternetAddress(emailFrom));
        mimeMsg.setSubject("This is a test", Charsets.UTF_8.toString());

        //You can obviously put more than 1 person on the mail to
        InternetAddress[] recipients = InternetAddress.parse(emailTo, true);
        mimeMsg.setRecipients(Message.RecipientType.TO, recipients);

        MimeBodyPart part = new MimeBodyPart();
        part.setText("<html><body><h3>Its your turn!</h3</body></html>",
                Charsets.UTF_8.toString(),
                "html");

        Multipart mp = new MimeMultipart();
        mp.addBodyPart(part);

        mimeMsg.setSentDate(new Date());
        mimeMsg.setContent(mp);

        Transport.send(mimeMsg);
    }

}
