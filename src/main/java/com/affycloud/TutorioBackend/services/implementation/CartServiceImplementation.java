package com.affycloud.TutorioBackend.services.implementation;

import com.affycloud.TutorioBackend.dtos.Cart.AddToCartDto;
import com.affycloud.TutorioBackend.enums.ItemType;
import com.affycloud.TutorioBackend.handler.InvalidOperationException;
import com.affycloud.TutorioBackend.handler.ResourceNotFoundException;
import com.affycloud.TutorioBackend.models.*;
import com.affycloud.TutorioBackend.repositories.*;
import com.affycloud.TutorioBackend.services.CartService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class CartServiceImplementation implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CourseBatchRepository courseBatchRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CourseAttemptRepository courseAttemptRepository;

    @Autowired
    private CourseBundleRepository courseBundleRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private EbookRepository ebookRepository;
    @Autowired
    private TestSeriesRepository testSeriesRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Override
    public Map<String, Object> addToCart(AddToCartDto request) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> studentOptional = studentRepository.findById(request.getStudentId());
            if (studentOptional.isEmpty()) {
                response.put("message", "Student not found.");
                response.put("error", true);
                return response;
            }
            Student student = studentOptional.get();

            // ✅ Check if the student already has a cart
            Cart cart = cartRepository.findByStudentId(student.getId())
                    .stream().findFirst().orElse(null);

            CourseAttempt courseAttempt = null; // ✅ Declare courseAttempt
            CourseBatch courseBatch = null;
            Ebook ebook = null;
            TestSeries testSeries = null;
            Book book = null;
            CourseBundle courseBundle = null;

            // ✅ Determine the item type
            if ("COURSE".equalsIgnoreCase(request.getItemType())) {
                Optional<CourseAttempt> courseAttemptOptional = courseAttemptRepository.findById(request.getItemId());
                if (courseAttemptOptional.isEmpty()) {
                    response.put("message", "Course Attempt not found.");
                    response.put("error", true);
                    return response;
                }
                courseAttempt = courseAttemptOptional.get();
                if (request.getBatchId() != null) {
                    courseBatch = courseBatchRepository.findById(request.getBatchId())
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Course Batch not found with id: " + request.getBatchId()));

                    // Check batch capacity
                    if (courseBatch.getStudentLimit() != null && courseBatch.getStudentLimit() > 0) {
                        long currentEnrollments = enrollmentRepository.countByCourseBatch_Id(courseBatch.getId());
                        if (currentEnrollments >= courseBatch.getStudentLimit()) {
                            response.put("message", "This course batch has reached its maximum student limit.");
                            response.put("error", true);
                            return response;
                        }
                    }
                }
            } else if ("BOOK".equalsIgnoreCase(request.getItemType())) {
                Optional<Book> bookOptional = bookRepository.findById(request.getItemId());
                if (bookOptional.isEmpty()) {
                    response.put("message", "Book not found.");
                    response.put("error", true);
                    return response;
                }
                book = bookOptional.get();
            } else if ("EBOOK".equalsIgnoreCase(request.getItemType())) {
                Optional<Ebook> ebookOptional = ebookRepository.findById(request.getItemId());
                if (ebookOptional.isEmpty()) {
                    response.put("message", "Ebook not found.");
                    response.put("error", true);
                    return response;
                }
                ebook = ebookOptional.get();
            } else if ("TESTSERIES".equalsIgnoreCase(request.getItemType())) {
                Optional<TestSeries> testSeriesOptional = testSeriesRepository.findById(request.getItemId());
                if (testSeriesOptional.isEmpty()) {
                    response.put("message", "Test Series not found.");
                    response.put("error", true);
                    return response;
                }
                testSeries = testSeriesOptional.get();
            } else if ("COURSEBUNDLE".equalsIgnoreCase(request.getItemType())) {
                Optional<CourseBundle> courseBundleOptional = courseBundleRepository.findById(request.getItemId());
                if (courseBundleOptional.isEmpty()) {
                    response.put("message", "Course Bundle not found.");
                    response.put("error", true);
                    return response;
                }
                courseBundle = courseBundleOptional.get();
            } else {
                response.put("message", "Invalid item type.");
                response.put("error", true);
                return response;
            }

            // ✅ If no cart exists, create a new one
            if (cart == null) {
                cart = new Cart();
                cart.setStudent(student);
                if (courseAttempt != null) {
                    cart.setCourseAttempt(courseAttempt);
                }
                cart = cartRepository.save(cart);
            } else {
                // For non-book items, check if the item already exists in the cart.
                if (courseAttempt != null) {
                    List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                    boolean alreadyExists = existingItems.stream()
                            .anyMatch(item -> item.getItemType() == ItemType.COURSE &&
                                    item.getCourseAttempt() != null &&
                                    item.getCourseAttempt().getId().equals(request.getItemId()));
                    if (alreadyExists) {
                        response.put("message", "This course is already in this cart.");
                        response.put("error", true);
                        return response;
                    }
                } else if (courseBatch != null) {
                    List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                    boolean alreadyExists = existingItems.stream()
                            .anyMatch(item -> item.getItemType() == ItemType.COURSE &&
                                    item.getCourseAttempt() != null &&
                                    item.getCourseAttempt().getId().equals(request.getItemId()));
                    if (alreadyExists) {
                        response.put("message", "This course is already in this cart.");
                        response.put("error", true);
                        return response;
                    }
                }

                else if (ebook != null) {
                    List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                    boolean alreadyExists = existingItems.stream()
                            .anyMatch(item -> item.getItemType() == ItemType.EBOOK &&
                                    item.getEbook() != null &&
                                    item.getEbook().getId().equals(request.getItemId()));
                    if (alreadyExists) {
                        response.put("message", "This E-book is already in this cart.");
                        response.put("error", true);
                        return response;
                    }
                } else if (testSeries != null) {
                    List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                    boolean alreadyExists = existingItems.stream()
                            .anyMatch(item -> item.getItemType() == ItemType.TESTSERIES &&
                                    item.getTestSeries() != null &&
                                    item.getTestSeries().getId().equals(request.getItemId()));
                    if (alreadyExists) {
                        response.put("message", "This Test-Series is already in this cart.");
                        response.put("error", true);
                        return response;
                    }
                } else if (courseBundle != null) {
                    List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                    boolean alreadyExists = existingItems.stream()
                            .anyMatch(item -> item.getItemType() == ItemType.COURSEBUNDLE &&
                                    item.getCourseBundle() != null &&
                                    item.getCourseBundle().getId().equals(request.getItemId()));
                    if (alreadyExists) {
                        response.put("message", "This Course-Bundle is already in this cart.");
                        response.put("error", true);
                        return response;
                    }
                }
            }

            // ✅ For BOOK items, if the book already exists in the cart, update its
            // quantity.
            if (book != null) {
                List<CartItem> existingItems = cartItemRepository.findByCart(cart);
                Optional<CartItem> existingBookItemOpt = existingItems.stream()
                        .filter(item -> item.getItemType() == ItemType.BOOK &&
                                item.getBook() != null &&
                                item.getBook().getBookId().equals(request.getItemId()))
                        .findFirst();
                if (existingBookItemOpt.isPresent()) {
                    CartItem existingBookItem = existingBookItemOpt.get();
                    int currentQuantity = existingBookItem.getQuantity() != null ? existingBookItem.getQuantity() : 1;
                    int addQuantity = (request.getQuantity() != null && request.getQuantity() > 0)
                            ? request.getQuantity()
                            : 1;
                    existingBookItem.setQuantity(currentQuantity + addQuantity);
                    cartItemRepository.save(existingBookItem);
                    response.put("message", "Book quantity updated in cart.");
                    response.put("error", false);
                    return response;
                }
            }

            // ✅ Create and save the cart item (for new entries)
            CartItem cartItem = new CartItem();
            cartItem.setStudent(student);
            cartItem.setCart(cart);

            if (courseAttempt != null) {
                if (courseBatch != null) {
                    cartItem.setCourseBatch(courseBatch);
                }
                cartItem.setCourseAttempt(courseAttempt);
                cartItem.setItemType(ItemType.COURSE);
            } else if (book != null) {
                cartItem.setBook(book);
                cartItem.setItemType(ItemType.BOOK);
                int quantity = (request.getQuantity() != null && request.getQuantity() > 0) ? request.getQuantity() : 1;
                cartItem.setQuantity(quantity);
            } else if (ebook != null) {
                cartItem.setEbook(ebook);
                cartItem.setItemType(ItemType.EBOOK);
            } else if (testSeries != null) {
                cartItem.setTestSeries(testSeries);
                cartItem.setItemType(ItemType.TESTSERIES);
            } else if (courseBundle != null) {
                cartItem.setCourseBundle(courseBundle);
                cartItem.setItemType(ItemType.COURSEBUNDLE);
            }

            cartItemRepository.save(cartItem);
            response.put("message", "Item added to cart successfully.");
            response.put("error", false);
        } catch (Exception e) {
            logger.error("Error adding to cart: ", e);
            response.put("message", "Unexpected error");
            response.put("error", true);
        }
        return response;
    }

    public Map<String, Object> removeFromCart(UUID studentId, UUID itemId, String itemType) {
        Map<String, Object> response = new HashMap<>();

        try {
            Optional<Student> studentOptional = studentRepository.findById(studentId);
            if (studentOptional.isEmpty()) {
                response.put("message", "Student not found.");
                response.put("error", true);
                return response;
            }

            Student student = studentOptional.get();

            // Fetch the cart for the student
            Cart cart = cartRepository.findByStudentId(studentId)
                    .stream().findFirst().orElse(null);

            if (cart == null) {
                response.put("message", "Cart not found.");
                response.put("error", true);
                return response;
            }

            CartItem cartItemToRemove = null;

            // Check the type of item and find the CartItem to remove
            if ("COURSE".equalsIgnoreCase(itemType)) {
                // Find the CartItem by courseAttemptId
                cartItemToRemove = cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItemType() == ItemType.COURSE
                                && cartItem.getCourseAttempt() != null
                                && cartItem.getCourseAttempt().getId().equals(itemId))
                        .findFirst().orElse(null);
            } else if ("BOOK".equalsIgnoreCase(itemType)) {
                // Find the CartItem by bookId
                cartItemToRemove = cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItemType() == ItemType.BOOK
                                && cartItem.getBook() != null
                                && cartItem.getBook().getBookId().equals(itemId))
                        .findFirst().orElse(null);
            } else if ("EBOOK".equalsIgnoreCase(itemType)) {
                // Find the CartItem by bookId
                cartItemToRemove = cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItemType() == ItemType.EBOOK
                                && cartItem.getEbook() != null
                                && cartItem.getEbook().getId().equals(itemId))
                        .findFirst().orElse(null);
            } else if ("TESTSERIES".equalsIgnoreCase(itemType)) {
                // Find the CartItem by bookId
                cartItemToRemove = cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItemType() == ItemType.TESTSERIES
                                && cartItem.getTestSeries() != null
                                && cartItem.getTestSeries().getId().equals(itemId))
                        .findFirst().orElse(null);
            } else if ("COURSEBUNDLE".equalsIgnoreCase(itemType)) {
                // Find the CartItem by bookId
                cartItemToRemove = cart.getCartItems().stream()
                        .filter(cartItem -> cartItem.getItemType() == ItemType.COURSEBUNDLE
                                && cartItem.getCourseBundle() != null
                                && cartItem.getCourseBundle().getId().equals(itemId))
                        .findFirst().orElse(null);
            } else {
                response.put("message", "Invalid item type.");
                response.put("error", true);
                return response;
            }

            if (cartItemToRemove == null) {
                response.put("message", "Item not found in cart.");
                response.put("error", true);
                return response;
            }

            // Remove the found CartItem
            cart.getCartItems().remove(cartItemToRemove); // Remove it from the cart's list of items
            cartItemRepository.delete(cartItemToRemove); // Actually delete from the DB

            // Save the updated cart (this step may be necessary for cascading behavior)
            cartRepository.save(cart);

            response.put("message", "Item removed from cart successfully.");
            response.put("error", false);
        } catch (Exception e) {
            logger.error("Error removing from cart: ", e);
            response.put("message", "Unexpected error.");
            response.put("error", true);
        }

        return response;
    }

    @Override
    public Map<String, Object> getCartDetails(UUID studentId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch the student to ensure the student exists
            Optional<Student> studentOptional = studentRepository.findById(studentId);

            if (studentOptional.isEmpty()) {
                response.put("message", "Student not found.");
                response.put("error", true);
                return response;
            }

            Student student = studentOptional.get();

            // Fetch the cart for the student
            Cart cart = cartRepository.findByStudentId(studentId)
                    .stream().findFirst().orElse(null);

            // Fetch all cart items for the student

            List<CartItem> cartItems = (cart != null) ? cartItemRepository.findByCartId(cart.getId())
                    : new ArrayList<>();

            // Prepare the response structure
            List<Map<String, Object>> cartItemsDetails = new ArrayList<>();
            Double grandTotal = 0.0; // Initialize grand total
            if (cartItems.isEmpty()) {
                // Return an empty cart if no items are found
                response.put("message", "Cart details fetched successfully.");
                response.put("error", false);
                response.put("meta", new HashMap<>()); // Empty cart details
                return response;
            }
            // Iterate through each cart item and populate the details
            for (CartItem cartItem : cartItems) {
                Map<String, Object> itemDetails = new HashMap<>();
                itemDetails.put("itemType", cartItem.getItemType().name());
                if (cartItem.getItemType() == ItemType.COURSE) {
                    // Populate course attempt details
                    CourseAttempt courseAttempt = cartItem.getCourseAttempt();
                    if (courseAttempt != null) {
                        Map<String, Object> courseDetails = new HashMap<>();
                        courseDetails.put("courseAttemptId", courseAttempt.getId());
                        courseDetails.put("courseId", courseAttempt.getCourseType().getCourse().getId());
                        courseDetails.put("title", courseAttempt.getCourseType().getCourse().getTitle());
                        courseDetails.put("description", courseAttempt.getCourseType().getDescription());
                        courseDetails.put("thumbnail", courseAttempt.getCourseType().getCourse().getThumbnail());
                        courseDetails.put("plan", courseAttempt.getPlan());
                        courseDetails.put("courseType", courseAttempt.getCourseType().getCourseType());
                        courseDetails.put("courseAttempt", courseAttempt.getAttempt());
                        courseDetails.put("coursePrice", courseAttempt.getPrice());
                        courseDetails.put("courseDiscountType", courseAttempt.getDiscountType());
                        courseDetails.put("courseDiscount", courseAttempt.getDiscount());
                        courseDetails.put("courseFinalPrice", courseAttempt.getFinalPrice());

                        courseDetails.put("totalPrice", courseAttempt.getFinalPrice());
                        if (cartItem.getCourseBatch() != null) {
                            CourseBatch batch = cartItem.getCourseBatch();
                            Map<String, Object> batchDetails = new HashMap<>();
                            batchDetails.put("batchId", batch.getId());
                            batchDetails.put("batchName", batch.getBatch());
                            courseDetails.put("batch", batchDetails);
                        }
                        // Add to grand total
                        grandTotal += courseAttempt.getFinalPrice();

                        itemDetails.put("course", courseDetails);
                    }
                } else if (cartItem.getItemType() == ItemType.BOOK) {
                    // Populate book details
                    Book book = cartItem.getBook();
                    if (book != null) {
                        Map<String, Object> ebookDetails = new HashMap<>();
                        ebookDetails.put("bookId", book.getBookId());
                        ebookDetails.put("title", book.getTitle());
                        ebookDetails.put("description", book.getDescription());
                        ebookDetails.put("thumbnail", book.getImages());
                        ebookDetails.put("price", book.getPrice());
                        ebookDetails.put("discount", book.getDiscount());
                        ebookDetails.put("discountType", book.getDiscountType());
                        ebookDetails.put("finalPrice", book.getFinalPrice());

                        // Multiply finalPrice by quantity for total price
                        Double totalPrice = book.getFinalPrice() * cartItem.getQuantity();
                        ebookDetails.put("totalPrice", totalPrice);
                        // Add to grand total
                        grandTotal += totalPrice;

                        itemDetails.put("book", ebookDetails);
                    }

                } else if (cartItem.getItemType() == ItemType.EBOOK) {
                    // Populate ebook details
                    Ebook ebook = cartItem.getEbook();
                    if (ebook != null) {
                        Map<String, Object> ebookDetails = new HashMap<>();
                        ebookDetails.put("ebookId", ebook.getId());
                        ebookDetails.put("title", ebook.getTitle());
                        ebookDetails.put("description", ebook.getDescription());
                        ebookDetails.put("url", ebook.getUrl());
                        ebookDetails.put("thumbnail", ebook.getThumbnailUrl());
                        ebookDetails.put("price", ebook.getPrice());
                        ebookDetails.put("discount", ebook.getDiscount());
                        ebookDetails.put("discountType", ebook.getDiscountType());
                        ebookDetails.put("finalPrice", ebook.getFinalPrice());

                        ebookDetails.put("totalPrice", ebook.getFinalPrice());
                        // Add to grand total
                        grandTotal += ebook.getFinalPrice();
                        itemDetails.put("ebook", ebookDetails);
                    }
                } else if (cartItem.getItemType() == ItemType.TESTSERIES) {
                    // Populate test series details
                    TestSeries testSeries = cartItem.getTestSeries();
                    if (testSeries != null) {
                        Map<String, Object> testSeriesDetails = new HashMap<>();
                        testSeriesDetails.put("testSeriesId", testSeries.getId());
                        testSeriesDetails.put("title", testSeries.getTitle());
                        testSeriesDetails.put("description", testSeries.getDescription());
                        testSeriesDetails.put("thumbnail", testSeries.getThumbnail());
                        testSeriesDetails.put("price", testSeries.getPrice());
                        testSeriesDetails.put("discount", testSeries.getDiscount());
                        testSeriesDetails.put("discountType", testSeries.getDiscountType());
                        testSeriesDetails.put("finalPrice", testSeries.getFinalPrice());

                        testSeriesDetails.put("totalPrice", testSeries.getFinalPrice());
                        grandTotal += testSeries.getFinalPrice();

                        itemDetails.put("testSeries", testSeriesDetails);
                    }
                } else if (cartItem.getItemType() == ItemType.COURSEBUNDLE) {
                    // Populate test series details
                    CourseBundle courseBundle = cartItem.getCourseBundle();
                    if (courseBundle != null) {
                        Map<String, Object> courseBundleDetails = new HashMap<>();
                        courseBundleDetails.put("courseBundleId", courseBundle.getId());
                        courseBundleDetails.put("title", courseBundle.getTitle());
                        courseBundleDetails.put("description", courseBundle.getDescription());
                        courseBundleDetails.put("thumbnail", courseBundle.getThumbnail());
                        courseBundleDetails.put("price", courseBundle.getPrice());
                        courseBundleDetails.put("discount", courseBundle.getDiscount());
                        courseBundleDetails.put("discountType", courseBundle.getDiscountType());
                        courseBundleDetails.put("finalPrice", courseBundle.getFinalPrice());

                        courseBundleDetails.put("totalPrice", courseBundle.getFinalPrice());
                        grandTotal += courseBundle.getFinalPrice();

                        itemDetails.put("courseBundle", courseBundleDetails);
                    }
                }
                // Add cart item details to the response list
                cartItemsDetails.add(itemDetails);
            }

            // Construct the final response with cart and cart items details
            Map<String, Object> cartDetails = new HashMap<>();
            cartDetails.put("studentId", studentId);
            cartDetails.put("cartId", cart.getId());
            cartDetails.put("items", cartItemsDetails);
            cartDetails.put("appliedCoupon", null);
            cartDetails.put("finalPrice", grandTotal);
            cartDetails.put("couponDiscount", 0.0);
            cartDetails.put("grandTotal", grandTotal); // add grand total
            response.put("message", "Cart details fetched successfully.");
            response.put("error", false);
            response.put("meta", cartDetails);

        } catch (Exception e) {
            logger.error("Error fetching cart details: ", e);
            response.put("message", "Unexpected error occurred.");
            response.put("error", true);
        }
        return response;
    }

    @Override
    public Map<String, Object> applyCouponToCart(UUID studentId, String couponCode) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch the student to ensure the student exists
            Optional<Student> studentOptional = studentRepository.findById(studentId);

            if (studentOptional.isEmpty()) {
                response.put("message", "Student not found.");
                response.put("error", true);
                return response;
            }

            Student student = studentOptional.get();

            // Fetch coupon
            Optional<Coupon> couponOptional = couponRepository.findByCode(couponCode);

            if (couponOptional.isEmpty()) {
                response.put("message", "Coupon not found.");
                response.put("error", true);
                return response;
            }

            Coupon coupon = couponOptional.get();

            if (!coupon.isActive()) {
                throw new InvalidOperationException("Coupon is not active");
            }

            if (coupon.getStudent() != null) {
                if (!coupon.getStudent().getId().equals(studentId)) {
                    throw new InvalidOperationException("This coupon is not assigned to this student");
                }
                if (coupon.isUsed()) {
                    throw new InvalidOperationException("Coupon has already been used");
                }
            }
            // Get cart details using existing logic
            Map<String, Object> cartData = getCartDetails(studentId);
            if ((boolean) cartData.getOrDefault("error", true)) {
                return cartData; // return existing error if any
            }

            Map<String, Object> meta = (Map<String, Object>) cartData.get("meta");
            double originalGrandTotal = (Double) meta.get("grandTotal");

            if (originalGrandTotal <= coupon.getDiscount()) {
                throw new InvalidOperationException("minimum amount should be greater than the coupon discount");
            }

            double newGrandTotal = originalGrandTotal - coupon.getDiscount();
            newGrandTotal = Math.max(0, newGrandTotal); // ensure non-negative total

            // Update meta with applied coupon
            meta.put("appliedCoupon", coupon.getCode());
            meta.put("finalPrice", originalGrandTotal);
            meta.put("couponDiscount", coupon.getDiscount());
            meta.put("grandTotal", newGrandTotal); // update grand total

            response.put("message", "Coupon applied successfully");
            response.put("error", false);
            response.put("meta", meta);

        } catch (ResourceNotFoundException | InvalidOperationException ex) {
            response.put("message", ex.getMessage());
            response.put("error", true);
        } catch (Exception ex) {
            logger.error("Error applying coupon: ", ex);
            response.put("message", "Unexpected error occurred.");
            response.put("error", true);
        }
        return response;
    }

}