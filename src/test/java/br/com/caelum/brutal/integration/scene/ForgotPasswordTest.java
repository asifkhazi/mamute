package br.com.caelum.brutal.integration.scene;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;
import org.junit.BeforeClass;
import org.junit.Test;

import br.com.caelum.brutal.integration.pages.ResetPasswordPage;
import br.com.caelum.brutal.providers.SessionFactoryCreator;
import br.com.caelum.pagpag.aceitacao.util.ServerInfo;

public class ForgotPasswordTest extends AcceptanceTestBase implements ServerInfo.TesteAceitacao  {
    
    private static Session SESSION;
    private String validEmail = "francisco.sokol@caelum.com.br";

    @BeforeClass
    public static void setup() throws IOException {
        SessionFactoryCreator sessionFactoryCreator = new SessionFactoryCreator(env);
        SessionFactory sf = sessionFactoryCreator.getInstance();
        Session session = sf.openSession();
        SESSION = session;
    }
    
    @Test
    public void should_deny_recovery_for_inexistant_email() throws Exception {
        boolean sentEmail = tryToResetPassword("unexistant@brutal.com");
        assertFalse(sentEmail);
    }

    
    @Test
    public void should_accept_recovery_for_existant_email() throws Exception {
        boolean sentEmail = tryToResetPassword(validEmail);
        assertTrue(sentEmail);
    }
    
    @Test
    public void should_show_new_password_form_for_reseted_password_user() throws Exception {
        tryToResetPassword(validEmail);
        
        String recoverUrl = getRecoverURL();
        
        ResetPasswordPage resetPasswordPage = new ResetPasswordPage(driver, recoverUrl);
        resetPasswordPage
            .typePassword("newpass")
            .submitNewPassword();
        
        boolean isLoggedIn = home()
            .toLoginPage()
            .login(validEmail, "newpass")
            .isLoggedIn();
        
        assertTrue(isLoggedIn);
    }

    private String getRecoverURL() {
        Query query = SESSION.createQuery("select u.id,u.forgotPasswordToken from User u where u.email=:email");
        Object[] result = (Object[]) query.setParameter("email", validEmail).uniqueResult();
        String recoverUrl = SERVER.urlFor("/newpassword/"+result[0]+"/"+result[1]);
        return recoverUrl;
    }

    private boolean tryToResetPassword(String email) {
        boolean sentEmail = home().toLoginPage().toForgotPasswordPage()
                .typeEmail(email).resetPassword()
                .emailWasSent();
        return sentEmail;
    }
}
