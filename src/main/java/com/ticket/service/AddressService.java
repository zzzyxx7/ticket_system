package com.ticket.service;

import com.ticket.common.Result;
import com.ticket.entity.Address;

import java.util.List;

public interface AddressService {

    Result<List<Address>> getAddressList(Long userId);

    Result<String> addAddress(Address address, Long userId);

    Result<String> updateAddress(Long id, Address address, Long userId);

    Result<String> deleteAddress(Long id, Long userId);

    Result<String> setDefaultAddress(Long id, Long userId);
}


