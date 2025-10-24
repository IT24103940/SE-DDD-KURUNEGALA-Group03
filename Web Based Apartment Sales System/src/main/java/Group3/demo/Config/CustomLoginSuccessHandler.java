package Group3.demo.Config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    protected void handle(HttpServletRequest request, HttpServletResponse response,
                          Authentication authentication) throws IOException, ServletException {

        String redirectUrl = "/home"; // default redirect

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            switch (role) {
                case "ROLE_ADMIN_OFFICER":
                    redirectUrl = "/admin"; break; // Admin dashboard
                case "ROLE_IT_TECHNICIAN":
                    redirectUrl = "/it"; break; // IT dashboard
                case "ROLE_SALES_MANAGER":
                    redirectUrl = "/sales"; break; // Sales dashboard
                case "ROLE_FINANCE_ASSISTANT":
                    redirectUrl = "/finance"; break; // Finance dashboard
                case "ROLE_MARKETING_EXECUTIVE":
                    redirectUrl = "/marketing"; break; // Marketing dashboard
                case "ROLE_CUSTOMER_SUPPORT":
                    redirectUrl = "/support"; break; // Support dashboard first
                case "ROLE_CUSTOMER":
                    redirectUrl = "/home"; break;  // customer sees Home first
            }
        }

        System.out.println("✅ Logged-in user: " + authentication.getName());
        authentication.getAuthorities()
                .forEach(a -> System.out.println("Authority: " + a.getAuthority()));


        // ✅ Spring Security provides this in SimpleUrlAuthenticationSuccessHandler
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
