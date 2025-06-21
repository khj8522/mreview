package org.zerock.mreview.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@ToString(exclude = "movie")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class MovieImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inum;

    private String uuid; // 고유 번호

    private String imgName;

    private String path; // 저장경로

    @ManyToOne(fetch = FetchType.LAZY)
    private Movie movie;
}
