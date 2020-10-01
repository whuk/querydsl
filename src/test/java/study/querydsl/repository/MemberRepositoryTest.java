package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> all = memberRepository.findAll();
        assertThat(all).containsExactly(member);

        List<Member> all_querydsl = memberRepository.findAll();
        assertThat(all_querydsl).containsExactly(member);

        List<Member> member1 = memberRepository.findByUsername("member1");
        assertThat(member1).containsExactly(member);

        List<Member> member1_querydsl = memberRepository.findByUsername("member1");
        assertThat(member1_querydsl).containsExactly(member);
    }

}