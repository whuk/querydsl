package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.jpaQueryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> search(MemberSearchCondition memberSearchCondition) {
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")
                ))
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(memberSearchCondition.getUsername()),
                        teamNameEq(memberSearchCondition.getTeamName()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        ageLoe(memberSearchCondition.getAgeLoe())
                )
                .fetch();
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition memberSearchCondition, Pageable pageable) {
        QueryResults<MemberTeamDto> results = jpaQueryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")
                ))
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(memberSearchCondition.getUsername()),
                        teamNameEq(memberSearchCondition.getTeamName()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        ageLoe(memberSearchCondition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();    // fetchResult 는 쿼리 최적화가 불가해서 최적화를 위해 별도로 만

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition memberSearchCondition, Pageable pageable) {

        List<MemberTeamDto> content = jpaQueryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")
                ))
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(memberSearchCondition.getUsername()),
                        teamNameEq(memberSearchCondition.getTeamName()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        ageLoe(memberSearchCondition.getAgeLoe())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<MemberTeamDto> countQuery = jpaQueryFactory
                .select(new QMemberTeamDto(
                        QMember.member.id.as("memberId"),
                        QMember.member.username,
                        QMember.member.age,
                        QTeam.team.id.as("teamId"),
                        QTeam.team.name.as("teamName")
                ))
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                .where(
                        usernameEq(memberSearchCondition.getUsername()),
                        teamNameEq(memberSearchCondition.getTeamName()),
                        ageGoe(memberSearchCondition.getAgeGoe()),
                        ageLoe(memberSearchCondition.getAgeLoe())
                );

//        return new PageImpl<>(content,
//                pageable,
//                total);
        // 토탈 쿼리는 첫 페이지(토탈이 페이징보다 작을 경우)와 마지막 페이지인 경우에는 필요가없음. 최적화 가능
        return PageableExecutionUtils
                .getPage(content, pageable,
                        countQuery::fetchCount);
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? QMember.member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? QTeam.team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? QMember.member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? QMember.member.age.loe(ageLoe) : null;
    }
}
