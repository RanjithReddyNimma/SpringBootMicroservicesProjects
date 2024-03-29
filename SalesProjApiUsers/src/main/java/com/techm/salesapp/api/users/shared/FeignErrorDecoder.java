package com.techm.salesapp.api.users.shared;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.Response;
import feign.codec.ErrorDecoder;

@Component
public class FeignErrorDecoder implements ErrorDecoder {
	
	Environment environment;
	
	@Autowired
	public FeignErrorDecoder(Environment environment)
	{
		this.environment = environment;
	}

	@Override
	public Exception decode(String methodKey, Response response) {
		switch (response.status()) {
		case 400:
			// Do something
			// return new BadRequestException();
			break;
		case 404: {
			if (methodKey.contains("getCreditCards")) {
				return new ResponseStatusException(HttpStatus.valueOf(response.status()), environment.getProperty("creditcards.exceptions.creditcards-not-found"));
			}
			break;
		}
		default:
			return new Exception(response.reason());
		}
		return null;
	}

}
