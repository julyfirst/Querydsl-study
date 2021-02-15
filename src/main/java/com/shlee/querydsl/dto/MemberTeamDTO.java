package com.shlee.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;

@Data
public class MemberTeamDTO {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;

    // Q 타입 클래스 생성
    // DTO가 querydsl 라이브러리에 의존하게 되는 단점이 있음 순수하지 않음
    @QueryProjection
    public MemberTeamDTO(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }



}
