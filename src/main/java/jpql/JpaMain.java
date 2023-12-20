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

            /*
            * Team객체에서 batch size를 100으로 했기 때문에
            * 기존 Team에서 members를 가지고 올때 lazy로딩 상태 인데 -> 해당 lazy상태에서 team뿐만 아니라 result 리스트에 담긴 team도 한번에 100개식 넘긴다
            * -> query의 team을 조회하는 것 + 조회된 team과 연관된 member의 lazy 로딩 쿼리 (n + 1 문제를 해결하는 방법은 해당 방법을 사용하고나 fetch join을 사용)
            * */
           String query = "select  t from  Team t"; //Member를 조회하는데 team과 조인해서 한번에 가지고 오라는 쿼리


           List<Team> result = em.createQuery(query, Team.class)
                   .setFirstResult(0)
                   .setMaxResults(2)
                   .getResultList();

           /* 설명
           * */
           for(Team team : result){
               System.out.println("team : " + team.getName() + "," + team.getMembers().size());
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
