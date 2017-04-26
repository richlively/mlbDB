package dataaccesslayer;

import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

import bo.Player;
import bo.Team;
import bo.TeamSeason;

public class HibernateUtil {

	private static final SessionFactory sessionFactory;

	static {
		try {
			Configuration cfg = new Configuration()
				.addAnnotatedClass(bo.Player.class)
				.addAnnotatedClass(bo.PlayerSeason.class)
				.addAnnotatedClass(bo.BattingStats.class)
				.addAnnotatedClass(bo.CatchingStats.class)
				.addAnnotatedClass(bo.FieldingStats.class)
				.addAnnotatedClass(bo.PitchingStats.class)
				.configure();
			StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
			applySettings(cfg.getProperties());
			sessionFactory = cfg.buildSessionFactory(builder.build());
		} catch (Throwable ex) {
			System.err.println("Initial SessionFactory creation failed." + ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	
	public static Player retrievePlayerById(Integer id) {
        Player p=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			query = session.createQuery("from bo.Player where id = :id ");
		    query.setParameter("id", id);
		    if (query.list().size()>0) {
		    	p = (Player) query.list().get(0);
		    	Hibernate.initialize(p.getSeasons());
		    }
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return p;
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<Player> retrievePlayersByName(String nameQuery, Boolean exactMatch) {
        List<Player> list=null;
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			org.hibernate.Query query;
			if (exactMatch) {
				query = session.createQuery("from bo.Player where name = :name ");
			} else {
				query = session.createQuery("from bo.Player where name like '%' + :name + '%' ");
			}
		    query.setParameter("name", nameQuery);
		    list = query.list();
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
		} finally {
			if (session.isOpen()) session.close();
		}
		return list;
	}

	public static Team retrieveTeamById(Integer id) {
            Team t = null;
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.getTransaction();
            try {
                    tx.begin();
                    org.hibernate.Query query;
                    query = session.createQuery("from bo.Team where id = :id ");
                query.setParameter("id", id);
                if (query.list().size()>0) {
                    t = (Team) query.list().get(0);
                    Hibernate.initialize(t.getSeasons());
                }
                    tx.commit();
            } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
            } finally {
                    if (session.isOpen()) session.close();
            }
            return t;
	}
	
	public static TeamSeason retrieveTeamSeasonById(Integer tid, Integer yid) {
        TeamSeason ts = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.getTransaction();
        try {
                tx.begin();
                org.hibernate.Query query;
                query = session.createQuery("from bo.TeamSeason where teamId = :tid and year = :yid ");
            query.setParameter("tid", tid);
            query.setParameter("yid", yid);
            if (query.list().size()>0) {
                ts = (TeamSeason) query.list().get(0);
                Hibernate.initialize(ts.getPlayers());
            }
                tx.commit();
        } catch (Exception e) {
                tx.rollback();
                e.printStackTrace();
        } finally {
                if (session.isOpen()) session.close();
        }
        return ts;
}
	
	
	@SuppressWarnings("unchecked")
	public static List<Team> retrieveTeamsByName(String nameQuery, Boolean exactMatch) {
            List<Team> list=null;
            Session session = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = session.getTransaction();
            try {
                    tx.begin();
                    org.hibernate.Query query;
                    if (exactMatch) {
                            query = session.createQuery("from bo.Team where name = :name ");
                    } else {
                            query = session.createQuery("from bo.Team where name like '%' + :name + '%' ");
                    }
                query.setParameter("name", nameQuery);
                list = query.list();
                    tx.commit();
            } catch (Exception e) {
                    tx.rollback();
                    e.printStackTrace();
            } finally {
                    if (session.isOpen()) session.close();
            }
            return list;
	}
	
	public static boolean persistPlayer(Player p) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			session.save(p);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			return false;
		} finally {
			if (session.isOpen()) session.close();
		}
		return true;
	}
	
	public static boolean persistTeam(Team t) {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();
		try {
			tx.begin();
			session.save(t);
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			e.printStackTrace();
			return false;
		} finally {
			if (session.isOpen()) session.close();
		}
		return true;
	}
		
}