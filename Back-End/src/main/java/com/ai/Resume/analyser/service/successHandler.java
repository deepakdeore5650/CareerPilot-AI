package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.jwt.jwtService;
import com.ai.Resume.analyser.model.usersTable;
import com.ai.Resume.analyser.repository.usersTableRepo;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class successHandler implements AuthenticationSuccessHandler {


    @Autowired
    private usersTableRepo usersTableRepository;

    @Autowired
    private jwtService jwtService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:Lax}")
    private String cookieSameSite;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String,Object> userdata = oAuth2User.getAttributes();

        String email = userdata.get("email").toString();
        String name = userdata.get("name") != null ? userdata.get("name").toString() : "";
        String picture = userdata.get("picture") != null ? userdata.get("picture").toString() : null;

        if (!usersTableRepository.existsById(email)){
            usersTable newUser = usersTable.builder()
                    .username(name)
                    .email(email)
                    .password("")
                    .previousResults(false)
                    .resetOtp(null)
                    .resetExpiration(null)
                    .profilePhoto(picture)
                    .build();
            usersTableRepository.save(newUser);
        }

        String token = jwtService.generateToken(email);
        String sameSite = cookieSameSite == null || cookieSameSite.isBlank() ? "Lax" : cookieSameSite;
        boolean secureCookie = cookieSecure || request.isSecure();
        ResponseCookie cookie = com.ai.Resume.analyser.service.securityService.buildAuthCookie(token, secureCookie, sameSite, false);

        response.addHeader("Set-Cookie", cookie.toString());
        String redirectUrl = frontendUrl;
        if (redirectUrl == null || redirectUrl.isBlank()) {
            redirectUrl = "http://localhost:5173";
        }
        response.sendRedirect(redirectUrl);
    }
}
