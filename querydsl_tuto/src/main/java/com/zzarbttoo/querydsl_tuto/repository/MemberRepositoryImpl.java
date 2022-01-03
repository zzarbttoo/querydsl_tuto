package com.zzarbttoo.querydsl_tuto.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zzarbttoo.querydsl_tuto.dto.MemberSearchCondition;
import com.zzarbttoo.querydsl_tuto.dto.MemberTeamDTO;
import com.zzarbttoo.querydsl_tuto.dto.QMemberTeamDTO;
import com.zzarbttoo.querydsl_tuto.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;

import javax.persistence.EntityManager;
import java.util.List;

import static com.zzarbttoo.querydsl_tuto.entity.QMember.member;
import static com.zzarbttoo.querydsl_tuto.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

public class MemberRepositoryImpl extends QuerydslRepositorySupport implements MemberRepositoryCustom{

    //QuerydslRepositorySupport가 추상클래스이기 때문에 제공해줌
    //public MemberRepositoryImpl(Class<?> domainClass){
    public MemberRepositoryImpl(){
        //super(domainClass.class);
        super(Member.class);
    }

    //private final JPAQueryFactory queryFactory;
    //public MemberRepositoryImpl(EntityManager em){
        //this.queryFactory = new JPAQueryFactory(em);
    //}


    @Override
    public List<MemberTeamDTO> search(MemberSearchCondition condition){

        //from을 제공해줘서 from부터 하면 된다(querydsl 3버전에서는 from부터 시작
        return from(member).leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                ).select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")))
                .fetch();

    }

    //Predicate 대신 BooleanExpression을 이용하면 조합해서 이용이 가능해서 좋다
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }


    @Override
    public Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {

        QueryResults<MemberTeamDTO> results = queryFactory.select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
        ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                //.orderBy()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        List<MemberTeamDTO> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total);

    }

    @Override
    public Page<MemberTeamDTO> searchPageSimple2(MemberSearchCondition condition, Pageable pageable) {


        JPQLQuery<MemberTeamDTO> jpaQuery = from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ));

        //offset, limit는 줄일 수 있지만 method chain이 끊겨서 지져분..
        JPQLQuery<MemberTeamDTO> query = getQuerydsl().applyPagination(pageable, jpaQuery);
        List<MemberTeamDTO> fetch = query.fetch();

        //sort가 적용이 안된다
        //지저분하다..

        return null;
    }

    //데이터 내용과 count를 별도로 구현하는 쿼리
    @Override
    public Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDTO> content = queryFactory.select(new QMemberTeamDTO(
                member.id.as("memberId"),
                member.username,
                member.age,
                team.id.as("teamId"),
                team.name.as("teamName")
        ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                //.orderBy()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        // TODO : deprecated 돼서 직접 count 하는 쿼리를 작성해야한다
        JPAQuery<Member> countQuery = queryFactory.select(member).from(member)//.leftJoin(member.team, team) //count Query 최적화 가능
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                );//.fetchCount();


        //return new PageImpl<>(content, pageable, total);

        //count를 생략할 수 있을 때 생략하도록 한다
        //return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);

    }
}

