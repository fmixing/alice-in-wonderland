import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Drive {

    Long driveID;

    Long userID;

    Long from;

    Long to;

    Long date;

    int vacantPlaces;

    Set<Long> joinedUsers;

    public Long getDriveID() {
        return driveID;
    }

    public void setDriveID(Long driveID) {
        this.driveID = driveID;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Long getFrom() {
        return from;
    }

    public void setFrom(Long from) {
        this.from = from;
    }

    public Long getTo() {
        return to;
    }

    public void setTo(Long to) {
        this.to = to;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public int getVacantPlaces() {
        return vacantPlaces;
    }

    public void setVacantPlaces(int vacantPlaces) {
        this.vacantPlaces = vacantPlaces;
    }

    public Set<Long> getJoinedUsers() {
        return joinedUsers;
    }

    public void setJoinedUsers(Set<Long> joinedUsers) {
        this.joinedUsers = joinedUsers;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Drive{");
        sb.append("driveID=").append(driveID);
        sb.append(", userID=").append(userID);
        sb.append(", from=").append(from);
        sb.append(", to=").append(to);
        sb.append(", date=").append(date);
        sb.append(", vacantPlaces=").append(vacantPlaces);
        sb.append(", joinedUsers=").append(joinedUsers);
        sb.append('}');
        return sb.toString();
    }
}
