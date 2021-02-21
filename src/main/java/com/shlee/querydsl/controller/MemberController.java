package com.shlee.querydsl.controller;

import com.shlee.querydsl.dto.MemberSearchCondition;
import com.shlee.querydsl.dto.MemberTeamDTO;
import com.shlee.querydsl.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;




    @GetMapping("/v2/members")
    public Page<MemberTeamDTO> searchMemberV2(MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageSimple(condition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDTO> searchMemberV3(MemberSearchCondition condition, Pageable pageable){
        return memberRepository.searchPageComplex(condition, pageable);
    }

}
