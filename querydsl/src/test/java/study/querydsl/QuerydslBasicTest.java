package study.querydsl;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
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
    member.age.between(10,30);              // between 10, 30
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


}
