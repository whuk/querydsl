package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslBasic {

    @Autowired
    EntityManager em;

    JPAQueryFactory jpaQueryFactory;

    @BeforeEach
    public void before() {
        jpaQueryFactory = new JPAQueryFactory(em);
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
    }

    @Test
    public void startJPQL() {
        // member1 찾기
        String qlString = "select m from Member m where m.username = :username";
        Member member = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {

        Member member = jpaQueryFactory.select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();
        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        Member member = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1")
                        .and(QMember.member.age.eq(10)))
                .fetchOne();
        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member member = jpaQueryFactory.selectFrom(QMember.member)
                .where(
                        QMember.member.username.eq("member1"),
                        QMember.member.age.eq(10)
                )
                .fetchOne();
        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @Test
    public
    void resultFetchTest() {
//        List<Member> fetch = jpaQueryFactory.selectFrom(QMember.member)
//                .fetch();
//
//        Member fetchOne = jpaQueryFactory.selectFrom(QMember.member)
//                .fetchOne();
//
//        Member fetchFirst = jpaQueryFactory.selectFrom(QMember.member)
//                .fetchFirst();

//        QueryResults<Member> fetchResults = jpaQueryFactory.selectFrom(QMember.member)
//                .fetchResults();
//        long total = fetchResults.getTotal();
//        List<Member> results = fetchResults.getResults();

        long fetchCount = jpaQueryFactory.selectFrom(QMember.member)
                .fetchCount();
    }
}
