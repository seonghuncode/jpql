package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        //애플리케이션 로딩 시점에 한개만 만든다.(EntityManagerFactory : DB당 하나만 생성 / 애플리케이션이 실행될때 실행)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        //일괄적인 트랜잭션 단위마다 만든다. (EntityManager : 요청이 올때마다 사용 삭재를 반복 하면서 사용 -> 쓰레드 공간을 공유하면 안된다.(쓰고 버린다는 개념으로 사용))
        //jpa모든 변경은 트랜잭션 안에서 실행해야 한다.
        EntityManager em = emf.createEntityManager();

        //트랜잭션을 얻는 작업
        EntityTransaction tx = em.getTransaction();
        tx.begin(); //트랜잭션 시작


        try {

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            em.persist(member3);

            em.flush();
            em.clear();

           String query = "select m from Member m "; //한줄로 반환을 해준다.


           List<Member> result = em.createQuery(query, Member.class)
                   .getResultList();

           /* 설명
           1. 첫번째 루프 : Team을 DB에서 가지고 온다 -> 영속성 컨텍스트에 Team이 들어간다.
           2. 두번째 루프 : 1차 캐시(영속성 컨텍스트)에서 값을 가지고 온다 (쿼리가 나가지 않는다.)
           3. 세번째 루프 : 영속성 컨텍스트에 없는 데이터가 필요하기 때문에 쿼리를 통해 DB에서 데이터를 가지고온다.
           ..
           ..
           -패치 조인을 사용하는 이유
           -> 만약  회원이 여러명일 경우 DB에 조회하기 위해 나가는 쿼리가 너무 많아진다 -> 해당 문제를 해결하기 위해서 사용하는 것이 패치조인을 사용
           * */
           for(Member m : result){
               System.out.println("member : " + m.getUsername() + "," + m.getTeam().getName());
           }

            tx.commit(); // -> 이때 DB에 쿼라가 날라간다.

        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close(); //동작이 끝나면 항상 닫아준다
        }

        emf.close(); //was가 내려갈대 종료 (리소스가 내부적으로 종료)
    }


}
