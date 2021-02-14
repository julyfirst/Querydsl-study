package com.shlee.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shlee.querydsl.dto.MemberDTO;
import com.shlee.querydsl.dto.QMemberDTO;
import com.shlee.querydsl.dto.UserDTO;
import com.shlee.querydsl.entity.Member;
import com.shlee.querydsl.entity.QMember;
import com.shlee.querydsl.entity.QTeam;
import com.shlee.querydsl.entity.Team;
;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static com.shlee.querydsl.entity.QMember.*;
import static com.shlee.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.assertThat;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    // JPAQueryFactory를 만들 떄 entitymanager를 생성자로  넘겨줘야 이걸 이용해 데이터를 찾음
    JPAQueryFactory queryFactory;


    // 테스트 하기전에 데이터를 넣기위한
    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl() {
        queryFactory = new JPAQueryFactory(em);
        // 구분할려고하는 별칭 m 예제파일이라 일단 씀
        //QMember m = new QMember("m");
        QMember member = QMember.member;

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                                .and(member.age.eq(10))
                )
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(
                        // and 생략
                        member.username.eq("member1"),
                        member.age.eq(10)
                )
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
//        // 멤버를 리스트로 조회한다
////        List<Member> fetch = queryFactory
////                .selectFrom(member)
////                .fetch();
////
////        // 단건조회 결과값이 하나만
////        Member fetchOne = queryFactory
////                .selectFrom(QMember.member)
////                .fetchOne();
////
////        Member fetchFirst = queryFactory
////                .selectFrom(QMember.member)
////                .fetchFirst();

