package bo;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity(name = "player")
public class Player {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer playerId;
	
	@ElementCollection
	@CollectionTable(name = "playerposition", joinColumns = @JoinColumn(name = "playerid"))
	@Column(name = "position")
	@Fetch(FetchMode.JOIN)
	Set<String> positions = new HashSet<String>();
	
	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="id.player")
	@Fetch(FetchMode.JOIN)
	Set<PlayerSeason> seasons = new HashSet<PlayerSeason>();
	
	//make a connection to the teamseasonplayer table
	@ManyToMany(fetch = FetchType.LAZY, mappedBy = "players")
	Set<TeamSeason> teamSeasons = new HashSet<TeamSeason>();
	
	@Column
	String name;
	@Column
	String givenName;
	@Column
	Date birthDay;
	@Column
	Date deathDay;
	@Column
	String battingHand;
	@Column
	String throwingHand;
	@Column
	String birthCity;
	@Column
	String birthState;
	@Column
	Date firstGame;
	@Column
	Date lastGame;

	// utility function
	public PlayerSeason getPlayerSeason(Integer year) {
		for (PlayerSeason ps : seasons) {
			if (ps.getYear().equals(year)) return ps;
		}
		return null;
	}
	
	public void addPosition(String p) {
		positions.add(p);
	}

	public Set<String> getPositions() {
		return positions;
	}

	public void setPositions(Set<String> positions) {
		this.positions = positions;
	}

	public void addSeason(PlayerSeason s) {
		seasons.add(s);
	}

	public Set<PlayerSeason> getSeasons() {
		return seasons;
	}
	
	public void setSeasons(Set<PlayerSeason> seasons) {
		this.seasons = seasons;
	}
	
	public Integer getId() {
		return playerId;
	}
	public void setId(Integer id) {
		this.playerId = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String nickName) {
		this.givenName = nickName;
	}

	public String getBattingHand() {
		return battingHand;
	}

	public void setBattingHand(String battingHand) {
		this.battingHand = battingHand;
	}

	public String getThrowingHand() {
		return throwingHand;
	}

	public void setThrowingHand(String throwingHand) {
		this.throwingHand = throwingHand;
	}

	public String getBirthCity() {
		return birthCity;
	}

	public void setBirthCity(String birthCity) {
		this.birthCity = birthCity;
	}

	public String getBirthState() {
		return birthState;
	}

	public void setBirthState(String birthState) {
		this.birthState = birthState;
	}

	public Date getFirstGame() {
		return firstGame;
	}

	public void setFirstGame(Date firstGame) {
		this.firstGame = firstGame;
	}

	public Date getLastGame() {
		return lastGame;
	}

	public void setLastGame(Date lastGame) {
		this.lastGame = lastGame;
	}

	public Date getBirthDay() {
		return birthDay;
	}

	public void setBirthDay(Date birthDay) {
		this.birthDay = birthDay;
	}

	public Date getDeathDay() {
		return deathDay;
	}

	public void setDeathDay(Date deathDay) {
		this.deathDay = deathDay;
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Player)){
			return false;
		}
		Player other = (Player) obj;
		return (this.getName().equalsIgnoreCase(other.getName()) &&
				this.getBirthDay()==other.getBirthDay() &&
				this.getDeathDay()==other.getDeathDay());
	}
	 
	@Override
	public int hashCode() {
		Integer hash = 0;
		if (this.getName()!=null) hash += this.getName().hashCode(); 
		if (this.getBirthDay()!=null) hash += this.getBirthDay().hashCode();
		if (this.getDeathDay()!=null) hash += this.getDeathDay().hashCode();
		return hash;
	}

	/**
	 * Adds a team season to the player
	 * @param ts
	 */
	public void addTeamSeason(TeamSeason ts) {
		teamSeasons.add(ts);
	}
	
	/**
	 * This returns only the first team season for a given year.
	 * Use getTeamSeasons(Integer year) to return a Set of team seasons,
	 * which is helpful for players were multiple teams within a season.
	 * @param year
	 * @return the first team season for a given year
	 */
	public TeamSeason getTeamSeason(Integer year) {
		for (TeamSeason ts : teamSeasons) {
			if (ts.getYear().equals(year)) return ts;
		}
		return null;
	}
	
	/**
	 * This returns a Set of the team seasons a player participated
	 * in for a given year. If you are only interested in the first
	 * team season for a given year, use getTeamSeason(Integer year)
	 * @param year
	 * @return the set of team seasons for a given year
	 */
	public Set<TeamSeason> getTeamSeasons(Integer year) {
		Set<TeamSeason> seasons = new HashSet<TeamSeason>();
		for (TeamSeason ts : teamSeasons) {
			if (ts.getYear().equals(year)) seasons.add(ts);
		}
		return seasons;
	}
	
	/**
	 * 
	 * @return all the team seasons for a player
	 */
	public Set<TeamSeason> getTeamSeasons() {
		return teamSeasons;
	}
	
	public void setTeamSeasons(Set<TeamSeason> teamSeasons) {
		this.teamSeasons = teamSeasons;
	}
}
