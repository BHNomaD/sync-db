package com.nomad.app.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Md Shariful Islam
 */
@RestController
public class AppController {
    private static final Logger logger = LoggerFactory.getLogger(AppController.class);

    @GetMapping(value = "/info")
    public Map<String, Object> showInfo(HttpServletResponse response, @RequestParam("test") String test) {

        logger.info("Inside info method... ");

        response.setStatus(200);
        Map<String, Object> info = new HashMap<>();
        info.put("app", this.getClass().getPackage().getImplementationTitle());
        info.put("version", this.getClass().getPackage().getImplementationVersion());
        info.put("vendor", this.getClass().getPackage().getImplementationVendor());
        info.put("test-string", test);
        return info;
    }
}
