//package com.ngo.matching.repository;
//
//import com.ngo.matching.model.PostingResponse;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface PostingRepository extends JpaRepository<PostingResponse, Long> {
//
////    // both location and date
////    List<PostingResponse> findByLocationIgnoreCaseAndDateAndSlotsAvailableGreaterThan(
////            String location, LocalDate date, int slots);
////
////    // location only
////    List<PostingResponse> findByLocationIgnoreCaseAndSlotsAvailableGreaterThan(
////            String location, int slots);
////
////    // date only
////    List<PostingResponse> findByDateAndSlotsAvailableGreaterThan(
////            LocalDate date, int slots);
////
////    // no filters → only check slots
////    List<PostingResponse> findBySlotsAvailableGreaterThan(int slots);
//List<PostingResponse> findByVolunteersNeededGreaterThan(int minVolunteersNeeded);
//}
//
