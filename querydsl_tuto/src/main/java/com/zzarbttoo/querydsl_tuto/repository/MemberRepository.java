package com.zzarbttoo.querydsl_tuto.repository;

import com.zzarbttoo.querydsl_tuto.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom
, QuerydslPredicateExecutor<Member> { //sprinb data JPA + querydsl

    //select m from Member m where m.username = ?
    List<Member> findByUsername(String username);
}