//        //
//        QueryResults<Member> results = queryFactory
//                .selectFrom(member)
//                .fetchResults();
//
//        // 페이징 처리를 위한 전체
//        results.getTotal();
//        List<Member> content = results.getResults();

        //select절 쿼리를 count로 바꿈
        long total = queryFactory
                .selectFrom(QMember.member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();

        assertThat(result.size()).isEqualTo(2);

    }

    // 전체조회수가 필요하면
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();


        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);

    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.max()
                )
                .from(member)
                .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     *
     * @throws Exception
     */
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name)
                .fetch();


        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15); // (10 + 20) / 2

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35); // (30 + 40) / 2
    }

    /**
     * 팀 A에 소속된 모든 회원
     */
    @Test
    public void join() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타조인
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL :
     * select m , t
     * from Member m
     * left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        result.forEach(tuple ->
                System.out.println("tuple = " + tuple)
        );
    }

    /**
     * 연관관계 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인
     */
    @Test
    public void join_on_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();


        result.forEach(tuple ->
                System.out.println("tuple = " + tuple)
        );

    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() {
        // 영속성 컨텍스트를 db반영하고 clear한 후 시작할 예정
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isFalse();


    }

    /**
     * Query dsl 패치 조인
     */
    @Test
    public void fetchJoinUse() {
        // 영속성 컨텍스트를 db반영하고 clear한 후 시작할 예정
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치 조인 미적용").isTrue();

    }

    /**
     * 나이가 가장 많은 회원 조회
     * 서브쿼리 사용 (eq 사용)
     */
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");


        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub) // 결과는 40
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * 나이가 평균 이상인 회원
     * 서브쿼리 사용 (Goe 사용)
     */
    @Test
    public void subQueryGoe() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub) // 결과는 40
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    @Test
    public void subQueryIn() {

        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        JPAExpressions
                                .select(memberSub.age)
                                .from(memberSub) // 결과는 40
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20, 30, 40);
    }

    @Test
    public void selectsubQuery() {

        QMember memberSub = new QMember("memberSub");

        List<Tuple> result = queryFactory
                .select(member.username,
                        JPAExpressions
                                .select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();

        result.forEach(tuple -> {
            System.out.println("tuple = " + tuple);
        });

    }

    /**
     * case문은 잘 사용하지않고 좋지 않은 방법
     */
    @Test
    void basicCase() {
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }

    }

    @Test
    public void complexCase() {
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                )
                .from(member)
                .fetch();

        result.forEach(r -> System.out.println("result = " + r));
    }

    @Test
    public void constant() {
        List<Tuple> result = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    /**
     * cancat을 이용한 문자 더하기
     */
    @Test
    public void concat() {

        // {username}_{age}
        List<String> result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)
                .where(member.username.eq("member1"))
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);

        }


    }

    /**
     * 중급문법 대상 결과 값이 하나 일 때
     */
    @Test
    public void simpleProjection() {

        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        result.forEach(projection -> {
            System.out.println("projection = " + projection);
        });
    }


    /**
     * 중급문법 tuple 대상 결과 값이 하나 이상일 떄
     */
    @Test
    public void tupleProjection() {

        List<Tuple> tupleResult = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        tupleResult.forEach(
                result -> {
                    String username = result.get(member.username);
                    Integer age = result.get(member.age);
                    System.out.println("username = " + username);
                    System.out.println("age = " + age);
                }
        );
    }

    /**
     * 효율성 x 패키지 이름 까지 사용
     * 생성자로 주입
     */
    @Test
    public void findDTOByJPQL(){
        List<MemberDTO> resultList = em.createQuery("select new com.shlee.querydsl.dto.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
                .getResultList();

        for (MemberDTO memberDTO : resultList) {
            System.out.println("memberDTO = " + memberDTO);
        }
    }


    /**
     * setter를 활용한 방법
     */
    @Test
    public void findDTOBySetter() {
        List<MemberDTO> fetch = queryFactory
                .select(Projections.bean(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        fetch.forEach(result->{
            System.out.println("result = " + result);
        });
    }

    /**
     * getter, setter 무시하고 값이 바로 필드에 꼳힘
     * 라이브러리이용
     */

    @Test
    public void findDTOByField() {
        List<MemberDTO> fetch = queryFactory
                .select(Projections.fields(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        fetch.forEach(result->{
            System.out.println("result = " + result);
        });
    }


    /**
     * 생성자 접근방법
     */
    @Test
    public void findDTOByConstructor() {
        List<MemberDTO> fetch = queryFactory
                .select(Projections.constructor(MemberDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        fetch.forEach(result->{
            System.out.println("result = " + result);
        });
    }

    /**
     * 값이 넣어야할 이름이 다를경우에는 as를 사용
     * 서브쿼리일떄는 이걸로 감쌓아야 함
     * setter를 이용해서 할 경우엔 ExpressionUtils를 사용할 것
     */
    @Test
    public void findUserDTO() {
        QMember memberSub = new QMember("memberSub");

        List<UserDTO> fetch = queryFactory
                .select(Projections.fields(UserDTO.class,
                        QMember.member.username.as("name"),
                        ExpressionUtils.as(JPAExpressions
                        .select(memberSub.age.max())
                                        .from(memberSub), "age")
                ))
                .from(QMember.member)
                .fetch();

        fetch.forEach(result->{
            System.out.println("result = " + result);
        });
    }

    /**
     * 생성자 접근방법 (user)
     * 오류는 컴파일시에만
     */
    @Test
    public void findDTOByConstructorUser() {
        List<UserDTO> fetch = queryFactory
                .select(Projections.constructor(UserDTO.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        fetch.forEach(result->{
            System.out.println("result = " + result);
        });
    }

    /**
     * q타입 클래스 실행시 오류 잡아 낼 수 있음 인자 값이 다른 경우엔
     * 컴파일 하기 전에 잡을 수 있음
     * DTO가 Query dsl 의존관계가 있어서 음
     */
    @Test
    public void findDTOByQueryProjection() {
        List<MemberDTO> result = queryFactory
                .select(new QMemberDTO(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDTO memberDTO : result) {
            System.out.println("memberDTO = " + memberDTO);

        }
    }

    /**
     * 동적 쿼리 - BooleanBuilder 사용
     */
    @Test
    public void dynamicQuery_BooleanBuilder() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();
        if(usernameCond != null){
            builder.and(member.username.eq(usernameCond));
        }
        if(ageCond != null){
            builder.and(member.age.eq(ageCond));
        }


        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();

    }

    @Test
    public void dynamicQuery_WhereParam() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    /**
     * 조금더 직관적으로 조건을 볼 수 있어 BooleanBuilder보다 깔끔
     * @param usernameCond
     * @param ageCond
     * @return
     */
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {

        return queryFactory
                .selectFrom(member)
//                .where(usernameEq(usernameCond), ageEq(ageCond))
                .where(allEq(usernameCond, ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        if(usernameCond == null) {
            return null; // null 을 반환하면 where(null, ageEq(ageCond)) 가됨
            // null이 되면 무시가 됨 -> 아무 역활을 하지 않게 됨 그래서 동적쿼리를 만들게 되는거임
        }
        return member.username.eq(usernameCond);

    }

    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private Predicate allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    // 벌크 연산 -> 한번 에처리
    @Test
    @Commit
    public void bulkUpdate() {
        // member1 = 10 -> 비회원
        // member2 = 20 -> 비회원
        // member3 = 30 -> member3
        // member4 = 40 -> member4

        long count = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        em.flush();
        em.clear();
        // member1 = 10 -> 비회원
        // member2 = 20 -> 비회원
        // member3 = 30 -> member3
        // member4 = 40 -> member4

        List<Member> result = queryFactory
                .selectFrom(member)
                .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }
    }

    /**
     * bulk 더하기
     */
    @Test
    public void bulkAdd() {
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();

    }

    /**
     * 18살이상 모든 회원 삭제
     */
    @Test
    public void bulkDelete() {
        queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }

    @Test
    public void sqlFunction() {
        List<String> result = queryFactory
                .select(Expressions.stringTemplate(
                        "function('regexp_replace', {0}, {1}, {2})",
                        member.username, "member", "M"))
                .from(member)
                .fetch();

        result.forEach(s ->
                        System.out.println("resultSQLFunction = " + s));
    }

    @Test
    public void sqlFunction2() {
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(
//                        Expressions.stringTemplate("function('lower',{0})", member.username)))
                .where(member.username.eq(member.username.lower())) // ANSI 표준
                .fetch();
        fetch.forEach(result ->
                        System.out.println("result = " + result)
        );
    }
}

