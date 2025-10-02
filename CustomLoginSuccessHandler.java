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
                    redirectUrl = "/admin/apartments"; break;
                case "ROLE_IT_TECHNICIAN":
                    redirectUrl = "/it/users"; break;
                case "ROLE_SALES_MANAGER":
                    redirectUrl = "/sales/leads"; break;
                case "ROLE_FINANCE_ASSISTANT":
                    redirectUrl = "/finance/invoices"; break;
                case "ROLE_MARKETING_EXECUTIVE":
                    redirectUrl = "/marketing/promotions"; break;
                case "ROLE_CUSTOMER_SUPPORT":
                    redirectUrl = "/support/tickets"; break;
                case "ROLE_CUSTOMER":
                    redirectUrl = "/home"; break;  // ✅ customer goes home
            }
        }

        // ✅ Spring Security provides this in SimpleUrlAuthenticationSuccessHandler
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
