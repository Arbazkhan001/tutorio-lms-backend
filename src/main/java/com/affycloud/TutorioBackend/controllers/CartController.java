package com.affycloud.TutorioBackend.controllers;

import com.affycloud.TutorioBackend.dtos.Cart.AddToCartDto;
import com.affycloud.TutorioBackend.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Operation(summary = "Add any item in the cart with studentId")
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody AddToCartDto request) {
        Map<String, Object> response = cartService.addToCart(request);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Remove any item from the cart with studentId, itemId and itemType")
    @DeleteMapping("/remove/{studentId}/{itemId}/{itemType}")
    public ResponseEntity<Map<String, Object>> removeFromCart(@PathVariable UUID studentId,
                                                              @PathVariable UUID itemId,
                                                              @PathVariable String itemType) {
        Map<String, Object> response = cartService.removeFromCart(studentId, itemId, itemType);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Get all items in the cart with studentId")
    @GetMapping("{studentId}")
    public ResponseEntity<Map<String, Object>> getCartDetails(@PathVariable UUID studentId) {
        Map<String, Object> response = cartService.getCartDetails(studentId);
        if (response.containsKey("error") && (boolean) response.get("error")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "Apply coupon to cart and return updated details")
    @GetMapping("/apply-coupon")
    public ResponseEntity<Map<String, Object>> applyCouponToCart(
            @RequestParam UUID studentId,
            @RequestParam(required = false) String couponCode) {


        Map<String, Object> response;
        if (couponCode == null || couponCode.isEmpty()) {
            response = cartService.getCartDetails(studentId);
        } else {
            response = cartService.applyCouponToCart(studentId, couponCode);
        }
        if ((boolean) response.getOrDefault("error", true)) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

}