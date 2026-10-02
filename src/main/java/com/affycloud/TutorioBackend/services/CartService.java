package com.affycloud.TutorioBackend.services;

import com.affycloud.TutorioBackend.dtos.Cart.AddToCartDto;

import java.util.Map;
import java.util.UUID;

public interface CartService {
    Map<String, Object> addToCart(AddToCartDto request);
    Map<String, Object> getCartDetails(UUID studentId);
    Map<String, Object> applyCouponToCart(UUID studentId, String couponCode);
    Map<String, Object> removeFromCart(UUID studentId, UUID itemId, String itemType);
}
