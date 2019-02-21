package com.mmall.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by tttppp606 on 2019/1/28.
 */
@Controller
@RequestMapping("/test/")
public class testLog {
    Logger logger = LoggerFactory.getLogger(testLog.class);

    @RequestMapping(value = "log.do",method = RequestMethod.GET)
    @ResponseBody
    public void demo(){
        try {
            int i = 3/0;
        } catch (Exception e) {
            logger.debug(e.getMessage());
        }
    }
}
