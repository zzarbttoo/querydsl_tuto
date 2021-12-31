package com.zzarbttoo.querydsl_tuto.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) //연관관계 필드는 적으면 안된다(무한루프)
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY) //연관관계의 주인
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username, int age, Team team){
        this.username = username;
        this.age = age;
        if(team != null){
            changeTeam(team);
        }
    }

    public Member(String username, int age){
        this(username, age, null);
    }

    public Member(String username){
        this(username, 0 , null);
    }




    private void changeTeam(Team team) {
       this.team = team;
       team.getMembers().add(this);
    }

}
