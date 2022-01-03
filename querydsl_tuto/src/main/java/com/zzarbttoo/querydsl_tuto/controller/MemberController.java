package com.zzarbttoo.querydsl_tuto.controller;

import com.zzarbttoo.querydsl_tuto.dto.MemberSearchCondition;
import com.zzarbttoo.querydsl_tuto.dto.MemberTeamDTO;
import com.zzarbttoo.querydsl_tuto.repository.MemberJpaRepository;
import com.zzarbttoo.querydsl_tuto.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    //http://localhost:8080/v1/members?teamName=teamB&ageGoe=31&ageLoe=35&username=member31
    @GetMapping("/v1/members")
    public List<MemberTeamDTO> searchMemberV1(MemberSearchCondition condition){
        return memberJpaRepository.search(condition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDTO> searchMemberV3(MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageComplex(condition, pageable);
    }

}
