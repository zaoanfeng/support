package com.hanshow.support.permission.api.v1;

import java.sql.SQLException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.permission.model.ClientDetails;
import com.hanshow.support.permission.model.Pages;
import com.hanshow.support.permission.service.ClientDetailsService;

@RestController
@RequestMapping("/v1/clients")
public class ClientDetailsController  {

	@Autowired
	private ClientDetailsService clientDetailsService;
	
	/**
	 * 添加一个客户信息
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public HttpEntity<Void> insert(@RequestBody ClientDetails clientDetails) throws SQLException {
		clientDetails.setClientSecret(new BCryptPasswordEncoder().encode(clientDetails.getClientSecret()));
		clientDetails.setCreateDate(new Date());
		if(!clientDetailsService.existsById(clientDetails.getClientId())) {
			if (clientDetailsService.insert(clientDetails)) {
				return ResponseEntity.status(HttpStatus.CREATED).build();
			} else {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
			}	
		} else {
			return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
		}
	}
	
	/**
	 * 根据客户编号删除一个客户
	 * @param clientId
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public HttpEntity<Void> delete(@PathVariable("id") String clientId) throws SQLException {
			clientDetailsService.deleteById(clientId);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
	
	/**
	 * 修改客户信息，局部字段更新
	 * @param user
	 * @return 无返回值
	 * @throws SQLException
	 */
	@RequestMapping(method=RequestMethod.PATCH)
	public HttpEntity<Void> updateBySelective(@RequestBody ClientDetails clientDetails) throws SQLException {
		if(clientDetailsService.existsById(clientDetails.getClientId())) {
			clientDetails.setClientSecret(null);
			if (clientDetailsService.updateBySelective(clientDetails)) {
				return ResponseEntity.status(HttpStatus.CREATED).build();
			} else {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
			}	
		} else {
			return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
		}
	}
	
	/**
	 * 修改客户信息，全部字段更新
	 * @param user
	 * @return 无返回值
	 * @throws SQLException
	 */
	@RequestMapping(method=RequestMethod.PUT)
	public HttpEntity<Void> update(@RequestBody ClientDetails clientDetails) throws SQLException {
		return update(clientDetails);
	}
	
	/**
	 * 分页查询数据
	 * @param page
	 * @param size
	 * @return 客户列表
	 */
	@RequestMapping(method=RequestMethod.GET)
	public HttpEntity<Pages<ClientDetails>> query(@RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		return ResponseEntity.ok().body(clientDetailsService.queryForPage((page <= 0 ? 1 : page) - 1, size));

	}
	
	/**
	 * 查询指定用户
	 * @param username
	 * @return 客户对象
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.GET)
	public HttpEntity<ClientDetails> queryById(@PathVariable("id") String clientId) {
		return ResponseEntity.ok().body(clientDetailsService.queryById(clientId));
	}
}
