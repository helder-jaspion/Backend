package br.com.cdsoft.rest;

import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RestResource {
	@RequestMapping(produces = MediaType.APPLICATION_JSON, path = "/properties", method = RequestMethod.GET)
	public ResponseEntity<Properties> getProperties() {

		return new ResponseEntity<Properties>(System.getProperties(), HttpStatus.OK);
	}

}
