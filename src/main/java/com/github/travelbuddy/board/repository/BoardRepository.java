package com.github.travelbuddy.board.repository;

import com.github.travelbuddy.board.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {

    @Query(value = "SELECT b.id, b.category, b.title, b.summary, u.name AS author, r.start_at, r.end_at, pi.url AS representative_image, " +
                   "COALESCE(l.like_count, 0) AS like_count " +
                   "FROM boards b " +
                   "LEFT JOIN users u ON b.user_id = u.id " +
                   "LEFT JOIN routes r ON b.route_id = r.id " +
                   "LEFT JOIN post_imgs pi ON b.id = pi.post_id " +
                   "LEFT JOIN (SELECT post_id, COUNT(*) AS like_count FROM likes GROUP BY post_id) l ON b.id = l.post_id " +
                   "WHERE (:category IS NULL OR b.category = :category) " +
                   "AND (:startDate IS NULL OR :endDate IS NULL OR (DATE(r.start_at) <= DATE(:endDate) AND DATE(r.end_at) >= DATE(:startDate))) " +
                   "AND pi.id = (SELECT pi2.id FROM post_imgs pi2 WHERE pi2.post_id = b.id ORDER BY pi2.id LIMIT 1) " +
                   "ORDER BY " +
                   "CASE WHEN :sortBy = 'createdAt' AND :order = 'desc' THEN b.created_at END DESC, " +
                   "CASE WHEN :sortBy = 'createdAt' AND :order = 'asc' THEN b.created_at END ASC, " +
                   "CASE WHEN :sortBy = 'likes' AND :order = 'desc' THEN like_count END DESC, " +
                   "CASE WHEN :sortBy = 'likes' AND :order = 'asc' THEN like_count END ASC, " +
                   "CASE WHEN :sortBy = 'title' AND :order = 'asc' THEN b.title END ASC, " +
                   "CASE WHEN :sortBy = 'title' AND :order = 'desc' THEN b.title END DESC", nativeQuery = true)
    List<Object[]> findAllWithRepresentativeImageAndDateRange(@Param("category") String category, @Param("startDate") Date startDate, @Param("endDate") Date endDate, @Param("sortBy") String sortBy, @Param("order") String order);


    @Query(value = "SELECT b.id, b.title, b.summary, b.content, b.category, u.name AS author, " +
                   "r.start_at, r.end_at, " +
                   "COALESCE(l.like_count, 0) AS like_count, " +
                   "pi.url AS image, " +
                   "rd.day AS route_day, " +
                   "rdp.place_name AS place_name, " +
                   "rdp.place_category AS place_category, " +
                   "t.age_min, t.age_max, t.target_number, t.participant_count, t.gender " +
                   "FROM boards b " +
                   "LEFT JOIN users u ON b.user_id = u.id " +
                   "LEFT JOIN routes r ON b.route_id = r.id " +
                   "LEFT JOIN post_imgs pi ON b.id = pi.post_id " +
                   "LEFT JOIN (SELECT post_id, COUNT(*) AS like_count FROM likes GROUP BY post_id) l ON b.id = l.post_id " +
                   "LEFT JOIN routes_day rd ON r.id = rd.routes_id " +
                   "LEFT JOIN routes_day_place rdp ON rd.id = rdp.routes_day_id " +
                   "LEFT JOIN trips t ON b.id = t.post_id " +
                   "WHERE b.id = :postId " +
                   "GROUP BY b.id, b.title, b.summary, b.content, b.category, u.name, r.start_at, r.end_at, l.like_count, pi.url, rd.day, rdp.place_name, rdp.place_category, t.age_min, t.age_max, t.target_number, t.participant_count, t.gender", nativeQuery = true)
    List<Object[]> findPostDetailsById(@Param("postId") Integer postId);
}