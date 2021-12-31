package com.zzarbttoo.querydsl_tuto.entity;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Team {

    @Id @GeneratedValue
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team") //연관관계 주인이 아니라 update 하지 않는다
    private List<Member> members = new ArrayList<>();

    public Team(String name){
        this.name = name;
    }

}
