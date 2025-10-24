package Group3.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sales")
public class SalesDashboardController {

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard() {
        return "sales/dashboard";
    }
}

