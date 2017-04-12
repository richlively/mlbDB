package bo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import javax.persistence.JoinColumn;

@SuppressWarnings("serial")
@Entity(name = "teamseason")
public class TeamSeason implements Serializable {
	// similar to PlayerSeason

	@EmbeddedId
	TeamSeasonId id;

	@Embeddable
	static class TeamSeasonId implements Serializable {
		@ManyToOne
		@JoinColumn(name = "teamid", referencedColumnName = "teamid", insertable = false, updatable = false)
		Team team;
		@Column(name = "year")
		Integer teamYear;

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TeamSeasonId)) {
				return false;
			}
			TeamSeasonId other = (TeamSeasonId) obj;
			// in order for two different object of this type to be equal,
			// they must be for the same year and for the same team
			return (this.team == other.team && this.teamYear == other.teamYear);
		}

		@Override
		public int hashCode() {
			Integer hash = 0;
			if (this.team != null)
				hash += this.team.hashCode();
			if (this.teamYear != null)
				hash += this.teamYear.hashCode();
			return hash;
		}
	}

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "teamseasonplayer",
			joinColumns = {
					@JoinColumn(name = "teamId", insertable = false, updatable = false),
					@JoinColumn(name = "year", insertable = false, updatable = false) },
			inverseJoinColumns = {
					@JoinColumn(name = "playerId", insertable = false, updatable = false) })
	Set<Player> players = new HashSet<Player>();
}
