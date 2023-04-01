package polis;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AppController {
    private static final String CODE_PARAM = "code";

    @GetMapping("/showCode")
    String showCode(
            @RequestParam(required = false, name = CODE_PARAM) String code,
            Model model
    ) {
        if (code == null || code.isEmpty()) {
            return "wrongCodePage";
        }
        model.addAttribute(CODE_PARAM, code);
        return "showCodePage";
    }
}
