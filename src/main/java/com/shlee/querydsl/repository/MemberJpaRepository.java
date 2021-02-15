package com.shlee.querydsl.repository;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shlee.querydsl.dto.MemberSearchCondition;
import com.shlee.querydsl.dto.MemberTeamDTO;
import com.shlee.querydsl.dto.QMemberTeamDTO;
import com.shlee.querydsl.entity.Member;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import static com.shlee.querydsl.entity.QMember.member;
import static com.shlee.querydsl.entity.QTeam.team;
import static org.springframework.util.StringUtils.hasText;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;


    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member findMember = em.find(Member.class, id);
        return Optional.ofNullable(findMember);
    }
    // 순수 jpa
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }
    // query dsl 변경
    public List<Member> findAll_Querydsl() {
        return queryFactory
                .selectFrom(member).fetch();
    }
    // 순서 jpa
    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username =:username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }
    // querydsl 변경
    public List<Member> findByUsername_Querydsl(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    // 검색 하는 조건
    public List<MemberTeamDTO> searchByBuilder(MemberSearchCondition condtition){

        BooleanBuilder builder = new BooleanBuilder();
        if(hasText(condtition.getUsername())){
            builder.and(member.username.eq(condtition.getUsername()));
        }
        if(hasText(condtition.getTeamName())){
            builder.and(team.name.eq(condtition.getTeamName()));
        }
        if(condtition.getAgeGoe() != null){
            builder.and(member.age.goe(condtition.getAgeGoe()));
        }
        if(condtition.getAgeLoe() != null){
            builder.and(member.age.loe(condtition.getAgeLoe()));
        }
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }
    
    public List<MemberTeamDTO> search(MemberSearchCondition condition){
        return queryFactory
                .select(new QMemberTeamDTO(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())                        
                )
                .fetch();
    }

    
    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }
    
    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

}