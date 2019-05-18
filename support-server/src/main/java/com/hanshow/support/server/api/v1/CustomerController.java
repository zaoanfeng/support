package com.hanshow.support.server.api.v1;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.server.model.Customer;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.CustomerService;

@RestController
@RequestMapping("/v1/customer")
public class CustomerController {
	
	@Autowired
	private CustomerService customerService;
	
	@PostMapping
	public HttpEntity<Void> insert(@RequestBody Customer customer) {
		customer.setCreateTime(new Date());
		customerService.insert(customer);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value="/{id}")
	public HttpEntity<Void> delete(@PathVariable long id) {
		customerService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	@PutMapping
	public HttpEntity<Void> update(@RequestBody Customer customer) {
		if (customerService.updateById(customer, customer.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}
	}
	
	@PatchMapping
	public HttpEntity<Void> updateSelective(@RequestBody Customer customer) {
		if (customerService.updateSelectiveById(customer, customer.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}	
	}
	
	@GetMapping
	public HttpEntity<Pages<Customer>> query(@RequestBody Customer customer, @RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		int count = customerService.queryCount(customer);
		List<Customer> customers = customerService.queryForPage(customer, page, size);
		Pages<Customer> pages = new Pages<>();
		pages.setTotal(count);
		pages.setItems(customers);
		pages.setPageNo(page);
		pages.setPageSize(size);
		return ResponseEntity.ok().body(pages);
	}

}
