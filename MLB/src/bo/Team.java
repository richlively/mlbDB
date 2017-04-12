package bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "team")
public class Team {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer teamId;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "id.team")
	@Fetch(FetchMode.JOIN)
	Set<TeamSeason> seasons = new HashSet<TeamSeason>();

	@Column
	String name;
	@Column
	String league;
	@Column
	Date yearFounded;
	@Column
	Date yearLast;

	/**
	 * Gets an individual season
	 * @param year of the season
	 * @return the individual season
	 */
	public TeamSeason getTeamSeason(Integer year) {
		for (TeamSeason ps : seasons) {
			if (ps.getYear().equals(year))
				return ps;
		}
		return null;
	}
	
	/**
	 * Adds a season to the seasons
	 * @param s
	 */
	public void addSeason(TeamSeason s) {
		seasons.add(s);
	}

	/*---------- begin getters and setters ----------*/
	public Set<TeamSeason> getSeasons() {
		return seasons;
	}
	
	public void setSeasons(Set<TeamSeason> seasons) {
		this.seasons = seasons;
	}

	public Integer getId() {
		return teamId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLeague() {
		return league;
	}

	public void setLeague(String league) {
		this.league = league;
	}

	public Date getYearFounded() {
		return yearFounded;
	}

	public void setYearFounded(Date yearFounded) {
		this.yearFounded = yearFounded;
	}

	public Date getYearLast() {
		return yearLast;
	}

	public void setYearLast(Date yearLast) {
		this.yearLast = yearLast;
	}
	
	/*---------- end getters and setters ----------*/

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Team)) {
			return false;
		}
		Team other = (Team) obj;
		return (this.getName().equalsIgnoreCase(other.getName())
				&& this.getLeague().equalsIgnoreCase(other.getLeague())
				&& this.getYearFounded().equals(other.getYearFounded())
				&& this.getYearLast().equals(other.getYearLast()));
	}

	@Override
	public int hashCode() {
		Integer hash = 0;
		if (this.getName() != null)
			hash += this.getName().hashCode();
		if (this.getLeague() != null)
			hash += this.getLeague().hashCode();
		if (this.getYearFounded() != null)
			hash += this.getYearFounded().hashCode();
		if (this.getYearLast() != null)
			hash += this.getYearLast().hashCode();
		return hash;
	}
}
