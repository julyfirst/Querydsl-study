package com.shlee.querydsl.repository;

import com.shlee.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    //select m from Member m whrere m.username =?
    List<Member> findByUsername(String userName);


}
