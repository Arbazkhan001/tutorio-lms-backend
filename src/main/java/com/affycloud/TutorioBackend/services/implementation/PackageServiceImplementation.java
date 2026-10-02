package com.affycloud.TutorioBackend.services.implementation;


import com.affycloud.TutorioBackend.dtos.address.AddressRequestDTO;
import com.affycloud.TutorioBackend.dtos.billingDetails.BillingDetailDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.BillingDetailsAddDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.CardAddDto;
import com.affycloud.TutorioBackend.dtos.billingDetails.CardDetailsDto;
import com.affycloud.TutorioBackend.dtos.feature.FeatureDetailDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.BuyPackageRequestDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageAddDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageDetailDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.PackageUpdateDto;
import com.affycloud.TutorioBackend.dtos.lmsPackage.purchased.MyPackageResponseDto;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponse;
import com.affycloud.TutorioBackend.dtos.reponse.ApiResponseWithMeta;
import com.affycloud.TutorioBackend.enums.Role;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.AdminRepository;
import com.affycloud.TutorioBackend.repositories.FeatureRepository;
import com.affycloud.TutorioBackend.repositories.PackagePurchaseRepository;
import com.affycloud.TutorioBackend.repositories.PackageRepository;
import com.affycloud.TutorioBackend.services.PackageService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackageServiceImplementation implements PackageService {

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PackagePurchaseRepository packagePurchaseRepository;

    @Autowired
    private FeatureRepository featureRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Map<String, Object> createPackage(PackageAddDto request) {
        Map<String, Object> response = new HashMap<>();

        if (packageRepository.existsByPackageNameIgnoreCase(request.getPackageName())) {
            response.put("message", "Package already exists.");
            response.put("error", true);
            return response;
        }

        LmsPackage pkg = modelMapper.map(request, LmsPackage.class);
        // Fetch and assign features
        List<Feature> features = new ArrayList<>();

        for(UUID id : request.getFeatureIds() ) {
        Feature feature = featureRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature not found."));
            features.add(feature);
        }
        pkg.setFeatures(features);

        // Calculate final price based on discount type
        Double finalPrice = request.getPrice();
        String discountType = request.getDiscountType();
        Double discountValue = request.getDiscountValue();

        if ("FLAT".equalsIgnoreCase(discountType)) {
            finalPrice -= discountValue;
        } else if ("PERCENTAGE".equalsIgnoreCase(discountType)) {
            finalPrice -= (finalPrice * (discountValue / 100));
        }

        if (finalPrice < 0) finalPrice = 0.0;
        pkg.setFinalPrice(finalPrice);

        packageRepository.save(pkg);
        response.put("message", "Package created successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public Map<String, Object> updatePackage(UUID id, PackageUpdateDto request) {
        Map<String, Object> response = new HashMap<>();

        LmsPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found."));

        if (request.getPackageName() != null && !request.getPackageName().isEmpty()) {
            pkg.setPackageName(request.getPackageName());
        }
        if (request.getPackageDescription() != null && !request.getPackageDescription().isEmpty()) {
            pkg.setPackageDescription(request.getPackageDescription());
        }
        if (request.getPrice() != null) {
            pkg.setPrice(request.getPrice());
        }
        if (request.getDiscountType() != null) {
            pkg.setDiscountType(request.getDiscountType());
        }
        if (request.getDiscountValue() != null) {
            pkg.setDiscountValue(request.getDiscountValue());
        }

        // recalc price
        pkg.setFinalPrice(calculateFinalPrice(pkg.getPrice(), pkg.getDiscountType(), pkg.getDiscountValue()));
        packageRepository.save(pkg);

        response.put("message", "Package updated successfully.");
        response.put("error", false);
        return response;
    }

    @Override
    public PackageDetailDto getPackageById(UUID id) {
        LmsPackage pkg = packageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found."));

        return modelMapper.map(pkg, PackageDetailDto.class);
    }

    @Override
    public List<PackageDetailDto> getAllPackages() {
        List<LmsPackage> packages = packageRepository.findAll();
        if (packages.isEmpty()) {
            return Collections.emptyList();
        }

        return packages.stream()
                .sorted(Comparator.comparing(LmsPackage::getFinalPrice)) // ✅ sort by finalPrice ascending
                .map(pkg -> modelMapper.map(pkg, PackageDetailDto.class))
                .collect(Collectors.toList());
    }


    @Override
    public ApiResponse buyPackage(UUID userId, BuyPackageRequestDto request) {
        User user = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            return new ApiResponse("Only Admins can buy a package", true);
        }

        LmsPackage lmsPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        // --- Billing Details ---
        BillingDetails billingDetails = new BillingDetails();
        BillingDetailsAddDto billingDto = request.getBillingDetails();
        billingDetails.setName(billingDto.getName());
        billingDetails.setEmail(billingDto.getEmail());
        billingDetails.setPhone(billingDto.getPhone());
        billingDetails.setCompanyName(billingDto.getCompanyName());
        billingDetails.setTaxId(billingDto.getTaxId());

        AddressRequestDTO addrDto = billingDto.getAddress();
        BillingAddress address = new BillingAddress();
        address.setStreet(addrDto.getStreet());
        address.setCity(addrDto.getCity());
        address.setState(addrDto.getState());
        address.setPostalCode(addrDto.getPostalCode());
        address.setCountry(addrDto.getCountry());

        billingDetails.setBillingAddress(address);

        // --- Card Details ---
        CardAddDto cardDto = request.getCardDetails();
        CardDetails cardDetails = new CardDetails();
        cardDetails.setCardholderName(cardDto.getCardholderName());
        cardDetails.setCardNumber(cardDto.getCardNumber()); // full card number
        cardDetails.setExpiryMonth(cardDto.getExpiryMonth());
        cardDetails.setExpiryYear(cardDto.getExpiryYear());
        cardDetails.setCvv(cardDto.getCvv());
        cardDetails.setCardType(cardDto.getCardType());

        // --- Package Purchase ---
        PackagePurchase purchase = new PackagePurchase();
        purchase.setUser(user);
        purchase.setLmsPackage(lmsPackage);
        purchase.setBillingDetails(billingDetails);
        purchase.setCardDetails(cardDetails);
        purchase.setTransactionStatus("SUCCESS"); // later integrate payment gateway
        purchase.setPaymentReference(UUID.randomUUID().toString()); // placeholder

        // save purchase
        packagePurchaseRepository.save(purchase);

        // link user with the package (replace old package)
        user.setLmsPackage(lmsPackage);
        adminRepository.save(user);

        return new ApiResponse("Package purchased successfully!", false);
    }


   /* @Override
    public ApiResponse buyPackage(UUID userId, BuyPackageRequestDto request) {
        User user = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getRole() != Role.ADMIN) {
            return new ApiResponse("Only Admins can buy a package", true);
        }

        LmsPackage lmsPackage = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new ResourceNotFoundException("Package not found"));

        // Replace old package with new one
        user.setLmsPackage(lmsPackage);
        adminRepository.save(user);

        return new ApiResponse("Package purchased successfully!", false);
    }*/

    @Override
    public ApiResponseWithMeta getMyPackage(UUID userId) {
        User user = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getLmsPackage() == null) {
            return new ApiResponseWithMeta( "No package assigned to this user", true,null);
        }

        PackageDetailDto dto = modelMapper.map(user.getLmsPackage(), PackageDetailDto.class);
        Map <String, Object> meta = new HashMap<>();
        meta.put("package", dto);
        return new ApiResponseWithMeta("Package fetched successfully", false, meta);
    }

    @Override
    public ApiResponseWithMeta getMyPackageWithBilling(UUID userId) {
        User user = adminRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getLmsPackage() == null) {
            return new ApiResponseWithMeta("No package assigned to this user", true, null);
        }

        // find purchase record
        PackagePurchase purchase = packagePurchaseRepository
                .findTopByUserIdOrderByCreatedAtDesc(userId);

        // map package
        PackageDetailDto packageDto = modelMapper.map(purchase.getLmsPackage(), PackageDetailDto.class);

        // map billing
        BillingDetailDto billingDto = modelMapper.map(purchase.getBillingDetails(), BillingDetailDto.class);

        // map card (mask card number before returning)
        CardDetailsDto cardDto = modelMapper.map(purchase.getCardDetails(), CardDetailsDto.class);
        if (cardDto.getCardNumber() != null && cardDto.getCardNumber().length() > 4) {
            String last4 = cardDto.getCardNumber().substring(cardDto.getCardNumber().length() - 4);
            cardDto.setCardNumber("**** **** **** " + last4);
        }

        // prepare final response DTO
        MyPackageResponseDto responseDto = new MyPackageResponseDto();
        responseDto.setLmsPackage(packageDto);
        responseDto.setBillingDetails(billingDto);
        responseDto.setCardDetails(cardDto);
        responseDto.setTransactionStatus(purchase.getTransactionStatus());
        responseDto.setPaymentReference(purchase.getPaymentReference());
        responseDto.setPurchasedAt(purchase.getCreatedAt());

        Map<String, Object> meta = new HashMap<>();
        meta.put("myPackage", responseDto);

        return new ApiResponseWithMeta("Package fetched successfully", false, meta);
    }

    @Override
    public List<FeatureDetailDto> getAllFeaturesWithPackage(UUID packageId) {
        List<Feature> allFeatures = featureRepository.findAll();

        // Package fetch karo
        LmsPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found."));

        // Package ke features ko set me rakho for quick lookup
        Set<UUID> packageFeatureIds = pkg.getFeatures().stream()
                .map(Feature::getId)
                .collect(Collectors.toSet());

        // Map karke DTO banayein aur inPackage set karein
        return allFeatures.stream()
                .map(feature -> {
                    FeatureDetailDto dto = modelMapper.map(feature, FeatureDetailDto.class);
                    dto.setInPackage(packageFeatureIds.contains(feature.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String toggleFeatureInPackage(UUID packageId, UUID featureId) {
        // Package fetch karo
        LmsPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Package not found."));

        // Feature fetch karo
        Feature feature = featureRepository.findById(featureId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Feature not found."));

        String message;
        if (pkg.getFeatures().contains(feature)) {
            pkg.getFeatures().remove(feature);   // ✅ remove
            message = "Feature removed from package.";
        } else {
            pkg.getFeatures().add(feature);      // ✅ add
            message = "Feature added to package.";
        }

        packageRepository.save(pkg);
        return message;
    }




    private Double calculateFinalPrice(Double price, String discountType, Double discountValue) {
        if (price == null || discountValue == null || discountType == null) return price;
        if (discountType.equalsIgnoreCase("PERCENTAGE")) {
            return price - (price * (discountValue / 100));
        } else if (discountType.equalsIgnoreCase("FLAT")) {
            return price - discountValue;
        }
        return price;
    }

}


