package com.virtualStockPlatform.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualStockPlatform.api.Api;
import com.virtualStockPlatform.entity.Pair;
import com.virtualStockPlatform.entity.Price;
import com.virtualStockPlatform.entity.Property;
import com.virtualStockPlatform.entity.Stock;
import com.virtualStockPlatform.entity.Transaction;
import com.virtualStockPlatform.entity.User;
import com.virtualStockPlatform.entity.UserSymbolCheck;
import com.virtualStockPlatform.service.UserService;

@Controller
@RequestMapping("/user")
public class UserController {

	// need to inject user service
	@Autowired
	private UserService userService;

	@GetMapping("/list")
	public String listCustomers(Model theModel) {
		// get users from the service
		List<User> theUsers = userService.getUsers();

		// Get all comapany's stock prices
		Api api = new Api();
		HashMap<String, Double> priceMap = api.getAllPrices();

		// store users' sum of stocks
		List<Double> theSumOfStocks = new ArrayList<Double>();
		
		if (!api.isError(priceMap)) {
			for (int i = 0; i < theUsers.size(); i++) {
				double price = 0;
				List<Property> prop = userService.getProperties(theUsers.get(i).getId());
				for (int j = 0; j < prop.size(); j++) {
					if (priceMap.containsKey(prop.get(j).getStockName())) {
						price += priceMap.get(prop.get(j).getStockName()) * prop.get(j).getNumStocks();
					}
				}
				theSumOfStocks.add(price);
			}
		}else {
			System.out.println("Error happens");
		}

		// add the users to the model
		theModel.addAttribute("users", theUsers);
		theModel.addAttribute("sumOfStocks", theSumOfStocks);

		return "list-users";
	}

	@GetMapping("/showFormForAdd")
	public String showFormForAdd(Model theModel) {
		// create model attribute to bind form data
		User theUser = new User();
		theModel.addAttribute("user", theUser);
		return "user-form";
	}

	@PostMapping("/saveUser")
	public String saveCustomer(@ModelAttribute("user") User theUser) {

		// save the customer using our service
		userService.saveUser(theUser);

		return "redirect:/user/list";
	}

	@GetMapping("/showFormForUpdate")
	public String showFormForUpdate(@RequestParam("userId") int theId, Model theModel) {
		// get the user from the db
		User theUser = userService.getUser(theId);
		// set user as a model attribute to pre-populate the form
		theModel.addAttribute("user", theUser);
		// send over to form
		return "user-form";
	}

	@GetMapping("/delete")
	public String deleteUser(@RequestParam("userId") int theId) {
		// delete the user
		userService.deleteUser(theId);
		return "redirect:/user/list";
	}
	
	@GetMapping("/symbolCheck")
	public String symbolCheck(@RequestParam("userId") int theId, Model theModel) {
		// get the user from the db
		User theUser = userService.getUser(theId);
		Map<String, String> stocks = new HashMap<>();
        stocks.put("AMZN", "AMZN");
        stocks.put("GOOG", "GOOG");
        stocks.put("NVDA", "NVDA");
		// set user as a model attribute to pre-populate the form
		theModel.addAttribute("user", theUser);
		theModel.addAttribute("stocks", stocks);
		UserSymbolCheck userSymbolCheck = new UserSymbolCheck(theId);
		theModel.addAttribute("userSymbolCheck", userSymbolCheck);
		return "symbol-check";
	}

