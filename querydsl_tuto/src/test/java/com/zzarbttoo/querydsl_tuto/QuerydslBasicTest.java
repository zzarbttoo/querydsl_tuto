package com.zzarbttoo.querydsl_tuto;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zzarbttoo.querydsl_tuto.entity.Member;
import com.zzarbttoo.querydsl_tuto.entity.QMember;
import com.zzarbttoo.querydsl_tuto.entity.QTeam;
import com.zzarbttoo.querydsl_tuto.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static com.zzarbttoo.querydsl_tuto.entity.QMember.member;
import static com.zzarbttoo.querydsl_tuto.entity.QTeam.team;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    //동시성 문제가 없어서 필드로 가져와도 된다
    JPAQueryFactory queryFactory;

    @BeforeEach //data 넣고 시작
    public void before(){

        queryFactory= new JPAQueryFactory(em);
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


    // JPQL VS querydsl
    @Test
    public void startJPQL(){
        //member1을 찾아라

        String qlString = "select m from Member m where m.username = :username";
        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
        //JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        //QMember m = new QMember("m"); //어떤 Qmember인지를 구분하는 alias
        //QMember m = QMember.member; //기본으로 제공도 해준다

        //런타임 오류 방지 가능
        Member findMember = queryFactory.select(member) //static import 해서 사용하면 편리
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search(){

        //eq
        Member findMember = queryFactory.selectFrom(member) //select(member).from(member)
                .where(member.username.eq("member1")
                        //.and(member.age.eq(10)))
                        .and(member.age.between(10, 30)))
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    @Test
    public void searchAndParam(){

        //.and()를 쓰지 않고 쉼표로 구분 가능
        //중간에 null이 들어가도 무시할 수 있기 때문에 동적 쿼리를 짜기에 적합하다
        Member findMember = queryFactory.selectFrom(member) //select(member).from(member)
                .where(member.username.eq("member1")
                        , member.age.between(10, 30))
                .fetchOne();


        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch(){

        List<Member> fetch = queryFactory.selectFrom(member).fetch();

        //Member fetchOne = queryFactory.selectFrom(QMember.member).fetchOne();

        //selectFrom(member).limit(1).fetchOne()
        Member fetchFirst = queryFactory.selectFrom(QMember.member).fetchFirst();

        //deprecated : total count 를 가져와야 해서 쿼리가 두번 실행된다
        //fetch를 사용할 것을 권장한다
        //QueryResults<Member> results = queryFactory.selectFrom(member).fetchResults();
        //results.getTotal();
        //List<Member> content = results.getResults();


        //deprecated
        //long total = queryFactory.selectFrom(member)
                //.fetchCount();


    }


    /*
    * 회원 정렬 순서
    * 1. 회원 나이 내림차순(desc)
    * 2. 회원 이름 올림차순(asc)
    * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls, last)
    * */
    @Test
    public void sort(){

        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();



    }

    @Test
    public void paging1(){
        List<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //1부터
                .limit(2) //2개씩 끊어서 들고옴
                .fetch();

        assertThat(result.size()).isEqualTo(2);


    }

    @Test
    public void paging2(){
        QueryResults<Member> result = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) //1부터
                .limit(2) //2개씩 끊어서 들고옴
                .fetchResults();

        assertThat(result.getTotal()).isEqualTo(4);
        assertThat(result.getLimit()).isEqualTo(2);
        assertThat(result.getOffset()).isEqualTo(1);
        assertThat(result.getResults().size()).isEqualTo(2);


    }

    @Test
    public void aggregation(){
        //querydsl Tuple로 나온다
        //단일 타입이 아닐 때는 tuple로 쓴다
        //근데 DTO로 쓰는 경우가 더 많다
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);


    }


    /*
    * 팀의 이름과 각 팀의 평균 연령을 구해라
    *
    * */
    @Test
    public void group() throws Exception{


        List<Tuple> result = queryFactory.select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team) //member에 있는 team과 team을 join 해준다
                .groupBy(team.name) //team 이름으로 grouping
                .having(team.name.eq("teamA"))
                .fetch();

        Tuple teamA = result.get(0);
        //Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        //assertThat(teamB.get(team.name)).isEqualTo("teamB");
        //assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }


    /*
    * 팀 A에 소속된 모든 회원
    * */
    @Test
    public void join(){
        List<Member> result = queryFactory.selectFrom(member)
                .join(member.team, team) //Qmember.member.team QTeam.team
                //.innerJoin(member.team, team)
                //.leftJoin(member.team, team)//.on() join 대상 제약도 가능하다
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result).extracting("username")
                .containsExactly("member1", "member2");

    }


    /*
        세타 조인(연관관계 없어도 조인 가능하다)
        회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        //모든 회원과 모든 팀을 가져와서 조인을 하고 where로 필터링
        List<Member> result = queryFactory.select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");


    }


    /*
    *  예) 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    *
    * JPQL : select m, t from Member m left join m.team t on t.name = 'teamA'
    * */
    @Test
    public void join_on_filtering(){

        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                .join(member.team, team).where(team.name.eq("teamA")) //.Join(member.team, team).on(team.name.eq("teamA"))
                //.leftJoin(member.team, team).on(team.name.eq("teamA")) //left join이기 때문에 member 기준으로 다 들고온다
                .fetch();

        for (Tuple tuple:result){
            System.out.println("tuple ::: " + tuple);
        }

    }

    /*
        연관관계 없는 엔티티 외부 조인
        회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        //모든 회원과 모든 팀을 가져와서 조인을 하고 where로 필터링
        List<Tuple> result = queryFactory.select(member, team)
                .from(member)
                //.leftJoin(team).on(member.username.eq(team.name))
                .join(team).on(member.username.eq(team.name))
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple ::: " + tuple);
        }
    }


    //test 증명을 하기 위해 사용
    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo(){

        //fetch join 할 때는 초기화 하고 시작하는 것이 좋음
        em.flush();
        em.clear();

        //기본적으로는 team이 Lazy로 되어있다
        Member findMember = queryFactory.selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); //초기화 된 엔티티 여부
        assertThat(loaded).as("페치 조인 미적용").isFalse();


    }


    @Test
    public void fetchJoinUse(){

        //fetch join 할 때는 초기화 하고 시작하는 것이 좋음
        em.flush();
        em.clear();

        //기본적으로는 team이 Lazy로 되어있다
        Member findMember = queryFactory.selectFrom(QMember.member)
                .join(member.team, team).fetchJoin()
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); //초기화 된 엔티티 여부
        assertThat(loaded).as("페치 조인 적용").isTrue();


    }


    /**
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery(){

        //subquery의 alias는 밖의 alias와 겹치면 안된다
        //따라서 다른 alias를 가진 Q entity를 만들어준다
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.eq(select(memberSub.age.max())
                        .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryGoe(){

        //subquery의 alias는 밖의 alias와 겹치면 안된다
        //따라서 다른 alias를 가진 Q entity를 만들어준다
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.goe(select(memberSub.age.avg())
                        .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    @Test
    public void subQueryIn(){

        //subquery의 alias는 밖의 alias와 겹치면 안된다
        //따라서 다른 alias를 가진 Q entity를 만들어준다
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory.selectFrom(member)
                .where(member.age.in(select(memberSub.age)
                        .from(memberSub).where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery(){


        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory.select(member.username,
                select(memberSub.age.avg()).from(memberSub))
                .from(member)
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple ::: " + tuple);
        }

    }

    //JPA JPQL, querydsl은 from절에서 subquery(인라인뷰) 지원하지 않는다
    /*
     * from 절 서브쿼리 해결방안
    * 1. 서브 쿼리 -> join(가능한 상황이 있고 불가능한 상황이 있다)
    * 2. 어플리케이션에서 쿼리를 2번 분리해서 실행한다
    * 3. nativaSQL을 사용한다
    *
    *
    * 정말 복잡한 쿼리는 쪼개서 하면 더 나을 수 있다
    * SQL은 정말 최소한의 데이터를 호출/grouping 하는 것에 집중해서 쓰면 된다
    * */



    // case, 근데 이런건 application에서 하는 것이 좋을 것 같다
    @Test
    public void basicCase(){
        List<String> result = queryFactory.select(member.age
                .when(10).then("열살")
                .when(20).then("스무살")
                .otherwise("기타"))
                .from(member)
                .fetch();

        for(String s : result){
            System.out.println("s ::: " + s);
        }
    }


    @Test
    public void complaxCase(){
        List<String> result = queryFactory.select(new CaseBuilder()
                .when(member.age.between(0, 20)).then("20살")
                .when(member.age.between(21, 30)).then("21 ~ 30살")
                .otherwise("기타"))
                .from(member).fetch();

        for(String s : result){
            System.out.println("s ::: " + s);
        }

    }

    //상수 문자 더하기
    @Test
    public void constant(){
        List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for(Tuple tuple : result){
            System.out.println("tuple ::: " + tuple);
        }


    }

    @Test
    public void concat(){

        //{username}_{age}

        //age는 숫자이기 때문에 .stringValue()를 붙여 string type으로 변환함(특히 enum type일 때 많이 사용)
        List<String> result = queryFactory.select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for(String s : result){
            System.out.println("s ::: " + s);
        }

    }



}
