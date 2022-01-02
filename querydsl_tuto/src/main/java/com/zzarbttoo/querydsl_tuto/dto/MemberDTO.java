package com.zzarbttoo.querydsl_tuto.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data //기본 생성자는 안만들어준다
@NoArgsConstructor
public class MemberDTO {

    private String username;
    private int age;

    @QueryProjection //QueryProjection annotation 생성 후 querydsl compile 해줘야 한다
    public MemberDTO(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
