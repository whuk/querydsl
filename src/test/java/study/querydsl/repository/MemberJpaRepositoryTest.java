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
class MemberJpaRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private MemberJpaRepository memberJpaRepository;

    @Test
    void basicTest() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> all = memberJpaRepository.findAll();
        assertThat(all).containsExactly(member);

        List<Member> all_querydsl = memberJpaRepository.findAll_querydsl();
        assertThat(all_querydsl).containsExactly(member);

        List<Member> member1 = memberJpaRepository.findByUsername("member1");
        assertThat(member1).containsExactly(member);

        List<Member> member1_querydsl = memberJpaRepository.findByUsername_querydsl("member1");
        assertThat(member1_querydsl).containsExactly(member);
    }
}