	@PostMapping("/stockView")
	public String stockView(Model theModel, 
			@ModelAttribute("userSymbolCheck")UserSymbolCheck userSymbolCheck) {
		String stockName = userSymbolCheck.getStockName();
		int theId = userSymbolCheck.getUserId();
		Stock stock = getStockByName(stockName);
		Price price = stock.getTimeSeries().entrySet().iterator().next().getValue();
		// Get the property based on the id and stock name.
		Property property = userService.getProperty(theId, stockName);
		
		// In Case the property is null which means the user doesn't have this stock
		if (property == null) {
			property = new Property();
			property.setUserId(theId);
			property.setNumStocks(0);
			property.setStockName(stockName);
			userService.saveProperty(property);
		}
		theModel.addAttribute("price", price);
		theModel.addAttribute("userSymbolCheck", userSymbolCheck);
		theModel.addAttribute("property", property);
		return "stock-view";
	}
	
	
	
	
	// get from sell-stock
	@GetMapping("/sellStock")
	public String sellStock(Model theModel, @RequestParam("userId") int theId,
			@RequestParam("stockName") String stockName) {
		// get the user from the database
		User theUser = userService.getUser(theId);
		// Get the property based on the id and stock name.
		Property property = userService.getProperty(theId, stockName);
		
		// In Case the property is null which means the user doesn't have this stock
		if (property == null) {
			property = new Property();
			property.setUserId(theId);
			property.setNumStocks(0);
			property.setStockName(stockName);
			userService.saveProperty(property);
		}
		
		// Get the Stock information and price
		Stock stock = getStockByName(stockName);
		Price price = stock.getTimeSeries().entrySet().iterator().next().getValue();
		// Add transaction
		Transaction transaction = new Transaction(theUser.getId(), property.getStockName(), price.getClose());
		// set user as a model attribute to pre-populate the form
		theModel.addAttribute("user", theUser);
		theModel.addAttribute("property", property);
		theModel.addAttribute("stock", stock);
		theModel.addAttribute("price", price);
		theModel.addAttribute("transaction", transaction);
		return "sell-stock";
	}
	
	@GetMapping("/buyStock")
	public String buyStock(Model theModel, @RequestParam("userId") int theId,
			@RequestParam("stockName") String stockName) {
		// get the user from the database
		User theUser = userService.getUser(theId);
		// Get the property based on the id and stock name.
		Property property = userService.getProperty(theId, stockName);
		
		// In Case the property is null which means the user doesn't have this stock
		if (property == null) {
			property = new Property();
			property.setUserId(theId);
			property.setNumStocks(0);
			property.setStockName(stockName);
			userService.saveProperty(property);
		}
		
		// Get the Stock information and price
		Stock stock = getStockByName(stockName);
		Price price = stock.getTimeSeries().entrySet().iterator().next().getValue();
		// Add transaction
		Transaction transaction = new Transaction(theUser.getId(), property.getStockName(), price.getClose());
		// set user as a model attribute to pre-populate the form
		theModel.addAttribute("user", theUser);
		theModel.addAttribute("property", property);
		theModel.addAttribute("stock", stock);
		theModel.addAttribute("price", price);
		theModel.addAttribute("transaction", transaction);
		return "buy-stock";
	}
	
	@PostMapping("/sell")
	public String sellStockView(Model theModel, @ModelAttribute("transaction") Transaction transaction) {
		System.out.println(transaction);
		double price = transaction.getPrice();
		int numberToSell = transaction.getNumToBuyOrSell();
		int userId = transaction.getUserId();
		double moneyGet = price * numberToSell;
		String stockName = transaction.getStockName();
		User theUser = userService.getUser(userId);

		Property property = userService.getProperty(userId, stockName);
		int numberOwned = property.getNumStocks();
		
		// invalid input
		if (numberOwned < numberToSell) {
			theModel.addAttribute("user", theUser);
			return "Warning";
		}
		
		// save the sell
		theUser.setBalance(theUser.getBalance() + moneyGet);
		userService.saveUser(theUser);
		
		if (numberOwned == numberToSell) {
			userService.deleteProperty(property.getId());
		} else {
			property.setNumStocks(property.getNumStocks() - numberToSell);
			userService.saveProperty(property);
		}
		// get users from the service
		List<User> theUsers = userService.getUsers();
		// TODO temporally use the user of index 0
		// Please change it to the current user.
		User tmpUser = theUsers.get(0);

		// add the user to the model
		theModel.addAttribute("user", tmpUser);
		return "user-profile";
	}
	
