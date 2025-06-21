package org.zerock.mreview.service;


import org.zerock.mreview.dto.MovieDTO;
import org.zerock.mreview.dto.MovieImageDTO;
import org.zerock.mreview.dto.PageRequestDTO;
import org.zerock.mreview.dto.PageResultDTO;
import org.zerock.mreview.entity.Movie;
import org.zerock.mreview.entity.MovieImage;

import java.util.*;
import java.util.stream.Collectors;

public interface MovieService {

    Long register(MovieDTO movieDTO);
    // Map ==> key, value 형태 ( 대문자 차이)
    default Map<String,Object> dtoToEntity(MovieDTO movieDTO) {

        Map<String,Object> entityMap = new HashMap<>();

        Movie movie = Movie.builder()
                .mno(movieDTO.getMno())
                .title(movieDTO.getTitle())
                .build();

        entityMap.put("movie",movie); // key 값을 넣고 거기에 value 값을 넣음

        List<MovieImageDTO> imageDTOList = movieDTO.getImageDTOList();

        if(imageDTOList != null && imageDTOList.size() > 0) { // map() -> 각요소를 꺼내서 해당 객체로 사용
            List<MovieImage> movieImageList = imageDTOList.stream().map(movieImageDTO -> {
                //                                          stream --> 반복
                MovieImage movieImage = MovieImage.builder()
                        .uuid(movieImageDTO.getUuid())
                        .imgName(movieImageDTO.getImgName())
                        .path(movieImageDTO.getPath())
                        .movie(movie)
                        .build();
                return movieImage;
            }).collect(Collectors.toList());

            entityMap.put("imgList",movieImageList);
        }

        return entityMap;
    }

    PageResultDTO<MovieDTO, Object[]> getList(PageRequestDTO pageRequest);

    default MovieDTO entitiesToDTO(Movie movie, List<MovieImage> movieImages, Double avg, Long reviewCnt){
        MovieDTO movieDTO = MovieDTO.builder()
                .mno(movie.getMno())
                .title(movie.getTitle())
                .regDate(movie.getRegDate())
                .modDate(movie.getModDate())
                .build();

        List<MovieImageDTO> movieImageDTOList = movieImages.stream()
                .filter(Objects::nonNull).map(movieImage -> {
            return MovieImageDTO.builder().imgName(movieImage.getImgName())
                    .path(movieImage.getPath())
                    .uuid(movieImage.getUuid())
                    .build();
        }).collect(Collectors.toList());

        movieDTO.setImageDTOList(movieImageDTOList);
        movieDTO.setAvg(avg);
        movieDTO.setReviewCnt(reviewCnt.intValue());

        return movieDTO;

    }

    MovieDTO getMovie(Long mno);
}
