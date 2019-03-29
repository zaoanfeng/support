package com.hanshow.support.permission.api.v1;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.Link;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.hanshow.support.permission.model.Users;

@RestController
@RequestMapping("/v1")
public class IndexController {

	@RequestMapping(method=RequestMethod.GET)
    public HttpEntity<List<Link>> index() throws SQLException{
		List<Link> list = new ArrayList<>();
		list.add(linkTo(methodOn(UsersController.class).insert(new Users())).withRel("register").withTitle("users").withType("POST"));
        list.add(linkTo(methodOn(UsersController.class).query(1, 20)).withRel("collection").withTitle("users").withType("GET"));
        return ResponseEntity.ok(list);
    }
}
