package com.github.travelbuddy.board.repository;

import com.github.travelbuddy.board.dto.BoardAllDto;
import com.github.travelbuddy.board.entity.BoardEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BoardRepository extends JpaRepository<BoardEntity, Integer> {

    @Query("SELECT b " +
            "FROM BoardEntity b " +
            "LEFT JOIN FETCH b.user u " +
            "LEFT JOIN FETCH b.route r " +
            "LEFT JOIN FETCH b.postImages pi " +
            "WHERE (:category IS NULL OR b.category = :category) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (r.startAt <= :endDate AND r.endAt >= :startDate))")
    List<BoardEntity> findAllWithRepresentativeImageAndDateRange(@Param("category") BoardEntity.Category category, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Sort sort);

    @Query("SELECT COUNT(l) FROM LikesEntity l WHERE l.board.id = :boardId")
    Long countLikesByBoardId(@Param("boardId") Integer boardId);


    @Query(value = "SELECT b.id, b.title, b.summary, b.content, b.category, u.id, u.name AS author, u.profile_picture_url AS userProfile, " +
                   "r.id, r.start_at, r.end_at, " +
                   "COALESCE(l.like_count, 0) AS like_count, " +
                   "pi.url AS image, " +
                   "rd.day AS route_day, " +
                   "rdp.place_name AS place_name, " +
                   "rdp.place_category AS place_category, " +
                   "t.id, t.age_min, t.age_max, t.target_number, t.participant_count, t.gender " +
                   "FROM boards b " +
                   "LEFT JOIN users u ON b.user_id = u.id " +
                   "LEFT JOIN routes r ON b.route_id = r.id " +
                   "LEFT JOIN post_imgs pi ON b.id = pi.post_id " +
                   "LEFT JOIN (SELECT post_id, COUNT(*) AS like_count FROM likes GROUP BY post_id) l ON b.id = l.post_id " +
                   "LEFT JOIN routes_day rd ON r.id = rd.routes_id " +
                   "LEFT JOIN routes_day_place rdp ON rd.id = rdp.routes_day_id " +
                   "LEFT JOIN trips t ON b.id = t.post_id " +
                   "WHERE b.id = :postId " +
                   "GROUP BY b.id, b.title, b.summary, b.content, b.category, u.id, u.name, r.id, r.start_at, r.end_at, l.like_count, pi.url, rd.day, rdp.place_name, rdp.place_category, t.id, t.age_min, t.age_max, t.target_number, t.participant_count, t.gender", nativeQuery = true)
    List<Object[]> findPostDetailsById(@Param("postId") Integer postId);

    @Query("SELECT b.id, b.title, b.summary, pi.url, b.category , b.createdAt " +
            "FROM BoardEntity b " +
            "LEFT JOIN b.postImages pi ON pi.id = (SELECT MIN(pi2.id) FROM PostImageEntity pi2 WHERE pi2.board.id = b.id)" +
            "WHERE b.user.id = :userId AND b.category = :category " +
            "GROUP BY b.id, pi.url")
    List<Object[]> findBoardsByUserIdAndCategory(@Param("userId") Integer userId, @Param("category") BoardEntity.Category category);

    @Query("SELECT b " +
            "FROM BoardEntity b " +
            "LEFT JOIN FETCH b.user u " +
            "LEFT JOIN FETCH b.route r " +
            "LEFT JOIN FETCH b.postImages pi " +
            "LEFT JOIN LikesEntity l ON b.id = l.board.id " +
            "WHERE l.user.id = :userId " +
            "AND (:category IS NULL OR b.category = :category) " +
            "AND (:startDate IS NULL OR :endDate IS NULL OR (r.startAt <= :endDate AND r.endAt >= :startDate)) " +
            "GROUP BY b.id, u.id, r.id, pi.id")
    List<BoardEntity> findLikedPostsByUserIdAndCategory(
            @Param("userId") Integer userId,
            @Param("category") BoardEntity.Category category,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate,
            Sort sort);

    @Query("SELECT b.id, b.title, b.createdAt, u.name, COUNT(l.id) as likeCount," +
            "(SELECT pi.url FROM PostImageEntity pi WHERE pi.board.id = b.id ORDER BY pi.id LIMIT 1) as representativeImage " +
            "FROM BoardEntity b LEFT JOIN LikesEntity l ON b.id = l.board.id " +
            "JOIN UserEntity u ON b.user.id = u.id " +
            "WHERE b.category = :category " +
            "GROUP BY b.id, b.title, b.createdAt " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'likeCount' THEN COUNT(l.id) END DESC, " +
            "CASE WHEN :sortBy = 'createdAt' THEN b.createdAt END DESC " +
            "LIMIT 4")
    List<Object[]> findTop4BoardsByCategoryWithRepresentativeImage(
            @Param("category") BoardEntity.Category category,
            @Param("sortBy") String sortBy);

    @Query("SELECT COUNT(b.id) FROM BoardEntity b WHERE b.user.id = :userId and b.category = :category")
    Integer countByUserIdAndCategory(@Param("userId") Integer userId, @Param("category") BoardEntity.Category category);
}