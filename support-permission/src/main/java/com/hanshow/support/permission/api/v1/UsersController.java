package com.hanshow.support.permission.api.v1;

import java.security.Principal;
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

import com.hanshow.support.permission.model.Pages;
import com.hanshow.support.permission.model.Users;
import com.hanshow.support.permission.service.UsersService;

@RestController
@RequestMapping("/v1/users")
public class UsersController {
	
	@Autowired
	private UsersService usersService;

	/**
	 * 注册用户
	 * @param user
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value="register", method=RequestMethod.POST)
	public HttpEntity<Void> insert(@RequestBody Users user) throws SQLException {
		user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
		user.setCreateDate(new Date());
		if(!usersService.existsById(user.getUsername())) {
			if (usersService.insert(user)) {
				return ResponseEntity.status(HttpStatus.CREATED).build();
			} else {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
			}	
		} else {
			return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
		}
	}
	
	/**
	 * 根据用户名删除一个用户
	 * @param username
	 * @return
	 * @throws SQLException
	 */
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE)
	public HttpEntity<Void> delete(@PathVariable("id") String username) throws SQLException {
		usersService.deleteById(username);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	} 
	
	/**
	 * 修改用户信息，所有字段更新
	 * @param user
	 * @return 无返回值
	 * @throws SQLException
	 */
	@RequestMapping(method=RequestMethod.PUT)
	public HttpEntity<Void> update(@RequestBody Users user) throws SQLException {
		return updateBySelective(user);
	}
	
	/**
	 * 修改用户信息，局部字段更新
	 * @param user
	 * @return 无返回值
	 * @throws SQLException
	 */
	@RequestMapping(method=RequestMethod.PATCH)
	public HttpEntity<Void> updateBySelective(@RequestBody Users user) throws SQLException {
		if(!usersService.existsById(user.getUsername())) {
			user.setPassword(null);
			if (usersService.updateBySelective(user)) {
				return ResponseEntity.status(HttpStatus.OK).build();
			} else {
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).build();
			}	
		} else {
			return ResponseEntity.status(HttpStatus.ALREADY_REPORTED).build();
		}
	}
	
	/**
	 * 分页查询数据
	 * @param page
	 * @param size
	 * @return 用户列表
	 */
	@RequestMapping(method=RequestMethod.GET)
	public HttpEntity<Pages<Users>> query(@RequestBody Users user, @RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		return ResponseEntity.ok().body(usersService.queryForPage(user, (page <= 0 ? 1 : page) - 1, size));
	}
	
	/**
	 * 分页查询数据
	 * @param page
	 * @param size
	 * @return 用户列表
	 */
	/*@RequestMapping(method=RequestMethod.GET)
	public HttpEntity<Pages<Users>> query(@RequestParam(value="offset") int page, @RequestParam(value="limit") int size) {	
		return ResponseEntity.ok().body(usersService.queryForPage((page <= 0 ? 1 : page) - 1, size));
	}*/
	
	/**
	 * 查询指定用户
	 * @param username
	 * @return 用户对象
	 */
	@RequestMapping(value="/info", method=RequestMethod.GET)
	public HttpEntity<String> queryById(Principal principal) {
		return ResponseEntity.ok().body(principal.getName());
	}
}
