package study.querydsl.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

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

    @Test
    void searchTest1() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeGoe(35);
        memberSearchCondition.setAgeLoe(40);
        memberSearchCondition.setTeamName("teamB");

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(memberSearchCondition);
        assertThat(memberTeamDtos).extracting("username").containsExactly("member4");
    }

    @Test
    void searchTest2() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository.searchByBuilder(memberSearchCondition);
        assertThat(memberTeamDtos).extracting("username").containsExactly("member1", "member2", "member3", "member4");
    }
}