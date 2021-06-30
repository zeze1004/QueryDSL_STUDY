package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

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

    // jpql 사용해서 member1을 찾기
    @Test
    public void startJPQL() {
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");

        // JPQL이 제공하는 모든 검색 조건 제공
        member.username.eq("member1");          // username = 'member1'
        member.username.ne("member1");          // username != 'member1'
        member.username.eq("member1").not();    // username != 'member1'
        member.username.isNotNull();            // 이름이 is not null
        member.age.in(10, 20);                  // age in (10,20)
        member.age.notIn(10, 20);               // age not in (10, 20)
        member.age.between(10, 30);              // between 10, 30
        member.age.goe(30);                     // age >= 30
        member.age.gt(30);                      // age > 30
        member.age.loe(30);                     // age <= 30
        member.age.lt(30);                      // age < 30
        member.username.like("member%");        // like 검색
        member.username.contains("member");     // like ‘%member%’ 검색
        member.username.startsWith("member");   // like ‘member%’ 검색

    }

    // JPAQueryFactory를 필드에 넣지 않을 경우
    // dsl 사용해서 member1을 찾기
    @Test
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");       // 생성되는 JPQL의 별칭이 m
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // JPAQueryFactory를 필드에 넣는 경우
    @Test
    public void startQuerydsl2() {
        // JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember m = new QMember("m");       // 생성되는 JPQL의 별칭이 m
        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // 별칭(m)을 이용하지 않고 기본 인스턴스 사용
    // 같은 테이블을 조인해야 하는 경우 아닐시 기본 인스턴스 사용하자
    @Test
    public void startQuerydsl3() {
        // QMember m = new QMember("m");   // 생성되는 JPQL의 별칭이 m
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1")) // 파라미터 바인딩 처리
                .fetchOne();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        System.out.println("********test success*********");
    }

    // 기본 검색 쿼리를 알아보자
    @Test
    public void search() {
        System.out.println("기본 검색 쿼리 테스트*********");

        Member findMember = queryFactory
                .selectFrom(member)                     // select + from
                .where(member.username.eq("member1")
                        // .and(), .or()로 검색조건 연결할 수 있음
                        .and(member.age.eq(10)))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    // AND 조건을 파라미터로 처리
    @Test
    public void searchAndRaram() {
        System.out.println("AND 조건을 파라미터로 처리*********");
        List<Member> result1 = queryFactory
                .selectFrom(member)
                // where(조건, 조건, ...)
                .where(member.username.eq("member1"),
                        member.age.eq(10))
                // 리스트 조회, fetchOne(): 단 건 조회
                .fetch();

        assertThat(result1.size()).isEqualTo(1);
    }

    // 결과 조회
    @Test
    public void resultFetch() {
        // List: fetch()는 List 반환, 데이터 없으면 빈 리스트 반환
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        // 단 건, 결과가 없으면 null, 둘 이상이면 에러
        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        // 처음 한 건 조회 = limit(1).fetchOne()
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        // 페이징 정보 포함해 페이징에서 사용
        // total count 쿼리 추가 실행
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        // 쿼리 추가 실행
        results.getTotal();


        // count 쿼리로 변경해서 count 수 조회
        long total = queryFactory
                .selectFrom(member)
                .fetchCount();

    }

    // 정렬
    /* 회원 정렬 순서
        1. 회원 나이 내림차순(desc())
        2. 회원 이름 올림차순(asc())
        3. 단 2에서 회원 이름이 없으면 마지막에 출력(nullsLast())
    */
    @Test
    public void sort() {
        // 예제를 위해 데이터 추가
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast()) // nullsFirst(): null값을 처음에 정렬할 때 사용
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
    }

    // 페이징
    // 조회 건수 제한
    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)  // 0부터 시작(zero index)
                .limit(2)   // 최대 2건 조회
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }

    // 전체 조회 수
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();
        // QueryResults는 count 쿼리 실행해서 쿼리문이 한 번 더 실행되어 성능 떨어짐
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    // 집합 함수(1)
    // JPQL이 제공하는 집합 함수를 DSL도 제공
    // 실무에서는 Tuple보다는 DTO를 직접 만들어서 사용
    @Test
    public void aggregation() throws Exception {
        List<Tuple> result = queryFactory
                .select(member.count(),         // 멤버 수
                        member.age.sum(),       // 나이 합
                        member.age.avg(),       // 나이 평균
                        member.age.max(),       // 최대 나이
                        member.age.min())       // 최소 나이
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);

        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    // 집합 함수(2)
    // GroupBy 사용
    // 팀의 이름과 각 팀의 평균 연령을 구해라
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

    // 조인
    // join(조인 대상, Q타입의 별칭)

    @Test
    public void join() throws Exception {
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        // 팀 A에 소속한 모든 멤버 조회
        List<Member> result = queryFactory
                .selectFrom(member)
                // (inner)join
                .join(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    // 연관관계가 없어도 조인 가능
    // 세타 조인(맞 조인)
    // 세타 조인은 left, right 조인 같은 외부 조인 불가능(inner join만 가능)
    // 그러나 조인 on을 사용하면 외부 조인 가능
    // 예: 회원의 이름이 팀 이름과 같은 회원 조회
    @Test
    public void theta_join() throws Exception {
        // 예시를 위해 teamA, teamB 이름의 멤버 추가
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        List<Member> result = queryFactory
                .select(member)
                // from(... , ...) 나열해서 두 저장소 맞조인
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    // 조인 - on절
    // on 절을 활용한 조인
    // 1. 조인 대상 필터링(내부 조인)
    // 2. 연관관계 없는 엔티티 외부 조인(주로 쓰임)

    // 1. 조인 대상 필터링(내부 조인)
    // 예시: 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
    // JPQL 문법: select m, t from Member m left join m.team t on t.name = 'teamA // member와 team을 조인하는데, team.name이 teamA인 team만 조인
    @Test
    public void join_on_filtering() throws Exception {
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                // inner join일 경우 on 대신 where 써도 결과 같음
                // 외부조인은 무족권 on 사용
                .join(member.team, team).on(team.name.eq("teamA"))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    // 2. 연관관계 없는 엔티티 외부 조인
    // 회원의 이름이 팀 이름과 같은 대상 외부 조인
    @Test
    public void join_on_no_relation() throws Exception {
        // 예시를 위해 teamA, teamB 이름의 멤버 추가
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member) // 외부 조인이니깐 from 안에 하나
                .leftJoin(team).on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
}
