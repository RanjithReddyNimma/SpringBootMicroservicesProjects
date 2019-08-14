package com.techm.salesapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.techm.salesapp.api.users.data.*;
import com.techm.salesapp.api.users.shared.UserDto;
import com.techm.salesapp.api.users.ui.model.CrediCardResponseModel;

import feign.FeignException;

@Service
public class UsersServiceImpl implements UsersService {

	UsersRepository usersRepository;
	BCryptPasswordEncoder bCryptPasswordEncoder;
	//RestTemplate restTemplate;
	Environment environment;
	 CreditCardServiceClient creditCardServiceClient;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public UsersServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
			CreditCardServiceClient creditCardServiceClient, Environment environment/*
																					 * , RestTemplate restTemplate
																					 */
	) {
		this.usersRepository = usersRepository;
		this.bCryptPasswordEncoder = bCryptPasswordEncoder;
		this.creditCardServiceClient = creditCardServiceClient;
		this.environment = environment;
		// this.restTemplate=restTemplate;
	}

	@Override
	public UserDto createUser(UserDto userDetails) {
		// TODO Auto-generated method stub

		userDetails.setUserId(UUID.randomUUID().toString());
		userDetails.setEncryptedPassword(bCryptPasswordEncoder.encode(userDetails.getPassword()));

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

		UserEntity userEntity = modelMapper.map(userDetails, UserEntity.class);

		usersRepository.save(userEntity);

		UserDto returnValue = modelMapper.map(userEntity, UserDto.class);

		return returnValue;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = usersRepository.findByEmail(username);

		if (userEntity == null)
			throw new UsernameNotFoundException(username);

		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true,
				new ArrayList<>());
	}

	@Override
	public UserDto getUserDetailsByEmail(String email) {
		UserEntity userEntity = usersRepository.findByEmail(email);

		if (userEntity == null)
			throw new UsernameNotFoundException(email);

		return new ModelMapper().map(userEntity, UserDto.class);
	}

	@Override
	public UserDto getUserByUserId(String userId) {

		UserEntity userEntity = usersRepository.findByUserId(userId);
		if (userEntity == null)
			throw new UsernameNotFoundException("User not found");

		UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);

		/*
		 * String albumsUrl = String.format(environment.getProperty("creditcards.url"),
		 * userId);
		 * 
		 * ResponseEntity<List<CrediCardResponseModel>> creditCardsListResponse =
		 * restTemplate.exchange(albumsUrl, HttpMethod.GET, null, new
		 * ParameterizedTypeReference<List<CrediCardResponseModel>>() { });
		 * List<CrediCardResponseModel> creditCardsList =
		 * creditCardsListResponse.getBody();
		 */

		logger.info("Before calling credit card Microservice");
		List<CrediCardResponseModel> creditCardsList = creditCardServiceClient.getCreditCards(userId);
		logger.info("After calling credit card Microservice");

		userDto.setCreditCards(creditCardsList);

		return userDto;
	}

}
