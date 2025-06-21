package org.zerock.mreview.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Commit;
import org.zerock.mreview.entity.Movie;
import org.zerock.mreview.entity.MovieImage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MovieRepositoryTest {

    @Autowired
    private MovieImageRepository imageRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    @Transactional
    @Commit
    public void insertMovies() {
        IntStream.rangeClosed(1,100).forEach(i -> {

            Movie movie = Movie.builder()
                    .title("Movie...."+i).build();
            System.out.println("----------------------------------------------");

            movieRepository.save(movie);

            // Math.random == 0이상 1 미만 * 5 ==> 0이상 5 미만 --> 0,1,2,3,4
            int count = (int)(Math.random()*5)+1;

            for(int j=0;j<count;j++){ // 랜덤 갯수의 이미지가 들어감
                MovieImage movieImage = MovieImage.builder()
                        .uuid(UUID.randomUUID().toString())
                        .movie(movie)
                        .imgName("test"+j+".jpg").build();
                imageRepository.save(movieImage);
            }

            System.out.println("=====================================");

        });
    }

    @Test
    public void testListPage() {
        PageRequest pageRequest = PageRequest.of(0, 10,
                Sort.by(Sort.Direction.DESC, "mno"));

        Page<Object[]> result = movieRepository.getListPage(pageRequest);

        for(Object[] objects : result.getContent()){ // getContent ==> 리스트 형태로 데이터를 가져옴
            System.out.println(Arrays.toString(objects)); // Arrays.toString을 써야 사람이 읽을수 있게 가져옴
        }
    }

    @Test
    public void testMovieWithAll() {
        List<Object[]> result = movieRepository.getMovieWithAll(94L);

        System.out.println(result);

        for(Object[] arr : result) {
            System.out.println(Arrays.toString(arr)); // 0번 인덱스 영화 , 1번 인덱스 영화이미지
        }
    }

}