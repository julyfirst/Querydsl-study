package com.shlee.querydsl.repository;

import com.shlee.querydsl.dto.MemberSearchCondition;
import com.shlee.querydsl.dto.MemberTeamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberRepositoryCustom {

    List<MemberTeamDTO> search(MemberSearchCondition condition);
    // Pageable은 기본 offset이나 정보를 알 수 있다.
    Page<MemberTeamDTO> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
    Page<MemberTeamDTO> searchPageComplex(MemberSearchCondition condition, Pageable pageable);

}
