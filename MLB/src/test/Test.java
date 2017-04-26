package test;

import java.util.Iterator;
import java.util.List;

import bo.Team;
import dataaccesslayer.HibernateUtil;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Integer id = 13385;
		Team t = HibernateUtil.retrieveTeamById(id);
		System.out.println("test byId " + t.getName());
		List<Team> teams = HibernateUtil.retrieveTeamsByName("Reds", false);
		System.out.println("test byName");
		for (Iterator iterator = teams.iterator(); iterator.hasNext();) {
			Team team = (Team) iterator.next();
			System.out.println(team.getName());
		}
		System.out.println("Hooray");
	}

}
