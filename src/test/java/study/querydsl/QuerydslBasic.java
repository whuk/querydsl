package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
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
    public void resultFetchTest() {
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

    /**
     * 회원 정렬 순서
     * 1. 나이 내림차순(desc)
     * 2. 이름 올림차순(asc)
     * 단, 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> fetch = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.eq(100))
                .orderBy(QMember.member.age.desc(), QMember.member.username.asc().nullsLast())
                .fetch();

        Member member5 = fetch.get(0);
        Member member6 = fetch.get(1);
        Member memberNull = fetch.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> fetch = jpaQueryFactory.selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
        assertThat(fetch.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        QueryResults<Member> fetchResults = jpaQueryFactory.selectFrom(QMember.member)
                .orderBy(QMember.member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        assertThat(fetchResults.getTotal()).isEqualTo(4);
        assertThat(fetchResults.getLimit()).isEqualTo(2);
        assertThat(fetchResults.getOffset()).isEqualTo(1);
        assertThat(fetchResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = jpaQueryFactory
                .select(QMember.member.count(),
                        QMember.member.age.sum(),
                        QMember.member.age.avg(),
                        QMember.member.age.max(),
                        QMember.member.age.min()
                )
                .from(QMember.member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(QMember.member.count())).isEqualTo(4);
        assertThat(tuple.get(QMember.member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(QMember.member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(QMember.member.age.max())).isEqualTo(40);
        assertThat(tuple.get(QMember.member.age.min())).isEqualTo(10);

    }

    /**
     * 팀의 이름과 각팀의 평균 연령 구하
     *
     * @throws Exception
     */
    @Test
    public void groupBy() {
        List<Tuple> result = jpaQueryFactory.select(QTeam.team.name, QMember.member.age.avg())
                .from(QMember.member)
                .join(QMember.member.team, QTeam.team)
                .groupBy(QTeam.team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(QTeam.team.name)).isEqualTo("teamA");
        assertThat(teamA.get(QMember.member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(QTeam.team.name)).isEqualTo("teamB");
        assertThat(teamB.get(QMember.member.age.avg())).isEqualTo(35);
    }


    /**
     * 팀 A 에 소속된 모든 멤
     */
    @Test
    public void join() {
        List<Member> teamA = jpaQueryFactory.selectFrom(QMember.member)
                .join(QMember.member.team, QTeam.team)
                .where(QTeam.team.name.eq("teamA"))
                .fetch();

        assertThat(teamA)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    @Test
    public void thetaJoin() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = jpaQueryFactory.select(QMember.member)
                .from(QMember.member, QTeam.team)
                .where(QMember.member.username.eq(QTeam.team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 회원과 팀을 조인하면서 팀이름이 teamA 만 조인, 회원은 모두 조
     */
    @Test
    public void joinOnFiltering() {
        List<Tuple> teamA = jpaQueryFactory.select(QMember.member, QTeam.team)
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .on(QTeam.team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : teamA) {
            System.out.println("Tuple = " + tuple);
        }
    }

    /**
     * 연관관계가 없는 외부 조인
     * 회원 이름이 팀이름과 같은 대상 외부 조
     */
    @Test
    public void joinOnNoRelation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = jpaQueryFactory.select(QMember.member, QTeam.team)
                .from(QMember.member)
                .leftJoin(QTeam.team)
                .on(QMember.member.username.eq(QTeam.team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("Tuple = " + tuple);

        }
    }

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();

        Member member1 = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();
    }

    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();

        Member member1 = jpaQueryFactory.selectFrom(QMember.member)
                .join(QMember.member.team, QTeam.team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = entityManagerFactory.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치 조인 적").isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {

        QMember subMember = new QMember("subMember");

        List<Member> result = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.eq(
                        JPAExpressions.select(subMember.age.max())
                                .from(subMember)
                )).fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryGoeAvgAge() {

        QMember subMember = new QMember("subMember");

        List<Member> result = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.goe(
                        JPAExpressions.select(subMember.age.avg())
                                .from(subMember)
                )).fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    @Test
    public void subQueryIn() {

        QMember subMember = new QMember("subMember");

        List<Member> result = jpaQueryFactory.selectFrom(QMember.member)
                .where(QMember.member.age.in(
                        JPAExpressions.select(subMember.age)
                                .from(subMember)
                                .where(subMember.age.gt(10))
                )).fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    /**
     * from 의 서브쿼리 (인라인뷰) 는 JPQL 이 지원하지 않기 때문에 querydsl 도 지원하지 않는다.
     */
    @Test
    public void selectSubQuery() {
        QMember subMember = new QMember("subMember");
        List<Tuple> result = jpaQueryFactory.select(QMember.member.username,
                JPAExpressions
                        .select(subMember.age.avg())
                        .from(subMember))
                .from(QMember.member)
                .fetch();
        result.stream().forEach(tuple -> System.out.println(tuple));
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = jpaQueryFactory
                .select(QMember.member.username, QMember.member.age)
                .from(QMember.member)
                .fetch();

        for (Tuple t : result) {
            System.out.println(t.get(QMember.member.username) + "/" + t.get(QMember.member.age));
        }
    }

    @Test
    public void findDtoByField() {
        List<MemberDto> result = jpaQueryFactory
                .select(Projections.fields(MemberDto.class,
                        QMember.member.username,
                        QMember.member.age
                )).from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }

    @Test
    public void findUserDto() {
        List<UserDto> result = jpaQueryFactory
                .select(Projections.fields(UserDto.class,
                        QMember.member.username.as("name"),
                        QMember.member.age
                )).from(QMember.member)
                .fetch();
        for (UserDto u : result) {
            System.out.println(u);
        }
    }

    @Test
    public void findUserDtoWithSubQuery() {
        QMember subMember = new QMember("subMember");
        List<UserDto> result = jpaQueryFactory
                .select(Projections.fields(UserDto.class,
                        QMember.member.username.as("name"),
                        ExpressionUtils.as(
                                JPAExpressions.select(subMember.age.max())
                                        .from(subMember), "age")

                )).from(QMember.member)
                .fetch();
        for (UserDto u : result) {
            System.out.println(u);
        }
    }

    @Test
    public void findDtoByQueryProjection() {
        List<MemberDto> result = jpaQueryFactory
                .select(new QMemberDto(QMember.member.username, QMember.member.age))
                .from(QMember.member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void dynamicQueryBooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (usernameCond != null) {
            booleanBuilder.and(QMember.member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            booleanBuilder.and(QMember.member.age.eq(ageCond));
        }
        return jpaQueryFactory.selectFrom(QMember.member)
                .where(booleanBuilder)
                .fetch();
    }

    @Test
    public void dynamicQueryWhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return jpaQueryFactory.selectFrom(QMember.member)
//                .where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? QMember.member.age.eq(ageCond) : null;
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? QMember.member.username.eq(usernameCond) : null;
    }

    // composition 가능
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameCond != null && ageCond != null ? usernameEq(usernameCond).and(ageEq(ageCond)) : null;
    }

    @Test
    public void bulkUpdate() {
        // member1 = 10 -> DB member1
        // member2 = 20 -> DB member2
        // member3 = 30 -> DB member3
        // member4 = 40 -> DB member4

        // 영속성 컨텍스트 상태에 주의해야함.
        // 1차캐시를 무시하고 db에 바로 부어버림. 연산 후에 영속성 컨텍스트랑 db랑 상태가 다름
        long count = jpaQueryFactory.update(QMember.member)
                .set(QMember.member.username, "비회원")
                .where(QMember.member.age.lt(28))
                .execute();

        // member1 = 10 -> DB 비회원
        // member2 = 20 -> DB 비회원
        // member3 = 30 -> DB member3
        // member4 = 40 -> DB member4

        // 여기서 select 하면 쿼리는 나가지만 값은 1차캐시 값을 가져와서
        // 업데이트가 안된것처러 보임
        // 리피터블 리드 관련됨
        List<Member> members = jpaQueryFactory.selectFrom(QMember.member)
                .fetch();

        for (Member member : members) {
            System.out.println(member);
        }

        // 그래서 벌크 연산 후에는 영속성 컨텍스트를 날려야함
        em.flush();
        em.clear();

        List<Member> updatedMembers = jpaQueryFactory.selectFrom(QMember.member)
                .fetch();

        for (Member member : updatedMembers) {
            System.out.println(member);
        }
    }

    @Test
    public void bulkAdd() {
        long count = jpaQueryFactory
                .update(QMember.member)
                .set(QMember.member.age, QMember.member.age.multiply(2))
                .execute();

        List<Member> members = jpaQueryFactory.selectFrom(QMember.member)
                .fetch();

        for (Member member : members) {
            System.out.println(member);
        }

        // 벌크 연산 후에는 영속성 컨텍스트를 날려야함
        em.flush();
        em.clear();

        List<Member> updatedMembers = jpaQueryFactory.selectFrom(QMember.member)
                .fetch();

        for (Member member : updatedMembers) {
            System.out.println(member);
        }
    }

    @Test
    public void bulkDelete() {
        long count = jpaQueryFactory.delete(QMember.member)
                .where(QMember.member.age.lt(18))
                .execute();

        // 삭제는 바로 반영됨
        List<Member> members = jpaQueryFactory.selectFrom(QMember.member)
                .fetch();

        for (Member member : members) {
            System.out.println(member);
        }
    }

    @Test
    public void sqlFunction() {
        List<String> result = jpaQueryFactory
                .select(Expressions.stringTemplate(
                        "function('replace', {0}, {1}, {2})", QMember.member.username, "member", "M"
                )).from(QMember.member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void sqlFunction2() {
        List<Member> result = jpaQueryFactory
                .select(QMember.member)
                .from(QMember.member)
//                .where(QMember.member.username.eq(
//                        Expressions.stringTemplate(
//                                "function('lower', {0})", QMember.member.username)))
//
                .where(QMember.member.username.eq(QMember.member.username.lower()))
                .fetch();
        for (Member member : result) {
            System.out.println("s = " + member);
        }
    }
}
