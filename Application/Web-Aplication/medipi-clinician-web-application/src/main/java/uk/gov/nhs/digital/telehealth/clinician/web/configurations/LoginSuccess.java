/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.gov.nhs.digital.telehealth.clinician.web.configurations;

import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import uk.gov.nhs.digital.telehealth.clinician.web.controllers.PatientController;

/**
 *
 * @author riro
 */
@Component
public class LoginSuccess implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LogManager.getLogger(LoginSuccess.class);
//    public final Integer SESSION_TIMEOUT_IN_SECONDS = 60;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        LOGGER.info(authentication.getPrincipal() + " Logged in at " + new Date());
        response.sendRedirect("/");
//        request.getSession().setMaxInactiveInterval(SESSION_TIMEOUT_IN_SECONDS);
    }
}
