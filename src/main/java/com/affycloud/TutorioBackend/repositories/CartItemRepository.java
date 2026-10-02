package com.affycloud.TutorioBackend.repositories;

import com.affycloud.TutorioBackend.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByCartId(UUID cartId);

    List<CartItem> findByStudentId(UUID studentId); 
    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByEbook(Ebook ebook);
    List<CartItem> findByCourseAttempt(CourseAttempt courseAttempt);
    List<CartItem> findByBook(Book book);
    List<CartItem> findByCourseBundle(CourseBundle bundle);
    List<CartItem> findByTestSeries(TestSeries testSeries);

    @Query("SELECT ci.testSeries.id FROM CartItem ci WHERE ci.cart.student.id = :studentId AND ci.testSeries IS NOT NULL")
    Set<UUID> findTestSeriesIdsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT ci.book.id FROM CartItem ci WHERE ci.cart.student.id = :studentId AND ci.book IS NOT NULL")
    Set<UUID> findBookIdsByStudentId(@Param("studentId") UUID studentId);
    @Query("SELECT ci.courseAttempt.id FROM CartItem ci WHERE ci.cart.student.id = :studentId AND ci.courseAttempt IS NOT NULL")
    Set<UUID> findCourseAttemptIdsByStudentId(@Param("studentId") UUID studentId);
    @Query("SELECT ci.ebook.id FROM CartItem ci WHERE ci.cart.student.id = :studentId AND ci.ebook IS NOT NULL")
    Set<UUID> findEbookIdsByStudentId(@Param("studentId") UUID studentId);
    @Query("SELECT ci.courseBundle.id FROM CartItem ci WHERE ci.cart.student.id = :studentId AND ci.courseBundle IS NOT NULL")
    Set<UUID> findCourseBundleIdsByStudentId(@Param("studentId") UUID studentId);
    @Query("SELECT e.courseAttempt.courseType.course.id FROM CartItem e WHERE e.student.id = :studentId AND e.courseAttempt IS NOT NULL")
    Set<UUID> findCourseIdsByStudentId(@Param("studentId") UUID studentId);

    @Query("SELECT e.courseAttempt.id FROM CartItem e " +
            "WHERE e.student.id = :studentId AND e.courseAttempt.courseType.course.id = :courseId")
    List<UUID> findAttemptIdsByStudentIdAndCourseId(@Param("studentId") UUID studentId, @Param("courseId") UUID courseId);

}

