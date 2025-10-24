package Group3.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/it")
public class ItDashboardController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "it/dashboard";
    }
}

