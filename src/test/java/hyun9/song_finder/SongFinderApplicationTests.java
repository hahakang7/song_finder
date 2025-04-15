package hyun9.song_finder;

import hyun9.song_finder.domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SongFinderApplicationTests {

	@Test
	void contextLoads() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		try {

			User user = new User();


			tx.commit();

		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();


	}

}
