package com.zzarbttoo.querydsl_tuto.repository;

import com.zzarbttoo.querydsl_tuto.dto.MemberSearchCondition;
import com.zzarbttoo.querydsl_tuto.dto.MemberTeamDTO;
import com.zzarbttoo.querydsl_tuto.entity.Member;
import com.zzarbttoo.querydsl_tuto.entity.QMember;
import com.zzarbttoo.querydsl_tuto.entity.Team;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired MemberRepository memberRepository;

    @Test
    public void basicTest(){
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberRepository.findAll();

        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberRepository.findByUsername("member1");
        assertThat(result2).containsExactly(member);
    }



    @Test
    public void searchTest(){

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

        MemberSearchCondition condition = new MemberSearchCondition();
        //paging query + 조건은 대용량 쿼리에서 필요하다(장애나기 딱좋다~)
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");

        //List<MemberTeamDTO> result = memberJpaRepository.searchByBuilder(condition);
        List<MemberTeamDTO> result = memberRepository.search(condition);

        assertThat(result).extracting("username").containsExactly("member4");


    }

    @Test
    public void searchPageSimple(){

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

        MemberSearchCondition condition = new MemberSearchCondition();
        PageRequest pageRequest = PageRequest.of(0, 3);

        Page<MemberTeamDTO> result = memberRepository.searchPageSimple(condition, pageRequest);

        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).
                extracting("username").containsExactly("member1", "member2", "member3");


    }

    @Test
    public void querydslPredicateExecutoerTest(){
        QMember member = QMember.member;
        Iterable<Member> result = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));
        for (Member findMember : result) {
            System.out.println("member:::" + findMember);
        }

        //근데 left join을 못한다
        //클라이언트가 querydsl에 의존해야한다(querydsl 객체를 service에서 넘겨야 한다)
        //테이블 하나에서 쓰는거면 모르지만 실무에서는 사용 X


    }

    //QuerydslWeb은 사용하지 않는 편이 좋다(복잡, join 안됨, 아키텍쳐 지져분)



}