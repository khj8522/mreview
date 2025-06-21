package org.zerock.mreview.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.zerock.mreview.entity.Member;
import org.zerock.mreview.entity.Movie;
import org.zerock.mreview.entity.Review;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    public void insertMovieReviews() {
        IntStream.rangeClosed(1,200).forEach(i -> {

            Long mno = (long)(Math.random()*100) + 1; // 영화

            Long mid = (long)(Math.random()*100) + 1; // 회원
            Member member = Member.builder().mid(mid).build();

            Review review = Review.builder()
                    .member(member)
                    .movie(Movie.builder().mno(mno).build())
                    .grade((int)(Math.random()*5)+1)
                    .text("이 영화에 대한 느낌...")
                    .build();
            reviewRepository.save(review);
        });
    }

    @Test
    public void findByMovie() {
        Movie movie = Movie.builder().mno(92L).build();

        List<Review> result = reviewRepository.findByMovie(movie);

        result.forEach(movieReview -> {
            System.out.print(movieReview.getReviewnum());
            System.out.print("\t"+movieReview.getGrade());
            System.out.print("\t"+movieReview.getText());
            // 아래 코드 실행할때 lazy 패치라 한번에 조회 못함 --> @EntityGraph --> 리파지토리에서 eager로 바꿔줌
            System.out.print("\t"+movieReview.getMember().getEmail());
            System.out.println("----------------------------");

        });
    }

}