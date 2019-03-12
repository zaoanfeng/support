package com.hanshow.support.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StoreController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	 @GetMapping("/product")
	    public String getProduct() {
	        //for debug
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        logger.debug("authentication:{}",authentication);
	        return "product id : 11";
	    }

	    @GetMapping("/order/{id}")
	    public String getOrder(@PathVariable String id) {
	        //for debug
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	        logger.debug("authentication:{}",authentication);
	        return "order id : " + id;
	    }

}
