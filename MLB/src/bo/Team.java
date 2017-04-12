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
	//similar to Player
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Integer teamId;
	
	@OneToMany(fetch = FetchType.EAGER, cascade=CascadeType.ALL, mappedBy="id.team")
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
	
	public Integer getId() {
		return teamId;
	}
	
	public void setId(Integer teamId) {
		this.teamId = teamId;
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
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Team)){
			return false;
		}
		Team other = (Team) obj;
		return (this.getName().equalsIgnoreCase(other.getName()) &&
				this.getLeague().equalsIgnoreCase(other.getLeague()) &&
				this.getYearFounded().equals(other.getYearFounded()) &&
				this.getYearLast().equals(other.getYearLast()));
	}
	
	@Override
	public int hashCode() {
		Integer hash = 0;
		if (this.getName()!=null) hash += this.getName().hashCode(); 
		if (this.getLeague()!=null) hash += this.getLeague().hashCode();
		if (this.getYearFounded()!=null) hash += this.getYearFounded().hashCode();
		if (this.getYearLast()!=null) hash += this.getYearLast().hashCode();
		return hash;
	}
}
