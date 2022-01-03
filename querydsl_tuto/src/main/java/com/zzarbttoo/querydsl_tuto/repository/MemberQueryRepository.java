package com.zzarbttoo.querydsl_tuto.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zzarbttoo.querydsl_tuto.dto.MemberSearchCondition;
import com.zzarbttoo.querydsl_tuto.dto.MemberTeamDTO;
import com.zzarbttoo.querydsl_tuto.dto.QMemberTeamDTO;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;

import java.util.List;

import static com.zzarbttoo.querydsl_tuto.entity.QMember.member;
import static com.zzarbttoo.querydsl_tuto.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

//api나 화면에 너무 특화된 기능이면 그냥 따로 구현체를 만든다
@Repository
public class MemberQueryRepository {

    private final JPAQueryFactory queryFactory;

    public MemberQueryRepository(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    //querydsl 그냥 사용 가능
    public List<MemberTeamDTO> search(MemberSearchCondition condition){

        return queryFactory.select(new QMemberTeamDTO(
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

}
