package controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import view.TeamView;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;

public class TeamController extends BaseController {

	@Override
	public void init(String query) {
		// TODO Auto-generated method stub
		System.out.println("building dynamic html for team");
		view = new TeamView();
		process(query);
	}
	
	@Override
	protected void performAction() {
		// TODO Auto-generated method stub

	}

	

}
