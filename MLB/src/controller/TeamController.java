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
		System.out.println("building dynamic html for team");
		view = new TeamView();
		process(query);
	}
	
	@Override
	protected void performAction() {
		String action = keyVals.get("action");
		System.out.println("teamcontroller performing action: " + action);
		if (action.equalsIgnoreCase(ACT_SEARCHFORM)) {
			processSearchForm();
		} else if (action.equalsIgnoreCase(ACT_SEARCH)) {
			processSearch();
		} else if (action.equalsIgnoreCase(ACT_DETAIL)) {
			processDetails();
		} else if (action.equalsIgnoreCase(ACT_ROSTER)) {
			processRoster();
		}
	}
	protected void processSearchForm() {
		view.buildSearchForm();
	}

	protected final void processSearch() {
		// TODO Auto-generated method stub
		
	}
	
	private void processDetails() {
		// TODO Auto-generated method stub
		
	}
	
	private void processRoster() {
		// TODO Auto-generated method stub
		
	}

}
