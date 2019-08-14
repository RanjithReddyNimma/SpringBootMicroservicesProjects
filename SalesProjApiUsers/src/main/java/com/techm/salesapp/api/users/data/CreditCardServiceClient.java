package com.techm.salesapp.api.users.data;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.techm.salesapp.api.users.ui.model.CrediCardResponseModel;

import feign.FeignException;
import feign.hystrix.FallbackFactory;

@FeignClient(name = "creditcard-ws", fallbackFactory = AlbumsFallbackFactory.class)
public interface CreditCardServiceClient {

	@GetMapping("/users/{id}/creditcards")
	public List<CrediCardResponseModel> getCreditCards(@PathVariable String id);
}

@Component
class AlbumsFallbackFactory implements FallbackFactory<CreditCardServiceClient> {

	@Override
	public CreditCardServiceClient create(Throwable cause) {
		// TODO Auto-generated method stub
		return new CreditCardsServiceClientFallback(cause);
	}

}

class CreditCardsServiceClientFallback implements CreditCardServiceClient {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	private final Throwable cause;

	public CreditCardsServiceClientFallback(Throwable cause) {
		this.cause = cause;
	}

	@Override
	public List<CrediCardResponseModel> getCreditCards(String id) {
		// TODO Auto-generated method stub

		if (cause instanceof FeignException && ((FeignException) cause).status() == 404) {
			logger.error("404 error took place when getAlbums was called with userId: " + id + ". Error message: "
					+ cause.getLocalizedMessage());
		} else {
			logger.error("Other error took place: " + cause.getLocalizedMessage());
		}

		return new ArrayList<>();
	}

}
