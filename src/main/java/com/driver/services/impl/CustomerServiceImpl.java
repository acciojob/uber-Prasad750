package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.CabRepository;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		Driver availableDriver = null;
		for(Driver driver:driverList){
			if(driver.getCab().getAvailable()){
				availableDriver = driver;
				break;
			}
		}
		if(availableDriver==null){
			throw new Exception("No cab available!");
		}
		Customer customer = customerRepository2.findById(customerId).get();
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(availableDriver.getCab().getPerKmRate()*distanceInKm);
		tripBooking.setDriver(availableDriver);
		tripBooking.setCustomer(customer);

		tripBookingRepository2.save(tripBooking);

		availableDriver.getCab().setAvailable(false);
		availableDriver.getTripBookingList().add(tripBooking);
		driverRepository2.save(availableDriver);

		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		return tripBooking;







	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if(optionalTripBooking.isPresent())
		{
			TripBooking tripBooking=optionalTripBooking.get();
			tripBooking.setStatus(TripStatus.CANCELED);
			tripBooking.setBill(0);
			tripBooking.getDriver().getCab().setAvailable(true);

			tripBookingRepository2.save(tripBooking);
		}

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking=tripBookingRepository2.findById(tripId);
		if (optionalTripBooking.isPresent())
		{
			TripBooking tripBooking=optionalTripBooking.get();
			tripBooking.setStatus(TripStatus.COMPLETED);
			tripBooking.setBill(tripBooking.getDriver().getCab().getPerKmRate() * tripBooking.getDistanceInKm());
			tripBooking.getDriver().getCab().setAvailable(true);

			tripBookingRepository2.save(tripBooking);
		}



	}
}
