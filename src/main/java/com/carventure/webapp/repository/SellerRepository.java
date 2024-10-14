package com.carventure.webapp.repository;

import com.carventure.webapp.model.Seller;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRepository extends MongoRepository<Seller, String> {
    Optional<Seller> findByEmail(String email);
    Optional<Seller> findByPhone(String phone);
}