	@PostMapping("/buy")
	public String buyStockView(Model theModel, @ModelAttribute("transaction") Transaction transaction) {
		System.out.println(transaction);
		double price = transaction.getPrice();
		int numberToBuy = transaction.getNumToBuyOrSell();
		int userId = transaction.getUserId();
		double moneySpent = price * numberToBuy;
		String stockName = transaction.getStockName();
		User theUser = userService.getUser(userId);
		double balance = theUser.getBalance();
		
		// invalid input
		if (balance < moneySpent) {
			theModel.addAttribute("user", theUser);
			return "Warning";
		}
		
		// save change
		theUser.setBalance(balance - moneySpent);
		userService.saveUser(theUser);
		Property property = userService.getProperty(userId, stockName);

		int numberOwned = property.getNumStocks();
		property.setNumStocks(property.getNumStocks() + numberToBuy);
		userService.saveProperty(property);
		// get users from the service
		List<User> theUsers = userService.getUsers();
		
		// Please change it to the current user.
		User tmpUser = theUsers.get(0);

		// add the user to the model
		theModel.addAttribute("user", tmpUser);
		return "user-profile";
	}
	
	@GetMapping("/rank")
	public String userRanks(Model theModel) {
		// get users from the service
		List<User> theUsers = userService.getUsers();

		// Get all comapany's stock prices
		Api api = new Api();
		HashMap<String, Double> priceMap = api.getAllPrices();

		// store users' sum of stocks
		List<Double> theSum = new ArrayList<Double>();
		
		if (!api.isError(priceMap)) {
			for (int i = 0; i < theUsers.size(); i++) {
				double price = 0;
				List<Property> prop = userService.getProperties(theUsers.get(i).getId());
				for (int j = 0; j < prop.size(); j++) {
					if (priceMap.containsKey(prop.get(j).getStockName())) {
						price += priceMap.get(prop.get(j).getStockName()) * prop.get(j).getNumStocks();
					}
				}
				price += theUsers.get(i).getBalance();
				theSum.add(price);
			}
		}else {
			System.out.println("Error happens");
		}
		
		List<Pair> orderedUsers = new ArrayList<>();
		for (int i = 0; i < theUsers.size(); i++) {
			orderedUsers.add(new Pair(theUsers.get(i), theSum.get(i)));
		}
		Collections.sort(orderedUsers, (P1, P2) -> P2.getProperties() - P1.getProperties() > 0 ? 1 : -1);

		// add the users to the model
		theModel.addAttribute("allProperties", orderedUsers);
		return "users-rank";
	}
	
	
	
	@GetMapping("/test")
	public String test() {
		Api api = new Api();
		String companySymbol = "MSFT";
		double price = api.getPrice(companySymbol);
		System.out.print("Open Price " + price);
		return "test-json";
	}

	@GetMapping("/profile")
	public String showProfile(Model theModel) {
		// get users from the service
		List<User> theUsers = userService.getUsers();
		// TODO temporally use the user of index 0
		// Please change it to the current user.
		User tmpUser = theUsers.get(0);
		
		// add the user to the model
		theModel.addAttribute("user", tmpUser);
		return "user-profile";
	}

	// Test get list
	@GetMapping("/test1")
	public String listPropertiess(Model theModel) {
		List<Property> res = userService.getProperties(1);
		return "test-json";
	}

	// Test get list
	@GetMapping("/test2")
	public String listSumOfStocks(Model theModel) {
		List<Property> res = userService.getProperties(1);
		userService.getSumOfStocks(res);
		return "test-json";
	}
	
	private Stock getStockByName(String name) {
		String text = "";
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.getForEntity(
				String.format("https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=%s&interval=1min&apikey=ACPN4KEH4052XAQ6", name),
				String.class);

		if (HttpStatus.OK == response.getStatusCode()) {
			text = response.getBody().toString();
		} else {
			System.out.println("Error");
		}
		ObjectMapper mapper = new ObjectMapper();
		Stock stock = new Stock();
		try {
			// JSON string to Java object
			stock = mapper.readValue(text, Stock.class);
		} catch (IOException e) {
			System.out.println("Test failed.");
			e.printStackTrace();
		}
		return stock;
	}

}
