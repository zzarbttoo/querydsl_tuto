package com.zzarbttoo.querydsl_tuto;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zzarbttoo.querydsl_tuto.entity.Member;
import com.zzarbttoo.querydsl_tuto.entity.QMember;
import com.zzarbttoo.querydsl_tuto.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.List;

import static com.zzarbttoo.querydsl_tuto.entity.QMember.member;
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

}
