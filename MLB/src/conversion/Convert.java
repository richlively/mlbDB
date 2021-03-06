package conversion;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import bo.BattingStats;
import bo.CatchingStats;
import bo.FieldingStats;
import bo.PitchingStats;
import bo.Player;
import bo.PlayerSeason;
import bo.Team;
import bo.TeamSeason;
import dataaccesslayer.HibernateUtil;

public class Convert {

	static Connection conn;
	static final String MYSQL_CONN_URL =
			// "jdbc:mysql://192.168.183.132/mlb?user=root&password=password";
			// //nathan's
			"jdbc:mysql://192.168.133.129:3306/mlb?user=remote&password=password&noAccessToProcedureBodies=true"; // richie's

	/**
	 * Only store a single team object for each franchise key - franchID value -
	 * team object
	 */
	static HashMap<String, Team> teams = new HashMap<String, Team>();
	/**
	 * key - yearID + lgID + teamID value - teamSeason object
	 */
	static HashMap<String, TeamSeason> teamSeasons = new HashMap<String, TeamSeason>();

	public static void main(String[] args) {
		try {
			long startTime = System.currentTimeMillis();
			conn = DriverManager.getConnection(MYSQL_CONN_URL);
			// convert Teams
			convertTeams();
			// convert and persist Players
			convertPlayers();
			// persist Teams
			persistTeams();
			long endTime = System.currentTimeMillis();
			long elapsed = (endTime - startTime) / (1000 * 60);
			System.out.println("Elapsed time in mins: " + elapsed);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (!conn.isClosed())
					conn.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		HibernateUtil.getSessionFactory().close();
	}

	/**
	 * Converts the "Master" table to Player objects
	 */
	public static void convertPlayers() {
		try {
			CallableStatement cs = conn.prepareCall("{call players()}");
			ResultSet rs = cs.executeQuery();
			int count = 0; // for progress feedback only
			while (rs.next()) {
				count++;
				// this just gives us some progress feedback
				if (count % 100 == 0)
					System.out.println("num players: " + count);
				String pid = rs.getString("playerID");
				String firstName = rs.getString("nameFirst");
				String lastName = rs.getString("nameLast");
				// this check is for data scrubbing
				// don't want to bring anybody over that doesn't have a pid,
				// firstname and lastname
				if (pid == null || pid.isEmpty() || firstName == null || firstName.isEmpty() || lastName == null
						|| lastName.isEmpty())
					continue;
				Player p = new Player();
				p.setName(firstName + " " + lastName);
				p.setGivenName(rs.getString("nameGiven"));
				java.util.Date birthDay = convertIntsToDate(rs.getInt("birthYear"), rs.getInt("birthMonth"),
						rs.getInt("birthDay"));
				if (birthDay != null)
					p.setBirthDay(birthDay);
				java.util.Date deathDay = convertIntsToDate(rs.getInt("deathYear"), rs.getInt("deathMonth"),
						rs.getInt("deathDay"));
				if (deathDay != null)
					p.setDeathDay(deathDay);
				// need to do some data scrubbing for bats and throws columns
				String hand = rs.getString("bats");
				if (hand != null && hand.equalsIgnoreCase("B")) {
					hand = "S";
				}
				p.setBattingHand(hand);
				hand = rs.getString("throws");
				p.setThrowingHand(hand);
				p.setBirthCity(rs.getString("birthCity"));
				p.setBirthState(rs.getString("birthState"));
				java.util.Date firstGame = rs.getDate("debut");
				if (firstGame != null)
					p.setFirstGame(firstGame);
				java.util.Date lastGame = rs.getDate("finalGame");
				if (lastGame != null)
					p.setLastGame(lastGame);
				addPositions(p, pid);
				// players bio collected, now go after stats
				addSeasons(p, pid);
				// we can now persist player, and the seasons and stats will
				// cascade
				HibernateUtil.persistPlayer(p);
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Converts the "Teams" table to Team objects
	 */
	public static void convertTeams() {
		try {
			CallableStatement cs = conn.prepareCall("{call teams()}");
			ResultSet rs = cs.executeQuery();

			int count = 0; // for progress feedback only
			while (rs.next()) {
				count++;
				// this just gives us some progress feedback
				if (count % 500 == 0)
					System.out.println("num teams: " + count);

				String teamID = rs.getString("teamID");
				String lgID = rs.getString("lgID");
				Integer year = rs.getInt("yearID");
				/*
				 * because this is a one-time use convert program, our data
				 * shows that every team (even teams that lasted only one year)
				 * has a non-null franchID so no reason to check if it is null
				 * (i.e. even though the DB schema does allow null franchises,
				 * they do not occur in this instance) also, this is the an
				 * effective way to keep track of when a team changes its name
				 * since the teamID also changes when the name changes
				 */
				String franchID = rs.getString("franchID");
				// only create and add the team if we haven't already added it
				if (!teams.containsKey(franchID)) {
					String name;
					// the latest/last name used by the team is easily looked up
					// in the TeamFranchises table
					CallableStatement latestName_stmt = conn.prepareCall("{call latest_name(?)}");
					latestName_stmt.setString(1, franchID);
					ResultSet latestName_rs = latestName_stmt.executeQuery();
					// only one row per franchise
					latestName_rs.next();
					name = latestName_rs.getString("franchName");

					// find the founding and most recent years for the team
					CallableStatement years_stmt = conn.prepareCall("{call team_years(?)}");

					years_stmt.setString(1, franchID);
					ResultSet years_rs = years_stmt.executeQuery();

					// dates descending, so most recent year is first, oldest is
					// last
					years_rs.first();
					Integer recent = years_rs.getInt("yearID");
					years_rs.last();
					Integer founded = years_rs.getInt("yearID");

					Team t = new Team();
					t.setName(name);
					t.setLeague(lgID);
					t.setYearLast(recent);
					t.setYearFounded(founded);

					addTeamSeason(t, year, lgID, teamID);

					teams.put(franchID, t);

					latestName_rs.close();
					years_rs.close();
				}
				// this team has already been created, so just add on the season
				else {
					Team t = teams.get(franchID);
					addTeamSeason(t, year, lgID, teamID);
				}
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Persists every team object
	 */
	private static void persistTeams() {
		int count = 0;
		for (Team t : teams.values()) {
			HibernateUtil.persistTeam(t);
			if (++count % 100 == 0) {
				System.out.println("num teams persisted: " + count);
			}
		}
	}

	/**
	 * Converts three integers into a Date object
	 * 
	 * @param year
	 * @param month
	 * @param day
	 * @return the date
	 */
	private static java.util.Date convertIntsToDate(int year, int month, int day) {
		Calendar c = new GregorianCalendar();
		java.util.Date d = null;
		// if year is 0, then date wasn't populated in MySQL database
		if (year != 0) {
			c.set(year, month - 1, day);
			d = c.getTime();
		}
		return d;
	}

	/**
	 * Adds season data to a team
	 * 
	 * @param t
	 *            the team
	 * @param yearID
	 *            the year
	 * @param lgID
	 *            the league ID
	 * @param teamID
	 *            the team ID
	 */
	public static void addTeamSeason(Team t, Integer yearID, String lgID, String teamID) {
		try {
			CallableStatement teamStats_stmt = conn.prepareCall("{call team_stats(?,?,?)}");
			teamStats_stmt.setInt(1, yearID);
			teamStats_stmt.setString(2, lgID);
			teamStats_stmt.setString(3, teamID);
			ResultSet teamStats_rs = teamStats_stmt.executeQuery();
			// only one row (a team can only have one season in a year)
			teamStats_rs.next();

			Integer rank = teamStats_rs.getInt("Rank");
			Integer games = teamStats_rs.getInt("G");
			Integer wins = teamStats_rs.getInt("W");
			Integer losses = teamStats_rs.getInt("L");
			Integer attendance = teamStats_rs.getInt("attendance");

			TeamSeason ts = new TeamSeason(t, yearID);
			ts.setRank(rank);
			ts.setGamesPlayed(games);
			ts.setWins(wins);
			ts.setLosses(losses);
			ts.setTotalAttendance(attendance);

			t.addSeason(ts);
			teamSeasons.put(yearID + lgID + teamID, ts);

			teamStats_rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addPositions(Player p, String pid) {
		Set<String> positions = new HashSet<String>();
		try {
			CallableStatement cs = conn.prepareCall("{call positions(?)}");
			cs.setString(1, pid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				String pos = rs.getString("pos");
				positions.add(pos);
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		p.setPositions(positions);
	}

	/**
	 * Adds season statistics to a player
	 * 
	 * @param p
	 *            the player
	 * @param pid
	 *            player ID
	 */
	public static void addSeasons(Player p, String pid) {
		try {
			CallableStatement cs = conn
					.prepareCall("{call seasons(?)}");
			cs.setString(1, pid);
			ResultSet rs = cs.executeQuery();
			PlayerSeason s = null;
			while (rs.next()) {
				int yid = rs.getInt("yearID");
				s = p.getPlayerSeason(yid);
				// regardless of the circumstances, add the player to the team
				// season as well
				String leagueID = rs.getString("lgId");
				String teamID = rs.getString("teamID");
				TeamSeason ts = teamSeasons.get(yid + leagueID + teamID);
				ts.addPlayer(p);
				p.addTeamSeason(ts);

				// it is possible to see more than one of these per player if he
				// switched teams
				// set all of these attrs the first time we see this
				// playerseason
				if (s == null) {
					s = new PlayerSeason(p, yid);
					p.addSeason(s);
					s.setGamesPlayed(rs.getInt("gamesPlayed"));
					double salary = getSalary(pid, yid);
					s.setSalary(salary);
					BattingStats batting = getBatting(s, pid, yid);
					s.setBattingStats(batting);
					FieldingStats fielding = getFielding(s, pid, yid);
					s.setFieldingStats(fielding);
					PitchingStats pitching = getPitching(s, pid, yid);
					s.setPitchingStats(pitching);
					CatchingStats catching = getCatching(s, pid, yid);
					s.setCatchingStats(catching);
					// set this the consecutive time(s) so it is the total games
					// played regardless of team
				} else {
					s.setGamesPlayed(rs.getInt("gamesPlayed") + s.getGamesPlayed());
				}
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets a player's salary for the year
	 * 
	 * @param pid
	 *            player ID
	 * @param yid
	 *            the year
	 * @return the salary
	 */
	public static double getSalary(String pid, Integer yid) {
		double salary = 0;
		try {
			CallableStatement cs = conn.prepareCall("{call salary(?,?)}");
			cs.setString(1, pid);
			cs.setInt(2, yid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				salary = rs.getDouble("salary");
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return salary;
	}

	/**
	 * Gets a player's seasonal batting stats
	 * 
	 * @param psi
	 * @param pid
	 * @param yid
	 * @return the batting stats
	 */
	public static BattingStats getBatting(PlayerSeason psi, String pid, Integer yid) {
		BattingStats s = new BattingStats();
		try {
			CallableStatement cs = conn.prepareCall("{call batting_stats(?,?)}");
			cs.setString(1, pid);
			cs.setInt(2, yid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setAtBats(rs.getInt("atBats"));
				s.setHits(rs.getInt("hits"));
				s.setDoubles(rs.getInt("doubles"));
				s.setTriples(rs.getInt("triples"));
				s.setHomeRuns(rs.getInt("homeRuns"));
				s.setRunsBattedIn(rs.getInt("runsBattedIn"));
				s.setStrikeouts(rs.getInt("strikeouts"));
				s.setWalks(rs.getInt("walks"));
				s.setHitByPitch(rs.getInt("hitByPitch"));
				s.setIntentionalWalks(rs.getInt("intentionalWalks"));
				s.setSteals(rs.getInt("steals"));
				s.setStealsAttempted(rs.getInt("stealsAttempted"));
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Gets a player's seasonal fielding stats
	 * 
	 * @param psi
	 * @param pid
	 * @param yid
	 * @return the fielding stats
	 */
	public static FieldingStats getFielding(PlayerSeason psi, String pid, Integer yid) {
		FieldingStats s = new FieldingStats();
		try {
			CallableStatement cs = conn.prepareCall("{call fielding_stats(?,?)}");
			cs.setString(1, pid);
			cs.setInt(2, yid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setErrors(rs.getInt("errors"));
				s.setPutOuts(rs.getInt("putOuts"));
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Gets a player's seasonal pitching stats
	 * 
	 * @param psi
	 * @param pid
	 * @param yid
	 * @return the pitching stats
	 */
	public static PitchingStats getPitching(PlayerSeason psi, String pid, Integer yid) {
		PitchingStats s = new PitchingStats();
		try {
			CallableStatement cs = conn.prepareCall("{call pitching_stats(?,?)}");
			cs.setString(1, pid);
			cs.setInt(2, yid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setOutsPitched(rs.getInt("outsPitched"));
				s.setEarnedRunsAllowed(rs.getInt("earnedRunsAllowed"));
				s.setHomeRunsAllowed(rs.getInt("homeRunsAllowed"));
				s.setStrikeouts(rs.getInt("strikeouts"));
				s.setWalks(rs.getInt("walks"));
				s.setWins(rs.getInt("wins"));
				s.setLosses(rs.getInt("losses"));
				s.setWildPitches(rs.getInt("wildPitches"));
				s.setBattersFaced(rs.getInt("battersFaced"));
				s.setHitBatters(rs.getInt("hitBatters"));
				s.setSaves(rs.getInt("saves"));
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * Gets a player's seasonal catching stats
	 * 
	 * @param psi
	 * @param pid
	 * @param yid
	 * @return the catching stats
	 */
	public static CatchingStats getCatching(PlayerSeason psi, String pid, Integer yid) {
		CatchingStats s = new CatchingStats();
		CallableStatement cs = null;
		try {
			cs = conn.prepareCall("{call catching_stats(?,?)}");
			cs.setString(1, pid);
			cs.setInt(2, yid);
			ResultSet rs = cs.executeQuery();
			while (rs.next()) {
				s.setId(psi);
				s.setPassedBalls(rs.getInt("passedBalls"));
				s.setWildPitches(rs.getInt("wildPitches"));
				s.setStealsAllowed(rs.getInt("stealsAllowed"));
				s.setStealsCaught(rs.getInt("stealsCaught"));
			}
			rs.close();
			cs.close();
		} catch (Exception e) {
			cs.toString();
			e.printStackTrace();
		}
		return s;
	}

}

/* Stored procedure calls
DELIMITER //
CREATE
PROCEDURE players()
BEGIN
SELECT
  playerID,
  nameFirst,
  nameLast,
  nameGiven,
  birthDay,
  birthMonth,
  birthYear,
  deathDay,
  deathMonth,
  deathYear,
  bats,
  throws,
  birthCity,
  birthState,
  debut,
  finalGame
FROM 
  Master;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE positions(IN pid VARCHAR(9))
BEGIN
SELECT DISTINCT
  pos
FROM
  Fielding f
WHERE
  f.playerID = pid ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE seasons(IN pid VARCHAR(9))
BEGIN
SELECT
  yearID,
  teamID,
  lgId,
  SUM(G) AS gamesPlayed
FROM
  Batting b
WHERE
  b.playerID = pid
GROUP BY
  yearID,
  teamID,
  lgID ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE teams()
BEGIN
SELECT
  teamID,
  yearID,
  lgID,
  franchID,
  NAME
FROM
  Teams ;
END //
DELIMITER ;
DELIMITER //
CREATE
PROCEDURE team_years(IN fid VARCHAR(3))
BEGIN
SELECT
  yearID
FROM
  Teams t
WHERE
  t.franchID = fid
ORDER BY
  yearID DESC ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE latest_name(IN fid VARCHAR(3))
BEGIN
SELECT
  franchName
FROM
  TeamsFranchises ft
WHERE
  ft.franchID = fid ;
END //
DELIMITER ;
DELIMITER //
CREATE
PROCEDURE team_stats(
  IN yid INT(11),
  lid VARCHAR(2),
  tid VARCHAR(3)
)
BEGIN
SELECT
  Rank,
  G,
  W,
  L,
  attendance
FROM
  Teams t
WHERE
  t.yearID = yid AND t.lgID = lid AND t.teamID = tid ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE salary(IN pid VARCHAR(9), yid INT(11))
BEGIN
SELECT
  SUM(salary) AS salary
FROM
  Salaries s
WHERE
  s.playerID = pid AND s.yearID = yid ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE batting_stats(IN pid VARCHAR(9), yid INT(11))
BEGIN
SELECT
  SUM(AB) AS atBats,
  SUM(H) AS hits,
  SUM(2B) AS doubles,
  SUM(3B) AS triples,
  SUM(HR) AS homeRuns,
  SUM(RBI) AS runsBattedIn,
  SUM(SO) AS strikeouts,
  SUM(BB) AS walks,
  SUM(HBP) AS hitByPitch,
  SUM(IBB) AS intentionalWalks,
  SUM(SB) AS steals,
  SUM(CS) AS stealsAttempted
FROM
  Batting b
WHERE
  b.playerID = pid AND b.yearID = yid ;
END //
DELIMITER ;
DELIMITER //
CREATE
PROCEDURE fielding_stats(IN pid VARCHAR(9), yid INT(11))
BEGIN
SELECT
  SUM(E) AS ERRORS,
  SUM(PO) AS putOuts
FROM
  Fielding f
WHERE
  f.playerID = pid AND f.yearID = yid ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE pitching_stats(IN pid VARCHAR(9), yid INT(11))
BEGIN
SELECT
  SUM(IPOuts) AS outsPitched,
  SUM(ER) AS earnedRunsAllowed,
  SUM(HR) AS homeRunsAllowed,
  SUM(SO) AS strikeouts,
  SUM(BB) AS walks,
  SUM(W) AS wins,
  SUM(L) AS losses,
  SUM(WP) AS wildPitches,
  SUM(BFP) AS battersFaced,
  SUM(HBP) AS hitBatters,
  SUM(SV) AS saves
FROM
  Pitching p
WHERE
  p.playerID = pid AND p.yearID = yid ;
END //
DELIMITER ;

DELIMITER //
CREATE
PROCEDURE catching_stats(IN pid VARCHAR(9), yid INT(11))
BEGIN
SELECT
  SUM(PB) AS passedBalls,
  SUM(WP) AS wildPitches,
  SUM(SB) AS stealsAllowed,
  SUM(CS) AS stealsCaught
FROM
  Fielding f
WHERE
  f.playerID = pid AND f.yearID = yid ;
END //
DELIMITER ;
*/