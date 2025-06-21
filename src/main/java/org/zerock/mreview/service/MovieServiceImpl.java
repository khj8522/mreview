package org.zerock.mreview.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.zerock.mreview.dto.MovieDTO;
import org.zerock.mreview.dto.PageRequestDTO;
import org.zerock.mreview.dto.PageResultDTO;
import org.zerock.mreview.entity.Movie;
import org.zerock.mreview.entity.MovieImage;
import org.zerock.mreview.repository.MovieImageRepository;
import org.zerock.mreview.repository.MovieRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@Log4j2
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;

    private final MovieImageRepository imageRepository;

    @Override
    public Long register(MovieDTO movieDTO) {
        Map<String,Object> entityMap = dtoToEntity(movieDTO);

        Movie movie = (Movie) entityMap.get("movie");
        List<MovieImage> movieImageList = (List<MovieImage>) entityMap.get("imgList");

        movieRepository.save(movie);

        movieImageList.forEach(image -> {
            imageRepository.save(image);
        });

        return movie.getMno();
    }

    @Override
    public PageResultDTO<MovieDTO,Object[]> getList(PageRequestDTO requestDTO) {
        // 페이징 방법 설정
        Pageable pageable = requestDTO.getPageable(Sort.by("mno").descending());

        Page<Object[]> result = movieRepository.getListPage(pageable);
        // result ==> 페이징 된 영화, 영화이미지, 평점, 리뷰개수 반환 ( 모든 데이터를 가져옴)

        log.info("==============================================");
        result.getContent().forEach(arr -> {
            log.info(Arrays.toString(arr));
        });

        Function<Object[],MovieDTO> fn = (arr->entitiesToDTO(
                (Movie)arr[0] ,
                (List<MovieImage>)(Arrays.asList((MovieImage)arr[1])),
                (Double)arr[2],
                (Long)arr[3]
        ));
        // 각 Object[]를 MovieDTO로 변환
        return new PageResultDTO<>(result,fn);
        // 반환된 MovieDTO는 PageResultDTO의 dtoList에 들어감 --> PageResultDTO에서 확인(Stream 사용)
        // dtoList에는 영화마다 해당 데이터가 들어가고 다양한 영화가 존재
    }

    @Override
    public MovieDTO getMovie(Long mno) {
        List<Object[]> result = movieRepository.getMovieWithAll(mno);

        Movie movie = (Movie) result.get(0)[0]; // 이미지가 2개일경우 이미지만 다르고 나머진 같은 값이 2행 출력
        // get(0) ==> 첫번째 행(여러 컬럼값이 들어가있음)
        // [0] ==> 첫번째 컬럼 값

        List<MovieImage> movieImageList = new ArrayList<>();

        result.forEach(arr -> {
            MovieImage movieImage = (MovieImage) arr[1];
            movieImageList.add(movieImage);
        });

        Double avg = (Double) result.get(0)[2];
        Long reviewCnt = (Long) result.get(0)[3];

        return entitiesToDTO(movie, movieImageList, avg, reviewCnt); // MovieDTO로 변환
    }
}
