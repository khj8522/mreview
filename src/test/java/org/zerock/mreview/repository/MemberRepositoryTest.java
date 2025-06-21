package org.zerock.mreview.repository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.zerock.mreview.entity.Member;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    public void insertMembers() {
        IntStream.rangeClosed(1,100).forEach(i -> {
            Member member = Member.builder()
                    .email("r"+i + "@email.com")
                    .pw("1111")
                    .nickname("reviewer"+i).build();
            memberRepository.save(member);

        });
    }

    @Transactional
    @Commit
    @Test
    public void testDeleteMember() {
        Long mid = 1L;

        Member member = Member.builder().mid(mid).build();

        //순서 주의 아래 코드로 하면 FK 오류가 남
        //memberRepository.delete(member);
        //reviewRepository.deleteByMember(member);

        reviewRepository.deleteByMember(member);
        memberRepository.delete(member);

    }

}