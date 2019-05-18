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

import com.hanshow.support.server.model.Store;
import com.hanshow.support.server.mybatis.Pages;
import com.hanshow.support.server.service.StoreService;

@RestController
@RequestMapping("/v1/store")
public class StoreController {

	@Autowired
	private StoreService storeService;

	@PostMapping
	public HttpEntity<Void> insert(@RequestBody Store store) {
		store.setCreateTime(new Date());
		storeService.insert(store);
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	@DeleteMapping(value = "/{id}")
	public HttpEntity<Void> delete(@PathVariable String id) {
		storeService.deleteById(id);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}

	@PutMapping
	public HttpEntity<Void> update(@RequestBody Store store) {
		if (storeService.updateById(store, store.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}

	}

	@PatchMapping
	public HttpEntity<Void> updateSelective(@RequestBody Store store) {
		if (storeService.updateSelectiveById(store, store.getId())) {
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} else {
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
		}
	}
	
	@GetMapping
	public HttpEntity<Pages<Store>> query( @RequestParam(value = "page") int page, @RequestParam(value = "limit") int size) {
		int count = storeService.queryCount(null);
		List<Store> stores = storeService.queryForPage(null, page, size);
		Pages<Store> pages = new Pages<>();
		pages.setTotal(count);
		pages.setItems(stores);
		pages.setPageNo(page);
		pages.setPageSize(size);
		return ResponseEntity.ok().body(pages);
	}

	/*@GetMapping
	public HttpEntity<Pages<Store>> query(@RequestBody Store store, @RequestParam(value = "offset") int page, @RequestParam(value = "limit") int size) {
		return ResponseEntity.ok().body(storeService.queryForPage(store, page, size));
	}*/
}
