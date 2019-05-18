package com.hanshow.support.server.service.impl;

import org.springframework.stereotype.Service;

import com.hanshow.support.server.model.Customer;
import com.hanshow.support.server.service.CustomerService;

@Service
public class CustomerServiceImpl extends BaseServiceImpl<Customer, Long> implements CustomerService {

}
